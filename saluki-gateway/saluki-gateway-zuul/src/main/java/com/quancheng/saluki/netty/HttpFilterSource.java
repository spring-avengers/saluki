package com.quancheng.saluki.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;


public abstract class HttpFilterSource {

  public abstract AbstractHttpFilter filterRequest(HttpRequest originalRequest,
      ChannelHandlerContext ctx);

  public int getMaximumRequestBufferSizeInBytes() {
    return 0;
  }

  public int getMaximumResponseBufferSizeInBytes() {
    return 0;
  }

}
