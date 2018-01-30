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

import java.net.URLDecoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * URL参数黑名单参数拦截
 */
public class URLParamHttpRequestFilter extends HttpRequestFilter {

  public static HttpRequestFilter newFilter() {
    return new URLParamHttpRequestFilter();
  }

  @Override
  public HttpResponse doFilter(HttpRequest originalRequest, HttpObject httpObject,
      ChannelHandlerContext channelHandlerContext) {
    if (httpObject instanceof HttpRequest) {
      HttpRequest httpRequest = (HttpRequest) httpObject;
      String url = null;
      try {
        String uri = httpRequest.uri().replaceAll("%", "%25");
        url = URLDecoder.decode(uri, "UTF-8");
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (url != null) {
        int index = url.indexOf("?");
        if (index > -1) {
          String argsStr = url.substring(index + 1);
          String[] args = argsStr.split("&");
          for (String arg : args) {
            String[] kv = arg.split("=");
            if (kv.length == 2) {
              List<Pattern> patterns = super.getRule(this.getClass());
              for (Pattern pat : patterns) {
                String param = kv[1].toLowerCase();
                Matcher matcher = pat.matcher(param);
                if (matcher.find()) {
                  super.writeFilterLog(param, this.getClass(), pat.toString());
                  return super.createResponse(HttpResponseStatus.FORBIDDEN, originalRequest);
                }
              }
            }
          }
        }
      }
    }
    return null;
  }

  @Override
  public int filterOrder() {
    return RequestFilterOrder.URLPARAM.getFilterOrder();
  }

}
