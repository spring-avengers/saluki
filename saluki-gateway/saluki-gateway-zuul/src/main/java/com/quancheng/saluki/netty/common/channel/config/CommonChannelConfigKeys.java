/*
 * Copyright 2018 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package com.quancheng.saluki.netty.common.channel.config;

/**
 * User: michaels@netflix.com
 * Date: 2/8/17
 * Time: 6:21 PM
 */
public class CommonChannelConfigKeys{
    public static final ChannelConfigKey<Integer> idleTimeout = new ChannelConfigKey<>("idleTimeout");
    public static final ChannelConfigKey<Integer> httpRequestReadTimeout = new ChannelConfigKey<>("httpRequestReadTimeout");
    public static final ChannelConfigKey<Integer> maxConnections = new ChannelConfigKey<>("maxConnections");
    public static final ChannelConfigKey<Integer> connCloseDelay = new ChannelConfigKey<>("connCloseDelay");
    public static final ChannelConfigKey<Integer> maxRequestsPerConnection = new ChannelConfigKey<>("maxRequestsPerConnection", 4000);
    public static final ChannelConfigKey<Integer> maxRequestsPerConnectionInBrownout = new ChannelConfigKey<>("maxRequestsPerConnectionInBrownout", 100);
    public static final ChannelConfigKey<Integer> connectionExpiry = new ChannelConfigKey<>("connectionExpiry", 20 * 60 * 1000);
}
