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
package com.quancheng.saluki.oauth2.zuul.vo;

import java.io.Serializable;

import org.apache.commons.beanutils.BeanUtils;

import com.quancheng.saluki.oauth2.common.BDException;
import com.quancheng.saluki.oauth2.zuul.dto.ZuulDto;

/**
 * @author liushiming
 * @version ZuulVo.java, v 0.0.1 2018年1月9日 下午1:26:56 liushiming
 */
public class ZuulVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long routeId;

  private String path;

  private String serviceId;

  private String url;

  private String retryable;

  private Boolean enabled;

  private String stripPrefix;

  private String packageName;

  private String serviceName;

  private String methodName;

  private String serviceGroup;

  private String serviceVersion;

  private String serviceFileName;

  private Boolean grpc;

  private Boolean dubbo;

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

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
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

  public ZuulDto buildZuulDto() {
    ZuulDto dto = new ZuulDto();
    try {
      BeanUtils.copyProperties(dto, this);
    } catch (Exception e) {
      throw new BDException("构建zuulDto失败", e);
    }
    return dto;
  }

  @Override
  public String toString() {
    return "ZuulVo [routeId=" + routeId + ", path=" + path + ", serviceId=" + serviceId + ", url="
        + url + ", retryable=" + retryable + ", enabled=" + enabled + ", stripPrefix=" + stripPrefix
        + ", packageName=" + packageName + ", serviceName=" + serviceName + ", methodName="
        + methodName + ", serviceGroup=" + serviceGroup + ", serviceVersion=" + serviceVersion
        + ", serviceFileName=" + serviceFileName + ", grpc=" + grpc + ", dubbo=" + dubbo + "]";
  }



}
