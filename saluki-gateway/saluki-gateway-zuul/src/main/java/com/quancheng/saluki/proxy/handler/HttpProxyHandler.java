package com.quancheng.saluki.proxy.handler;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.saluki.proxy.IllegalRouteException;
import com.quancheng.saluki.proxy.connection.Connection;
import com.quancheng.saluki.proxy.connection.ConnectionPool;
import com.quancheng.saluki.zuul.api.Route;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpRequest;

public class HttpProxyHandler extends ChannelInboundHandlerAdapter {

  private static final Logger LOG = LoggerFactory.getLogger(HttpProxyHandler.class);
  private static final String HANDLER_NAME = "http-pipe-back";


  private final ConnectionPool connectionPool;
  private volatile Connection outboundConnection;


  public HttpProxyHandler(ConnectionPool connectionPool) {
    this.connectionPool = connectionPool;
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    ctx.channel().close();
    destroyConnection();
  }

  private void destroyConnection() {
    final Connection connection = outboundConnection;
    if (connection != null) {
      this.outboundConnection = null;
      removeHandler(connection);
      connectionPool.destroy(connection);
    }
  }

  private void removeHandler(Connection connection) {
    if (connection.getChannel().pipeline().get(HANDLER_NAME) != null) {
      connection.getChannel().pipeline().remove(HANDLER_NAME);
    }
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    final Channel inboundChannel = ctx.channel();
    try {
      if (msg instanceof HttpRequest) {
        final HttpRequest request = (HttpRequest) msg;
        URL routeHost = getRoute(request);
        if (!connected() || !routeHost.equals(outboundConnection.getRouteHost())) {
          disposeConnection();
          final Connection connection = newConnection(routeHost, inboundChannel);
          connection.getChannelFuture().addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
              final Channel outboundChannel = future.channel();
              performWrite(inboundChannel, outboundChannel, request);
            }

          });

        } else {
          performWrite(inboundChannel, outboundConnection.getChannel(), request);
        }
      }
    } catch (IllegalRouteException e) {
      inboundChannel.close();
      LOG.warn("dropped connection for bad route: {}", e.getRoute());
    }
  }

  private URL getRoute(HttpRequest request) throws IllegalRouteException {
    URL route;
    String sRoute = request.headers().get(Route.ROUTE_HEADER);
    try {
      route = new URL(sRoute);
    } catch (MalformedURLException e) {
      throw new IllegalRouteException(sRoute);
    }
    LOG.debug("found route: {}", route);
    return route;
  }

  private void disposeConnection() {
    final Connection connection = outboundConnection;
    if (connection != null) {
      this.outboundConnection = null;
      removeHandler(connection);
      connectionPool.release(connection);
    }
  }

  private boolean connected() {
    return outboundConnection != null && outboundConnection.getChannel().isActive();
  }

  private Connection newConnection(URL hostRoute, final Channel inboundChannel)
      throws IllegalRouteException {
    inboundChannel.config().setAutoRead(false);
    final Connection outboundConnection = connectionPool.borrow(hostRoute);
    outboundConnection.getChannel().pipeline().addLast(HANDLER_NAME,
        new OutboundHandler(inboundChannel));
    outboundConnection.getChannelFuture().addListener(new ChannelFutureListener() {
      @Override
      public void operationComplete(ChannelFuture future) throws Exception {
        inboundChannel.config().setAutoRead(true);
      }

    });

    this.outboundConnection = outboundConnection;
    return outboundConnection;
  }

  private void performWrite(Channel inboundChannel, Channel outboundChannel, final Object request) {
    if (outboundChannel.isActive()) {
      outboundChannel.write(request);
    } else {
      LOG.warn("write failed: not connected");
      inboundChannel.close();
    }
  }

  private class OutboundHandler extends ChannelDuplexHandler {
    private final Channel inboundChannel;

    public OutboundHandler(Channel inboundChannel) {
      this.inboundChannel = inboundChannel;
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
      LOG.debug("outbound disconnected");
      super.disconnect(ctx, promise);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      if (inboundChannel.isActive()) {
        inboundChannel.write(msg);
      } else {
        inboundChannel.close();
        destroyConnection();
      }
    }
  }


}
