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
package com.quancheng.saluki.proxy.routerules;

import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

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
public class RoutingCacheComponent {

  private static final Logger logger = LoggerFactory.getLogger(RoutingCacheComponent.class);

  private static final PathMatcher pathMatcher = new AntPathMatcher();

  private LoadingCache<String, RouteDO> ROUTE_CACHE;

  private LoadingCache<Long, RpcDO> RPC_CACHE;

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
      RouteDO routeCopy = route.copy();
      String path = routeCopy.getFromPath();
      ROUTE_CACHE.put(path, routeCopy);
    }
    // load all rpc
    List<RpcDO> rpcs = rpcDao.list(Maps.newHashMap());
    for (RpcDO rpc : rpcs) {
      // 这里防止mybatis一些持久化状态的问题，拷贝出一份来安全点
      RpcDO rpcCopy = rpc.copy();
      RPC_CACHE.put(rpcCopy.getRouteId(), rpcCopy);
    }
  }

  private void initRpcCache() {
    RPC_CACHE = CacheBuilder.newBuilder() //
        .concurrencyLevel(8) //
        .initialCapacity(10) //
        .maximumSize(1000) //
        .recordStats() //
        .removalListener(new RemovalListener<Long, RpcDO>() {

          @Override
          public void onRemoval(RemovalNotification<Long, RpcDO> notification) {
            logger
                .info("remove key:" + notification.getKey() + ",value:" + notification.getValue());
          }
        }) //
        .build(new CacheLoader<Long, RpcDO>() {

          @Override
          public RpcDO load(Long key) throws Exception {
            // 这里防止mybatis一些持久化状态的问题，拷贝出一份来安全点
            RpcDO rpcCopy = rpcDao.get(key);
            return rpcCopy;
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

  public RouteDO getRoute(String actorPath) {
    Set<String> allRoutePath = ROUTE_CACHE.asMap().keySet();
    for (String path : allRoutePath) {
      if (path.equals(actorPath) || pathMatcher.match(path, actorPath)) {
        try {
          return ROUTE_CACHE.get(path);
        } catch (Throwable e) {
          return null;
        }
      }
    }
    return null;

  }

  public RpcDO getRpc(String actorPath) {
    RouteDO route = getRoute(actorPath);
    if (route != null) {
      Long routeId = route.getId();
      try {
        return RPC_CACHE.get(routeId);
      } catch (Throwable e) {
        return null;
      }
    }
    return null;
  }


  public void expire() {
    ROUTE_CACHE.invalidateAll();
    RPC_CACHE.invalidateAll();
  }

}
