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
package com.quancheng.saluki.zuul.grpc;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.springframework.stereotype.Component;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;
import com.googlecode.protobuf.format.JsonFormat;
import com.quancheng.saluki.boot.SalukiReference;
import com.quancheng.saluki.core.grpc.exception.RpcServiceException;
import com.quancheng.saluki.core.grpc.service.GenericService;

import io.grpc.MethodDescriptor.MethodType;

/**
 * @author liushiming
 * @version DynamicGrpcClient1.java, v 0.0.1 2018年1月5日 下午5:38:46 liushiming
 */
@Component
public class DynamicGrpcClient {

  @SalukiReference
  private GenericService genricService;

  private static final JsonFormat protoBufJsonFormat = new JsonFormat();

  public Object call(final String group, final String version,
      final com.google.protobuf.Descriptors.MethodDescriptor protoMethodDescriptor,
      final String jsonInput) {
    try {
      final String serviceName = this.getServiceName(protoMethodDescriptor);
      final String methodName = this.getMethodName(protoMethodDescriptor);
      final io.grpc.MethodDescriptor<DynamicMessage, DynamicMessage> methodDesc =
          this.createGrpcMethodDescriptor(protoMethodDescriptor);
      final DynamicMessage message = this.json2Protobuf(protoMethodDescriptor, jsonInput);
      return genricService.$invoke(serviceName, group, version, methodName, methodDesc, message);
    } catch (Exception e) {
      throw new RpcServiceException(e);
    }
  }


  public DynamicMessage json2Protobuf(
      final com.google.protobuf.Descriptors.MethodDescriptor protoMethodDescriptor, String jsonStr)
      throws IOException {
    DynamicMessage.Builder messageBuilder =
        DynamicMessage.newBuilder(getRequestType(protoMethodDescriptor));
    protoBufJsonFormat.merge(new ByteArrayInputStream(jsonStr.getBytes()), messageBuilder);
    return messageBuilder.build();
  }

  private io.grpc.MethodDescriptor<DynamicMessage, DynamicMessage> createGrpcMethodDescriptor(
      final com.google.protobuf.Descriptors.MethodDescriptor protoMethodDescriptor) {
    String fullMethodName = getFullMethodName(protoMethodDescriptor);
    MethodType methodType = getMethodType(protoMethodDescriptor);
    if (methodType != MethodType.UNARY) {
      throw new IllegalArgumentException(
          "gateway not support stream call, The MethodTYpe is:" + methodType);
    }
    return io.grpc.MethodDescriptor.<DynamicMessage, DynamicMessage>newBuilder().setType(methodType)//
        .setFullMethodName(fullMethodName)//
        .setRequestMarshaller(new DynamicMessageMarshaller(getRequestType(protoMethodDescriptor)))//
        .setResponseMarshaller(new DynamicMessageMarshaller(getResponseType(protoMethodDescriptor)))//
        .setSafe(false)//
        .setIdempotent(false)//
        .build();
  }

  private Descriptor getRequestType(
      final com.google.protobuf.Descriptors.MethodDescriptor protoMethodDescriptor) {
    return protoMethodDescriptor.getInputType();
  }

  private Descriptor getResponseType(
      final com.google.protobuf.Descriptors.MethodDescriptor protoMethodDescriptor) {
    return protoMethodDescriptor.getOutputType();
  }

  private String getFullMethodName(
      final com.google.protobuf.Descriptors.MethodDescriptor protoMethodDescriptor) {
    String serviceName = protoMethodDescriptor.getService().getFullName();
    String methodName = protoMethodDescriptor.getName();
    return io.grpc.MethodDescriptor.generateFullMethodName(serviceName, methodName);
  }

  private String getServiceName(
      final com.google.protobuf.Descriptors.MethodDescriptor protoMethodDescriptor) {
    return protoMethodDescriptor.getService().getFullName();
  }

  private String getMethodName(
      final com.google.protobuf.Descriptors.MethodDescriptor protoMethodDescriptor) {
    return protoMethodDescriptor.getName();
  }

  private MethodType getMethodType(
      final com.google.protobuf.Descriptors.MethodDescriptor protoMethodDescriptor) {
    boolean clientStreaming = protoMethodDescriptor.toProto().getClientStreaming();
    boolean serverStreaming = protoMethodDescriptor.toProto().getServerStreaming();
    if (!clientStreaming && !serverStreaming) {
      return MethodType.UNARY;
    } else if (!clientStreaming && serverStreaming) {
      return MethodType.SERVER_STREAMING;
    } else if (clientStreaming && !serverStreaming) {
      return MethodType.CLIENT_STREAMING;
    } else {
      return MethodType.BIDI_STREAMING;
    }
  }
}
