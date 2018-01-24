package com.quancheng.saluki.netty.filter;

import org.slf4j.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;


public abstract class HttpRequestFilter {

  public abstract boolean doFilter(HttpRequest originalRequest, HttpObject httpObject,
      ChannelHandlerContext channelHandlerContext);


  public boolean isBlacklist() {
    return true;
  }


  public void hackLog(Logger logger, String realIp, String type, String cause) {
    if (isBlacklist()) {
      logger.info("type:{},realIp:{},cause:{}", type, realIp, cause);
    } else {
      logger.debug("type:{},realIp:{},cause:{}", type, realIp, cause);
    }
  }
}
