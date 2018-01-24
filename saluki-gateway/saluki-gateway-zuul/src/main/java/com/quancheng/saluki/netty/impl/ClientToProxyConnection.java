package com.quancheng.saluki.netty.impl;

import static com.quancheng.saluki.netty.impl.support.ConnectionState.AWAITING_CHUNK;
import static com.quancheng.saluki.netty.impl.support.ConnectionState.AWAITING_INITIAL;
import static com.quancheng.saluki.netty.impl.support.ConnectionState.DISCONNECT_REQUESTED;
import static com.quancheng.saluki.netty.impl.support.ConnectionState.NEGOTIATING_CONNECT;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSession;

import org.apache.commons.lang3.StringUtils;

import com.quancheng.saluki.netty.ActivityTracker;
import com.quancheng.saluki.netty.HttpFilter;
import com.quancheng.saluki.netty.impl.flow.ConnectionFlowStep;
import com.quancheng.saluki.netty.impl.flow.FlowContext;
import com.quancheng.saluki.netty.impl.flow.FullFlowContext;
import com.quancheng.saluki.netty.impl.support.ConnectionState;
import com.quancheng.saluki.utils.ProxyUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.util.AsciiString;
import io.netty.util.concurrent.Future;


public class ClientToProxyConnection extends ProxyConnection<HttpRequest> {
  private static final HttpResponseStatus CONNECTION_ESTABLISHED =
      new HttpResponseStatus(200, "Connection established");
  private static final String LOWERCASE_TRANSFER_ENCODING_HEADER =
      HttpHeaderNames.TRANSFER_ENCODING.toString().toLowerCase(Locale.US);
  private static final Pattern HTTP_SCHEME =
      Pattern.compile("^http://.*", Pattern.CASE_INSENSITIVE);
  private final Map<String, ProxyToServerConnection> serverConnectionsByHostAndPort =
      new ConcurrentHashMap<String, ProxyToServerConnection>();

  private final AtomicInteger numberOfCurrentlyConnectingServers = new AtomicInteger(0);
  private final AtomicInteger numberOfCurrentlyConnectedServers = new AtomicInteger(0);
  private final AtomicInteger numberOfReusedServerConnections = new AtomicInteger(0);
  private final GlobalTrafficShapingHandler globalTrafficShapingHandler;

  private volatile ProxyToServerConnection currentServerConnection;

  private volatile HttpFilter currentFilters = HttpFilter.NOOP_FILTER;

  private volatile SSLSession clientSslSession;

  private volatile boolean mitming = false;

  private volatile HttpRequest currentRequest;

  ClientToProxyConnection(final DefaultHttpProxyServer proxyServer, ChannelPipeline pipeline,
      GlobalTrafficShapingHandler globalTrafficShapingHandler) {
    super(AWAITING_INITIAL, proxyServer, false);
    initChannelPipeline(pipeline);
    this.globalTrafficShapingHandler = globalTrafficShapingHandler;
    LOG.debug("Created ClientToProxyConnection");
  }

  @Override
  public ConnectionState readHTTPInitial(HttpRequest httpRequest) {
    LOG.debug("Received raw request: {}", httpRequest);
    if (httpRequest.decoderResult().isFailure()) {
      LOG.debug("Could not parse request from client. Decoder result: {}",
          httpRequest.decoderResult().toString());
      FullHttpResponse response = ProxyUtils.createFullHttpResponse(HttpVersion.HTTP_1_1,
          HttpResponseStatus.BAD_REQUEST, "Unable to parse HTTP request");
      HttpUtil.setKeepAlive(response, false);
      respondWithShortCircuitResponse(response);
      return DISCONNECT_REQUESTED;
    }
    return doReadHTTPInitial(httpRequest);
  }


