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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

/**
 * @author liushiming
 * @version RateLimiterHttpRequestFilter.java, v 0.0.1 2018年1月26日 下午3:56:15 liushiming
 */
public class RateLimiterHttpRequestFilter extends HttpRequestFilter {

  @Override
  public HttpResponse doFilter(HttpRequest originalRequest, HttpObject httpObject,
      ChannelHandlerContext channelHandlerContext) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int filterOrder() {
    // TODO Auto-generated method stub
    return 0;
  }

}
