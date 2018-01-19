/*
 * Copyright 2018 Netflix, Inc.
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

package com.netflix.zuul.netty.server;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.saluki.config.DynamicIntProperty;
import com.quancheng.saluki.netty.common.accesslog.AccessLogPublisher;
import com.quancheng.saluki.netty.common.metrics.EventLoopGroupMetrics;
import com.quancheng.saluki.netty.common.proxyprotocol.StripUntrustedProxyHeadersHandler;
import com.quancheng.saluki.netty.common.ssl.ServerSslConfig;
import com.quancheng.saluki.netty.config.ChannelConfig;
import com.quancheng.saluki.netty.config.ChannelConfigValue;
import com.quancheng.saluki.netty.config.CommonChannelConfigKeys;
import com.quancheng.saluki.servo.DefaultMonitorRegistry;
import com.quancheng.saluki.servo.monitor.BasicCounter;
import com.quancheng.saluki.servo.monitor.MonitorConfig;
import com.quancheng.saluki.spectator.api.Registry;
import com.netflix.zuul.FilterLoader;
import com.netflix.zuul.FilterUsageNotifier;
import com.netflix.zuul.RequestCompleteHandler;
import com.netflix.zuul.context.SessionContextDecorator;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public abstract class BaseServerStartup {
  protected static final Logger LOG = LoggerFactory.getLogger(BaseServerStartup.class);

  protected final Registry registry;
  protected final DirectMemoryMonitor directMemoryMonitor;
  protected final EventLoopGroupMetrics eventLoopGroupMetrics;
  protected final AccessLogPublisher accessLogPublisher;
  protected final SessionContextDecorator sessionCtxDecorator;
  protected final RequestCompleteHandler reqCompleteHandler;
  protected final FilterLoader filterLoader;
  protected final FilterUsageNotifier usageNotifier;


  private Map<Integer, ChannelInitializer> portsToChannelInitializers;
  private ClientConnectionsShutdown clientConnectionsShutdown;
  private Server server;


  @Inject
  public BaseServerStartup(FilterLoader filterLoader, SessionContextDecorator sessionCtxDecorator,
      FilterUsageNotifier usageNotifier, RequestCompleteHandler reqCompleteHandler,
      Registry registry, DirectMemoryMonitor directMemoryMonitor,
      EventLoopGroupMetrics eventLoopGroupMetrics, AccessLogPublisher accessLogPublisher) {
    this.registry = registry;
    this.directMemoryMonitor = directMemoryMonitor;
    this.eventLoopGroupMetrics = eventLoopGroupMetrics;
    this.accessLogPublisher = accessLogPublisher;
    this.sessionCtxDecorator = sessionCtxDecorator;
    this.reqCompleteHandler = reqCompleteHandler;
    this.filterLoader = filterLoader;
    this.usageNotifier = usageNotifier;
  }

  public Server server() {
    return server;
  }

  @PostConstruct
  public void init() throws Exception {
    ChannelConfig channelDeps = new ChannelConfig();
    addChannelDependencies(channelDeps);

    ChannelGroup clientChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    clientConnectionsShutdown =
        new ClientConnectionsShutdown(clientChannels, GlobalEventExecutor.INSTANCE);

    portsToChannelInitializers = choosePortsAndChannels(clientChannels, channelDeps);

    server =
        new Server(portsToChannelInitializers, clientConnectionsShutdown, eventLoopGroupMetrics);
  }

  protected abstract Map<Integer, ChannelInitializer> choosePortsAndChannels(
      ChannelGroup clientChannels, ChannelConfig channelDependencies);

  protected void addChannelDependencies(ChannelConfig channelDeps) throws Exception {
    channelDeps.set(ZuulDependencyKeys.registry, registry);
    channelDeps.set(ZuulDependencyKeys.accessLogPublisher, accessLogPublisher);
    channelDeps.set(ZuulDependencyKeys.sessionCtxDecorator, sessionCtxDecorator);
    channelDeps.set(ZuulDependencyKeys.requestCompleteHandler, reqCompleteHandler);
    final BasicCounter httpRequestReadTimeoutCounter =
        new BasicCounter(MonitorConfig.builder("server.http.request.read.timeout").build());
    DefaultMonitorRegistry.getInstance().register(httpRequestReadTimeoutCounter);
    channelDeps.set(ZuulDependencyKeys.httpRequestReadTimeoutCounter,
        httpRequestReadTimeoutCounter);
    channelDeps.set(ZuulDependencyKeys.filterLoader, filterLoader);
    channelDeps.set(ZuulDependencyKeys.filterUsageNotifier, usageNotifier);
    channelDeps.set(ZuulDependencyKeys.eventLoopGroupMetrics, eventLoopGroupMetrics);
    directMemoryMonitor.init();
  }


  public static ChannelConfig defaultChannelConfig() {
    ChannelConfig config = new ChannelConfig();
    config.add(new ChannelConfigValue(CommonChannelConfigKeys.maxConnections,
        new DynamicIntProperty("server.connection.max", 20000).get()));
    config.add(new ChannelConfigValue(CommonChannelConfigKeys.maxRequestsPerConnection,
        new DynamicIntProperty("server.connection.max.requests", 20000).get()));
    config
        .add(
            new ChannelConfigValue(CommonChannelConfigKeys.maxRequestsPerConnectionInBrownout,
                new DynamicIntProperty("server.connection.max.requests.brownout",
                    CommonChannelConfigKeys.maxRequestsPerConnectionInBrownout.defaultValue())
                        .get()));
    config.add(new ChannelConfigValue(CommonChannelConfigKeys.connectionExpiry,
        new DynamicIntProperty("server.connection.expiry",
            CommonChannelConfigKeys.connectionExpiry.defaultValue()).get()));
    config.add(new ChannelConfigValue(CommonChannelConfigKeys.idleTimeout,
        new DynamicIntProperty("server.connection.idle.timeout", 65 * 1000).get()));
    config.add(new ChannelConfigValue(CommonChannelConfigKeys.httpRequestReadTimeout,
        new DynamicIntProperty("server.http.request.read.timeout", 5000).get()));

    // For security, default to NEVER allowing XFF/Proxy headers from client.
    config.add(new ChannelConfigValue(CommonChannelConfigKeys.allowProxyHeadersWhen,
        StripUntrustedProxyHeadersHandler.AllowWhen.NEVER));

    config.set(CommonChannelConfigKeys.withProxyProtocol, true);
    config.set(CommonChannelConfigKeys.preferProxyProtocolForClientIp, true);

    config.add(new ChannelConfigValue(CommonChannelConfigKeys.connCloseDelay,
        new DynamicIntProperty("zuul.server.connection.close.delay", 10).get()));

    return config;
  }

  protected void logPortConfigured(int port, ServerSslConfig serverSslConfig) {
    String msg = "Configured port: " + port;
    if (serverSslConfig != null) {
      msg = msg + " with SSL config: " + serverSslConfig.toString();
    }
    LOG.warn(msg);
  }
}
