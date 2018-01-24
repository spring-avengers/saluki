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
package com.quancheng.saluki.gateway.portal.filter.vo;

import java.io.Serializable;

import com.quancheng.saluki.gateway.portal.filter.dto.RouteDto;

/**
 * @author liushiming
 * @version ZuulVo.java, v 0.0.1 2018年1月9日 下午1:26:56 liushiming
 */
public class RouteVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long routeId;

  private String path;

  private String serviceId;

  private String url;

  private Boolean retryable;

  private Boolean enabled;

  private Boolean stripPrefix;

  private String serviceName;

  private String methodName;

  private String serviceGroup;

  private String serviceVersion;

  private String serviceFileName;

  private Boolean grpc;

  private Boolean dubbo;

  private Boolean httpRest;

  public Long getRouteId() {
    return routeId;
  }

  public void setRouteId(Long routeId) {
    this.routeId = routeId;
  }

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

  public Boolean isRetryable() {
    return retryable;
  }

  public void setRetryable(Boolean retryable) {
    this.retryable = retryable;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public Boolean isStripPrefix() {
    return stripPrefix;
  }

  public void setStripPrefix(Boolean stripPrefix) {
    this.stripPrefix = stripPrefix;
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

  public String getServiceFileName() {
    return serviceFileName;
  }

  public void setServiceFileName(String serviceFileName) {
    this.serviceFileName = serviceFileName;
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

  public Boolean isHttpRest() {
    return httpRest;
  }

  public void setHttpRest(Boolean httpRest) {
    this.httpRest = httpRest;
  }

  public RouteDto buildZuulDto() {
    RouteDto zuulDto = new RouteDto();
    zuulDto.setRouteId(this.routeId);
    zuulDto.setPath(this.path);
    zuulDto.setUrl(this.url);
    zuulDto.setServiceId(this.serviceId);
    zuulDto.setRetryable(this.retryable);
    zuulDto.setEnabled(this.enabled);
    zuulDto.setStripPrefix(this.stripPrefix);
    zuulDto.setGrpc(this.grpc);
    zuulDto.setDubbo(this.dubbo);
    zuulDto.setServiceName(this.serviceName);
    zuulDto.setMethodName(this.methodName);
    zuulDto.setServiceGroup(this.getServiceGroup());
    zuulDto.setServiceVersion(this.serviceVersion);
    return zuulDto;
  }

  public static RouteVo buildZuulVo(RouteDto zuulDto) {
    RouteVo zuulVo = new RouteVo();
    zuulVo.setRouteId(zuulDto.getRouteId());
    zuulVo.setPath(zuulDto.getPath());
    zuulVo.setUrl(zuulDto.getUrl());
    zuulVo.setServiceId(zuulDto.getServiceId());
    zuulVo.setRetryable(zuulDto.isRetryable());
    zuulVo.setEnabled(zuulDto.isEnabled());
    zuulVo.setStripPrefix(zuulDto.isStripPrefix());
    zuulVo.setGrpc(zuulDto.isGrpc());
    zuulVo.setDubbo(zuulDto.isDubbo());
    zuulVo.setServiceName(zuulDto.getServiceName());
    zuulVo.setMethodName(zuulDto.getMethodName());
    zuulVo.setServiceGroup(zuulDto.getServiceGroup());
    zuulVo.setServiceVersion(zuulDto.getServiceVersion());
    zuulVo.setHttpRest(!zuulDto.isDubbo() && !zuulDto.isGrpc());
    return zuulVo;
  }

  @Override
  public String toString() {
    return "ZuulVo [routeId=" + routeId + ", path=" + path + ", serviceId=" + serviceId + ", url="
        + url + ", retryable=" + retryable + ", enabled=" + enabled + ", stripPrefix=" + stripPrefix
        + ", serviceName=" + serviceName + ", methodName=" + methodName + ", serviceGroup="
        + serviceGroup + ", serviceVersion=" + serviceVersion + ", serviceFileName="
        + serviceFileName + ", grpc=" + grpc + ", dubbo=" + dubbo + ", httpRest=" + httpRest + "]";
  }

}
