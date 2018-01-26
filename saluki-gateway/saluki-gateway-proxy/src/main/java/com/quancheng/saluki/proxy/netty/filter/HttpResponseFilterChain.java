package com.quancheng.saluki.proxy.netty.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.quancheng.saluki.proxy.netty.filter.response.HttpResponseFilter;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;


public class HttpResponseFilterChain {
  public List<HttpResponseFilter> filters = new ArrayList<>();

  private static HttpResponseFilterChain filterChain = new HttpResponseFilterChain();

  public static HttpResponseFilterChain responseFilterChain() {
    return filterChain;
  }

  public HttpResponseFilterChain addFilter(HttpResponseFilter filter) {
    filters.add(filter);
    Collections.sort(filters, new Comparator<HttpResponseFilter>() {
      @Override
      public int compare(HttpResponseFilter o1, HttpResponseFilter o2) {
        return o1.filterOrder() - o2.filterOrder();
      }
    });
    return this;
  }

  public void doFilter(HttpRequest originalRequest, HttpResponse httpResponse) {
    for (HttpResponseFilter filter : filters) {
      filter.doFilter(originalRequest, httpResponse);
    }
  }
}
