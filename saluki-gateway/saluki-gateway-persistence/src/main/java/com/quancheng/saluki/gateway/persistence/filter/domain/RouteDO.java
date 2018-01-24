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
package com.quancheng.saluki.gateway.persistence.filter.domain;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author liushiming
 * @version RouteDO.java, v 0.0.1 2018年1月4日 上午10:28:15 liushiming
 */
public class RouteDO implements Serializable {

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

  private Boolean grpc = false;

  private Boolean dubbo = false;

  private Timestamp gmtCreate;

  private Timestamp gmtModified;

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

  public Boolean isEnabled() {
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

  public Boolean isDubbo() {
    return dubbo;
  }

  public void setDubbo(Boolean dubbo) {
    this.dubbo = dubbo;
  }

  public Boolean isGrpc() {
    return grpc;
  }

  public void setGrpc(Boolean grpc) {
    this.grpc = grpc;
  }

  public Timestamp getGmtCreate() {
    return gmtCreate;
  }

  public void setGmtCreate(Timestamp gmtCreate) {
    this.gmtCreate = gmtCreate;
  }

  public Timestamp getGmtModified() {
    return gmtModified;
  }

  public void setGmtModified(Timestamp gmtModified) {
    this.gmtModified = gmtModified;
  }

  @Override
  public String toString() {
    return "RouteDO [routeId=" + routeId + ", path=" + path + ", serviceId=" + serviceId + ", url="
        + url + ", retryable=" + retryable + ", enabled=" + enabled + ", stripPrefix=" + stripPrefix
        + ", serviceName=" + serviceName + ", methodName=" + methodName + ", serviceGroup="
        + serviceGroup + ", serviceVersion=" + serviceVersion + ", grpc=" + grpc + ", dubbo="
        + dubbo + ", gmtCreate=" + gmtCreate + ", gmtModified=" + gmtModified + "]";
  }



}
