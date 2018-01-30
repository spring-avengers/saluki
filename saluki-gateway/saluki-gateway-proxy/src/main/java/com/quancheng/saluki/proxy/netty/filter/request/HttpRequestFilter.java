package com.quancheng.saluki.proxy.netty.filter.request;

import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.saluki.proxy.config.SpringContextHolder;
import com.quancheng.saluki.proxy.netty.filter.FilterUtil;
import com.quancheng.saluki.proxy.routerules.FilterRuleCacheComponent;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;


public abstract class HttpRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger("ProxyFilterLog");


  public abstract HttpResponse doFilter(HttpRequest originalRequest, HttpObject httpObject,
      ChannelHandlerContext channelHandlerContext);

  public abstract int filterOrder();

  protected List<Pattern> getRule(Class<?> filterClazz) {
    FilterRuleCacheComponent ruleCache =
        SpringContextHolder.getBean(FilterRuleCacheComponent.class);
    return ruleCache.getFilterRuleByClass(filterClazz);

  }

  protected HttpResponse createResponse(HttpResponseStatus httpResponseStatus,
      HttpRequest originalRequest) {
    HttpHeaders httpHeaders = new DefaultHttpHeaders();
    httpHeaders.add("Transfer-Encoding", "chunked");
    HttpResponse httpResponse =
        new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus);
    List<String> originHeader = FilterUtil.getHeaderValues(originalRequest, "Origin");
    if (originHeader.size() > 0) {
      httpHeaders.set("Access-Control-Allow-Credentials", "true");
      httpHeaders.set("Access-Control-Allow-Origin", originHeader.get(0));
    }
    httpResponse.headers().add(httpHeaders);
    return httpResponse;
  }

  protected void writeFilterLog(String fact, Class<?> type, String cause) {
    logger.info("type:{},fact:{},cause:{}", type.getSimpleName(), fact, cause);
  }
}
