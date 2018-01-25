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

  private Long id;

  private String fromPath;

  private String fromPathpattern;

  private String toHostport;

  private String toPath;

  private String serviceId;

  private Boolean rpc = false;

  private Timestamp gmtCreate;

  private Timestamp gmtModified;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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
    return "RouteDO [id=" + id + ", fromPath=" + fromPath + ", fromPathpattern=" + fromPathpattern
        + ", toHostport=" + toHostport + ", toPath=" + toPath + ", serviceId=" + serviceId
        + ", rpc=" + rpc + ", gmtCreate=" + gmtCreate + ", gmtModified=" + gmtModified + "]";
  }

}
