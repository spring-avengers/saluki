package com.quancheng.saluki.netty.proxy.flow;

import com.quancheng.saluki.netty.proxy.connection.ClientToProxyConnection;
import com.quancheng.saluki.netty.proxy.connection.ProxyToServerConnection;


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
