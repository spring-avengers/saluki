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
package com.quancheng.saluki.proxy.cache;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Maps;
import com.quancheng.saluki.gateway.persistence.filter.dao.RouteDao;
import com.quancheng.saluki.gateway.persistence.filter.dao.RpcDao;
import com.quancheng.saluki.gateway.persistence.filter.domain.RouteDO;
import com.quancheng.saluki.gateway.persistence.filter.domain.RpcDO;

/**
 * @author liushiming
 * @version RouteCacheComponent.java, v 0.0.1 2018年1月26日 上午11:25:08 liushiming
 */
@Component
public class ProxyRuleCacheComponent {

  private static final Logger logger = LoggerFactory.getLogger(ProxyRuleCacheComponent.class);

  private static final String DEFAULT_RPC_KEY_SPLIT = "/";

  private LoadingCache<String, RouteDO> ROUTE_CACHE;

  private LoadingCache<String, RpcDO> RPC_CACHE;

  @Autowired
  private RouteDao routeDao;

  @Autowired
  private RpcDao rpcDao;

  @PostConstruct
  public void init() {
    initRouteCache();
    initRpcCache();
    // load all route
    List<RouteDO> routes = routeDao.list(Maps.newHashMap());
    for (RouteDO route : routes) {
      String path = route.getFromPath();
      String pathpattern = route.getFromPathpattern();
      if (StringUtils.isNotEmpty(path))
        ROUTE_CACHE.put(path, route);
      if (StringUtils.isNotEmpty(pathpattern))
        ROUTE_CACHE.put(pathpattern, route);
    }
    // load all rpc
    List<RpcDO> rpcs = rpcDao.list(Maps.newHashMap());
    for (RpcDO rpc : rpcs) {
      String key = this.buildCacheKey(rpc.getServiceName(), rpc.getMethodName(),
          rpc.getServiceGroup(), rpc.getServiceVersion());
      RPC_CACHE.put(key, rpc);
    }
  }

  private void initRpcCache() {
    RPC_CACHE = CacheBuilder.newBuilder() //
        .concurrencyLevel(8) //
        .initialCapacity(10) //
        .maximumSize(1000) //
        .recordStats() //
        .removalListener(new RemovalListener<String, RpcDO>() {

          @Override
          public void onRemoval(RemovalNotification<String, RpcDO> notification) {
            logger
                .info("remove key:" + notification.getKey() + ",value:" + notification.getValue());
          }
        }) //
        .build(new CacheLoader<String, RpcDO>() {

          @Override
          public RpcDO load(String key) throws Exception {
            String[] fullMethodNames = StringUtils.split(key, DEFAULT_RPC_KEY_SPLIT);
            if (fullMethodNames.length == 4) {
              String serviceName = fullMethodNames[0];
              String methodName = fullMethodNames[1];
              String serviceGroup = fullMethodNames[2];
              String serviceVersion = fullMethodNames[3];
              return rpcDao.getByService(serviceName, methodName, serviceGroup, serviceVersion);
            } else {
              return null;
            }
          }

        });
  }

  private void initRouteCache() {
    ROUTE_CACHE = CacheBuilder.newBuilder() //
        .concurrencyLevel(8) //
        .initialCapacity(10) //
        .maximumSize(1000) //
        .recordStats() //
        .removalListener(new RemovalListener<String, RouteDO>() {

          @Override
          public void onRemoval(RemovalNotification<String, RouteDO> notification) {
            logger
                .info("remove key:" + notification.getKey() + ",value:" + notification.getValue());
          }
        }) //
        .build(new CacheLoader<String, RouteDO>() {

          @Override
          public RouteDO load(String key) throws Exception {
            return routeDao.load(key);
          }

        });
  }

  private String buildCacheKey(String serviceName, String methodName, String group,
      String version) {
    return serviceName + DEFAULT_RPC_KEY_SPLIT + methodName + DEFAULT_RPC_KEY_SPLIT + group
        + DEFAULT_RPC_KEY_SPLIT + version;
  }


  public RouteDO getRoute(String urlPath) {
    try {
      return ROUTE_CACHE.get(urlPath);
    } catch (Throwable e) {
      return null;
    }
  }

  public RpcDO getRpc(String serviceName, String methodName, String group, String version) {
    String key = this.buildCacheKey(serviceName, methodName, group, version);
    try {
      return RPC_CACHE.get(key);
    } catch (Throwable e) {
      return null;
    }
  }


  public void expire() {
    ROUTE_CACHE.invalidateAll();
    RPC_CACHE.invalidateAll();
  }

}
