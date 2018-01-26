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
package com.quancheng.saluki.proxy.netty.filter.request;

/**
 * @author liushiming
 * @version FilterOrder.java, v 0.0.1 2018年1月26日 下午4:07:44 liushiming
 */
public enum RequestFilterOrder {


  /**
   * 各种限制
   */
  URLPARAM(1), // URL参数黑名单参数拦截
  COOKIE(2), // Cookie黑名单拦截
  UA(3), // User-Agent黑名单拦截
  BLACKURL(4), // URL路径黑名单拦截
  WRITEIP(5), // IP白名单
  BLACKIP(6), // IP黑名单
  SCANNER(7), // 扫描
  RATELIMIT(8), // 限流



  /**
   * 协议适配
   */
  GRPC(100), //
  DUBBO(101);
  private int filterOrder;

  RequestFilterOrder(int filteOrder) {
    this.filterOrder = filteOrder;
  }

  public int getFilterOrder() {
    return filterOrder;
  }

}
