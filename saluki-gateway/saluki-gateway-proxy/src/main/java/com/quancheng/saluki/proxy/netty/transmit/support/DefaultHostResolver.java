package   com.quancheng.saluki.proxy.netty.transmit.support;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;


public class DefaultHostResolver implements HostResolver {
  @Override
  public InetSocketAddress resolve(String host, int port) throws UnknownHostException {
    InetAddress addr = InetAddress.getByName(host);
    return new InetSocketAddress(addr, port);
  }
}
