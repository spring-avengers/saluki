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

package com.quancheng.saluki.context;

import com.netflix.util.UUIDFactory;
import com.netflix.util.concurrent.ConcurrentUUIDFactory;
import com.netflix.zuul.origins.OriginManager;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;


public class ZuulSessionContextDecorator implements SessionContextDecorator {

  private static final UUIDFactory UUID_FACTORY = new ConcurrentUUIDFactory();

  private final OriginManager originManager;

  public ZuulSessionContextDecorator(OriginManager originManager) {
    this.originManager = originManager;
  }

  @Override
  public SessionContext decorate(SessionContext ctx) {
    ChannelHandlerContext nettyCtx =
        (ChannelHandlerContext) ctx.get(CommonContextKeys.NETTY_SERVER_CHANNEL_HANDLER_CONTEXT);
    if (nettyCtx == null) {
      return null;
    }
    Channel channel = nettyCtx.channel();
    ctx.put(CommonContextKeys.ORIGIN_MANAGER, originManager);
    ctx.setUUID(UUID_FACTORY.generateRandomUuid().toString());

    return ctx;
  }
}
