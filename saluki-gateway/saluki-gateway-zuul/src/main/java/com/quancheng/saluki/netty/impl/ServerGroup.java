package com.quancheng.saluki.netty.impl;

import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.saluki.netty.HttpProxyServer;

import io.netty.channel.EventLoopGroup;


public class ServerGroup {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerGroup.class);

  public static final int DEFAULT_INCOMING_ACCEPTOR_THREADS = 2;


  public static final int DEFAULT_INCOMING_WORKER_THREADS = 8;


  public static final int DEFAULT_OUTGOING_WORKER_THREADS = 8;


  private static final AtomicInteger serverGroupCount = new AtomicInteger(0);

  private static final SelectorProvider selectorProvider = SelectorProvider.provider();


  public final List<HttpProxyServer> registeredServers = new ArrayList<HttpProxyServer>(1);


  private final ProxyThreadPools proxyThreadPools;

  private final Object SERVER_REGISTRATION_LOCK = new Object();

  private final AtomicBoolean stopped = new AtomicBoolean(false);

  public ServerGroup(String name, int incomingAcceptorThreads, int incomingWorkerThreads,
      int outgoingWorkerThreads) {
    this.proxyThreadPools = new ProxyThreadPools(selectorProvider, incomingAcceptorThreads,
        incomingWorkerThreads, outgoingWorkerThreads, name, serverGroupCount.getAndIncrement());
  }

  public void registerProxyServer(HttpProxyServer proxyServer) {
    synchronized (SERVER_REGISTRATION_LOCK) {
      registeredServers.add(proxyServer);
    }
  }

  public void unregisterProxyServer(HttpProxyServer proxyServer, boolean graceful) {
    synchronized (SERVER_REGISTRATION_LOCK) {
      boolean wasRegistered = registeredServers.remove(proxyServer);
      if (!wasRegistered) {
        LOGGER.warn(
            "Attempted to unregister proxy server from ServerGroup that it was not registered with. Was the proxy unregistered twice?");
      }

      if (registeredServers.isEmpty()) {
        LOGGER.debug(
            "Proxy server unregistered from ServerGroup. No proxy servers remain registered, so shutting down ServerGroup.");

        shutdown(graceful);
      } else {
        LOGGER.debug(
            "Proxy server unregistered from ServerGroup. Not shutting down ServerGroup ({} proxy servers remain registered).",
            registeredServers.size());
      }
    }
  }

  private void shutdown(boolean graceful) {
    if (!stopped.compareAndSet(false, true)) {
      LOGGER.info("Shutdown requested, but ServerGroup is already stopped. Doing nothing.");

      return;
    }
    LOGGER.info(
        "Shutting down server group event loops " + (graceful ? "(graceful)" : "(non-graceful)"));
    List<EventLoopGroup> allEventLoopGroups = proxyThreadPools.getAllEventLoops();
    for (EventLoopGroup group : proxyThreadPools.getAllEventLoops()) {
      if (graceful) {
        group.shutdownGracefully();
      } else {
        group.shutdownGracefully(0, 0, TimeUnit.SECONDS);
      }
    }
    if (graceful) {
      for (EventLoopGroup group : allEventLoopGroups) {
        try {
          group.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();

          LOGGER.warn("Interrupted while shutting down event loop");
        }
      }
    }
    LOGGER.debug("Done shutting down server group");
  }


  public EventLoopGroup getClientToProxyAcceptorPoolForTransport() {
    return proxyThreadPools.getClientToProxyAcceptorPool();
  }


  public EventLoopGroup getClientToProxyWorkerPoolForTransport() {
    return proxyThreadPools.getClientToProxyWorkerPool();
  }


  public EventLoopGroup getProxyToServerWorkerPoolForTransport() {
    return proxyThreadPools.getProxyToServerWorkerPool();
  }


  public boolean isStopped() {
    return stopped.get();
  }

}
