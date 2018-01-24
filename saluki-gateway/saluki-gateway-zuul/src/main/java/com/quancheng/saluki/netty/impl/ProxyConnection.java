package com.quancheng.saluki.netty.impl;

import static com.quancheng.saluki.netty.impl.support.ConnectionState.AWAITING_CHUNK;
import static com.quancheng.saluki.netty.impl.support.ConnectionState.AWAITING_INITIAL;
import static com.quancheng.saluki.netty.impl.support.ConnectionState.DISCONNECTED;
import static com.quancheng.saluki.netty.impl.support.ConnectionState.NEGOTIATING_CONNECT;

import com.quancheng.saluki.netty.callback.HttpFilter;
import com.quancheng.saluki.netty.impl.support.ConnectionState;
import com.quancheng.saluki.utils.ProxyUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCounted;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;


abstract class ProxyConnection<I extends HttpObject> extends SimpleChannelInboundHandler<Object> {
  protected final ProxyConnectionLogger LOG = new ProxyConnectionLogger(this);

  protected final DefaultHttpProxyServer proxyServer;
  protected final boolean runsAsSslClient;

  protected volatile ChannelHandlerContext ctx;
  protected volatile Channel channel;
  private volatile ConnectionState currentState;
  private volatile boolean tunneling = false;
  protected volatile long lastReadTime = 0;

  protected ProxyConnection(ConnectionState initialState, DefaultHttpProxyServer proxyServer,
      boolean runsAsSslClient) {
    become(initialState);
    this.proxyServer = proxyServer;
    this.runsAsSslClient = runsAsSslClient;
  }


  protected void read(Object msg) {
    LOG.debug("Reading: {}", msg);
    lastReadTime = System.currentTimeMillis();
    if (tunneling) {
      readRaw((ByteBuf) msg);
    } else {
      readHTTP((HttpObject) msg);
    }
  }


  @SuppressWarnings("unchecked")
  private void readHTTP(HttpObject httpObject) {
    ConnectionState nextState = getCurrentState();
    switch (getCurrentState()) {
      case AWAITING_INITIAL:
        if (httpObject instanceof HttpMessage) {
          nextState = readHTTPInitial((I) httpObject);
        } else {
          LOG.debug(
              "Dropping message because HTTP object was not an HttpMessage. HTTP object may be orphaned content from a short-circuited response. Message: {}",
              httpObject);
        }
        break;
      case AWAITING_CHUNK:
        HttpContent chunk = (HttpContent) httpObject;
        readHTTPChunk(chunk);
        nextState = ProxyUtils.isLastChunk(chunk) ? AWAITING_INITIAL : AWAITING_CHUNK;
        break;
      case AWAITING_PROXY_AUTHENTICATION:
        if (httpObject instanceof HttpRequest) {
          nextState = readHTTPInitial((I) httpObject);
        } else {
        }
        break;
      case CONNECTING:
        LOG.warn(
            "Attempted to read from connection that's in the process of connecting.  This shouldn't happen.");
        break;
      case NEGOTIATING_CONNECT:
        LOG.debug(
            "Attempted to read from connection that's in the process of negotiating an HTTP CONNECT.  This is probably the LastHttpContent of a chunked CONNECT.");
        break;
      case AWAITING_CONNECT_OK:
        LOG.warn("AWAITING_CONNECT_OK should have been handled by ProxyToServerConnection.read()");
        break;
      case HANDSHAKING:
        LOG.warn(
            "Attempted to read from connection that's in the process of handshaking.  This shouldn't happen.",
            channel);
        break;
      case DISCONNECT_REQUESTED:
      case DISCONNECTED:
        LOG.info("Ignoring message since the connection is closed or about to close");
        break;
    }
    become(nextState);
  }


  protected abstract ConnectionState readHTTPInitial(I httpObject);

  protected abstract void readHTTPChunk(HttpContent chunk);

  protected abstract void readRaw(ByteBuf buf);


  void write(Object msg) {
    if (msg instanceof ReferenceCounted) {
      LOG.debug("Retaining reference counted message");
      ((ReferenceCounted) msg).retain();
    }

    doWrite(msg);
  }

