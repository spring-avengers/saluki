package com.quancheng.saluki.netty;

import java.net.InetSocketAddress;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;


public class AbstractHttpFilter {

  public static final AbstractHttpFilter NOOP_FILTER = new AbstractHttpFilter(null);

  protected final HttpRequest originalRequest;

  protected final ChannelHandlerContext ctx;

  public AbstractHttpFilter(HttpRequest originalRequest, ChannelHandlerContext ctx) {
    this.originalRequest = originalRequest;
    this.ctx = ctx;
  }

  public AbstractHttpFilter(HttpRequest originalRequest) {
    this(originalRequest, null);
  }


  public HttpResponse clientToProxyRequest(HttpObject httpObject) {
    return null;
  }


  public HttpResponse proxyToServerRequest(HttpObject httpObject) {
    return null;
  }


  public void proxyToServerRequestSending() {}


  public void proxyToServerRequestSent() {}


  public HttpObject serverToProxyResponse(HttpObject httpObject) {
    return httpObject;
  }


  public void serverToProxyResponseTimedOut() {}


  public void serverToProxyResponseReceiving() {}


  public void serverToProxyResponseReceived() {}


  public HttpObject proxyToClientResponse(HttpObject httpObject) {
    return httpObject;
  }


  public void proxyToServerConnectionQueued() {}


  public InetSocketAddress proxyToServerResolutionStarted(String resolvingServerHostAndPort) {
    return null;
  }


  public void proxyToServerResolutionFailed(String hostAndPort) {}


  public void proxyToServerResolutionSucceeded(String serverHostAndPort,
      InetSocketAddress resolvedRemoteAddress) {}


  public void proxyToServerConnectionStarted() {}


  public void proxyToServerConnectionSSLHandshakeStarted() {}


  public void proxyToServerConnectionFailed() {}


  public void proxyToServerConnectionSucceeded(ChannelHandlerContext serverCtx) {}
}
