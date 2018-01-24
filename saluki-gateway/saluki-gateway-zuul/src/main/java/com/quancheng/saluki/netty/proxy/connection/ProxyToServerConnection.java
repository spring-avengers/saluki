package com.quancheng.saluki.netty.proxy.connection;

import static com.quancheng.saluki.netty.proxy.ConnectionState.AWAITING_CHUNK;
import static com.quancheng.saluki.netty.proxy.ConnectionState.AWAITING_INITIAL;
import static com.quancheng.saluki.netty.proxy.ConnectionState.CONNECTING;
import static com.quancheng.saluki.netty.proxy.ConnectionState.DISCONNECTED;
import static com.quancheng.saluki.netty.proxy.ConnectionState.HANDSHAKING;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.RejectedExecutionException;

import javax.net.ssl.SSLProtocolException;

import com.google.common.net.HostAndPort;
import com.quancheng.saluki.netty.ActivityTracker;
import com.quancheng.saluki.netty.HttpFilter;
import com.quancheng.saluki.netty.proxy.ConnectionState;
import com.quancheng.saluki.netty.proxy.DefaultHttpProxyServer;
import com.quancheng.saluki.netty.proxy.flow.ConnectionFlow;
import com.quancheng.saluki.netty.proxy.flow.ConnectionFlowStep;
import com.quancheng.saluki.netty.proxy.flow.FullFlowContext;
import com.quancheng.saluki.utils.ProxyUtils;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.util.ReferenceCounted;
import io.netty.util.concurrent.Future;


@Sharable
public class ProxyToServerConnection extends ProxyConnection<HttpResponse> {

  private final ClientToProxyConnection clientConnection;
  private final ProxyToServerConnection serverConnection = this;
  private final String serverHostAndPort;
  private final Object connectLock = new Object();


  private volatile InetSocketAddress remoteAddress;
  private volatile InetSocketAddress localAddress;
  private volatile HttpFilter currentFilters;
  private volatile ConnectionFlow connectionFlow;
  private volatile boolean disableSni = false;
  private volatile HttpRequest initialRequest;
  private volatile HttpRequest currentHttpRequest;
  private volatile HttpResponse currentHttpResponse;
  private volatile GlobalTrafficShapingHandler trafficHandler;



  public static ProxyToServerConnection create(DefaultHttpProxyServer proxyServer,
      ClientToProxyConnection clientConnection, String serverHostAndPort, HttpFilter initialFilters,
      HttpRequest initialHttpRequest, GlobalTrafficShapingHandler globalTrafficShapingHandler)
      throws UnknownHostException {
    return new ProxyToServerConnection(proxyServer, clientConnection, serverHostAndPort,
        initialFilters, globalTrafficShapingHandler);
  }

  private ProxyToServerConnection(DefaultHttpProxyServer proxyServer,
      ClientToProxyConnection clientConnection, String serverHostAndPort, HttpFilter initialFilters,
      GlobalTrafficShapingHandler globalTrafficShapingHandler) throws UnknownHostException {
    super(DISCONNECTED, proxyServer, true);
    this.clientConnection = clientConnection;
    this.serverHostAndPort = serverHostAndPort;
    this.trafficHandler = globalTrafficShapingHandler;
    this.currentFilters = initialFilters;
    currentFilters.proxyToServerConnectionQueued();
    setupConnectionParameters();
  }

  @Override
  public void read(Object msg) {
    if (isConnecting()) {
      LOG.debug("In the middle of connecting, forwarding message to connection flow: {}", msg);
      this.connectionFlow.read(msg);
    } else {
      super.read(msg);
    }
  }

  @Override
  public ConnectionState readHTTPInitial(HttpResponse httpResponse) {
    LOG.debug("Received raw response: {}", httpResponse);

    if (httpResponse.decoderResult().isFailure()) {
      LOG.debug("Could not parse response from server. Decoder result: {}",
          httpResponse.decoderResult().toString());
      FullHttpResponse substituteResponse = ProxyUtils.createFullHttpResponse(HttpVersion.HTTP_1_1,
          HttpResponseStatus.BAD_GATEWAY, "Unable to parse response from server");
      HttpUtil.setKeepAlive(substituteResponse, false);
      httpResponse = substituteResponse;
    }
    currentFilters.serverToProxyResponseReceiving();
    rememberCurrentResponse(httpResponse);
    respondWith(httpResponse);
    if (ProxyUtils.isChunked(httpResponse)) {
      return AWAITING_CHUNK;
    } else {
      currentFilters.serverToProxyResponseReceived();
      return AWAITING_INITIAL;
    }
  }

