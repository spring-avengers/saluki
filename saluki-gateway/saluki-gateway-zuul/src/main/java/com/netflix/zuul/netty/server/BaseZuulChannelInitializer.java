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

import static com.netflix.zuul.passport.PassportState.FILTERS_INBOUND_END;
import static com.netflix.zuul.passport.PassportState.FILTERS_INBOUND_START;
import static com.netflix.zuul.passport.PassportState.FILTERS_OUTBOUND_END;
import static com.netflix.zuul.passport.PassportState.FILTERS_OUTBOUND_START;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.quancheng.saluki.config.CachedDynamicIntProperty;
import com.quancheng.saluki.netty.Http1ConnectionCloseHandler;
import com.quancheng.saluki.netty.Http1ConnectionExpiryHandler;
import com.quancheng.saluki.netty.HttpRequestReadTimeoutHandler;
import com.quancheng.saluki.netty.HttpServerLifecycleChannelHandler;
import com.quancheng.saluki.netty.MaxInboundConnectionsHandler;
import com.quancheng.saluki.netty.SourceAddressChannelHandler;
import com.quancheng.saluki.netty.common.accesslog.AccessLogChannelHandler;
import com.quancheng.saluki.netty.common.accesslog.AccessLogPublisher;
import com.quancheng.saluki.netty.common.metrics.EventLoopGroupMetrics;
import com.quancheng.saluki.netty.common.metrics.HttpBodySizeRecordingChannelHandler;
import com.quancheng.saluki.netty.common.metrics.HttpMetricsChannelHandler;
import com.quancheng.saluki.netty.common.metrics.PerEventLoopMetricsChannelHandler;
import com.quancheng.saluki.netty.common.metrics.ServerChannelMetrics;
import com.quancheng.saluki.netty.config.ChannelConfig;
import com.quancheng.saluki.netty.config.CommonChannelConfigKeys;
import com.quancheng.saluki.servo.monitor.BasicCounter;
import com.quancheng.saluki.spectator.api.Registry;
import com.netflix.zuul.FilterLoader;
import com.netflix.zuul.FilterUsageNotifier;
import com.netflix.zuul.RequestCompleteHandler;
import com.netflix.zuul.context.SessionContextDecorator;
import com.netflix.zuul.filters.ZuulFilter;
import com.netflix.zuul.filters.passport.InboundPassportStampingFilter;
import com.netflix.zuul.filters.passport.OutboundPassportStampingFilter;
import com.netflix.zuul.message.ZuulMessage;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.message.http.HttpResponseMessage;
import com.netflix.zuul.netty.filter.FilterRunner;
import com.netflix.zuul.netty.filter.ZuulEndPointRunner;
import com.netflix.zuul.netty.filter.ZuulFilterChainHandler;
import com.netflix.zuul.netty.filter.ZuulFilterChainRunner;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * User: Mike Smith Date: 3/5/16 Time: 6:26 PM
 */
public abstract class BaseZuulChannelInitializer extends ChannelInitializer<Channel> {
  public static final String HTTP_CODEC_HANDLER_NAME = "codec";

  protected static final LoggingHandler nettyLogger =
      new LoggingHandler("zuul.server.nettylog", LogLevel.INFO);

  public static final CachedDynamicIntProperty MAX_INITIAL_LINE_LENGTH =
      new CachedDynamicIntProperty("server.http.decoder.maxInitialLineLength", 16384);
  public static final CachedDynamicIntProperty MAX_HEADER_SIZE =
      new CachedDynamicIntProperty("server.http.decoder.maxHeaderSize", 32768);
  public static final CachedDynamicIntProperty MAX_CHUNK_SIZE =
      new CachedDynamicIntProperty("server.http.decoder.maxChunkSize", 32768);

  protected final int port;
  protected final ChannelConfig channelConfig;
  protected final ChannelConfig channelDependencies;
  protected final int idleTimeout;
  protected final int httpRequestReadTimeout;
  protected final int maxRequestsPerConnection;
  protected final int maxRequestsPerConnectionInBrownout;
  protected final int connectionExpiry;
  protected final int maxConnections;
  private final int connCloseDelay;

