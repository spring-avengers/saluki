/*
 * Copyright 2014-2016 the original author or authors.
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
package io.github.saluki.grpc;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.net.InetAddresses;
import io.github.saluki.common.GrpcURL;
import io.github.saluki.grpc.router.GrpcRouterFactory;
import io.github.saluki.registry.NotifyListener;
import io.github.saluki.registry.Registry;
import io.github.saluki.registry.RegistryProvider;
import io.github.saluki.utils.NetUtils;

import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.Internal;
import io.grpc.NameResolver;
import io.grpc.Status;

/**
 * @author liushiming 2017年5月19日 下午1:43:22
 * @version: GrpcNameResolver.java, v 0.0.1 2017年5月19日 下午1:43:22 liushiming
 */
@Internal
public class GrpcNameResolver extends NameResolver {

  private static final Logger log = LoggerFactory.getLogger(GrpcNameResolver.class);

  private final Registry registry;

  private final GrpcURL subscribeUrl;

  private final NotifyListener.NotifyServiceListener serviceListener =
      new NotifyListener.NotifyServiceListener() {

        @Override
        public void notify(GrpcURL subscribeUrl, List<GrpcURL> urls) {
          log.info("Grpc nameresolve started listener,Receive notify from registry, prividerUrl is"
              + Arrays.toString(urls.toArray()));
          GrpcNameResolver.this.urls.put(subscribeUrl, urls);
          notifyLoadBalance(subscribeUrl, urls);
        }

      };

  private final NotifyListener.NotifyRouterListener routeListener =
      new NotifyListener.NotifyRouterListener() {

        @Override
        public void notify(GrpcURL subscribeUrl, String routerCondition) {
          GrpcRouterFactory.getInstance().cacheRoute(subscribeUrl.getServiceKey(), routerCondition);
        }
      };


  private Listener listener;

  private volatile Map<GrpcURL, List<SocketAddress>> addresses = Maps.newConcurrentMap();

  private volatile Map<GrpcURL, List<GrpcURL>> urls = Maps.newConcurrentMap();

  public GrpcNameResolver(URI targetUri, Attributes params, GrpcURL subscribeUrl) {
    GrpcURL registryUrl = GrpcURL.valueOf(targetUri.toString());
    this.registry = RegistryProvider.asFactory().newRegistry(registryUrl);
    this.subscribeUrl = subscribeUrl;
  }

  @Override
  public final String getServiceAuthority() {
    return "grpc";
  }

  @Override
  public final synchronized void start(Listener listener) {
    Preconditions.checkState(this.listener == null, "already started");
    this.listener = listener;
    this.listener = Preconditions.checkNotNull(listener, "listener");
    resolve();
  }

  @Override
  public final synchronized void refresh() {
    Preconditions.checkState(listener != null, "not started");
    resolve();
  }

  private void resolve() {
    registry.subscribe(subscribeUrl, serviceListener);
    registry.subscribe(subscribeUrl, routeListener);
  }


  @Override
  public void shutdown() {
    registry.unsubscribe(subscribeUrl, serviceListener);
    registry.unsubscribe(subscribeUrl, routeListener);
  }



  /**** help method *****/
  private void notifyLoadBalance(GrpcURL subscribeUrl, List<GrpcURL> urls) {
    if (urls != null && !urls.isEmpty()) {
      List<EquivalentAddressGroup> servers = Lists.newArrayList();
      List<SocketAddress> addresses = Lists.newArrayList();
      Map<List<SocketAddress>, GrpcURL> addressUrlMapping = Maps.newHashMap();
      for (GrpcURL url : urls) {
        String host = url.getHost();
        int port = url.getPort();
        List<SocketAddress> hostAddressMapping;
        if (NetUtils.isIP(host)) {
          hostAddressMapping = IpResolved(servers, addresses, host, port);
        } else {
          hostAddressMapping = DnsResolved(servers, addresses, host, port);
        }
        addressUrlMapping.put(hostAddressMapping, url);
      }
      this.addresses.put(subscribeUrl, addresses);
      Attributes config = this.buildAttributes(subscribeUrl, addressUrlMapping);
      GrpcNameResolver.this.listener.onAddresses(servers, config);
    } else {
      GrpcNameResolver.this.listener
          .onError(Status.NOT_FOUND.withDescription("There is no service registy in consul "));
    }
  }

  private List<SocketAddress> DnsResolved(List<EquivalentAddressGroup> servers,
      List<SocketAddress> addresses, String host, int port) {
    List<SocketAddress> hostAddressMapping = Lists.newArrayList();
    try {
      InetAddress[] inetAddrs = InetAddress.getAllByName(host);
      for (int j = 0; j < inetAddrs.length; j++) {
        InetAddress inetAddr = inetAddrs[j];
        SocketAddress sock = new InetSocketAddress(inetAddr, port);
        hostAddressMapping.add(sock);
        addSocketAddress(servers, addresses, sock);
      }
      return hostAddressMapping;
    } catch (UnknownHostException e) {
      GrpcNameResolver.this.listener.onError(Status.UNAVAILABLE.withCause(e));
    }
    return hostAddressMapping;
  }

  private List<SocketAddress> IpResolved(List<EquivalentAddressGroup> servers,
      List<SocketAddress> addresses, String host, int port) {
    List<SocketAddress> hostAddressMapping = Lists.newArrayList();
    SocketAddress sock = new InetSocketAddress(InetAddresses.forString(host), port);
    hostAddressMapping.add(sock);
    addSocketAddress(servers, addresses, sock);
    return hostAddressMapping;
  }

  private void addSocketAddress(List<EquivalentAddressGroup> servers, List<SocketAddress> addresses,
      SocketAddress sock) {
    EquivalentAddressGroup server = new EquivalentAddressGroup(sock);
    servers.add(server);
    addresses.add(sock);
  }

  private Attributes buildAttributes(GrpcURL subscribeUrl,
      Map<List<SocketAddress>, GrpcURL> addressUrlMapping) {
    Attributes.Builder builder = Attributes.newBuilder();
    if (listener != null) {
      builder.set(GrpcNameResolverProvider.NAMERESOVER_LISTENER, listener);
    }
    if (addresses.get(subscribeUrl) != null) {
      builder.set(GrpcNameResolverProvider.REMOTE_ADDR_KEYS, addresses.get(subscribeUrl));
    }
    if (!addressUrlMapping.isEmpty()) {
      builder.set(GrpcNameResolverProvider.GRPC_ADDRESS_GRPCURL_MAPPING, addressUrlMapping);
    }
    return builder.build();
  }


}
