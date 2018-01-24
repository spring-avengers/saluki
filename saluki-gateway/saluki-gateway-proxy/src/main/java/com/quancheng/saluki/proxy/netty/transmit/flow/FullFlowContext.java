package   com.quancheng.saluki.proxy.netty.transmit.flow;

import com.quancheng.saluki.proxy.netty.transmit.connection.ClientToProxyConnection;
import com.quancheng.saluki.proxy.netty.transmit.connection.ProxyToServerConnection;


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
