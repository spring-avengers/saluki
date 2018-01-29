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

import com.quancheng.saluki.gateway.persistence.filter.domain.RpcDO;
import com.quancheng.saluki.proxy.config.SpringContextHolder;
import com.quancheng.saluki.proxy.protocol.grpc.DynamicGrpcClient;
import com.quancheng.saluki.proxy.rule.RoutingCacheComponent;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

/**
 * @author liushiming
 * @version GrpcAdapterHttpRequestFilter.java, v 0.0.1 2018年1月26日 下午4:06:35 liushiming
 */
public class GrpcAdapterHttpRequestFilter extends HttpRequestFilter {

  private final DynamicGrpcClient grpcClient = SpringContextHolder.getBean(DynamicGrpcClient.class);

  private final RoutingCacheComponent routeRuleCache =
      SpringContextHolder.getBean(RoutingCacheComponent.class);

  public static HttpRequestFilter newFilter() {
    return new GrpcAdapterHttpRequestFilter();
  }

  @Override
  public HttpResponse doFilter(HttpRequest originalRequest, HttpObject httpObject,
      ChannelHandlerContext channelHandlerContext) {
    if (originalRequest instanceof FullHttpRequest) {
      FullHttpRequest request = (FullHttpRequest) originalRequest;
      String urlPath = request.uri();
      RpcDO rpc = routeRuleCache.getRpc(urlPath);
      if (rpc != null) {
        ByteBuf jsonBuf = request.content();
        String jsonInput = jsonBuf.toString(CharsetUtil.UTF_8);
        String jsonOutput = grpcClient.doRemoteCall(rpc, jsonInput);
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
            Unpooled.wrappedBuffer(jsonOutput.getBytes(CharsetUtil.UTF_8)));
      } else {
        // 如果从缓存没有查到grpc的映射信息，说明不是泛化调用，返回空，继续走下一个filter或者去走rest服务发现等
        return null;
      }
    }
    return null;
  }

  @Override
  public int filterOrder() {
    return RequestFilterOrder.GRPC.getFilterOrder();
  }

}
