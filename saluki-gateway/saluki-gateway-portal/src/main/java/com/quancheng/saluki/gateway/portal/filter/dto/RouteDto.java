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
package com.quancheng.saluki.gateway.portal.filter.dto;

import java.io.Serializable;

import com.quancheng.saluki.gateway.persistence.filter.domain.RouteDO;
import com.quancheng.saluki.gateway.persistence.filter.domain.RpcDO;

/**
 * @author liushiming
 * @version GateWayRouteDto.java, v 0.0.1 2018年1月5日 上午10:47:04 liushiming
 */
public class RouteDto implements Serializable {

  private static final long serialVersionUID = 1L;

  private Long routeId;

  private String fromPath;

  private String fromPathpattern;

  private String toHostport;

  private String toPath;

  private String serviceId;

  private Boolean rpc = false;

  private String serviceName;

  private String methodName;

  private String serviceGroup;

  private String serviceVersion;

  private byte[] protoContext;

  private byte[] protoReq;

  private byte[] protoRep;

  public String getFromPath() {
    return fromPath;
  }

  public void setFromPath(String fromPath) {
    this.fromPath = fromPath;
  }

  public String getFromPathpattern() {
    return fromPathpattern;
  }

  public void setFromPathpattern(String fromPathpattern) {
    this.fromPathpattern = fromPathpattern;
  }

  public String getToHostport() {
    return toHostport;
  }

  public void setToHostport(String toHostport) {
    this.toHostport = toHostport;
  }

  public String getToPath() {
    return toPath;
  }

  public void setToPath(String toPath) {
    this.toPath = toPath;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public Boolean getRpc() {
    return rpc;
  }

  public void setRpc(Boolean rpc) {
    this.rpc = rpc;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getMethodName() {
    return methodName;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public String getServiceGroup() {
    return serviceGroup;
  }

  public void setServiceGroup(String serviceGroup) {
    this.serviceGroup = serviceGroup;
  }

  public String getServiceVersion() {
    return serviceVersion;
  }

  public void setServiceVersion(String serviceVersion) {
    this.serviceVersion = serviceVersion;
  }

  public byte[] getProtoContext() {
    return protoContext;
  }

  public void setProtoContext(byte[] protoContext) {
    this.protoContext = protoContext;
  }

  public byte[] getProtoReq() {
    return protoReq;
  }

  public void setProtoReq(byte[] protoReq) {
    this.protoReq = protoReq;
  }

  public byte[] getProtoRep() {
    return protoRep;
  }

  public void setProtoRep(byte[] protoRep) {
    this.protoRep = protoRep;
  }

  public Long getRouteId() {
    return routeId;
  }

  public void setRouteId(Long routeId) {
    this.routeId = routeId;
  }

  public RouteDO buildRoute() {
    RouteDO routeDo = new RouteDO();
    if (this.routeId != null && this.routeId != 0) {
      routeDo.setId(this.routeId);
    }
    routeDo.setFromPath(this.fromPath);
    routeDo.setFromPathpattern(this.fromPathpattern);
    routeDo.setServiceId(this.serviceId);
    routeDo.setToHostport(this.toHostport);
    routeDo.setToPath(this.toPath);
    routeDo.setRpc(this.rpc);
    return routeDo;
  }

  public RpcDO buildRpc() {
    RpcDO rpcDO = new RpcDO();
    rpcDO.setServiceName(this.serviceName);
    rpcDO.setMethodName(this.methodName);
    rpcDO.setServiceGroup(this.serviceGroup);
    rpcDO.setServiceVersion(this.serviceVersion);
    rpcDO.setProtoContext(this.protoContext);
    rpcDO.setProtoRep(this.protoReq);
    rpcDO.setProtoRep(this.protoRep);
    rpcDO.setRouteId(this.routeId);
    return rpcDO;
  }

  public static RouteDto buildZuulDto(RouteDO route, RpcDO grpc) {
    RouteDto zuulDto = new RouteDto();
    zuulDto.setRouteId(route.getId());
    zuulDto.setFromPath(route.getFromPath());
    zuulDto.setFromPathpattern(route.getFromPathpattern());
    zuulDto.setServiceId(route.getServiceId());
    zuulDto.setRpc(route.getRpc());
    zuulDto.setToHostport(route.getToHostport());
    zuulDto.setToPath(route.getToPath());

    zuulDto.setServiceName(grpc.getServiceName());
    zuulDto.setMethodName(grpc.getMethodName());
    zuulDto.setServiceGroup(grpc.getServiceName());
    zuulDto.setServiceVersion(grpc.getServiceVersion());
    zuulDto.setProtoContext(grpc.getProtoContext());
    zuulDto.setProtoRep(grpc.getProtoReq());
    zuulDto.setProtoRep(grpc.getProtoRep());
    return zuulDto;
  }

  public static RouteDto buildZuulDto(RouteDO route) {
    RouteDto zuulDto = new RouteDto();
    zuulDto.setRouteId(route.getId());
    zuulDto.setFromPath(route.getFromPath());
    zuulDto.setFromPathpattern(route.getFromPathpattern());
    zuulDto.setServiceId(route.getServiceId());
    zuulDto.setRpc(route.getRpc());
    zuulDto.setToHostport(route.getToHostport());
    zuulDto.setToPath(route.getToPath());
    return zuulDto;
  }



}
