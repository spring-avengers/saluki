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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.quancheng.saluki.gateway.persistence.filter.domain.RouteDO;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;

/**
 * @author liushiming
 * @version RouteService.java, v 0.0.1 2018年1月25日 下午4:00:29 liushiming
 */
@Component
public class DynamicsRoutingComponent {


  @Autowired
  private RoutingCacheComponent routeCache;

  public void doRouting(HttpRequest httpRequest) {
    String actorPath = httpRequest.uri();
    RouteDO route = routeCache.getRoute(actorPath);
    if (route != null) {
      String targetPath = route.getToPath();
      String targetHostAndPort = route.getToHostport();
      if (targetHostAndPort != null)
        httpRequest.headers().set(HttpHeaderNames.HOST, targetHostAndPort);
      if (targetPath != null)
        httpRequest.setUri(targetPath);
    }
  }

}
