package com.quancheng.saluki.netty.impl.support;

import com.quancheng.saluki.netty.impl.ClientToProxyConnection;
import com.quancheng.saluki.netty.impl.ProxyToServerConnection;


public class FullFlowContext extends FlowContext {
  private final String serverHostAndPort;

  public FullFlowContext(ClientToProxyConnection clientConnection,
      ProxyToServerConnection serverConnection) {
    super(clientConnection);
    this.serverHostAndPort = serverConnection.getServerHostAndPort();
  }

  public String getServerHostAndPort() {
    return serverHostAndPort;
  }

}
