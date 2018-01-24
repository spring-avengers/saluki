/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.quancheng.saluki.netty;

import java.net.InetSocketAddress;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

/**
 * @author liushiming
 * @version HttpFiltersAdapter.java, v 0.0.1 2018年1月24日 下午3:06:25 liushiming
 */
public class HttpFiltersAdapter {

  protected final HttpRequest originalRequest;
  protected final ChannelHandlerContext ctx;

  public HttpFiltersAdapter(HttpRequest originalRequest, ChannelHandlerContext ctx) {
    this.originalRequest = originalRequest;
    this.ctx = ctx;
  }

  public HttpFiltersAdapter(HttpRequest originalRequest) {
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