  protected final Registry registry;
  protected final ServerChannelMetrics channelMetrics;
  protected final HttpMetricsChannelHandler httpMetricsHandler;
  protected final PerEventLoopMetricsChannelHandler.Connections perEventLoopConnectionMetricsHandler;
  protected final PerEventLoopMetricsChannelHandler.HttpRequests perEventLoopRequestsMetricsHandler;
  protected final MaxInboundConnectionsHandler maxConnectionsHandler;
  protected final AccessLogPublisher accessLogPublisher;

  protected final SessionContextDecorator sessionContextDecorator;
  protected final RequestCompleteHandler requestCompleteHandler;
  protected final BasicCounter httpRequestReadTimeoutCounter;
  protected final FilterLoader filterLoader;
  protected final FilterUsageNotifier filterUsageNotifier;

  /** A collection of all the active channels that we can use to things like graceful shutdown */
  protected final ChannelGroup channels;


  protected BaseZuulChannelInitializer(int port, ChannelConfig channelConfig,
      ChannelConfig channelDependencies, ChannelGroup channels) {
    this.port = port;
    this.channelConfig = channelConfig;
    this.channelDependencies = channelDependencies;
    this.channels = channels;
    this.accessLogPublisher = channelDependencies.get(ZuulDependencyKeys.accessLogPublisher);
    this.idleTimeout = channelConfig.get(CommonChannelConfigKeys.idleTimeout);
    this.httpRequestReadTimeout = channelConfig.get(CommonChannelConfigKeys.httpRequestReadTimeout);
    this.channelMetrics = new ServerChannelMetrics("http-" + port);
    this.registry = channelDependencies.get(ZuulDependencyKeys.registry);
    this.httpMetricsHandler = new HttpMetricsChannelHandler(registry, "server", "http-" + port);
    EventLoopGroupMetrics eventLoopGroupMetrics =
        channelDependencies.get(ZuulDependencyKeys.eventLoopGroupMetrics);
    PerEventLoopMetricsChannelHandler perEventLoopMetricsHandler =
        new PerEventLoopMetricsChannelHandler(eventLoopGroupMetrics);
    this.perEventLoopConnectionMetricsHandler = perEventLoopMetricsHandler.new Connections();
    this.perEventLoopRequestsMetricsHandler = perEventLoopMetricsHandler.new HttpRequests();

    this.maxConnections = channelConfig.get(CommonChannelConfigKeys.maxConnections);
    this.maxConnectionsHandler = new MaxInboundConnectionsHandler(maxConnections);
    this.maxRequestsPerConnection =
        channelConfig.get(CommonChannelConfigKeys.maxRequestsPerConnection);
    this.maxRequestsPerConnectionInBrownout =
        channelConfig.get(CommonChannelConfigKeys.maxRequestsPerConnectionInBrownout);
    this.connectionExpiry = channelConfig.get(CommonChannelConfigKeys.connectionExpiry);
    this.connCloseDelay = channelConfig.get(CommonChannelConfigKeys.connCloseDelay);
    this.sessionContextDecorator = channelDependencies.get(ZuulDependencyKeys.sessionCtxDecorator);
    this.requestCompleteHandler =
        channelDependencies.get(ZuulDependencyKeys.requestCompleteHandler);
    this.httpRequestReadTimeoutCounter =
        channelDependencies.get(ZuulDependencyKeys.httpRequestReadTimeoutCounter);

    this.filterLoader = channelDependencies.get(ZuulDependencyKeys.filterLoader);
    this.filterUsageNotifier = channelDependencies.get(ZuulDependencyKeys.filterUsageNotifier);

  }

  protected void storeChannel(Channel ch) {
    this.channels.add(ch);
  }

  protected void addTcpRelatedHandlers(ChannelPipeline pipeline) {
    pipeline.addLast(new SourceAddressChannelHandler());
    pipeline.addLast("channelMetrics", channelMetrics);
    pipeline.addLast(perEventLoopConnectionMetricsHandler);
    pipeline.addLast(maxConnectionsHandler);
  }

  protected void addHttp1Handlers(ChannelPipeline pipeline) {
    pipeline.addLast(HTTP_CODEC_HANDLER_NAME, createHttpServerCodec());

    pipeline.addLast(new Http1ConnectionCloseHandler(connCloseDelay));
    pipeline.addLast("conn_expiry_handler", new Http1ConnectionExpiryHandler(
        maxRequestsPerConnection, maxRequestsPerConnectionInBrownout, connectionExpiry));
  }

