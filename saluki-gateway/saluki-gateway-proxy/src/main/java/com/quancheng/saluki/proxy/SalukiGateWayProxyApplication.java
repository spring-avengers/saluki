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
package com.quancheng.saluki.proxy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.quancheng.saluki.proxy.netty.HttpFiltersSourceAdapter;
import com.quancheng.saluki.proxy.netty.transmit.DefaultHttpProxyServer;

/**
 * @author liushiming
 * @version SalukiGateWayApplication.java, v 0.0.1 2018年1月24日 下午4:37:37 liushiming
 */
@MapperScan(value = {"com.quancheng.saluki.gateway.persistence.*.dao"})
@SpringBootApplication
public class SalukiGateWayProxyApplication implements CommandLineRunner {
  public static void main(String[] args) {
    SpringApplication.run(SalukiGateWayProxyApplication.class, args);
  }

  @Override
  public void run(String... arg0) throws Exception {
    DefaultHttpProxyServer.bootstrap().withPort(9911)
        .withFiltersSource(new HttpFiltersSourceAdapter())//
        .withAllowRequestToOriginServer(true)//
        .start();
  }

}
