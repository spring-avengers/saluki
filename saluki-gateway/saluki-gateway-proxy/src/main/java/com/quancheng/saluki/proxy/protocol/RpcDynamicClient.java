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
package com.quancheng.saluki.proxy.protocol;

import com.quancheng.saluki.gateway.persistence.filter.domain.RpcDO;

/**
 * @author liushiming
 * @version RpcDynamicClient.java, v 0.0.1 2018年1月29日 下午3:59:33 liushiming
 */
public abstract class RpcDynamicClient {


  public abstract String doRemoteCall(final RpcDO rpcDo, final String jsonInput);
}
