package com.quancheng.saluki.netty.callback;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;


public class HttpFilterSource {

  public HttpFilter filterRequest(HttpRequest originalRequest) {
    return new HttpFilter(originalRequest, null);
  }

  public HttpFilter filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
    return filterRequest(originalRequest);
  }

  public int getMaximumRequestBufferSizeInBytes() {
    return 0;
  }

  public int getMaximumResponseBufferSizeInBytes() {
    return 0;
  }

}
