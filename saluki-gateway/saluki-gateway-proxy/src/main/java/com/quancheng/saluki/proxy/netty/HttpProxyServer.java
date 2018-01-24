package com.quancheng.saluki.proxy.netty;

import java.net.InetSocketAddress;


public interface HttpProxyServer {

  int getIdleConnectionTimeout();

  void setIdleConnectionTimeout(int idleConnectionTimeout);


  int getConnectTimeout();


  void setConnectTimeout(int connectTimeoutMs);


  HttpProxyServerBootstrap clone();

  void stop();


  void abort();

  InetSocketAddress getListenAddress();


  void setThrottle(long readThrottleBytesPerSecond, long writeThrottleBytesPerSecond);
}
