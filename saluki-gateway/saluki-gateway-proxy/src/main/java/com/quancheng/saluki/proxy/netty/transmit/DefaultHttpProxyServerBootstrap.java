/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package   com.quancheng.saluki.proxy.netty.transmit;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.quancheng.saluki.proxy.netty.ActivityTracker;
import com.quancheng.saluki.proxy.netty.HttpFiltersSourceAdapter;
import com.quancheng.saluki.proxy.netty.HttpProxyServer;
import com.quancheng.saluki.proxy.netty.HttpProxyServerBootstrap;
import com.quancheng.saluki.proxy.netty.transmit.support.DefaultHostResolver;
import com.quancheng.saluki.proxy.netty.transmit.support.DnsSecServerResolver;
import com.quancheng.saluki.proxy.netty.transmit.support.HostResolver;
import com.quancheng.saluki.proxy.utils.ProxyUtils;

/**
 * @author liushiming
 * @version DefaultHttpProxyServerBootstrap.java, v 0.0.1 2018年1月23日 下午6:11:45 liushiming
 */
public class DefaultHttpProxyServerBootstrap implements HttpProxyServerBootstrap {

  private static final int MAX_INITIAL_LINE_LENGTH_DEFAULT = 8192;
  private static final int MAX_HEADER_SIZE_DEFAULT = 8192 * 2;
  private static final int MAX_CHUNK_SIZE_DEFAULT = 8192 * 2;

  private String name = "LittleProxy";
  private ServerGroup serverGroup = null;
  private InetSocketAddress requestedAddress;
  private int port = 8080;
  private boolean allowLocalOnly = true;
  private HttpFiltersSourceAdapter filtersSource = new HttpFiltersSourceAdapter();
  private boolean transparent = false;
  private int idleConnectionTimeout = 70;
  private Collection<ActivityTracker> activityTrackers =
      new ConcurrentLinkedQueue<ActivityTracker>();
  private int connectTimeout = 40000;
  private HostResolver serverResolver = new DefaultHostResolver();
  private long readThrottleBytesPerSecond;
  private long writeThrottleBytesPerSecond;
  private InetSocketAddress localAddress;
  private String proxyAlias;
  private int clientToProxyAcceptorThreads = ServerGroup.DEFAULT_INCOMING_ACCEPTOR_THREADS;
  private int clientToProxyWorkerThreads = ServerGroup.DEFAULT_INCOMING_WORKER_THREADS;
  private int proxyToServerWorkerThreads = ServerGroup.DEFAULT_OUTGOING_WORKER_THREADS;
  private int maxInitialLineLength = MAX_INITIAL_LINE_LENGTH_DEFAULT;
  private int maxHeaderSize = MAX_HEADER_SIZE_DEFAULT;
  private int maxChunkSize = MAX_CHUNK_SIZE_DEFAULT;
  private boolean allowRequestToOriginServer = false;

  public DefaultHttpProxyServerBootstrap() {}

  public DefaultHttpProxyServerBootstrap(ServerGroup serverGroup,
      InetSocketAddress requestedAddress, HttpFiltersSourceAdapter filtersSource, boolean transparent,
      int idleConnectionTimeout, Collection<ActivityTracker> activityTrackers, int connectTimeout,
      HostResolver serverResolver, long readThrottleBytesPerSecond,
      long writeThrottleBytesPerSecond, InetSocketAddress localAddress, String proxyAlias,
      int maxInitialLineLength, int maxHeaderSize, int maxChunkSize,
      boolean allowRequestToOriginServer) {
    this.serverGroup = serverGroup;
    this.requestedAddress = requestedAddress;
    this.port = requestedAddress.getPort();
    this.filtersSource = filtersSource;
    this.transparent = transparent;
    this.idleConnectionTimeout = idleConnectionTimeout;
    if (activityTrackers != null) {
      this.activityTrackers.addAll(activityTrackers);
    }
    this.connectTimeout = connectTimeout;
    this.serverResolver = serverResolver;
    this.readThrottleBytesPerSecond = readThrottleBytesPerSecond;
    this.writeThrottleBytesPerSecond = writeThrottleBytesPerSecond;
    this.localAddress = localAddress;
    this.proxyAlias = proxyAlias;
    this.maxInitialLineLength = maxInitialLineLength;
    this.maxHeaderSize = maxHeaderSize;
    this.maxChunkSize = maxChunkSize;
    this.allowRequestToOriginServer = allowRequestToOriginServer;
  }

  public DefaultHttpProxyServerBootstrap(Properties props) {
    this.withUseDnsSec(ProxyUtils.extractBooleanDefaultFalse(props, "dnssec"));
    this.transparent = ProxyUtils.extractBooleanDefaultFalse(props, "transparent");
    this.idleConnectionTimeout = ProxyUtils.extractInt(props, "idle_connection_timeout");
    this.connectTimeout = ProxyUtils.extractInt(props, "connect_timeout", 0);
    this.maxInitialLineLength =
        ProxyUtils.extractInt(props, "max_initial_line_length", MAX_INITIAL_LINE_LENGTH_DEFAULT);
    this.maxHeaderSize = ProxyUtils.extractInt(props, "max_header_size", MAX_HEADER_SIZE_DEFAULT);
    this.maxChunkSize = ProxyUtils.extractInt(props, "max_chunk_size", MAX_CHUNK_SIZE_DEFAULT);
  }