  private ConnectionState doReadHTTPInitial(HttpRequest httpRequest) {
    this.currentRequest = copy(httpRequest);
    HttpFilter filterInstance = proxyServer.getFiltersSource().filterRequest(currentRequest, ctx);
    if (filterInstance != null) {
      currentFilters = filterInstance;
    } else {
      currentFilters = HttpFilter.NOOP_FILTER;
    }
    HttpResponse clientToProxyFilterResponse = currentFilters.clientToProxyRequest(httpRequest);
    if (clientToProxyFilterResponse != null) {
      LOG.debug("Responding to client with short-circuit response from filter: {}",
          clientToProxyFilterResponse);
      boolean keepAlive = respondWithShortCircuitResponse(clientToProxyFilterResponse);
      if (keepAlive) {
        return AWAITING_INITIAL;
      } else {
        return DISCONNECT_REQUESTED;
      }
    }
    if (!proxyServer.isAllowRequestsToOriginServer() && isRequestToOriginServer(httpRequest)) {
      boolean keepAlive = writeBadRequest(httpRequest);
      if (keepAlive) {
        return AWAITING_INITIAL;
      } else {
        return DISCONNECT_REQUESTED;
      }
    }
    String serverHostAndPort = identifyHostAndPort(httpRequest);
    LOG.debug("Ensuring that hostAndPort are available in {}", httpRequest.uri());
    if (serverHostAndPort == null || StringUtils.isBlank(serverHostAndPort)) {
      LOG.warn("No host and port found in {}", httpRequest.uri());
      boolean keepAlive = writeBadGateway(httpRequest);
      if (keepAlive) {
        return AWAITING_INITIAL;
      } else {
        return DISCONNECT_REQUESTED;
      }
    }
    LOG.debug("Finding ProxyToServerConnection for: {}", serverHostAndPort);
    currentServerConnection = isMitming() || isTunneling() ? this.currentServerConnection
        : this.serverConnectionsByHostAndPort.get(serverHostAndPort);
    boolean newConnectionRequired = false;
    if (ProxyUtils.isCONNECT(httpRequest)) {
      LOG.debug("Not reusing existing ProxyToServerConnection because request is a CONNECT for: {}",
          serverHostAndPort);
      newConnectionRequired = true;
    } else if (currentServerConnection == null) {
      LOG.debug("Didn't find existing ProxyToServerConnection for: {}", serverHostAndPort);
      newConnectionRequired = true;
    }
    if (newConnectionRequired) {
      try {
        currentServerConnection = ProxyToServerConnection.create(proxyServer, this,
            serverHostAndPort, currentFilters, httpRequest, globalTrafficShapingHandler);
        if (currentServerConnection == null) {
          LOG.debug("Unable to create server connection, probably no chained proxies available");
          boolean keepAlive = writeBadGateway(httpRequest);
          resumeReading();
          if (keepAlive) {
            return AWAITING_INITIAL;
          } else {
            return DISCONNECT_REQUESTED;
          }
        }
        serverConnectionsByHostAndPort.put(serverHostAndPort, currentServerConnection);
      } catch (UnknownHostException uhe) {
        LOG.info("Bad Host {}", httpRequest.uri());
        boolean keepAlive = writeBadGateway(httpRequest);
        resumeReading();
        if (keepAlive) {
          return AWAITING_INITIAL;
        } else {
          return DISCONNECT_REQUESTED;
        }
      }
    } else {
      LOG.debug("Reusing existing server connection: {}", currentServerConnection);
      numberOfReusedServerConnections.incrementAndGet();
    }
    modifyRequestHeadersToReflectProxying(httpRequest);
    HttpResponse proxyToServerFilterResponse = currentFilters.proxyToServerRequest(httpRequest);
    if (proxyToServerFilterResponse != null) {
      LOG.debug("Responding to client with short-circuit response from filter: {}",
          proxyToServerFilterResponse);

      boolean keepAlive = respondWithShortCircuitResponse(proxyToServerFilterResponse);
      if (keepAlive) {
        return AWAITING_INITIAL;
      } else {
        return DISCONNECT_REQUESTED;
      }
    }
    LOG.debug("Writing request to ProxyToServerConnection");
    currentServerConnection.write(httpRequest, currentFilters);
    if (ProxyUtils.isCONNECT(httpRequest)) {
      return NEGOTIATING_CONNECT;
    } else if (ProxyUtils.isChunked(httpRequest)) {
      return AWAITING_CHUNK;
    } else {
      return AWAITING_INITIAL;
    }
  }


