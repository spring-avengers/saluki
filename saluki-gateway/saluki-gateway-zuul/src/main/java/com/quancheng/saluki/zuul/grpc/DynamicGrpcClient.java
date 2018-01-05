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

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Component;

import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;
import com.quancheng.saluki.boot.SalukiReference;
import com.quancheng.saluki.core.grpc.exception.RpcServiceException;
import com.quancheng.saluki.core.grpc.service.GenericService;
import com.quancheng.saluki.oauth2.zuul.dao.GrpcDao;
import com.quancheng.saluki.oauth2.zuul.domain.GrpcDO;

import io.grpc.MethodDescriptor.MethodType;

/**
 * @author liushiming
 * @version DynamicGrpcClient1.java, v 0.0.1 2018年1月5日 下午5:38:46 liushiming
 */
@Component
public class DynamicGrpcClient {

  @SalukiReference
  private GenericService genricService;

  @Autowired
  private GrpcDao grpcDao;

  @Autowired
  private EhCacheCacheManager echacheMaanger;

  private org.springframework.cache.Cache gRpcDynamicCache;

  private static final JsonFormat protoBufJsonFormat = new JsonFormat();

  @PostConstruct
  public void init() {
    gRpcDynamicCache = echacheMaanger.getCache("grpcDynamic");
  }

  private com.google.protobuf.Descriptors.MethodDescriptor creatProtoMethodDescriptor(
      final String packageName, String serviceName, final String methodName, final String group,
      final String version) {
    final String cacheKey = serviceName + ":" + methodName + ":" + group + ":" + version;
    com.google.protobuf.Descriptors.MethodDescriptor protoMethodDescriptor =
        (com.google.protobuf.Descriptors.MethodDescriptor) gRpcDynamicCache.get(cacheKey).get();
    if (protoMethodDescriptor != null) {
      return protoMethodDescriptor;
    } else {
      GrpcDO grpcDo = grpcDao.get(serviceName, methodName, group, version);
      byte[] protoBytes = grpcDo.getProtoContext();
      try {
        FileDescriptorSet descriptorSet = FileDescriptorSet.parseFrom(protoBytes);
        ServiceResolver serviceResolver = ServiceResolver.fromFileDescriptorSet(descriptorSet);
        ProtoMethodName protoMethodName = ProtoMethodName
            .parseFullGrpcMethodName(packageName + "." + serviceName + "/" + methodName);
        return serviceResolver.resolveServiceMethod(protoMethodName);
      } catch (InvalidProtocolBufferException e) {
        throw new RpcServiceException(e);
      }
    }

  }

  public Object call(final String packageName, final String serviceName, final String methodName,
      final String group, final String version, final String jsonInput) {
    try {
      com.google.protobuf.Descriptors.MethodDescriptor protoMethodDescriptor =
          this.creatProtoMethodDescriptor(packageName, serviceName, methodName, group, version);
      final io.grpc.MethodDescriptor<DynamicMessage, DynamicMessage> methodDesc =
          this.createGrpcMethodDescriptor(protoMethodDescriptor);
      final DynamicMessage message = this.json2Protobuf(protoMethodDescriptor, jsonInput);
      return genricService.$invoke(serviceName, group, version, methodName, methodDesc, message);
    } catch (Exception e) {
      throw new RpcServiceException(e);
    }
  }


  private DynamicMessage json2Protobuf(
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
    String serviceName = getServiceName(protoMethodDescriptor);
    String methodName = getMethodName(protoMethodDescriptor);
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
