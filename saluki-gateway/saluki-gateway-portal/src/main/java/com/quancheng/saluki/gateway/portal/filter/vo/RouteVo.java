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

  private String serviceFileName;

  public Long getRouteId() {
    return routeId;
  }

  public void setRouteId(Long routeId) {
    this.routeId = routeId;
  }

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

  public String getServiceFileName() {
    return serviceFileName;
  }

  public void setServiceFileName(String serviceFileName) {
    this.serviceFileName = serviceFileName;
  }

  public RouteDto buildZuulDto() {
    RouteDto zuulDto = new RouteDto();
    zuulDto.setRouteId(this.routeId);
    zuulDto.setFromPath(this.fromPath);
    zuulDto.setFromPathpattern(this.fromPathpattern);
    zuulDto.setServiceId(this.serviceId);
    zuulDto.setRpc(this.rpc);
    zuulDto.setToHostport(this.toHostport);
    zuulDto.setToPath(this.toPath);

    zuulDto.setServiceName(this.serviceName);
    zuulDto.setMethodName(this.methodName);
    zuulDto.setServiceGroup(this.serviceGroup);
    zuulDto.setServiceVersion(this.serviceVersion);
    return zuulDto;
  }

  public static RouteVo buildZuulVo(RouteDto zuulDto) {
    RouteVo zuulVo = new RouteVo();
    zuulVo.setRouteId(zuulDto.getRouteId());
    zuulVo.setFromPath(zuulDto.getFromPath());
    zuulVo.setFromPathpattern(zuulDto.getFromPathpattern());
    zuulVo.setServiceId(zuulDto.getServiceId());
    zuulVo.setRpc(zuulDto.getRpc());
    zuulVo.setToHostport(zuulDto.getToHostport());
    zuulVo.setToPath(zuulDto.getToPath());

    zuulVo.setServiceName(zuulDto.getServiceName());
    zuulVo.setMethodName(zuulDto.getMethodName());
    zuulVo.setServiceGroup(zuulDto.getServiceGroup());
    zuulVo.setServiceVersion(zuulDto.getServiceVersion());
    return zuulVo;
  }

  @Override
  public String toString() {
    return "RouteVo [routeId=" + routeId + ", fromPath=" + fromPath + ", fromPathpattern="
        + fromPathpattern + ", toHostport=" + toHostport + ", toPath=" + toPath + ", serviceId="
        + serviceId + ", rpc=" + rpc + ", serviceName=" + serviceName + ", methodName=" + methodName
        + ", serviceGroup=" + serviceGroup + ", serviceVersion=" + serviceVersion
        + ", serviceFileName=" + serviceFileName + "]";
  }



}
