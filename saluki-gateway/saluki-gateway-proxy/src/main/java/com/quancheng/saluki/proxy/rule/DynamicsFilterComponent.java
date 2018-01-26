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
package com.quancheng.saluki.proxy.rule;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

/**
 * @author liushiming
 * @version FilterCacheComponent.java, v 0.0.1 2018年1月26日 下午4:42:26 liushiming
 */
@Component
public class DynamicsFilterComponent {


  private AtomicBoolean requestChanged = new AtomicBoolean(true);

  private AtomicBoolean responseChanged = new AtomicBoolean(true);

  public Boolean requestChanged() {
    return requestChanged.get();
  }

  public Boolean responseChanged() {
    return responseChanged.get();
  }


  public List<String> loadRequestGroovyCode() {
    return Lists.newArrayList();
  }

  public List<String> loadResponseGroovyCode() {
    return Lists.newArrayList();
  }
}
