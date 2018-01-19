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

package com.netflix.zuul.netty.server;

import com.quancheng.saluki.netty.common.accesslog.AccessLogPublisher;
import com.quancheng.saluki.netty.common.channel.config.ChannelConfigKey;
import com.quancheng.saluki.netty.common.metrics.EventLoopGroupMetrics;
import com.quancheng.saluki.servo.monitor.BasicCounter;
import com.quancheng.saluki.spectator.api.Registry;
import com.netflix.zuul.FilterLoader;
import com.netflix.zuul.FilterUsageNotifier;
import com.netflix.zuul.RequestCompleteHandler;
import com.netflix.zuul.context.SessionContextDecorator;

/**
 * User: michaels@netflix.com Date: 2/9/17 Time: 9:35 AM
 */
public class ZuulDependencyKeys {

  public static final ChannelConfigKey<AccessLogPublisher> accessLogPublisher =
      new ChannelConfigKey<>("accessLogPublisher");
  public static final ChannelConfigKey<Registry> registry = new ChannelConfigKey<>("registry");
  public static final ChannelConfigKey<EventLoopGroupMetrics> eventLoopGroupMetrics =
      new ChannelConfigKey<>("eventLoopGroupMetrics");
  public static final ChannelConfigKey<SessionContextDecorator> sessionCtxDecorator =
      new ChannelConfigKey<>("sessionCtxDecorator");
  public static final ChannelConfigKey<RequestCompleteHandler> requestCompleteHandler =
      new ChannelConfigKey<>("requestCompleteHandler");
  public static final ChannelConfigKey<BasicCounter> httpRequestReadTimeoutCounter =
      new ChannelConfigKey<>("httpRequestReadTimeoutCounter");
  public static final ChannelConfigKey<FilterLoader> filterLoader =
      new ChannelConfigKey<>("filterLoader");
  public static final ChannelConfigKey<FilterUsageNotifier> filterUsageNotifier =
      new ChannelConfigKey<>("filterUsageNotifier");

}