  private boolean isRequestToOriginServer(HttpRequest httpRequest) {
    if (httpRequest.method() == HttpMethod.CONNECT || isMitming()) {
      return false;
    }
    String uri = httpRequest.uri();
    return !HTTP_SCHEME.matcher(uri).matches();
  }

  @Override
  public void readHTTPChunk(HttpContent chunk) {
    currentFilters.clientToProxyRequest(chunk);
    currentFilters.proxyToServerRequest(chunk);
    currentServerConnection.write(chunk);
  }

  @Override
  public void readRaw(ByteBuf buf) {
    currentServerConnection.write(buf);
  }

  protected void respond(ProxyToServerConnection serverConnection, HttpFilter filters,
      HttpRequest currentHttpRequest, HttpResponse currentHttpResponse, HttpObject httpObject) {
    this.currentRequest = null;
    httpObject = filters.serverToProxyResponse(httpObject);
    if (httpObject == null) {
      forceDisconnect(serverConnection);
      return;
    }

    if (httpObject instanceof HttpResponse) {
      HttpResponse httpResponse = (HttpResponse) httpObject;
      if (!ProxyUtils.isHEAD(currentHttpRequest)
          && !ProxyUtils.isResponseSelfTerminating(httpResponse)) {
        if (!(httpResponse instanceof FullHttpResponse)) {
          HttpResponse duplicateResponse = ProxyUtils.duplicateHttpResponse(httpResponse);
          httpObject = httpResponse = duplicateResponse;
        }
        HttpUtil.setTransferEncodingChunked(httpResponse, true);
      }
      fixHttpVersionHeaderIfNecessary(httpResponse);
      modifyResponseHeadersToReflectProxying(httpResponse);
    }
    httpObject = filters.proxyToClientResponse(httpObject);
    if (httpObject == null) {
      forceDisconnect(serverConnection);
      return;
    }
    write(httpObject);
    if (ProxyUtils.isLastChunk(httpObject)) {
      writeEmptyBuffer();
    }
    closeConnectionsAfterWriteIfNecessary(serverConnection, currentHttpRequest, currentHttpResponse,
        httpObject);
  }


  ConnectionFlowStep RespondCONNECTSuccessful = new ConnectionFlowStep(this, NEGOTIATING_CONNECT) {
    @Override
    public boolean shouldSuppressInitialRequest() {
      return true;
    }

    public Future<?> execute() {
      LOG.debug("Responding with CONNECT successful");
      HttpResponse response =
          ProxyUtils.createFullHttpResponse(HttpVersion.HTTP_1_1, CONNECTION_ESTABLISHED);
      response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
      ProxyUtils.addVia(response, proxyServer.getProxyAlias());
      return writeToChannel(response);
    };
  };


  @Override
  public void connected() {
    super.connected();
    become(AWAITING_INITIAL);
    recordClientConnected();
  }

  void timedOut(ProxyToServerConnection serverConnection) {
    if (currentServerConnection == serverConnection
        && this.lastReadTime > currentServerConnection.lastReadTime) {
      LOG.warn("Server timed out: {}", currentServerConnection);
      currentFilters.serverToProxyResponseTimedOut();
      writeGatewayTimeout(currentRequest);
    }
  }

  @Override
  public void timedOut() {
    if (currentServerConnection == null
        || this.lastReadTime <= currentServerConnection.lastReadTime) {
      super.timedOut();
    }
  }


  @Override
  public void disconnected() {
    super.disconnected();
    for (ProxyToServerConnection serverConnection : serverConnectionsByHostAndPort.values()) {
      serverConnection.disconnect();
    }
    recordClientDisconnected();
  }


  public void serverConnectionFlowStarted(ProxyToServerConnection serverConnection) {
    stopReading();
    this.numberOfCurrentlyConnectingServers.incrementAndGet();
  }


  public void serverConnectionSucceeded(ProxyToServerConnection serverConnection,
      boolean shouldForwardInitialRequest) {
    LOG.debug("Connection to server succeeded: {}", serverConnection.getRemoteAddress());
    resumeReadingIfNecessary();
    become(shouldForwardInitialRequest ? getCurrentState() : AWAITING_INITIAL);
    numberOfCurrentlyConnectedServers.incrementAndGet();
  }


