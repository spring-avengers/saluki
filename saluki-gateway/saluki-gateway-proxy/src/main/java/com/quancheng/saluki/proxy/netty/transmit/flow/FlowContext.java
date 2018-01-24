package   com.quancheng.saluki.proxy.netty.transmit.flow;

import java.net.InetSocketAddress;

import com.quancheng.saluki.proxy.netty.transmit.connection.ClientToProxyConnection;


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
