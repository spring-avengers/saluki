package com.quancheng.saluki.proxy.netty;

import java.net.InetSocketAddress;

import com.quancheng.saluki.proxy.netty.transmit.flow.FlowContext;
import com.quancheng.saluki.proxy.netty.transmit.flow.FullFlowContext;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;


public abstract class ActivityTracker {

  public void bytesReceivedFromClient(FlowContext flowContext, int numberOfBytes) {}

  public void requestReceivedFromClient(FlowContext flowContext, HttpRequest httpRequest) {}

  public void bytesSentToServer(FullFlowContext flowContext, int numberOfBytes) {}

  public void requestSentToServer(FullFlowContext flowContext, HttpRequest httpRequest) {}

  public void bytesReceivedFromServer(FullFlowContext flowContext, int numberOfBytes) {}

  public void responseReceivedFromServer(FullFlowContext flowContext, HttpResponse httpResponse) {}

  public void bytesSentToClient(FlowContext flowContext, int numberOfBytes) {}

  public void responseSentToClient(FlowContext flowContext, HttpResponse httpResponse) {}

  public void clientConnected(InetSocketAddress clientAddress) {}

  public void clientDisconnected(InetSocketAddress clientAddress) {}

}
