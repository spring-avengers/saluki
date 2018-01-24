package com.quancheng.saluki.netty.proxy.flow;

import java.net.InetSocketAddress;

import com.quancheng.saluki.netty.proxy.connection.ClientToProxyConnection;


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
