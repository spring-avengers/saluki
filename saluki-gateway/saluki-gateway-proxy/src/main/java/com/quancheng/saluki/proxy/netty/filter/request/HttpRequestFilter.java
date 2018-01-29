package com.quancheng.saluki.proxy.netty.filter.request;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.quancheng.saluki.proxy.config.SpringContextHolder;
import com.quancheng.saluki.proxy.routerules.FilterRuleCacheComponent;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;


public abstract class HttpRequestFilter {

  public abstract HttpResponse doFilter(HttpRequest originalRequest, HttpObject httpObject,
      ChannelHandlerContext channelHandlerContext);

  public abstract int filterOrder();

  public List<Pattern> getRule(Class<?> filterClazz) {
    FilterRuleCacheComponent ruleCache =
        SpringContextHolder.getBean(FilterRuleCacheComponent.class);
    return ruleCache.getFilterRuleByClass(filterClazz);

  }


  protected void writeFilterLog(Logger logger, String realIp, String type, String cause) {
    logger.debug("type:{},realIp:{},cause:{}", type, realIp, cause);
  }

  protected static List<String> getHeaderValues(HttpMessage httpMessage, String headerName) {
    List<String> list = Lists.newArrayList();
    for (Map.Entry<String, String> header : httpMessage.headers().entries()) {
      if (header.getKey().toLowerCase().equals(headerName.toLowerCase())) {
        list.add(header.getValue());
      }
    }
    return list;
  }

  protected static HttpResponse createResponse(HttpResponseStatus httpResponseStatus,
      HttpRequest originalRequest) {
    HttpHeaders httpHeaders = new DefaultHttpHeaders();
    httpHeaders.add("Transfer-Encoding", "chunked");
    HttpResponse httpResponse =
        new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus);
    List<String> originHeader = getHeaderValues(originalRequest, "Origin");
    if (originHeader.size() > 0) {
      httpHeaders.set("Access-Control-Allow-Credentials", "true");
      httpHeaders.set("Access-Control-Allow-Origin", originHeader.get(0));
    }
    httpResponse.headers().add(httpHeaders);
    return httpResponse;
  }

  protected static String getRealIp(HttpRequest httpRequest,
      ChannelHandlerContext channelHandlerContext) {
    List<String> headerValues = getHeaderValues(httpRequest, "X-Real-IP");
    return headerValues.get(0);
  }

}