  protected HttpServerCodec createHttpServerCodec() {
    return new HttpServerCodec(MAX_INITIAL_LINE_LENGTH.get(), MAX_HEADER_SIZE.get(),
        MAX_CHUNK_SIZE.get(), false);
  }

  protected void addHttpRelatedHandlers(ChannelPipeline pipeline) {
    if (httpRequestReadTimeout > -1) {
      HttpRequestReadTimeoutHandler.addLast(pipeline, httpRequestReadTimeout, TimeUnit.MILLISECONDS,
          httpRequestReadTimeoutCounter);
    }
    pipeline.addLast(new HttpServerLifecycleChannelHandler());
    pipeline.addLast(new HttpBodySizeRecordingChannelHandler());
    pipeline.addLast(httpMetricsHandler);
    pipeline.addLast(perEventLoopRequestsMetricsHandler);
    pipeline.addLast(new AccessLogChannelHandler(accessLogPublisher));
  }

  protected void addTimeoutHandlers(ChannelPipeline pipeline) {
    pipeline.addLast(new IdleStateHandler(0, 0, idleTimeout, TimeUnit.MILLISECONDS));
  }


  protected void addZuulHandlers(final ChannelPipeline pipeline) {
    pipeline.addLast("logger", nettyLogger);
    pipeline.addLast(new ClientRequestReceiver(sessionContextDecorator));
    addZuulFilterChainHandler(pipeline);
    pipeline.addLast(new ClientResponseWriter(requestCompleteHandler));
  }

  protected void addZuulFilterChainHandler(final ChannelPipeline pipeline) {
    final ZuulFilter<HttpResponseMessage, HttpResponseMessage>[] responseFilters =
        getFilters(new OutboundPassportStampingFilter(FILTERS_OUTBOUND_START),
            new OutboundPassportStampingFilter(FILTERS_OUTBOUND_END));

    // response filter chain
    final ZuulFilterChainRunner<HttpResponseMessage> responseFilterChain =
        getFilterChainRunner(responseFilters, filterUsageNotifier);

    // endpoint | response filter chain
    final FilterRunner<HttpRequestMessage, HttpResponseMessage> endPoint =
        getEndpointRunner(responseFilterChain, filterUsageNotifier, filterLoader);

    final ZuulFilter<HttpRequestMessage, HttpRequestMessage>[] requestFilters =
        getFilters(new InboundPassportStampingFilter(FILTERS_INBOUND_START),
            new InboundPassportStampingFilter(FILTERS_INBOUND_END));

    // request filter chain | end point | response filter chain
    final ZuulFilterChainRunner<HttpRequestMessage> requestFilterChain =
        getFilterChainRunner(requestFilters, filterUsageNotifier, endPoint);

    pipeline.addLast(new ZuulFilterChainHandler(requestFilterChain, responseFilterChain));
  }

  protected ZuulEndPointRunner getEndpointRunner(
      ZuulFilterChainRunner<HttpResponseMessage> responseFilterChain,
      FilterUsageNotifier filterUsageNotifier, FilterLoader filterLoader) {
    return new ZuulEndPointRunner(filterUsageNotifier, filterLoader, responseFilterChain);
  }

  protected <T extends ZuulMessage> ZuulFilterChainRunner<T> getFilterChainRunner(
      ZuulFilter<T, T>[] filters, FilterUsageNotifier filterUsageNotifier) {
    return new ZuulFilterChainRunner<>(filters, filterUsageNotifier);
  }

  protected <T extends ZuulMessage, R extends ZuulMessage> ZuulFilterChainRunner<T> getFilterChainRunner(
      ZuulFilter<T, T>[] filters, FilterUsageNotifier filterUsageNotifier,
      FilterRunner<T, R> filterRunner) {
    return new ZuulFilterChainRunner<>(filters, filterUsageNotifier, filterRunner);
  }

  public <T extends ZuulMessage> ZuulFilter<T, T>[] getFilters(final ZuulFilter start,
      final ZuulFilter stop) {
    final List<ZuulFilter> zuulFilters = filterLoader.getFiltersByType(start.filterType());
    final ZuulFilter[] filters = new ZuulFilter[zuulFilters.size() + 2];
    filters[0] = start;
    for (int i = 1, j = 0; i < filters.length && j < zuulFilters.size(); i++, j++) {
      filters[i] = zuulFilters.get(j);
    }
    filters[filters.length - 1] = stop;
    return filters;
  }

}
