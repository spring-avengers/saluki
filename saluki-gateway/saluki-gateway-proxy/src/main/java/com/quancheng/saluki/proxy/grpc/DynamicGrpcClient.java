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
package com.quancheng.saluki.proxy.grpc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.tuple.Pair;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;
import com.quancheng.saluki.core.grpc.exception.RpcFrameworkException;
import com.quancheng.saluki.core.grpc.exception.RpcServiceException;
import com.quancheng.saluki.core.grpc.service.GenericService;
import com.quancheng.saluki.gateway.persistence.filter.domain.RpcDO;

import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.Marshaller;
import io.grpc.MethodDescriptor.MethodType;
import io.netty.util.CharsetUtil;

/**
 * @author liushiming
 * @version DynamicGrpcClient1.java, v 0.0.1 2018年1月5日 下午5:38:46 liushiming
 */
public class DynamicGrpcClient {

  private final GenericService genricService;


  private static final JsonFormat JSON2PROTOBUF = new JsonFormat();

  static {
    JSON2PROTOBUF.setDefaultCharset(CharsetUtil.UTF_8);
  }

  public DynamicGrpcClient(GenericService genricService) {
    this.genricService = genricService;
  }

  public String call(final RpcDO rpcDo, final String jsonInput) {
    try {

      final String serviceName = rpcDo.getServiceName();
      final String methodName = rpcDo.getMethodName();
      final String group = rpcDo.getServiceGroup();
      final String version = rpcDo.getServiceVersion();
      Pair<Descriptor, Descriptor> inOutType = ProtobufUtil.resolveServiceInputOutputType(rpcDo);
      Descriptor inPutType = inOutType.getLeft();
      Descriptor outPutType = inOutType.getRight();
      MethodDescriptor<DynamicMessage, DynamicMessage> methodDesc =
          this.createGrpcMethodDescriptor(serviceName, methodName, inPutType, outPutType);
      DynamicMessage message = this.createGrpcDynamicMessage(inPutType, jsonInput);
      Message response = (Message) genricService.$invoke(serviceName, group, version, methodName,
          methodDesc, message);
      return JSON2PROTOBUF.printToString(response);
    } catch (IOException e) {
      throw new RpcServiceException(String.format(
          "json covert to DynamicMessage failed! the json is :%s, the protobuf type is: %s",
          jsonInput), e);
    } catch (Exception e) {
      throw new RpcFrameworkException(String.format(
          "service definition is wrong,please check the proto file you update,service is %s, method is %s",
          rpcDo.getServiceName(), rpcDo.getMethodName()), e);
    }
  }


  private DynamicMessage createGrpcDynamicMessage(final Descriptor messageDefine, String json)
      throws IOException {
    DynamicMessage.Builder messageBuilder = DynamicMessage.newBuilder(messageDefine);
    JSON2PROTOBUF.merge(new ByteArrayInputStream(json.getBytes()), messageBuilder);
    return messageBuilder.build();
  }


  private MethodDescriptor<DynamicMessage, DynamicMessage> createGrpcMethodDescriptor(
      String serviceName, String methodName, Descriptor inPutType, Descriptor outPutType) {
    String fullMethodName = MethodDescriptor.generateFullMethodName(serviceName, methodName);
    return io.grpc.MethodDescriptor.<DynamicMessage, DynamicMessage>newBuilder()
        .setType(MethodType.UNARY)//
        .setFullMethodName(fullMethodName)//
        .setRequestMarshaller(new DynamicMessageMarshaller(inPutType))//
        .setResponseMarshaller(new DynamicMessageMarshaller(outPutType))//
        .setSafe(false)//
        .setIdempotent(false)//
        .build();
  }

  protected class DynamicMessageMarshaller implements Marshaller<DynamicMessage> {
    private final Descriptor messageDescriptor;

    public DynamicMessageMarshaller(Descriptor messageDescriptor) {
      this.messageDescriptor = messageDescriptor;
    }

    @Override
    public DynamicMessage parse(InputStream inputStream) {
      try {
        return DynamicMessage.newBuilder(messageDescriptor)
            .mergeFrom(inputStream, ExtensionRegistryLite.getEmptyRegistry()).build();
      } catch (IOException e) {
        throw new RuntimeException("Unable to merge from the supplied input stream", e);
      }
    }

    @Override
    public InputStream stream(DynamicMessage abstractMessage) {
      return abstractMessage.toByteString().newInput();
    }
  }

}
