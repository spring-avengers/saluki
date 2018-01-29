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

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;

/**
 * @author liushiming
 * @version FilterUtil.java, v 0.0.1 2018年1月29日 下午6:27:10 liushiming
 */
public class FilterUtil {
  private FilterUtil() {

  }

  public static List<String> getHeaderValues(HttpMessage httpMessage, String headerName) {
    List<String> list = Lists.newArrayList();
    for (Map.Entry<String, String> header : httpMessage.headers().entries()) {
      if (header.getKey().toLowerCase().equals(headerName.toLowerCase())) {
        list.add(header.getValue());
      }
    }
    return list;
  }

  public static String getRealIp(HttpRequest httpRequest,
      ChannelHandlerContext channelHandlerContext) {
    List<String> headerValues = getHeaderValues(httpRequest, "X-Real-IP");
    return headerValues.get(0);
  }



}
