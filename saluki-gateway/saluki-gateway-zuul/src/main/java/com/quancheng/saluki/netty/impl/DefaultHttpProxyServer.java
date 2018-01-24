package com.quancheng.saluki.netty.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.saluki.netty.ActivityTracker;
import com.quancheng.saluki.netty.HttpFilterSource;
import com.quancheng.saluki.netty.HttpProxyServer;
import com.quancheng.saluki.netty.HttpProxyServerBootstrap;
import com.quancheng.saluki.netty.impl.connection.ClientToProxyConnection;
import com.quancheng.saluki.netty.impl.internal.HostResolver;
import com.quancheng.saluki.netty.impl.support.ServerGroup;
import com.quancheng.saluki.utils.ProxyUtils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.util.concurrent.GlobalEventExecutor;


public class DefaultHttpProxyServer implements HttpProxyServer {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultHttpProxyServer.class);


  private static final long TRAFFIC_SHAPING_CHECK_INTERVAL_MS = 250L;
  private static final String FALLBACK_PROXY_ALIAS = "littleproxy";


  private final ServerGroup serverGroup;
  private final HttpFilterSource filtersSource;
  private final boolean transparent;
  private final InetSocketAddress requestedAddress;
  private final HostResolver serverResolver;
  private final int maxInitialLineLength;
  private final int maxHeaderSize;
  private final int maxChunkSize;
  private final boolean allowRequestsToOriginServer;
  private final String proxyAlias;
  private final AtomicBoolean stopped = new AtomicBoolean(false);
  private final Collection<ActivityTracker> activityTrackers =
      new ConcurrentLinkedQueue<ActivityTracker>();
  private final ChannelGroup allChannels =
      new DefaultChannelGroup("HTTP-Proxy-Server", GlobalEventExecutor.INSTANCE);
  private final Thread jvmShutdownHook = new Thread(new Runnable() {
    @Override
    public void run() {
      abort();
    }
  }, "LittleProxy-JVM-shutdown-hook");



  private volatile InetSocketAddress localAddress;
  private volatile InetSocketAddress boundAddress;
  private volatile int connectTimeout;
  private volatile int idleConnectionTimeout;
  private volatile GlobalTrafficShapingHandler globalTrafficShapingHandler;



  public static HttpProxyServerBootstrap bootstrap() {
    return new DefaultHttpProxyServerBootstrap();
  }


  public static HttpProxyServerBootstrap bootstrapFromFile(String path) {
    final File propsFile = new File(path);
    Properties props = new Properties();

    if (propsFile.isFile()) {
      try (InputStream is = new FileInputStream(propsFile)) {
        props.load(is);
      } catch (final IOException e) {
        LOG.warn("Could not load props file?", e);
      }
    }

    return new DefaultHttpProxyServerBootstrap(props);
  }


  public DefaultHttpProxyServer(ServerGroup serverGroup, InetSocketAddress requestedAddress,
      HttpFilterSource filtersSource, boolean transparent, int idleConnectionTimeout,
      Collection<ActivityTracker> activityTrackers, int connectTimeout, HostResolver serverResolver,
      long readThrottleBytesPerSecond, long writeThrottleBytesPerSecond,
      InetSocketAddress localAddress, String proxyAlias, int maxInitialLineLength,
      int maxHeaderSize, int maxChunkSize, boolean allowRequestsToOriginServer) {
    this.serverGroup = serverGroup;
    this.requestedAddress = requestedAddress;
    this.filtersSource = filtersSource;
    this.transparent = transparent;
    this.idleConnectionTimeout = idleConnectionTimeout;
    if (activityTrackers != null) {
      this.activityTrackers.addAll(activityTrackers);
    }
    this.connectTimeout = connectTimeout;
    this.serverResolver = serverResolver;

    if (writeThrottleBytesPerSecond > 0 || readThrottleBytesPerSecond > 0) {
      this.globalTrafficShapingHandler = createGlobalTrafficShapingHandler(
          readThrottleBytesPerSecond, writeThrottleBytesPerSecond);
    } else {
      this.globalTrafficShapingHandler = null;
    }
    this.localAddress = localAddress;

    if (proxyAlias == null) {
      String hostname = ProxyUtils.getHostName();
      if (hostname == null) {
        hostname = FALLBACK_PROXY_ALIAS;
      }
      this.proxyAlias = hostname;
    } else {
      this.proxyAlias = proxyAlias;
    }
    this.maxInitialLineLength = maxInitialLineLength;
    this.maxHeaderSize = maxHeaderSize;
    this.maxChunkSize = maxChunkSize;
    this.allowRequestsToOriginServer = allowRequestsToOriginServer;
  }


  private GlobalTrafficShapingHandler createGlobalTrafficShapingHandler(
      long readThrottleBytesPerSecond, long writeThrottleBytesPerSecond) {
    EventLoopGroup proxyToServerEventLoop = this.getProxyToServerWorkerFor();
    return new GlobalTrafficShapingHandler(proxyToServerEventLoop, writeThrottleBytesPerSecond,
        readThrottleBytesPerSecond, TRAFFIC_SHAPING_CHECK_INTERVAL_MS, Long.MAX_VALUE);
  }

  public boolean isTransparent() {
    return transparent;
  }

  @Override
  public int getIdleConnectionTimeout() {
    return idleConnectionTimeout;
  }

  @Override
  public void setIdleConnectionTimeout(int idleConnectionTimeout) {
    this.idleConnectionTimeout = idleConnectionTimeout;
  }

  @Override
  public int getConnectTimeout() {
    return connectTimeout;
  }

  @Override
  public void setConnectTimeout(int connectTimeoutMs) {
    this.connectTimeout = connectTimeoutMs;
  }

  public HostResolver getServerResolver() {
    return serverResolver;
  }

  public InetSocketAddress getLocalAddress() {
    return localAddress;
  }

  @Override
  public InetSocketAddress getListenAddress() {
    return boundAddress;
  }

  @Override
  public void setThrottle(long readThrottleBytesPerSecond, long writeThrottleBytesPerSecond) {
    if (globalTrafficShapingHandler != null) {
      globalTrafficShapingHandler.configure(writeThrottleBytesPerSecond,
          readThrottleBytesPerSecond);
    } else {
      if (readThrottleBytesPerSecond > 0 || writeThrottleBytesPerSecond > 0) {
        globalTrafficShapingHandler = createGlobalTrafficShapingHandler(readThrottleBytesPerSecond,
            writeThrottleBytesPerSecond);
      }
    }
  }

  public long getReadThrottle() {
    return globalTrafficShapingHandler.getReadLimit();
  }

  public long getWriteThrottle() {
    return globalTrafficShapingHandler.getWriteLimit();
  }

  public int getMaxInitialLineLength() {
    return maxInitialLineLength;
  }

  public int getMaxHeaderSize() {
    return maxHeaderSize;
  }

  public int getMaxChunkSize() {
    return maxChunkSize;
  }

  public boolean isAllowRequestsToOriginServer() {
    return allowRequestsToOriginServer;
  }

  @Override
  public HttpProxyServerBootstrap clone() {
    return new DefaultHttpProxyServerBootstrap(serverGroup,
        new InetSocketAddress(requestedAddress.getAddress(),
            requestedAddress.getPort() == 0 ? 0 : requestedAddress.getPort() + 1),
        filtersSource, transparent, idleConnectionTimeout, activityTrackers, connectTimeout,
        serverResolver,
        globalTrafficShapingHandler != null ? globalTrafficShapingHandler.getReadLimit() : 0,
        globalTrafficShapingHandler != null ? globalTrafficShapingHandler.getWriteLimit() : 0,
        localAddress, proxyAlias, maxInitialLineLength, maxHeaderSize, maxChunkSize,
        allowRequestsToOriginServer);
  }

  @Override
  public void stop() {
    doStop(true);
  }

  @Override
  public void abort() {
    doStop(false);
  }


  protected void doStop(boolean graceful) {
    if (stopped.compareAndSet(false, true)) {
      if (graceful) {
        LOG.info("Shutting down proxy server gracefully");
      } else {
        LOG.info("Shutting down proxy server immediately (non-graceful)");
      }
      closeAllChannels(graceful);
      serverGroup.unregisterProxyServer(this, graceful);
      try {
        Runtime.getRuntime().removeShutdownHook(jvmShutdownHook);
      } catch (IllegalStateException e) {
      }

      LOG.info("Done shutting down proxy server");
    }
  }


  public void registerChannel(Channel channel) {
    allChannels.add(channel);
  }


  protected void closeAllChannels(boolean graceful) {
    LOG.info("Closing all channels " + (graceful ? "(graceful)" : "(non-graceful)"));

    ChannelGroupFuture future = allChannels.close();
    if (graceful) {
      try {
        future.await(10, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();

        LOG.warn("Interrupted while waiting for channels to shut down gracefully.");
      }

      if (!future.isSuccess()) {
        for (ChannelFuture cf : future) {
          if (!cf.isSuccess()) {
            LOG.info("Unable to close channel.  Cause of failure for {} is {}", cf.channel(),
                cf.cause());
          }
        }
      }
    }
  }

  public HttpProxyServer start() {
    if (!serverGroup.isStopped()) {
      LOG.info("Starting proxy at address: " + this.requestedAddress);

      serverGroup.registerProxyServer(this);

      doStart();
    } else {
      throw new IllegalStateException(
          "Attempted to start proxy, but proxy's server group is already stopped");
    }

    return this;
  }

  private void doStart() {
    ServerBootstrap serverBootstrap =
        new ServerBootstrap().group(serverGroup.getClientToProxyAcceptorPoolForTransport(),
            serverGroup.getClientToProxyWorkerPoolForTransport());
    ChannelInitializer<Channel> initializer = new ChannelInitializer<Channel>() {
      protected void initChannel(Channel ch) throws Exception {
        new ClientToProxyConnection(DefaultHttpProxyServer.this, ch.pipeline(),
            globalTrafficShapingHandler);
      };
    };
    serverBootstrap.channelFactory(new ChannelFactory<ServerChannel>() {
      @Override
      public ServerChannel newChannel() {
        return new NioServerSocketChannel();
      }
    });
    serverBootstrap.childHandler(initializer);
    ChannelFuture future =
        serverBootstrap.bind(requestedAddress).addListener(new ChannelFutureListener() {
          @Override
          public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isSuccess()) {
              registerChannel(future.channel());
            }
          }
        }).awaitUninterruptibly();

    Throwable cause = future.cause();
    if (cause != null) {
      throw new RuntimeException(cause);
    }

    this.boundAddress = ((InetSocketAddress) future.channel().localAddress());
    LOG.info("Proxy started at address: " + this.boundAddress);

    Runtime.getRuntime().addShutdownHook(jvmShutdownHook);
  }


  public HttpFilterSource getFiltersSource() {
    return filtersSource;
  }

  public Collection<ActivityTracker> getActivityTrackers() {
    return activityTrackers;
  }

  public String getProxyAlias() {
    return proxyAlias;
  }


  public EventLoopGroup getProxyToServerWorkerFor() {
    return serverGroup.getProxyToServerWorkerPoolForTransport();
  }

}