  @Override
  public HttpProxyServerBootstrap withName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public HttpProxyServerBootstrap withAddress(InetSocketAddress address) {
    this.requestedAddress = address;
    return this;
  }

  @Override
  public HttpProxyServerBootstrap withPort(int port) {
    this.requestedAddress = null;
    this.port = port;
    return this;
  }

  @Override
  public HttpProxyServerBootstrap withNetworkInterface(InetSocketAddress inetSocketAddress) {
    this.localAddress = inetSocketAddress;
    return this;
  }

  @Override
  public HttpProxyServerBootstrap withProxyAlias(String alias) {
    this.proxyAlias = alias;
    return this;
  }

  @Override
  public HttpProxyServerBootstrap withAllowLocalOnly(boolean allowLocalOnly) {
    this.allowLocalOnly = allowLocalOnly;
    return this;
  }


  @Override
  public HttpProxyServerBootstrap withFiltersSource(HttpFiltersSourceAdapter filtersSource) {
    this.filtersSource = filtersSource;
    return this;
  }

  @Override
  public HttpProxyServerBootstrap withUseDnsSec(boolean useDnsSec) {
    if (useDnsSec) {
      this.serverResolver = new DnsSecServerResolver();
    } else {
      this.serverResolver = new DefaultHostResolver();
    }
    return this;
  }

  @Override
  public HttpProxyServerBootstrap withTransparent(boolean transparent) {
    this.transparent = transparent;
    return this;
  }

  @Override
  public HttpProxyServerBootstrap withIdleConnectionTimeout(int idleConnectionTimeout) {
    this.idleConnectionTimeout = idleConnectionTimeout;
    return this;
  }

  @Override
  public HttpProxyServerBootstrap withConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
    return this;
  }

  @Override
  public HttpProxyServerBootstrap withServerResolver(HostResolver serverResolver) {
    this.serverResolver = serverResolver;
    return this;
  }

  @Override
  public HttpProxyServerBootstrap plusActivityTracker(ActivityTracker activityTracker) {
    activityTrackers.add(activityTracker);
    return this;
  }

  @Override
  public HttpProxyServerBootstrap withThrottling(long readThrottleBytesPerSecond,
      long writeThrottleBytesPerSecond) {
    this.readThrottleBytesPerSecond = readThrottleBytesPerSecond;
    this.writeThrottleBytesPerSecond = writeThrottleBytesPerSecond;
    return this;
  }

  @Override
  public HttpProxyServerBootstrap withMaxInitialLineLength(int maxInitialLineLength) {
    this.maxInitialLineLength = maxInitialLineLength;
    return this;
  }

  @Override
  public HttpProxyServerBootstrap withMaxHeaderSize(int maxHeaderSize) {
    this.maxHeaderSize = maxHeaderSize;
    return this;
  }

  @Override
  public HttpProxyServerBootstrap withMaxChunkSize(int maxChunkSize) {
    this.maxChunkSize = maxChunkSize;
    return this;
  }

  @Override
  public HttpProxyServerBootstrap withAllowRequestToOriginServer(
      boolean allowRequestToOriginServer) {
    this.allowRequestToOriginServer = allowRequestToOriginServer;
    return this;
  }

  @Override
  public HttpProxyServer start() {
    return build().start();
  }

  @Override
  public HttpProxyServerBootstrap withThreadPoolConfiguration(
      ThreadPoolConfiguration configuration) {
    this.clientToProxyAcceptorThreads = configuration.getAcceptorThreads();
    this.clientToProxyWorkerThreads = configuration.getClientToProxyWorkerThreads();
    this.proxyToServerWorkerThreads = configuration.getProxyToServerWorkerThreads();
    return this;
  }

  private DefaultHttpProxyServer build() {
    final ServerGroup serverGroup;

    if (this.serverGroup != null) {
      serverGroup = this.serverGroup;
    } else {
      serverGroup = new ServerGroup(name, clientToProxyAcceptorThreads, clientToProxyWorkerThreads,
          proxyToServerWorkerThreads);
    }

    return new DefaultHttpProxyServer(serverGroup, determineListenAddress(), filtersSource,
        transparent, idleConnectionTimeout, activityTrackers, connectTimeout, serverResolver,
        readThrottleBytesPerSecond, writeThrottleBytesPerSecond, localAddress, proxyAlias,
        maxInitialLineLength, maxHeaderSize, maxChunkSize, allowRequestToOriginServer);
  }

  private InetSocketAddress determineListenAddress() {
    if (requestedAddress != null) {
      return requestedAddress;
    } else {
      // Binding only to localhost can significantly improve the
      // security of the proxy.
      if (allowLocalOnly) {
        return new InetSocketAddress("127.0.0.1", port);
      } else {
        return new InetSocketAddress(port);
      }
    }
  }

}
