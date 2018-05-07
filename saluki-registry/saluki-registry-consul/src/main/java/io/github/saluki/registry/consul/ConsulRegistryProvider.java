/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package io.github.saluki.registry.consul;

import io.github.saluki.common.GrpcURL;
import io.github.saluki.registry.Registry;
import io.github.saluki.registry.RegistryProvider;

/**
 * @author shimingliu 2016年12月16日 上午10:24:16
 * @version ConsulRegistryProvider.java, v 0.0.1 2016年12月16日 上午10:24:16 shimingliu
 */
public class ConsulRegistryProvider extends RegistryProvider {

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 0;
    }

    @Override
    public Registry newRegistry(GrpcURL url) {
        return new ConsulRegistry(url);
    }

}
