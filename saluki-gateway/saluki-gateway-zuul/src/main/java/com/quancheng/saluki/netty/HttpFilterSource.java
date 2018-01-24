package com.quancheng.saluki.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;


public class HttpFilterSource {

  public AbstractHttpFilter filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
    return new AbstractHttpFilter(originalRequest, ctx);
  }

  public int getMaximumRequestBufferSizeInBytes() {
    return 0;
  }

  public int getMaximumResponseBufferSizeInBytes() {
    return 0;
  }

}
