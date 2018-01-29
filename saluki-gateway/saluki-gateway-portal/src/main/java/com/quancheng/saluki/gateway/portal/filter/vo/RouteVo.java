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
 * @version routeVo.java, v 0.0.1 2018年1月9日 下午1:26:56 liushiming
 */
public class RouteVo implements Serializable {
  private static final long serialVersionUID = 1L;


  private Long routeId;

  private String fromPath;

  private String toHostport;

  private String toPath;

  private String serviceId;

  private Boolean rpc = false;

  private String serviceName;

  private String methodName;

  private String serviceGroup;

  private String serviceVersion;

  private String serviceFileName;

  private String inputParam;

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


  public String getInputParam() {
    return inputParam;
  }

  public void setInputParam(String inputParam) {
    this.inputParam = inputParam;
  }

  public RouteDto buildRouteDto() {
    RouteDto routeDto = new RouteDto();
    routeDto.setRouteId(this.routeId);
    routeDto.setFromPath(this.fromPath);
    routeDto.setServiceId(this.serviceId);
    routeDto.setRpc(this.rpc);
    routeDto.setToHostport(this.toHostport);
    routeDto.setToPath(this.toPath);

    routeDto.setServiceName(this.serviceName);
    routeDto.setMethodName(this.methodName);
    routeDto.setServiceGroup(this.serviceGroup);
    routeDto.setServiceVersion(this.serviceVersion);
    routeDto.setInputParam(this.inputParam);
    return routeDto;
  }

  public static RouteVo buildRouteVo(RouteDto routeDto) {
    RouteVo routeVo = new RouteVo();
    routeVo.setRouteId(routeDto.getRouteId());
    routeVo.setFromPath(routeDto.getFromPath());
    routeVo.setServiceId(routeDto.getServiceId());
    routeVo.setRpc(routeDto.getRpc());
    routeVo.setToHostport(routeDto.getToHostport());
    routeVo.setToPath(routeDto.getToPath());

    routeVo.setServiceName(routeDto.getServiceName());
    routeVo.setMethodName(routeDto.getMethodName());
    routeVo.setServiceGroup(routeDto.getServiceGroup());
    routeVo.setServiceVersion(routeDto.getServiceVersion());
    routeVo.setInputParam(routeDto.getInputParam());
    return routeVo;
  }

  @Override
  public String toString() {
    return "RouteVo [routeId=" + routeId + ", fromPath=" + fromPath + ", toHostport=" + toHostport
        + ", toPath=" + toPath + ", serviceId=" + serviceId + ", rpc=" + rpc + ", serviceName="
        + serviceName + ", methodName=" + methodName + ", serviceGroup=" + serviceGroup
        + ", serviceVersion=" + serviceVersion + ", serviceFileName=" + serviceFileName
        + ", inputParam=" + inputParam + "]";
  }



}