  @Override
  public void readHTTPChunk(HttpContent chunk) {
    respondWith(chunk);
  }

  @Override
  public void readRaw(ByteBuf buf) {
    clientConnection.write(buf);
  }

  private class HeadAwareHttpResponseDecoder extends HttpResponseDecoder {

    public HeadAwareHttpResponseDecoder(int maxInitialLineLength, int maxHeaderSize,
        int maxChunkSize) {
      super(maxInitialLineLength, maxHeaderSize, maxChunkSize);
    }

    @Override
    public boolean isContentAlwaysEmpty(HttpMessage httpMessage) {
      if (currentHttpRequest == null) {
        return true;
      } else {
        return ProxyUtils.isHEAD(currentHttpRequest) || super.isContentAlwaysEmpty(httpMessage);
      }
    }
  };

  public void write(Object msg, HttpFilter filters) {
    this.currentFilters = filters;
    write(msg);
  }

  @Override
  public void write(Object msg) {
    LOG.debug("Requested write of {}", msg);

    if (msg instanceof ReferenceCounted) {
      LOG.debug("Retaining reference counted message");
      ((ReferenceCounted) msg).retain();
    }

    if (is(DISCONNECTED) && msg instanceof HttpRequest) {
      LOG.debug("Currently disconnected, connect and then write the message");
      connectAndWrite((HttpRequest) msg);
    } else {
      if (isConnecting()) {
        synchronized (connectLock) {
          if (isConnecting()) {
            LOG.debug(
                "Attempted to write while still in the process of connecting, waiting for connection.");
            clientConnection.stopReading();
            try {
              connectLock.wait(30000);
            } catch (InterruptedException ie) {
              LOG.warn("Interrupted while waiting for connect monitor");
            }
          }
        }
      }
      if (isConnecting() || getCurrentState().isDisconnectingOrDisconnected()) {
        LOG.debug(
            "Connection failed or timed out while waiting to write message to server. Message will be discarded: {}",
            msg);
        return;
      }
      LOG.debug("Using existing connection to: {}", remoteAddress);
      doWrite(msg);
    }
  };

  @Override
  public void writeHttp(HttpObject httpObject) {
    if (httpObject instanceof HttpRequest) {
      HttpRequest httpRequest = (HttpRequest) httpObject;
      currentHttpRequest = httpRequest;
    }
    super.writeHttp(httpObject);
  }

  @Override
  public void become(ConnectionState newState) {
    if (getCurrentState() == DISCONNECTED && newState == CONNECTING) {
      currentFilters.proxyToServerConnectionStarted();
    } else if (getCurrentState() == CONNECTING) {
      if (newState == HANDSHAKING) {
        currentFilters.proxyToServerConnectionSSLHandshakeStarted();
      } else if (newState == AWAITING_INITIAL) {
        currentFilters.proxyToServerConnectionSucceeded(ctx);
      } else if (newState == DISCONNECTED) {
        currentFilters.proxyToServerConnectionFailed();
      }
    } else if (getCurrentState() == HANDSHAKING) {
      if (newState == AWAITING_INITIAL) {
        currentFilters.proxyToServerConnectionSucceeded(ctx);
      } else if (newState == DISCONNECTED) {
        currentFilters.proxyToServerConnectionFailed();
      }
    } else if (getCurrentState() == AWAITING_CHUNK && newState != AWAITING_CHUNK) {
      currentFilters.serverToProxyResponseReceived();
    }

    super.become(newState);
  }

  @Override
  public void becameSaturated() {
    super.becameSaturated();
    this.clientConnection.serverBecameSaturated(this);
  }

  @Override
  public void becameWritable() {
    super.becameWritable();
    this.clientConnection.serverBecameWriteable(this);
  }

  @Override
  public void timedOut() {
    super.timedOut();
    clientConnection.timedOut(this);
  }

  @Override
  public void disconnected() {
    super.disconnected();
    clientConnection.serverDisconnected(this);
  }

