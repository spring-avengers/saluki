package com.quancheng.saluki.netty.proxy;

import com.google.common.collect.ImmutableList;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.nio.channels.spi.SelectorProvider;
import java.util.List;


public class ProxyThreadPools {

  private final NioEventLoopGroup clientToProxyAcceptorPool;

  private final NioEventLoopGroup clientToProxyWorkerPool;

  private final NioEventLoopGroup proxyToServerWorkerPool;

  public ProxyThreadPools(SelectorProvider selectorProvider, int incomingAcceptorThreads,
      int incomingWorkerThreads, int outgoingWorkerThreads, String serverGroupName,
      int serverGroupId) {
    clientToProxyAcceptorPool = new NioEventLoopGroup(incomingAcceptorThreads,
        new CategorizedThreadFactory(serverGroupName, "ClientToProxyAcceptor", serverGroupId),
        selectorProvider);

    clientToProxyWorkerPool = new NioEventLoopGroup(incomingWorkerThreads,
        new CategorizedThreadFactory(serverGroupName, "ClientToProxyWorker", serverGroupId),
        selectorProvider);
    clientToProxyWorkerPool.setIoRatio(90);

    proxyToServerWorkerPool = new NioEventLoopGroup(outgoingWorkerThreads,
        new CategorizedThreadFactory(serverGroupName, "ProxyToServerWorker", serverGroupId),
        selectorProvider);
    proxyToServerWorkerPool.setIoRatio(90);
  }

  public List<EventLoopGroup> getAllEventLoops() {
    return ImmutableList.<EventLoopGroup>of(clientToProxyAcceptorPool, clientToProxyWorkerPool,
        proxyToServerWorkerPool);
  }

  public NioEventLoopGroup getClientToProxyAcceptorPool() {
    return clientToProxyAcceptorPool;
  }

  public NioEventLoopGroup getClientToProxyWorkerPool() {
    return clientToProxyWorkerPool;
  }

  public NioEventLoopGroup getProxyToServerWorkerPool() {
    return proxyToServerWorkerPool;
  }
}