  public boolean serverConnectionFailed(ProxyToServerConnection serverConnection,
      ConnectionState lastStateBeforeFailure, Throwable cause) {
    resumeReadingIfNecessary();
    HttpRequest initialRequest = serverConnection.getInitialRequest();
    try {
      boolean retrying = serverConnection.connectionFailed(cause);
      if (retrying) {
        LOG.debug(
            "Failed to connect to upstream server or chained proxy. Retrying connection. Last state before failure: {}",
            lastStateBeforeFailure, cause);
        return true;
      } else {
        LOG.debug(
            "Connection to upstream server or chained proxy failed: {}.  Last state before failure: {}",
            serverConnection.getRemoteAddress(), lastStateBeforeFailure, cause);
        connectionFailedUnrecoverably(initialRequest, serverConnection);
        return false;
      }
    } catch (UnknownHostException uhe) {
      connectionFailedUnrecoverably(initialRequest, serverConnection);
      return false;
    }
  }

  private void connectionFailedUnrecoverably(HttpRequest initialRequest,
      ProxyToServerConnection serverConnection) {
    serverConnection.disconnect();
    this.serverConnectionsByHostAndPort.remove(serverConnection.getServerHostAndPort());
    boolean keepAlive = writeBadGateway(initialRequest);
    if (keepAlive) {
      become(AWAITING_INITIAL);
    } else {
      become(DISCONNECT_REQUESTED);
    }
  }

  private void resumeReadingIfNecessary() {
    if (this.numberOfCurrentlyConnectingServers.decrementAndGet() == 0) {
      LOG.debug("All servers have finished attempting to connect, resuming reading from client.");
      resumeReading();
    }
  }


  protected void serverDisconnected(ProxyToServerConnection serverConnection) {
    numberOfCurrentlyConnectedServers.decrementAndGet();
    if (isTunneling() || isMitming()) {
      disconnect();
    }
  }


  @Override
  public synchronized void becameSaturated() {
    super.becameSaturated();
    for (ProxyToServerConnection serverConnection : serverConnectionsByHostAndPort.values()) {
      synchronized (serverConnection) {
        if (this.isSaturated()) {
          serverConnection.stopReading();
        }
      }
    }
  }


  @Override
  public synchronized void becameWritable() {
    super.becameWritable();
    for (ProxyToServerConnection serverConnection : serverConnectionsByHostAndPort.values()) {
      synchronized (serverConnection) {
        if (!this.isSaturated()) {
          serverConnection.resumeReading();
        }
      }
    }
  }


  synchronized protected void serverBecameSaturated(ProxyToServerConnection serverConnection) {
    if (serverConnection.isSaturated()) {
      LOG.info("Connection to server became saturated, stopping reading");
      stopReading();
    }
  }


  synchronized protected void serverBecameWriteable(ProxyToServerConnection serverConnection) {
    boolean anyServersSaturated = false;
    for (ProxyToServerConnection otherServerConnection : serverConnectionsByHostAndPort.values()) {
      if (otherServerConnection.isSaturated()) {
        anyServersSaturated = true;
        break;
      }
    }
    if (!anyServersSaturated) {
      LOG.info("All server connections writeable, resuming reading");
      resumeReading();
    }
  }

  @Override
  public void exceptionCaught(Throwable cause) {
    try {
      if (cause instanceof IOException) {
        LOG.info("An IOException occurred on ClientToProxyConnection: " + cause.getMessage());
        LOG.debug("An IOException occurred on ClientToProxyConnection", cause);
      } else if (cause instanceof RejectedExecutionException) {
        LOG.info(
            "An executor rejected a read or write operation on the ClientToProxyConnection (this is normal if the proxy is shutting down). Message: "
                + cause.getMessage());
        LOG.debug("A RejectedExecutionException occurred on ClientToProxyConnection", cause);
      } else {
        LOG.error("Caught an exception on ClientToProxyConnection", cause);
      }
    } finally {
      disconnect();
    }
  }

