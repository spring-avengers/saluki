package com.quancheng.saluki.netty.filter;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;


public interface HttpResponseFilter {
  HttpResponse doFilter(HttpRequest originalRequest, HttpResponse httpResponse);
}
