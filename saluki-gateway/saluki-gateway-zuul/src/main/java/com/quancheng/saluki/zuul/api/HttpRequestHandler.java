package com.quancheng.saluki.zuul.api;

public interface HttpRequestHandler {

  void requestReceived(HttpRequestMessage request);

}
