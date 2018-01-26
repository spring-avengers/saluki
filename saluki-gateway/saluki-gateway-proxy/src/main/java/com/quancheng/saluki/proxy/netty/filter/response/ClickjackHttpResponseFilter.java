package com.quancheng.saluki.proxy.netty.filter.response;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;


public class ClickjackHttpResponseFilter extends HttpResponseFilter {

  private static final X_Frame_Options X_Frame_Option = X_Frame_Options.SAMEORIGIN;

  private static enum X_Frame_Options {
    DENY, SAMEORIGIN
  }

  public static HttpResponseFilter newFilter() {
    return new ClickjackHttpResponseFilter();
  }

  @Override
  public HttpResponse doFilter(HttpRequest originalRequest, HttpResponse httpResponse) {
    httpResponse.headers().add("X-FRAME-OPTIONS", X_Frame_Option);
    return httpResponse;
  }

  @Override
  public int filterOrder() {
    return ResponseFilterOrder.CLICKJACK.getFilterOrder();
  }
}