  @Override
  public void exceptionCaught(Throwable cause) {
    try {
      if (cause instanceof IOException) {
        LOG.info("An IOException occurred on ProxyToServerConnection: " + cause.getMessage());
        LOG.debug("An IOException occurred on ProxyToServerConnection", cause);
      } else if (cause instanceof RejectedExecutionException) {
        LOG.info(
            "An executor rejected a read or write operation on the ProxyToServerConnection (this is normal if the proxy is shutting down). Message: "
                + cause.getMessage());
        LOG.debug("A RejectedExecutionException occurred on ProxyToServerConnection", cause);
      } else {
        LOG.error("Caught an exception on ProxyToServerConnection", cause);
      }
    } finally {
      if (!is(DISCONNECTED)) {
        LOG.info("Disconnecting open connection to server");
        disconnect();
      }
    }
  }


  public InetSocketAddress getRemoteAddress() {
    return remoteAddress;
  }

  public String getServerHostAndPort() {
    return serverHostAndPort;
  }

  public HttpRequest getInitialRequest() {
    return initialRequest;
  }

  @Override
  public HttpFilter getHttpFiltersFromProxyServer(HttpRequest httpRequest) {
    return currentFilters;
  }


  private void rememberCurrentResponse(HttpResponse response) {
    LOG.debug("Remembering the current response.");
    currentHttpResponse = ProxyUtils.copyMutableResponseFields(response);
  }


  private void respondWith(HttpObject httpObject) {
    clientConnection.respond(this, currentFilters, currentHttpRequest, currentHttpResponse,
        httpObject);
  }


  private void connectAndWrite(HttpRequest initialRequest) {
    LOG.debug("Starting new connection to: {}", remoteAddress);
    this.initialRequest = initialRequest;
    initializeConnectionFlow();
    connectionFlow.start();
  }


  private void initializeConnectionFlow() {
    this.connectionFlow =
        new ConnectionFlow(clientConnection, this, connectLock).then(ConnectChannel);
    if (ProxyUtils.isCONNECT(initialRequest)) {
      connectionFlow.then(serverConnection.StartTunneling)
          .then(clientConnection.RespondCONNECTSuccessful).then(clientConnection.StartTunneling);
    }
  }


