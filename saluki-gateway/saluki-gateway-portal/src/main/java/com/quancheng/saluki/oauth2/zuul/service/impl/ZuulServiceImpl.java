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
package com.quancheng.saluki.oauth2.zuul.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.quancheng.saluki.oauth2.system.domain.PageDO;
import com.quancheng.saluki.oauth2.utils.Query;
import com.quancheng.saluki.oauth2.zuul.dao.GrpcDao;
import com.quancheng.saluki.oauth2.zuul.dao.RouteDao;
import com.quancheng.saluki.oauth2.zuul.domain.GrpcDO;
import com.quancheng.saluki.oauth2.zuul.domain.RouteDO;
import com.quancheng.saluki.oauth2.zuul.dto.ZuulDto;
import com.quancheng.saluki.oauth2.zuul.service.ZuulService;

/**
 * @author liushiming
 * @version ZuulServiceImpl.java, v 0.0.1 2018年1月8日 上午11:38:49 liushiming
 */
public class ZuulServiceImpl implements ZuulService {

  @Autowired
  private RouteDao routeDao;

  @Autowired
  private GrpcDao grpcDao;

  @Override
  public PageDO<RouteDO> queryList(Query query) {
    int total = routeDao.count(query);
    List<RouteDO> routes = routeDao.list(query);
    PageDO<RouteDO> page = new PageDO<>();
    page.setTotal(total);
    page.setRows(routes);
    return page;
  }

  @Override
  public ZuulDto get(Long routeId) {
    RouteDO route = routeDao.get(routeId);
    String packageName = route.getPackageName();
    String serviceName = route.getServiceName();
    String methodName = route.getMethodName();
    String group = route.getGroup();
    String version = route.getVersion();
    GrpcDO grpc = grpcDao.get(packageName, serviceName, methodName, group, version);
    ZuulDto zuulDto = ZuulDto.buildZuulDto(route, grpc);
    return zuulDto;
  }

  @Override
  public List<ZuulDto> list(Map<String, Object> map) {
    List<RouteDO> routes = routeDao.list(map);
    List<ZuulDto> zuulDtos = Lists.newArrayList();
    for (RouteDO route : routes) {
      String packageName = route.getPackageName();
      String serviceName = route.getServiceName();
      String methodName = route.getMethodName();
      String group = route.getGroup();
      String version = route.getVersion();
      GrpcDO grpc = grpcDao.get(packageName, serviceName, methodName, group, version);
      ZuulDto zuulDto = ZuulDto.buildZuulDto(route, grpc);
      zuulDtos.add(zuulDto);
    }
    return zuulDtos;
  }

  @Override
  public int count(Map<String, Object> map) {
    int total = routeDao.count(map);
    return total;
  }

  @Override
  public int save(ZuulDto zuulDto) {
    RouteDO routeDo = zuulDto.buildRoute();
    GrpcDO grpcDo = zuulDto.buildGrpc();
    int success1 = grpcDao.save(grpcDo);
    int success2 = routeDao.save(routeDo);
    if (success1 == 0 && success2 == 0) {
      return 0;
    } else {
      return 1;
    }
  }

  @Override
  public int update(ZuulDto zuulDto) {
    RouteDO routeDo = zuulDto.buildRoute();
    GrpcDO grpcDo = zuulDto.buildGrpc();
    int success1 = grpcDao.update(grpcDo);
    int success2 = routeDao.update(routeDo);
    if (success1 == 0 && success2 == 0) {
      return 0;
    } else {
      return 1;
    }
  }

  @Override
  public int remove(Long routeId) {
    RouteDO route = routeDao.get(routeId);
    String packageName = route.getPackageName();
    String serviceName = route.getServiceName();
    String methodName = route.getMethodName();
    String group = route.getGroup();
    String version = route.getVersion();
    GrpcDO grpc = grpcDao.get(packageName, serviceName, methodName, group, version);
    routeDao.remove(routeId);
    grpcDao.remove(grpc.getId());
    return 0;
  }

  @Override
  public int batchRemove(Long[] routeIds) {
    List<Long> ids = Lists.newArrayList();
    for (Long routeId : routeIds) {
      RouteDO route = routeDao.get(routeId);
      String packageName = route.getPackageName();
      String serviceName = route.getServiceName();
      String methodName = route.getMethodName();
      String group = route.getGroup();
      String version = route.getVersion();
      GrpcDO grpc = grpcDao.get(packageName, serviceName, methodName, group, version);
      ids.add(grpc.getId());
    }
    routeDao.batchRemove(routeIds);
    Long[] idArray = new Long[ids.size()];
    grpcDao.batchRemove(ids.toArray(idArray));
    return 0;
  }



}
