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
package com.quancheng.saluki.proxy.netty.filter;

/**
 * @author liushiming
 * @version FilterOrder.java, v 0.0.1 2018年1月26日 下午4:07:44 liushiming
 */
public enum FilterOrder {


  /**
   * 各种限制
   */
  URLPARAM(1), //
  COOKIE(2), //
  UA(3), //
  WRITEURL(4), //
  WRITEIP(5), //
  BLACKIP(6), //
  SCANNER(7), //
  RATELIMIT(8), //



  /**
   * 协议适配
   */
  GRPC(100), //
  DUBBO(101);
  private int filterOrder;

  FilterOrder(int filteOrder) {
    this.filterOrder = filteOrder;
  }

  public int getFilterOrder() {
    return filterOrder;
  }

}
