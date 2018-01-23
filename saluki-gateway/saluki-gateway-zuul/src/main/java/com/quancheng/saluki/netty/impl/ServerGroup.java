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
import io.netty.handler.codec.haproxy.HAProxyProxiedProtocol.TransportProtocol;


public class ServerGroup {
  private static final Logger log = LoggerFactory.getLogger(ServerGroup.class);


  public static final int DEFAULT_INCOMING_ACCEPTOR_THREADS = 2;


  public static final int DEFAULT_INCOMING_WORKER_THREADS = 8;


  public static final int DEFAULT_OUTGOING_WORKER_THREADS = 8;


  private static final AtomicInteger serverGroupCount = new AtomicInteger(0);



  public final List<HttpProxyServer> registeredServers = new ArrayList<HttpProxyServer>(1);


  private final ProxyThreadPools proxyThreadPools;

   
  private static final SelectorProvider selectorProvider = SelectorProvider.provider();

  /**
   * True when this ServerGroup is stopped.
   */
  private final AtomicBoolean stopped = new AtomicBoolean(false);

  /**
   * Creates a new ServerGroup instance for a proxy. Threads created for this ServerGroup will have
   * the specified ServerGroup name in the Thread name. This constructor does not actually
   * initialize any thread pools; instead, thread pools for specific transport protocols are lazily
   * initialized as needed.
   *
   * @param name ServerGroup name to include in thread names
   * @param incomingAcceptorThreads number of acceptor threads per protocol
   * @param incomingWorkerThreads number of client-to-proxy worker threads per protocol
   * @param outgoingWorkerThreads number of proxy-to-server worker threads per protocol
   */
  public ServerGroup(String name, int incomingAcceptorThreads, int incomingWorkerThreads,
      int outgoingWorkerThreads) {
    this.proxyThreadPools = new ProxyThreadPools(selectorProvider, incomingAcceptorThreads,
        incomingWorkerThreads, outgoingWorkerThreads, name, serverGroupCount.getAndIncrement());
  }



  /**
   * Lock controlling access to the {@link #registerProxyServer(HttpProxyServer)} and
   * {@link #unregisterProxyServer(HttpProxyServer, boolean)} methods.
   */
  private final Object SERVER_REGISTRATION_LOCK = new Object();

  /**
   * Registers the specified proxy server as a consumer of this server group. The server group will
   * not be shut down until the proxy unregisters itself.
   *
   * @param proxyServer proxy server instance to register
   */
  public void registerProxyServer(HttpProxyServer proxyServer) {
    synchronized (SERVER_REGISTRATION_LOCK) {
      registeredServers.add(proxyServer);
    }
  }

  /**
   * Unregisters the specified proxy server from this server group. If this was the last registered
   * proxy server, the server group will be shut down.
   *
   * @param proxyServer proxy server instance to unregister
   * @param graceful when true, the server group shutdown (if necessary) will be graceful
   */
  public void unregisterProxyServer(HttpProxyServer proxyServer, boolean graceful) {
    synchronized (SERVER_REGISTRATION_LOCK) {
      boolean wasRegistered = registeredServers.remove(proxyServer);
      if (!wasRegistered) {
        log.warn(
            "Attempted to unregister proxy server from ServerGroup that it was not registered with. Was the proxy unregistered twice?");
      }

      if (registeredServers.isEmpty()) {
        log.debug(
            "Proxy server unregistered from ServerGroup. No proxy servers remain registered, so shutting down ServerGroup.");

        shutdown(graceful);
      } else {
        log.debug(
            "Proxy server unregistered from ServerGroup. Not shutting down ServerGroup ({} proxy servers remain registered).",
            registeredServers.size());
      }
    }
  }

  /**
   * Shuts down all event loops owned by this server group.
   *
   * @param graceful when true, event loops will "gracefully" terminate, waiting for submitted tasks
   *        to finish
   */
  private void shutdown(boolean graceful) {
    if (!stopped.compareAndSet(false, true)) {
      log.info("Shutdown requested, but ServerGroup is already stopped. Doing nothing.");

      return;
    }

    log.info(
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

          log.warn("Interrupted while shutting down event loop");
        }
      }
    }
    log.debug("Done shutting down server group");
  }

  /**
   * Retrieves the client-to-proxy acceptor thread pool for the specified protocol. Initializes the
   * pool if it has not yet been initialized.
   * <p>
   * This method is thread-safe; no external locking is necessary.
   *
   * @param protocol transport protocol to retrieve the thread pool for
   * @return the client-to-proxy acceptor thread pool
   */
  public EventLoopGroup getClientToProxyAcceptorPoolForTransport() {
    return proxyThreadPools.getClientToProxyAcceptorPool();
  }

  /**
   * Retrieves the client-to-proxy acceptor worker pool for the specified protocol. Initializes the
   * pool if it has not yet been initialized.
   * <p>
   * This method is thread-safe; no external locking is necessary.
   *
   * @param protocol transport protocol to retrieve the thread pool for
   * @return the client-to-proxy worker thread pool
   */
  public EventLoopGroup getClientToProxyWorkerPoolForTransport() {
    return proxyThreadPools.getClientToProxyWorkerPool();
  }

  /**
   * Retrieves the proxy-to-server worker thread pool for the specified protocol. Initializes the
   * pool if it has not yet been initialized.
   * <p>
   * This method is thread-safe; no external locking is necessary.
   *
   * @param protocol transport protocol to retrieve the thread pool for
   * @return the proxy-to-server worker thread pool
   */
  public EventLoopGroup getProxyToServerWorkerPoolForTransport() {
    return proxyThreadPools.getProxyToServerWorkerPool();
  }

  /**
   * @return true if this ServerGroup has already been stopped
   */
  public boolean isStopped() {
    return stopped.get();
  }

}
