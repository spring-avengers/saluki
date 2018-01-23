package com.quancheng.saluki.netty.impl.support;

import com.quancheng.saluki.netty.impl.ClientToProxyConnection;
import com.quancheng.saluki.netty.impl.ProxyToServerConnection;

/**
 * Extension of {@link FlowContext} that provides additional information (which we know after
 * actually processing the request from the client).
 */
public class FullFlowContext extends FlowContext {
  private final String serverHostAndPort;

  public FullFlowContext(ClientToProxyConnection clientConnection,
      ProxyToServerConnection serverConnection) {
    super(clientConnection);
    this.serverHostAndPort = serverConnection.getServerHostAndPort();
  }

  /**
   * The host and port for the server (i.e. the ultimate endpoint).
   * 
   * @return
   */
  public String getServerHostAndPort() {
    return serverHostAndPort;
  }

}