  private void initChannelPipeline(ChannelPipeline pipeline) {
    LOG.debug("Configuring ChannelPipeline");
    pipeline.addLast("bytesReadMonitor", bytesReadMonitor);
    pipeline.addLast("bytesWrittenMonitor", bytesWrittenMonitor);
    pipeline.addLast("encoder", new HttpResponseEncoder());
    pipeline.addLast("decoder", new HttpRequestDecoder(proxyServer.getMaxInitialLineLength(),
        proxyServer.getMaxHeaderSize(), proxyServer.getMaxChunkSize()));
    int numberOfBytesToBuffer = proxyServer.getFiltersSource().getMaximumRequestBufferSizeInBytes();
    if (numberOfBytesToBuffer > 0) {
      aggregateContentForFiltering(pipeline, numberOfBytesToBuffer);
    }
    pipeline.addLast("requestReadMonitor", requestReadMonitor);
    pipeline.addLast("responseWrittenMonitor", responseWrittenMonitor);
    pipeline.addLast("idle", new IdleStateHandler(0, 0, proxyServer.getIdleConnectionTimeout()));
    pipeline.addLast("handler", this);
  }


  private void closeConnectionsAfterWriteIfNecessary(ProxyToServerConnection serverConnection,
      HttpRequest currentHttpRequest, HttpResponse currentHttpResponse, HttpObject httpObject) {
    boolean closeServerConnection =
        shouldCloseServerConnection(currentHttpRequest, currentHttpResponse, httpObject);
    boolean closeClientConnection =
        shouldCloseClientConnection(currentHttpRequest, currentHttpResponse, httpObject);

    if (closeServerConnection) {
      LOG.debug("Closing remote connection after writing to client");
      serverConnection.disconnect();
    }

    if (closeClientConnection) {
      LOG.debug("Closing connection to client after writes");
      disconnect();
    }
  }

  private void forceDisconnect(ProxyToServerConnection serverConnection) {
    LOG.debug("Forcing disconnect");
    serverConnection.disconnect();
    disconnect();
  }

  private boolean shouldCloseClientConnection(HttpRequest req, HttpResponse res,
      HttpObject httpObject) {
    if (ProxyUtils.isChunked(res)) {
      if (httpObject != null) {
        if (!ProxyUtils.isLastChunk(httpObject)) {
          String uri = null;
          if (req != null) {
            uri = req.uri();
          }
          LOG.debug("Not closing client connection on middle chunk for {}", uri);
          return false;
        } else {
          LOG.debug("Handling last chunk. Using normal client connection closing rules.");
        }
      }
    }
    if (!HttpUtil.isKeepAlive(req)) {
      LOG.debug("Closing client connection since request is not keep alive: {}", req);
      return true;
    }
    LOG.debug("Not closing client connection for request: {}", req);
    return false;
  }


  private boolean shouldCloseServerConnection(HttpRequest req, HttpResponse res, HttpObject msg) {
    if (ProxyUtils.isChunked(res)) {
      if (msg != null) {
        if (!ProxyUtils.isLastChunk(msg)) {
          String uri = null;
          if (req != null) {
            uri = req.uri();
          }
          LOG.debug("Not closing server connection on middle chunk for {}", uri);
          return false;
        } else {
          LOG.debug("Handling last chunk. Using normal server connection closing rules.");
        }
      }
    }
    if (!HttpUtil.isKeepAlive(res)) {
      LOG.debug("Closing server connection since response is not keep alive: {}", res);
      return true;
    }

    LOG.debug("Not closing server connection for response: {}", res);
    return false;
  }



  private HttpRequest copy(HttpRequest original) {
    if (original instanceof FullHttpRequest) {
      return ((FullHttpRequest) original).copy();
    } else {
      HttpRequest request =
          new DefaultHttpRequest(original.protocolVersion(), original.method(), original.uri());
      request.headers().set(original.headers());
      return request;
    }
  }


