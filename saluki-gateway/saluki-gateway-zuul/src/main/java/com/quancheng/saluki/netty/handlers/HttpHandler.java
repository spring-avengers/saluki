/*
 * Copyright 2018 Netflix, Inc.
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

package com.quancheng.saluki.netty.handlers;

import java.util.function.Consumer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.util.AttributeKey;

/**
 * Http2 Or Http Handler
 *
 * Author: Arthur Gonigberg Date: December 15, 2017
 */
public class HttpHandler extends ApplicationProtocolNegotiationHandler {
  public static final AttributeKey<String> PROTOCOL_NAME = AttributeKey.valueOf("protocol_name");
  private final Consumer<ChannelPipeline> addHttpHandlerFn;

  public HttpHandler(Consumer<ChannelPipeline> addHttpHandlerFn) {
    super(ApplicationProtocolNames.HTTP_1_1);
    this.addHttpHandlerFn = addHttpHandlerFn;
  }

  @Override
  protected void configurePipeline(ChannelHandlerContext ctx, String protocol) throws Exception {
    if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
      ctx.channel().attr(PROTOCOL_NAME).set("HTTP/1.1");
      configureHttp1(ctx.pipeline());
      return;
    }
    throw new IllegalStateException("unknown protocol: " + protocol);
  }

  private void configureHttp1(ChannelPipeline pipeline) {
    addHttpHandlerFn.accept(pipeline);
  }

}
