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
package com.quancheng.saluki.oauth2.zuul.dto;

import java.io.Serializable;

import com.quancheng.saluki.oauth2.zuul.domain.GrpcDO;
import com.quancheng.saluki.oauth2.zuul.domain.RouteDO;

/**
 * @author liushiming
 * @version GateWayRouteDto.java, v 0.0.1 2018年1月5日 上午10:47:04 liushiming
 */
public class ZuulDto implements Serializable {

  private static final long serialVersionUID = 1L;

  private Long routeId;

  private String path;

  private String serviceId;

  private String url;

  private String retryable;

  private Boolean enabled;

  private String stripPrefix;

  private String servicePackageName;

  private String serviceName;

  private String methodName;

  private String serviceGroup;

  private String serviceVersion;

  private byte[] protoContext;

  private byte[] protoReq;

  private byte[] protoRep;

  private Boolean grpc;

  private Boolean dubbo;

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getRetryable() {
    return retryable;
  }

  public void setRetryable(String retryable) {
    this.retryable = retryable;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public String getStripPrefix() {
    return stripPrefix;
  }

  public void setStripPrefix(String stripPrefix) {
    this.stripPrefix = stripPrefix;
  }

  public String getServicePackageName() {
    return servicePackageName;
  }

  public void setServicePackageName(String servicePackageName) {
    this.servicePackageName = servicePackageName;
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

  public Boolean getGrpc() {
    return grpc;
  }

  public void setGrpc(Boolean grpc) {
    this.grpc = grpc;
  }

  public Boolean getDubbo() {
    return dubbo;
  }

  public void setDubbo(Boolean dubbo) {
    this.dubbo = dubbo;
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
      routeDo.setRouteId(this.routeId);
    }
    routeDo.setPath(this.path);
    routeDo.setServiceId(this.serviceId);
    routeDo.setUrl(this.url);
    routeDo.setRetryable(this.retryable);
    routeDo.setEnabled(this.enabled);
    routeDo.setStripPrefix(this.stripPrefix);
    routeDo.setServiceName(this.serviceName);
    routeDo.setMethodName(this.methodName);
    routeDo.setGrpc(this.grpc);
    routeDo.setDubbo(this.dubbo);
    return routeDo;
  }

  public GrpcDO buildGrpc() {
    GrpcDO grpc = new GrpcDO();
    grpc.setServiceName(this.serviceName);
    grpc.setMethodName(this.methodName);
    grpc.setServiceGroup(this.serviceGroup);
    grpc.setServiceVersion(this.serviceVersion);
    grpc.setProtoContext(this.protoContext);
    grpc.setProtoRep(this.protoReq);
    grpc.setProtoRep(this.protoRep);
    return grpc;
  }

  public static ZuulDto buildZuulDto(RouteDO route, GrpcDO grpc) {
    ZuulDto zuulDto = new ZuulDto();
    zuulDto.setRouteId(route.getRouteId());
    zuulDto.setPath(route.getPath());
    zuulDto.setUrl(route.getUrl());
    zuulDto.setServiceId(route.getServiceId());
    zuulDto.setRetryable(route.getRetryable());
    zuulDto.setEnabled(route.isEnabled());
    zuulDto.setStripPrefix(route.getStripPrefix());
    zuulDto.setGrpc(route.isGrpc());
    zuulDto.setDubbo(route.isDubbo());

    zuulDto.setServicePackageName(grpc.getServicePackageName());
    zuulDto.setServiceName(grpc.getServiceName());
    zuulDto.setServiceGroup(grpc.getServiceName());
    zuulDto.setServiceVersion(grpc.getServiceVersion());
    zuulDto.setProtoContext(grpc.getProtoContext());
    zuulDto.setProtoRep(grpc.getProtoReq());
    zuulDto.setProtoRep(grpc.getProtoRep());
    return zuulDto;
  }



}
