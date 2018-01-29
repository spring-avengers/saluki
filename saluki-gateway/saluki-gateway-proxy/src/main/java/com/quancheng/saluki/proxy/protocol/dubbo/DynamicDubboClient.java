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
package com.quancheng.saluki.proxy.protocol.dubbo;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.utils.ReferenceConfigCache;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.quancheng.saluki.gateway.persistence.filter.domain.RpcDO;
import com.quancheng.saluki.proxy.protocol.RpcDynamicClient;

/**
 * @author liushiming
 * @version DynamicDubboClient.java, v 0.0.1 2018年1月29日 下午2:38:28 liushiming
 */
public class DynamicDubboClient extends RpcDynamicClient {

  private final ApplicationConfig applicationConfig;

  private final RegistryConfig registryConfig;

  public DynamicDubboClient(final ApplicationConfig applicationConfig,
      RegistryConfig registryConfig) {
    super();
    this.applicationConfig = applicationConfig;
    this.registryConfig = registryConfig;
  }

  @Override
  public String doRemoteCall(final RpcDO rpcDo, final String jsonInput) {
    try {
      final String serviceName = rpcDo.getServiceName();
      final String methodName = rpcDo.getMethodName();
      final String group = rpcDo.getServiceGroup();
      final String version = rpcDo.getServiceVersion();
      final String paramType = rpcDo.getInputParam();
      ReferenceConfig<GenericService> reference = new ReferenceConfig<GenericService>();
      reference.setApplication(applicationConfig);
      reference.setRegistry(registryConfig);
      reference.setInterface(serviceName);
      reference.setGroup(group);
      reference.setGeneric(true);
      reference.setCheck(false);
      reference.setVersion(version);
      ReferenceConfigCache cache = ReferenceConfigCache.getCache();
      GenericService genericService = cache.get(reference);
      Pair<String[], Object[]> typeAndValue = paramTypeAndValue(paramType, jsonInput);
      Object response =
          genericService.$invoke(methodName, typeAndValue.getLeft(), typeAndValue.getRight());
      return JSON.toJSONString(response);
    } catch (Throwable e) {
      throw new IllegalArgumentException(String.format(
          "service definition is wrong,please check the proto file you update,service is %s, method is %s",
          rpcDo.getServiceName(), rpcDo.getMethodName()), e);
    }

  }

  /**
   * dubbo仅支持以POJO的方式作为入参，和grpc保持一致，如果要支持复杂的，需要修改
   */
  private Pair<String[], Object[]> paramTypeAndValue(String inputParamType, String inputJson) {
    Map<String, Object> value = JSON.parseObject(inputJson);
    List<String> paramTypeList = Lists.newArrayList();
    List<Object> paramValueList = Lists.newArrayList();
    paramValueList.add(value);
    paramTypeList.add(inputParamType);

    String[] targetParamType = (String[]) paramTypeList.toArray(new String[paramTypeList.size()]);
    Object[] targetParamValue =
        (Object[]) paramValueList.toArray(new Object[paramValueList.size()]);
    return new ImmutablePair<String[], Object[]>(targetParamType, targetParamValue);
  }



}
