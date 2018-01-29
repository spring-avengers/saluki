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
package com.quancheng.saluki.proxy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.quancheng.saluki.core.grpc.service.GenericService;
import com.quancheng.saluki.proxy.grpc.DynamicGrpcClient;

/**
 * @author liushiming
 * @version GateWayProxyConfig.java, v 0.0.1 2018年1月29日 上午10:32:34 liushiming
 */
@Configuration
public class GateWayProxyConfig {

  @Configuration
  @ConditionalOnClass(GenericService.class)
  protected class GrpcConfig {

    @Autowired
    protected GenericService generciService;

    @Bean
    protected DynamicGrpcClient dynamicGrpcClient() {
      return new DynamicGrpcClient(generciService);
    }

  }


}