  private void fixHttpVersionHeaderIfNecessary(HttpResponse httpResponse) {
    String te = httpResponse.headers().get(HttpHeaderNames.TRANSFER_ENCODING);
    if (AsciiString.contentEqualsIgnoreCase(HttpHeaderValues.CHUNKED, te)) {
      if (httpResponse.protocolVersion() != HttpVersion.HTTP_1_1) {
        LOG.debug("Fixing HTTP version.");
        httpResponse.setProtocolVersion(HttpVersion.HTTP_1_1);
      }
    }
  }


  private void modifyRequestHeadersToReflectProxying(HttpRequest httpRequest) {
    if (!proxyServer.isTransparent()) {
      LOG.debug("Modifying request headers for proxying");
      HttpHeaders headers = httpRequest.headers();
      ProxyUtils.removeSdchEncoding(headers);
      switchProxyConnectionHeader(headers);
      stripConnectionTokens(headers);
      stripHopByHopHeaders(headers);
      ProxyUtils.addVia(httpRequest, proxyServer.getProxyAlias());
    }
  }


  private void modifyResponseHeadersToReflectProxying(HttpResponse httpResponse) {
    if (!proxyServer.isTransparent()) {
      HttpHeaders headers = httpResponse.headers();
      stripConnectionTokens(headers);
      stripHopByHopHeaders(headers);
      ProxyUtils.addVia(httpResponse, proxyServer.getProxyAlias());
      if (!headers.contains(HttpHeaderNames.DATE)) {
        headers.set(HttpHeaderNames.DATE, new Date());
      }
    }
  }

  private void switchProxyConnectionHeader(HttpHeaders headers) {
    String proxyConnectionKey = "Proxy-Connection";
    if (headers.contains(proxyConnectionKey)) {
      String header = headers.get(proxyConnectionKey);
      headers.remove(proxyConnectionKey);
      headers.set(HttpHeaderNames.CONNECTION, header);
    }
  }


  private void stripConnectionTokens(HttpHeaders headers) {
    if (headers.contains(HttpHeaderNames.CONNECTION)) {
      for (String headerValue : headers.getAll(HttpHeaderNames.CONNECTION)) {
        for (String connectionToken : ProxyUtils.splitCommaSeparatedHeaderValues(headerValue)) {
          if (!LOWERCASE_TRANSFER_ENCODING_HEADER.equals(connectionToken.toLowerCase(Locale.US))) {
            headers.remove(connectionToken);
          }
        }
      }
    }
  }

  private void stripHopByHopHeaders(HttpHeaders headers) {
    Set<String> headerNames = headers.names();
    for (String headerName : headerNames) {
      if (ProxyUtils.shouldRemoveHopByHopHeader(headerName)) {
        headers.remove(headerName);
      }
    }
  }


  private boolean writeBadGateway(HttpRequest httpRequest) {
    String body = "Bad Gateway: " + httpRequest.uri();
    FullHttpResponse response = ProxyUtils.createFullHttpResponse(HttpVersion.HTTP_1_1,
        HttpResponseStatus.BAD_GATEWAY, body);
    if (ProxyUtils.isHEAD(httpRequest)) {
      response.content().clear();
    }
    return respondWithShortCircuitResponse(response);
  }


  private boolean writeBadRequest(HttpRequest httpRequest) {
    String body = "Bad Request to URI: " + httpRequest.uri();
    FullHttpResponse response = ProxyUtils.createFullHttpResponse(HttpVersion.HTTP_1_1,
        HttpResponseStatus.BAD_REQUEST, body);
    if (ProxyUtils.isHEAD(httpRequest)) {
      response.content().clear();
    }
    return respondWithShortCircuitResponse(response);
  }


  private boolean writeGatewayTimeout(HttpRequest httpRequest) {
    String body = "Gateway Timeout";
    FullHttpResponse response = ProxyUtils.createFullHttpResponse(HttpVersion.HTTP_1_1,
        HttpResponseStatus.GATEWAY_TIMEOUT, body);
    if (httpRequest != null && ProxyUtils.isHEAD(httpRequest)) {
      response.content().clear();
    }

    return respondWithShortCircuitResponse(response);
  }


