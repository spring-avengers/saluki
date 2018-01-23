package com.quancheng.saluki.netty;

import java.net.InetSocketAddress;

import com.quancheng.saluki.netty.callback.ActivityTracker;
import com.quancheng.saluki.netty.callback.HttpFilterSource;
import com.quancheng.saluki.netty.impl.support.ThreadPoolConfiguration;



public interface HttpProxyServerBootstrap {

  HttpProxyServerBootstrap withName(String name);

  HttpProxyServerBootstrap withAddress(InetSocketAddress address);

  HttpProxyServerBootstrap withPort(int port);


  HttpProxyServerBootstrap withAllowLocalOnly(boolean allowLocalOnly);


  HttpProxyServerBootstrap withFiltersSource(HttpFilterSource filtersSource);


  HttpProxyServerBootstrap withUseDnsSec(boolean useDnsSec);


  HttpProxyServerBootstrap withTransparent(boolean transparent);


  HttpProxyServerBootstrap withIdleConnectionTimeout(int idleConnectionTimeout);


  HttpProxyServerBootstrap withConnectTimeout(int connectTimeout);


  HttpProxyServerBootstrap withServerResolver(HostResolver serverResolver);


  HttpProxyServerBootstrap plusActivityTracker(ActivityTracker activityTracker);


  HttpProxyServerBootstrap withThrottling(long readThrottleBytesPerSecond,
      long writeThrottleBytesPerSecond);


  HttpProxyServerBootstrap withNetworkInterface(InetSocketAddress inetSocketAddress);


  HttpProxyServerBootstrap withMaxInitialLineLength(int maxInitialLineLength);


  HttpProxyServerBootstrap withMaxHeaderSize(int maxHeaderSize);


  HttpProxyServerBootstrap withMaxChunkSize(int maxChunkSize);


  HttpProxyServerBootstrap withAllowRequestToOriginServer(boolean allowRequestToOriginServer);


  HttpProxyServerBootstrap withProxyAlias(String alias);


  HttpProxyServer start();


  HttpProxyServerBootstrap withThreadPoolConfiguration(ThreadPoolConfiguration configuration);
}
