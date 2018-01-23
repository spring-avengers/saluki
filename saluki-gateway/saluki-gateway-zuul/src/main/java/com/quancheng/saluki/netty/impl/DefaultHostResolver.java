package com.quancheng.saluki.netty.impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import com.quancheng.saluki.netty.HostResolver;


public class DefaultHostResolver implements HostResolver {
  @Override
  public InetSocketAddress resolve(String host, int port) throws UnknownHostException {
    InetAddress addr = InetAddress.getByName(host);
    return new InetSocketAddress(addr, port);
  }
}
