package com.quancheng.saluki.netty.filter;

import java.util.ArrayList;
import java.util.List;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;


public class HttpResponseFilterChain {
  public List<HttpResponseFilter> filters = new ArrayList<>();

  public HttpResponseFilterChain addFilter(HttpResponseFilter filter) {
    filters.add(filter);
    return this;
  }

  public void doFilter(HttpRequest originalRequest, HttpResponse httpResponse) {
    for (HttpResponseFilter filter : filters) {
      filter.doFilter(originalRequest, httpResponse);
    }
  }
}