  private boolean respondWithShortCircuitResponse(HttpResponse httpResponse) {
    this.currentRequest = null;
    HttpResponse filteredResponse =
        (HttpResponse) currentFilters.proxyToClientResponse(httpResponse);
    if (filteredResponse == null) {
      disconnect();
      return false;
    }
    boolean isKeepAlive = HttpUtil.isKeepAlive(httpResponse);
    int statusCode = httpResponse.status().code();
    if (statusCode != HttpResponseStatus.BAD_GATEWAY.code()
        && statusCode != HttpResponseStatus.GATEWAY_TIMEOUT.code()) {
      modifyResponseHeadersToReflectProxying(httpResponse);
    }
    HttpUtil.setKeepAlive(httpResponse, isKeepAlive);
    write(httpResponse);
    if (ProxyUtils.isLastChunk(httpResponse)) {
      writeEmptyBuffer();
    }
    if (!HttpUtil.isKeepAlive(httpResponse)) {
      disconnect();
      return false;
    }

    return true;
  }


  private String identifyHostAndPort(HttpRequest httpRequest) {
    String hostAndPort = ProxyUtils.parseHostAndPort(httpRequest);
    if (StringUtils.isBlank(hostAndPort)) {
      List<String> hosts = httpRequest.headers().getAll(HttpHeaderNames.HOST);
      if (hosts != null && !hosts.isEmpty()) {
        hostAndPort = hosts.get(0);
      }
    }
    return hostAndPort;
  }


  private void writeEmptyBuffer() {
    write(Unpooled.EMPTY_BUFFER);
  }

  public boolean isMitming() {
    return mitming;
  }

  protected void setMitming(boolean isMitming) {
    this.mitming = isMitming;
  }


  private final BytesReadMonitor bytesReadMonitor = new BytesReadMonitor() {
    @Override
    public void bytesRead(int numberOfBytes) {
      FlowContext flowContext = flowContext();
      for (ActivityTracker tracker : proxyServer.getActivityTrackers()) {
        tracker.bytesReceivedFromClient(flowContext, numberOfBytes);
      }
    }
  };

  private RequestReadMonitor requestReadMonitor = new RequestReadMonitor() {
    @Override
    protected void requestRead(HttpRequest httpRequest) {
      FlowContext flowContext = flowContext();
      for (ActivityTracker tracker : proxyServer.getActivityTrackers()) {
        tracker.requestReceivedFromClient(flowContext, httpRequest);
      }
    }
  };

  private BytesWrittenMonitor bytesWrittenMonitor = new BytesWrittenMonitor() {
    @Override
    protected void bytesWritten(int numberOfBytes) {
      FlowContext flowContext = flowContext();
      for (ActivityTracker tracker : proxyServer.getActivityTrackers()) {
        tracker.bytesSentToClient(flowContext, numberOfBytes);
      }
    }
  };

  private ResponseWrittenMonitor responseWrittenMonitor = new ResponseWrittenMonitor() {
    @Override
    protected void responseWritten(HttpResponse httpResponse) {
      FlowContext flowContext = flowContext();
      for (ActivityTracker tracker : proxyServer.getActivityTrackers()) {
        tracker.responseSentToClient(flowContext, httpResponse);
      }
    }
  };

  private void recordClientConnected() {
    try {
      InetSocketAddress clientAddress = getClientAddress();
      for (ActivityTracker tracker : proxyServer.getActivityTrackers()) {
        tracker.clientConnected(clientAddress);
      }
    } catch (Exception e) {
      LOG.error("Unable to recordClientConnected", e);
    }
  }

  private void recordClientDisconnected() {
    try {
      InetSocketAddress clientAddress = getClientAddress();
      for (ActivityTracker tracker : proxyServer.getActivityTrackers()) {
        tracker.clientDisconnected(clientAddress, clientSslSession);
      }
    } catch (Exception e) {
      LOG.error("Unable to recordClientDisconnected", e);
    }
  }

  public InetSocketAddress getClientAddress() {
    if (channel == null) {
      return null;
    }
    return (InetSocketAddress) channel.remoteAddress();
  }

  private FlowContext flowContext() {
    if (currentServerConnection != null) {
      return new FullFlowContext(this, currentServerConnection);
    } else {
      return new FlowContext(this);
    }
  }

}