  void doWrite(Object msg) {
    LOG.debug("Writing: {}", msg);

    try {
      if (msg instanceof HttpObject) {
        writeHttp((HttpObject) msg);
      } else {
        writeRaw((ByteBuf) msg);
      }
    } finally {
      LOG.debug("Wrote: {}", msg);
    }
  }


  protected void writeHttp(HttpObject httpObject) {
    if (ProxyUtils.isLastChunk(httpObject)) {
      channel.write(httpObject);
      LOG.debug("Writing an empty buffer to signal the end of our chunked transfer");
      writeToChannel(Unpooled.EMPTY_BUFFER);
    } else {
      writeToChannel(httpObject);
    }
  }


  protected void writeRaw(ByteBuf buf) {
    writeToChannel(buf);
  }

  protected ChannelFuture writeToChannel(final Object msg) {
    return channel.writeAndFlush(msg);
  }


  protected void connected() {
    LOG.debug("Connected");
  }


  protected void disconnected() {
    become(DISCONNECTED);
    LOG.debug("Disconnected");
  }

  protected void timedOut() {
    disconnect();
  }


  protected ConnectionFlowStep StartTunneling = new ConnectionFlowStep(this, NEGOTIATING_CONNECT) {
    @Override
    boolean shouldSuppressInitialRequest() {
      return true;
    }

    protected Future<?> execute() {
      try {
        ChannelPipeline pipeline = ctx.pipeline();
        if (pipeline.get("encoder") != null) {
          pipeline.remove("encoder");
        }
        if (pipeline.get("responseWrittenMonitor") != null) {
          pipeline.remove("responseWrittenMonitor");
        }
        if (pipeline.get("decoder") != null) {
          pipeline.remove("decoder");
        }
        if (pipeline.get("requestReadMonitor") != null) {
          pipeline.remove("requestReadMonitor");
        }
        tunneling = true;
        return channel.newSucceededFuture();
      } catch (Throwable t) {
        return channel.newFailedFuture(t);
      }
    }
  };



  protected void aggregateContentForFiltering(ChannelPipeline pipeline, int numberOfBytesToBuffer) {
    pipeline.addLast("inflater", new HttpContentDecompressor());
    pipeline.addLast("aggregator", new HttpObjectAggregator(numberOfBytesToBuffer));
  }


  protected void becameSaturated() {
    LOG.debug("Became saturated");
  }

  protected void becameWritable() {
    LOG.debug("Became writeable");
  }

  protected void exceptionCaught(Throwable cause) {}