  private ConnectionFlowStep ConnectChannel = new ConnectionFlowStep(this, CONNECTING) {
    @Override
    public boolean shouldExecuteOnEventLoop() {
      return false;
    }

    @Override
    public Future<?> execute() {
      Bootstrap cb = new Bootstrap();
      cb.group(proxyServer.getProxyToServerWorkerFor())//
          .channel(NioSocketChannel.class)
          .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, proxyServer.getConnectTimeout())
          .handler(new ChannelInitializer<Channel>() {
            public void initChannel(Channel ch) throws Exception {
              initChannelPipeline(ch.pipeline(), initialRequest);
            };
          });
      if (localAddress != null) {
        return cb.connect(remoteAddress, localAddress);
      } else {
        return cb.connect(remoteAddress);
      }
    }
  };


  public boolean connectionFailed(Throwable cause) throws UnknownHostException {
    if (!disableSni && cause instanceof SSLProtocolException) {
      if (cause.getMessage() != null && cause.getMessage().contains("unrecognized_name")) {
        LOG.debug(
            "Failed to connect to server due to an unrecognized_name SSL warning. Retrying connection without SNI.");
        disableSni = true;
        resetConnectionForRetry();
        connectAndWrite(initialRequest);
        return true;
      }
    }
    disableSni = false;
    return false;
  }


  private void resetConnectionForRetry() throws UnknownHostException {
    this.ctx.pipeline().remove(this);
    this.ctx.close();
    this.ctx = null;
    this.setupConnectionParameters();
  }


  private void setupConnectionParameters() throws UnknownHostException {
    this.remoteAddress = this.currentFilters.proxyToServerResolutionStarted(serverHostAndPort);
    String hostAndPort = null;
    try {
      if (this.remoteAddress == null) {
        hostAndPort = serverHostAndPort;
        this.remoteAddress = addressFor(serverHostAndPort, proxyServer);
      } else if (this.remoteAddress.isUnresolved()) {
        hostAndPort = HostAndPort
            .fromParts(this.remoteAddress.getHostName(), this.remoteAddress.getPort()).toString();
        this.remoteAddress = proxyServer.getServerResolver()
            .resolve(this.remoteAddress.getHostName(), this.remoteAddress.getPort());
      }
    } catch (UnknownHostException e) {
      this.currentFilters.proxyToServerResolutionFailed(hostAndPort);
      throw e;
    }
    this.currentFilters.proxyToServerResolutionSucceeded(serverHostAndPort, this.remoteAddress);
    this.localAddress = proxyServer.getLocalAddress();
  }


  private void initChannelPipeline(ChannelPipeline pipeline, HttpRequest httpRequest) {
    if (trafficHandler != null) {
      pipeline.addLast("global-traffic-shaping", trafficHandler);
    }
    pipeline.addLast("bytesReadMonitor", bytesReadMonitor);
    pipeline.addLast("bytesWrittenMonitor", bytesWrittenMonitor);
    pipeline.addLast("encoder", new HttpRequestEncoder());
    pipeline.addLast("decoder",
        new HeadAwareHttpResponseDecoder(proxyServer.getMaxInitialLineLength(),
            proxyServer.getMaxHeaderSize(), proxyServer.getMaxChunkSize()));
    int numberOfBytesToBuffer =
        proxyServer.getFiltersSource().getMaximumResponseBufferSizeInBytes();
    if (numberOfBytesToBuffer > 0) {
      aggregateContentForFiltering(pipeline, numberOfBytesToBuffer);
    }
    pipeline.addLast("responseReadMonitor", responseReadMonitor);
    pipeline.addLast("requestWrittenMonitor", requestWrittenMonitor);
    pipeline.addLast("idle", new IdleStateHandler(0, 0, proxyServer.getIdleConnectionTimeout()));
    pipeline.addLast("handler", this);
  }


  public void connectionSucceeded(boolean shouldForwardInitialRequest) {
    become(AWAITING_INITIAL);
    clientConnection.serverConnectionSucceeded(this, shouldForwardInitialRequest);
    if (shouldForwardInitialRequest) {
      LOG.debug("Writing initial request: {}", initialRequest);
      write(initialRequest);
    } else {
      LOG.debug("Dropping initial request: {}", initialRequest);
    }
    if (initialRequest instanceof ReferenceCounted) {
      ((ReferenceCounted) initialRequest).release();
    }
  }

  public static InetSocketAddress addressFor(String hostAndPort, DefaultHttpProxyServer proxyServer)
      throws UnknownHostException {
    HostAndPort parsedHostAndPort;
    try {
      parsedHostAndPort = HostAndPort.fromString(hostAndPort);
    } catch (IllegalArgumentException e) {
      throw new UnknownHostException(hostAndPort);
    }
    String host = parsedHostAndPort.getHost();
    int port = parsedHostAndPort.getPortOrDefault(80);

    return proxyServer.getServerResolver().resolve(host, port);
  }

  private final BytesReadMonitor bytesReadMonitor = new BytesReadMonitor() {
    @Override
    public void bytesRead(int numberOfBytes) {
      FullFlowContext flowContext =
          new FullFlowContext(clientConnection, ProxyToServerConnection.this);
      for (ActivityTracker tracker : proxyServer.getActivityTrackers()) {
        tracker.bytesReceivedFromServer(flowContext, numberOfBytes);
      }
    }
  };

  private ResponseReadMonitor responseReadMonitor = new ResponseReadMonitor() {
    @Override
    public void responseRead(HttpResponse httpResponse) {
      FullFlowContext flowContext =
          new FullFlowContext(clientConnection, ProxyToServerConnection.this);
      for (ActivityTracker tracker : proxyServer.getActivityTrackers()) {
        tracker.responseReceivedFromServer(flowContext, httpResponse);
      }
    }
  };

  private BytesWrittenMonitor bytesWrittenMonitor = new BytesWrittenMonitor() {
    @Override
    public void bytesWritten(int numberOfBytes) {
      FullFlowContext flowContext =
          new FullFlowContext(clientConnection, ProxyToServerConnection.this);
      for (ActivityTracker tracker : proxyServer.getActivityTrackers()) {
        tracker.bytesSentToServer(flowContext, numberOfBytes);
      }
    }
  };

  private RequestWrittenMonitor requestWrittenMonitor = new RequestWrittenMonitor() {
    @Override
    public void requestWriting(HttpRequest httpRequest) {
      FullFlowContext flowContext =
          new FullFlowContext(clientConnection, ProxyToServerConnection.this);
      try {
        for (ActivityTracker tracker : proxyServer.getActivityTrackers()) {
          tracker.requestSentToServer(flowContext, httpRequest);
        }
      } catch (Throwable t) {
        LOG.warn("Error while invoking ActivityTracker on request", t);
      }

      currentFilters.proxyToServerRequestSending();
    }

    @Override
    public void requestWritten(HttpRequest httpRequest) {}

    @Override
    public void contentWritten(HttpContent httpContent) {
      if (httpContent instanceof LastHttpContent) {
        currentFilters.proxyToServerRequestSent();
      }
    }
  };

}
