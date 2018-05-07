/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package io.github.saluki.grpc.server.internal;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.protobuf.Message;
import io.github.saluki.common.GrpcURL;
import io.github.saluki.grpc.annotation.GrpcMethodType;
import io.github.saluki.grpc.exception.RpcErrorMsgConstant;
import io.github.saluki.grpc.exception.RpcServiceException;
import io.github.saluki.grpc.server.GrpcProtocolExporter;
import io.github.saluki.grpc.service.ClientServerMonitor;
import io.github.saluki.grpc.service.MonitorService;
import io.github.saluki.grpc.util.GrpcUtil;
import io.github.saluki.utils.ReflectUtils;

import io.grpc.MethodDescriptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.ServerCalls;

/**
 * @author shimingliu 2016年12月14日 下午10:10:33
 * @version DefaultProxyExporter.java, v 0.0.1 2016年12月14日 下午10:10:33 shimingliu
 */
public class DefaultProxyExporter implements GrpcProtocolExporter {

  private static final Logger log = LoggerFactory.getLogger(DefaultProxyExporter.class);

  private final GrpcURL providerUrl;

  private final MonitorService clientServerMonitor;

  public DefaultProxyExporter(GrpcURL providerUrl) {
    Long monitorinterval = providerUrl.getParameter("monitorinterval", 60L);
    this.clientServerMonitor = ClientServerMonitor.newClientServerMonitor(monitorinterval);
    this.providerUrl = providerUrl;
  }

  @Override
  public ServerServiceDefinition export(Class<?> protocol, Object protocolImpl) {
    Class<?> serivce = protocol;
    Object serviceRef = protocolImpl;
    String serviceName = protocol.getName();
    ServerServiceDefinition.Builder serviceDefBuilder =
        ServerServiceDefinition.builder(serviceName);
    List<Method> methods = ReflectUtils.findAllPublicMethods(serivce);
    if (methods.isEmpty()) {
      throw new IllegalStateException(
          "protocolClass " + serviceName + " not have export method" + serivce);
    }
    final ConcurrentMap<String, AtomicInteger> concurrents = Maps.newConcurrentMap();
    for (Method method : methods) {
      MethodDescriptor<Message, Message> methodDescriptor =
          GrpcUtil.createMethodDescriptor(serivce, method);
      GrpcMethodType grpcMethodType = method.getAnnotation(GrpcMethodType.class);
      switch (grpcMethodType.methodType()) {
        case UNARY:
          serviceDefBuilder.addMethod(methodDescriptor,
              ServerCalls.asyncUnaryCall(new ServerInvocation(serviceRef, method, grpcMethodType,
                  providerUrl, concurrents, clientServerMonitor)));
          break;
        case CLIENT_STREAMING:
          serviceDefBuilder.addMethod(methodDescriptor,
              ServerCalls.asyncClientStreamingCall(new ServerInvocation(serviceRef, method,
                  grpcMethodType, providerUrl, concurrents, clientServerMonitor)));
          break;
        case SERVER_STREAMING:
          serviceDefBuilder.addMethod(methodDescriptor,
              ServerCalls.asyncServerStreamingCall(new ServerInvocation(serviceRef, method,
                  grpcMethodType, providerUrl, concurrents, clientServerMonitor)));
          break;
        case BIDI_STREAMING:
          serviceDefBuilder.addMethod(methodDescriptor,
              ServerCalls.asyncBidiStreamingCall(new ServerInvocation(serviceRef, method,
                  grpcMethodType, providerUrl, concurrents, clientServerMonitor)));
          break;
        default:
          RpcServiceException rpcFramwork =
              new RpcServiceException(RpcErrorMsgConstant.SERVICE_UNFOUND);
          throw rpcFramwork;
      }
    }
    log.info("'{}' service has been registered.", serviceName);
    return serviceDefBuilder.build();
  }

}