  Future<Void> disconnect() {
    if (channel == null) {
      return null;
    } else {
      final Promise<Void> promise = channel.newPromise();
      writeToChannel(Unpooled.EMPTY_BUFFER)
          .addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
              closeChannel(promise);
            }
          });
      return promise;
    }
  }

  private void closeChannel(final Promise<Void> promise) {
    channel.close().addListener(new GenericFutureListener<Future<? super Void>>() {
      public void operationComplete(Future<? super Void> future) throws Exception {
        if (future.isSuccess()) {
          promise.setSuccess(null);
        } else {
          promise.setFailure(future.cause());
        }
      };
    });
  }

  protected boolean isSaturated() {
    return !this.channel.isWritable();
  }

  protected boolean is(ConnectionState state) {
    return currentState == state;
  }

  protected boolean isConnecting() {
    return currentState.isPartOfConnectionFlow();
  }

  protected void become(ConnectionState state) {
    this.currentState = state;
  }

  protected ConnectionState getCurrentState() {
    return currentState;
  }

  public boolean isTunneling() {
    return tunneling;
  }

  protected void stopReading() {
    LOG.debug("Stopped reading");
    this.channel.config().setAutoRead(false);
  }


  protected void resumeReading() {
    LOG.debug("Resumed reading");
    this.channel.config().setAutoRead(true);
  }

  protected HttpFilter getHttpFiltersFromProxyServer(HttpRequest httpRequest) {
    return proxyServer.getFiltersSource().filterRequest(httpRequest, ctx);
  }

  ProxyConnectionLogger getLOG() {
    return LOG;
  }

  @Override
  protected final void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
    read(msg);
  }

  @Override
  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    try {
      this.ctx = ctx;
      this.channel = ctx.channel();
      this.proxyServer.registerChannel(ctx.channel());
    } finally {
      super.channelRegistered(ctx);
    }
  }

  @Override
  public final void channelActive(ChannelHandlerContext ctx) throws Exception {
    try {
      connected();
    } finally {
      super.channelActive(ctx);
    }
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    try {
      disconnected();
    } finally {
      super.channelInactive(ctx);
    }
  }

  @Override
  public final void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
    LOG.debug("Writability changed. Is writable: {}", channel.isWritable());
    try {
      if (this.channel.isWritable()) {
        becameWritable();
      } else {
        becameSaturated();
      }
    } finally {
      super.channelWritabilityChanged(ctx);
    }
  }

  @Override
  public final void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    exceptionCaught(cause);
  }

  @Override
  public final void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    try {
      if (evt instanceof IdleStateEvent) {
        LOG.debug("Got idle");
        timedOut();
      }
    } finally {
      super.userEventTriggered(ctx, evt);
    }
  }

  @Sharable
  protected abstract class BytesReadMonitor extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      try {
        if (msg instanceof ByteBuf) {
          bytesRead(((ByteBuf) msg).readableBytes());
        }
      } catch (Throwable t) {
        LOG.warn("Unable to record bytesRead", t);
      } finally {
        super.channelRead(ctx, msg);
      }
    }

    protected abstract void bytesRead(int numberOfBytes);
  }

  @Sharable
  protected abstract class RequestReadMonitor extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      try {
        if (msg instanceof HttpRequest) {
          requestRead((HttpRequest) msg);
        }
      } catch (Throwable t) {
        LOG.warn("Unable to record bytesRead", t);
      } finally {
        super.channelRead(ctx, msg);
      }
    }

    protected abstract void requestRead(HttpRequest httpRequest);
  }

  @Sharable
  protected abstract class ResponseReadMonitor extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      try {
        if (msg instanceof HttpResponse) {
          responseRead((HttpResponse) msg);
        }
      } catch (Throwable t) {
        LOG.warn("Unable to record bytesRead", t);
      } finally {
        super.channelRead(ctx, msg);
      }
    }

    protected abstract void responseRead(HttpResponse httpResponse);
  }

  @Sharable
  protected abstract class BytesWrittenMonitor extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
        throws Exception {
      try {
        if (msg instanceof ByteBuf) {
          bytesWritten(((ByteBuf) msg).readableBytes());
        }
      } catch (Throwable t) {
        LOG.warn("Unable to record bytesRead", t);
      } finally {
        super.write(ctx, msg, promise);
      }
    }

    protected abstract void bytesWritten(int numberOfBytes);
  }

  @Sharable
  protected abstract class RequestWrittenMonitor extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
        throws Exception {
      HttpRequest originalRequest = null;
      if (msg instanceof HttpRequest) {
        originalRequest = (HttpRequest) msg;
      }

      if (null != originalRequest) {
        requestWriting(originalRequest);
      }

      super.write(ctx, msg, promise);

      if (null != originalRequest) {
        requestWritten(originalRequest);
      }

      if (msg instanceof HttpContent) {
        contentWritten((HttpContent) msg);
      }
    }

    protected abstract void requestWriting(HttpRequest httpRequest);

    protected abstract void requestWritten(HttpRequest httpRequest);

    protected abstract void contentWritten(HttpContent httpContent);
  }

  @Sharable
  protected abstract class ResponseWrittenMonitor extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
        throws Exception {
      try {
        if (msg instanceof HttpResponse) {
          responseWritten(((HttpResponse) msg));
        }
      } catch (Throwable t) {
        LOG.warn("Error while invoking responseWritten callback", t);
      } finally {
        super.write(ctx, msg, promise);
      }
    }

    protected abstract void responseWritten(HttpResponse httpResponse);
  }

}
