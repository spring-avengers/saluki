package com.quancheng.saluki.netty.impl.support;

import java.net.InetSocketAddress;

import com.quancheng.saluki.netty.impl.ClientToProxyConnection;


public class FlowContext {
  private final InetSocketAddress clientAddress;

  public FlowContext(ClientToProxyConnection clientConnection) {
    super();
    this.clientAddress = clientConnection.getClientAddress();
  }

  public InetSocketAddress getClientAddress() {
    return clientAddress;
  }

}
