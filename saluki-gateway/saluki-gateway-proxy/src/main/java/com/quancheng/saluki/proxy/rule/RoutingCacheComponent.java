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
package com.quancheng.saluki.proxy.rule;

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
public class RoutingCacheComponent {

  private static final Logger logger = LoggerFactory.getLogger(RoutingCacheComponent.class);

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
      RPC_CACHE.put(rpc.getRouteId(), rpc);
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
            return rpcDao.get(key);
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

  public RouteDO getRoute(String urlPath) {
    try {
      return ROUTE_CACHE.get(urlPath);
    } catch (Throwable e) {
      return null;
    }
  }

  public RpcDO getRpc(String urlPath) {
    RouteDO route = getRoute(urlPath);
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
