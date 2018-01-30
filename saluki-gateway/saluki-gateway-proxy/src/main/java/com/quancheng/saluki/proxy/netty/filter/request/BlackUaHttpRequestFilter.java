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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.quancheng.saluki.proxy.netty.filter.FilterUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author liushiming
 * @version UaHttpRequestFilter.java, v 0.0.1 2018年1月26日 下午4:00:28 liushiming
 */
public class BlackUaHttpRequestFilter extends HttpRequestFilter {

  public static HttpRequestFilter newFilter() {
    return new BlackUaHttpRequestFilter();
  }

  @Override
  public HttpResponse doFilter(HttpRequest originalRequest, HttpObject httpObject,
      ChannelHandlerContext channelHandlerContext) {
    if (httpObject instanceof HttpRequest) {
      List<String> headerValues = FilterUtil.getHeaderValues(originalRequest, "User-Agent");
      List<Pattern> patterns = super.getRule(this.getClass());
      if (headerValues.size() > 0 && headerValues.get(0) != null) {
        for (Pattern pat : patterns) {
          Matcher matcher = pat.matcher(headerValues.get(0));
          if (matcher.find()) {
            super.writeFilterLog(headerValues.toString(), BlackIpHttpRequesFilter.class,
                pat.toString());
            return super.createResponse(HttpResponseStatus.FORBIDDEN, originalRequest);
          }
        }
      }
    }
    return null;
  }

  @Override
  public int filterOrder() {
    return RequestFilterOrder.UA.getFilterOrder();
  }

}
