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
package com.quancheng.saluki.gateway.portal.filter.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.quancheng.saluki.gateway.persistence.filter.dao.RouteDao;
import com.quancheng.saluki.gateway.persistence.filter.dao.RpcDao;
import com.quancheng.saluki.gateway.persistence.filter.domain.RouteDO;
import com.quancheng.saluki.gateway.persistence.filter.domain.RpcDO;
import com.quancheng.saluki.gateway.portal.common.CommonResponse;
import com.quancheng.saluki.gateway.portal.filter.dto.RouteDto;
import com.quancheng.saluki.gateway.portal.filter.service.FilterService;
import com.quancheng.saluki.gateway.portal.system.domain.PageDO;
import com.quancheng.saluki.gateway.portal.utils.Query;

/**
 * @author liushiming
 * @version routeServiceImpl.java, v 0.0.1 2018年1月8日 上午11:38:49 liushiming
 */
@Service
public class FilterServiceImpl implements FilterService {

  @Autowired
  private RouteDao routeDao;

  @Autowired
  private RpcDao rpcDao;

  @Override
  public PageDO<RouteDto> queryList(Query query) {
    int total = routeDao.count(query);
    List<RouteDO> routes = routeDao.list(query);
    List<RouteDto> dtos = Lists.newArrayListWithCapacity(routes.size());
    for (RouteDO routeDo : routes) {
      RpcDO rpcDO = rpcDao.get(routeDo.getId());
      RouteDto dto = RouteDto.buildRouteDto(routeDo, rpcDO);
      dtos.add(dto);
    }
    PageDO<RouteDto> page = new PageDO<>();
    page.setTotal(total);
    page.setRows(dtos);
    return page;
  }

  @Override
  public RouteDto get(Long routeId) {
    RouteDO route = routeDao.get(routeId);
    RpcDO rpc = rpcDao.get(routeId);
    RouteDto routeDto = RouteDto.buildRouteDto(route, rpc);
    return routeDto;
  }

  @Override
  public List<RouteDto> list(Map<String, Object> map) {
    List<RouteDO> routes = routeDao.list(map);
    List<RouteDto> routeDtos = Lists.newArrayList();
    for (RouteDO route : routes) {
      RpcDO rpc = rpcDao.get(route.getId());
      RouteDto routeDto = RouteDto.buildRouteDto(route, rpc);
      routeDtos.add(routeDto);
    }
    return routeDtos;
  }

  @Override
  public int count(Map<String, Object> map) {
    int total = routeDao.count(map);
    return total;
  }

  @Override
  public int save(RouteDto routeDto) {
    RouteDO routeDo = routeDto.buildRoute();
    RpcDO rpcDo = routeDto.buildRpc();
    int success2 = routeDao.save(routeDo);
    Long routeId = routeDo.getId();
    rpcDo.setRouteId(routeId);
    if (routeDo.getRpc()) {
      int success1 = rpcDao.save(rpcDo);
      if (success1 > 0 && success2 > 0) {
        return CommonResponse.SUCCESS;
      } else {
        return CommonResponse.ERROR;
      }
    }
    return success2;

  }

  @Override
  public int update(RouteDto routeDto) {
    RouteDO routeDo = routeDto.buildRoute();
    RpcDO rpcDo = routeDto.buildRpc();
    int success2 = routeDao.update(routeDo);
    if (routeDo.getRpc()) {
      int success1 = rpcDao.update(rpcDo);
      if (success1 > 0 && success2 > 0) {
        return CommonResponse.SUCCESS;
      } else {
        return CommonResponse.ERROR;
      }
    }
    return success2;
  }

  @Override
  public int remove(Long routeId) {
    int success1 = routeDao.remove(routeId);
    int success2 = rpcDao.removeByRouteId(routeId);
    if (success1 > 0 && success2 > 0) {
      return CommonResponse.SUCCESS;
    } else {
      return CommonResponse.ERROR;
    }
  }

  @Override
  public int batchRemove(Long[] routeIds) {
    int success1 = routeDao.batchRemove(routeIds);
    int success2 = rpcDao.batchRemove(routeIds);
    if (success1 > 0 && success2 > 0) {
      return CommonResponse.SUCCESS;
    } else {
      return CommonResponse.ERROR;
    }
  }
}
