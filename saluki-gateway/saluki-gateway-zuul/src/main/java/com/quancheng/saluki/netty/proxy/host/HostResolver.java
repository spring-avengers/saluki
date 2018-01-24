package com.quancheng.saluki.netty.proxy.host;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;


public interface HostResolver {
  public InetSocketAddress resolve(String host, int port) throws UnknownHostException;
}
