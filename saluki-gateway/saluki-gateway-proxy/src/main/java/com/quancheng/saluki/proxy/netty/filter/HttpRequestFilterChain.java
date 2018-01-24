package com.quancheng.saluki.proxy.netty.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;


public class HttpRequestFilterChain {
  public List<HttpRequestFilter> filters = new ArrayList<>();

  private static HttpRequestFilterChain filterChain = new HttpRequestFilterChain();

  public static HttpRequestFilterChain requestFilterChain() {
    return filterChain;
  }

  public HttpRequestFilterChain addFilter(HttpRequestFilter filter) {
    filters.add(filter);
    Collections.sort(filters, new Comparator<HttpRequestFilter>() {
      @Override
      public int compare(HttpRequestFilter o1, HttpRequestFilter o2) {
        return o1.filterOrder() - o2.filterOrder();
      }
    });
    return this;
  }


  public HttpResponse doFilter(HttpRequest originalRequest, HttpObject httpObject,
      ChannelHandlerContext channelHandlerContext) {
    for (HttpRequestFilter filter : filters) {
      HttpResponse response = filter.doFilter(originalRequest, httpObject, channelHandlerContext);
      if (response != null) {
        return response;
      }
    }
    return null;
  }
}
