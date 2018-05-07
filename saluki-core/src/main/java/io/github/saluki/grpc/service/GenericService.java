package io.github.saluki.grpc.service;

import com.google.protobuf.DynamicMessage;

import io.grpc.MethodDescriptor;

public interface GenericService {

  Object $invoke(String serviceName, String group, String version, String method, Object[] args);

  Object $invoke(String serviceName, String group, String version, String method,
      MethodDescriptor<DynamicMessage, DynamicMessage> methodDesc, DynamicMessage message);

}
