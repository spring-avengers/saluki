package com.quancheng.saluki.zuul.api;

public interface HttpResponseHandler {

  void responseReceived(HttpRequestMessage request, HttpResponseMessage response);

}
