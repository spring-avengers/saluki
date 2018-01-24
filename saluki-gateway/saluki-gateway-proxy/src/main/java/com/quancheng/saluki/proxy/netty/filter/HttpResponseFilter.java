package com.quancheng.saluki.proxy.netty.filter;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;


public abstract class HttpResponseFilter {

  public abstract HttpResponse doFilter(HttpRequest originalRequest, HttpResponse httpResponse);

  public abstract int filterOrder();
}
