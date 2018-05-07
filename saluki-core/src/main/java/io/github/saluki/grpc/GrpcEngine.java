/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package io.github.saluki.grpc;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.saluki.common.Constants;
import io.github.saluki.common.GrpcURL;
import io.github.saluki.common.NamedThreadFactory;
import io.github.saluki.grpc.client.GrpcClientStrategy;
import io.github.saluki.grpc.client.GrpcProtocolClient;
import io.github.saluki.grpc.exception.RpcFrameworkException;
import io.github.saluki.grpc.interceptor.HeaderClientInterceptor;
import io.github.saluki.grpc.interceptor.HeaderServerInterceptor;
import io.github.saluki.grpc.server.GrpcServerStrategy;
import io.github.saluki.grpc.util.SslUtil;
import io.github.saluki.registry.Registry;
import io.github.saluki.registry.RegistryProvider;

import io.grpc.Attributes;
import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.Internal;
import io.grpc.LoadBalancer;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServerTransportFilter;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.util.TransmitStatusRuntimeExceptionInterceptor;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

/**
 * @author shimingliu 2016年12月14日 下午10:43:19
 * @version GrpcEngine1.java, v 0.0.1 2016年12月14日 下午10:43:19 shimingliu
 */
@Internal
public final class GrpcEngine {

  private static final Logger log = LoggerFactory.getLogger(GrpcEngine.class);

  private static final Map<String, Channel> CHANNEL_SERVICE_POOL =
      Collections.synchronizedMap(new WeakHashMap<String, Channel>());

  private final GrpcURL registryUrl;

  private final Registry registry;


  public GrpcEngine(GrpcURL registryUrl) {
    this.registryUrl = registryUrl;
    this.registry = RegistryProvider.asFactory().newRegistry(registryUrl);
  }


  public Object getClient(GrpcURL refUrl) throws Exception {
    GrpcProtocolClient.ChannelCall channelCall = new GrpcProtocolClient.ChannelCall() {

      @Override
      public Channel getChannel(final GrpcURL realRefUrl) {
        GrpcURL subscribeUrl = realRefUrl;
        if (subscribeUrl == null) {
          subscribeUrl = refUrl;
        }
        String serviceKey = subscribeUrl.getServiceKey();
        Channel channel = CHANNEL_SERVICE_POOL.get(serviceKey);
        if (channel == null) {
          channel = this.create(subscribeUrl);
          CHANNEL_SERVICE_POOL.put(serviceKey, channel);
        }
        return channel;
      }

      private Channel create(GrpcURL subscribeUrl) {
        Channel channel = NettyChannelBuilder.forTarget(registryUrl.toJavaURI().toString())//
            .nameResolverFactory(new GrpcNameResolverProvider(subscribeUrl))//
            .loadBalancerFactory(buildLoadBalanceFactory())//
            .sslContext(buildClientSslContext())//
            .negotiationType(NegotiationType.TLS)//
            .eventLoopGroup(createWorkEventLoopGroup())//
            .keepAliveTime(60, TimeUnit.SECONDS)//
            .maxHeaderListSize(4 * 1024 * 1024)//
            .directExecutor()//
            .build();//
        return ClientInterceptors.intercept(channel,
            Arrays.asList(HeaderClientInterceptor.instance()));
      }

    };
    GrpcClientStrategy strategy = new GrpcClientStrategy(refUrl, channelCall);
    return strategy.getGrpcClient();
  }



  public io.grpc.Server getServer(Map<GrpcURL, Object> providerUrls, int rpcPort) throws Exception {

    final NettyServerBuilder remoteServer = NettyServerBuilder.forPort(rpcPort)//
        .sslContext(buildServerSslContext())//
        .keepAliveTime(60, TimeUnit.SECONDS)//
        .bossEventLoopGroup(createBossEventLoopGroup())//
        .workerEventLoopGroup(createWorkEventLoopGroup())//
        .maxHeaderListSize(4 * 1024 * 1024)//
        .addTransportFilter(new ServerTransportFilter() {
          @Override
          public Attributes transportReady(Attributes transportAttrs) {
            log.debug("network transport is ready!");
            return transportAttrs;
          }

          @Override
          public void transportTerminated(Attributes transportAttrs) {
            log.debug("network transport is terminated!");
          }
        }).directExecutor();

    final List<ServerInterceptor> interceptors = Arrays.asList(HeaderServerInterceptor.instance(),
        TransmitStatusRuntimeExceptionInterceptor.instance());

    for (Map.Entry<GrpcURL, Object> entry : providerUrls.entrySet()) {
      GrpcURL providerUrl = entry.getKey();
      Object protocolImpl = entry.getValue();
      GrpcServerStrategy strategy = new GrpcServerStrategy(providerUrl, protocolImpl);
      ServerServiceDefinition serviceDefinition =
          ServerInterceptors.intercept(strategy.getServerDefintion(), interceptors);
      remoteServer.addService(serviceDefinition);
      int registryRpcPort = providerUrl.getParameter(Constants.REGISTRY_RPC_PORT_KEY, rpcPort);
      providerUrl = providerUrl.setPort(registryRpcPort);
      registry.register(providerUrl);
    }
    log.info("grpc server is build complete ");
    return remoteServer.build();

  }

  private SslContext buildClientSslContext() {
    try {
      InputStream certs = SslUtil.loadInputStreamCert("server.pem");
      return GrpcSslContexts
          .configure(SslContextBuilder.forClient()//
              .trustManager(certs))//
          .build();
    } catch (SSLException e) {
      throw new RpcFrameworkException(e);
    }
  }

  private LoadBalancer.Factory buildLoadBalanceFactory() {
    return GrpcRouteRoundRobinLbFactory.getInstance();
  }

  private SslContext buildServerSslContext() {
    try {
      InputStream certs = SslUtil.loadInputStreamCert("server.pem");
      InputStream keys = SslUtil.loadInputStreamCert("server_pkcs8.key");
      return GrpcSslContexts.configure(SslContextBuilder.forServer(certs, keys)).build();
    } catch (SSLException e) {
      throw new RpcFrameworkException(e);
    }
  }

  private NioEventLoopGroup createBossEventLoopGroup() {
    ThreadFactory threadFactory = new NamedThreadFactory("grpc-default-boss-ELG", true);
    return new NioEventLoopGroup(1, Executors.newCachedThreadPool(threadFactory));
  }

  private NioEventLoopGroup createWorkEventLoopGroup() {
    ThreadFactory threadFactory = new NamedThreadFactory("grpc-default-worker-ELG", true);
    return new NioEventLoopGroup(0, Executors.newCachedThreadPool(threadFactory));
  }

}
