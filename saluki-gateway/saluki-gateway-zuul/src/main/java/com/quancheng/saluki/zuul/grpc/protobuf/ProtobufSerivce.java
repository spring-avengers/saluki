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
package com.quancheng.saluki.zuul.grpc.protobuf;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.quancheng.saluki.core.grpc.exception.RpcBizException;
import com.quancheng.saluki.oauth2.zuul.dao.GrpcDao;
import com.quancheng.saluki.oauth2.zuul.domain.GrpcDO;

/**
 * @author liushiming
 * @version GrpcRouteService.java, v 0.0.1 2018年1月7日 下午12:59:14 liushiming
 */
@Service
public class ProtobufSerivce {

  private static final Logger LOG = LoggerFactory.getLogger(ProtobufSerivce.class);

  @Autowired
  private GrpcDao grpcDao;


  @Cacheable(value = "serviceTypes", key = "#packageName+ '.' + #serviceName" + "_" + "#methodName"
      + "_" + "#group" + "_" + "#version")
  public Pair<Descriptor, Descriptor> resolveServiceInputOutputType(final String packageName,
      String serviceName, final String methodName, final String group, final String version) {
    Pair<Descriptor, Descriptor> argsDesc =
        this.findSingleProtobuf(packageName, serviceName, methodName, group, version);
    if (argsDesc == null) {
      argsDesc = this.findDirectyprotobuf(packageName, serviceName, methodName, group, version);
    }
    return argsDesc;
  }


  private Pair<Descriptor, Descriptor> findDirectyprotobuf(final String packageName,
      String serviceName, final String methodName, final String group, final String version) {
    GrpcDO grpcDo = grpcDao.get(packageName, serviceName, methodName, group, version);
    byte[] protoContent = grpcDo.getProtoContext();
    FileDescriptorSet descriptorSet = null;
    if (protoContent != null && protoContent.length > 0) {
      try {
        descriptorSet = FileDescriptorSet.parseFrom(protoContent);
        ServiceResolver serviceResolver = ServiceResolver.fromFileDescriptorSet(descriptorSet);
        ProtoMethodName protoMethodName = ProtoMethodName
            .parseFullGrpcMethodName(packageName + "." + serviceName + "/" + methodName);
        MethodDescriptor protoMethodDesc = serviceResolver.resolveServiceMethod(protoMethodName);
        return new ImmutablePair<Descriptor, Descriptor>(protoMethodDesc.getInputType(),
            protoMethodDesc.getOutputType());
      } catch (InvalidProtocolBufferException e) {
        LOG.error(e.getMessage(), e);
        throw new RpcBizException(
            "protobuf service definition is invalid,the descriptorSet is: " + descriptorSet, e);
      }
    }
    return null;
  }

  private Pair<Descriptor, Descriptor> findSingleProtobuf(final String packageName,
      String serviceName, final String methodName, final String group, final String version) {
    GrpcDO grpcDo = grpcDao.get(packageName, serviceName, methodName, group, version);
    byte[] in = grpcDo.getProtoReq();
    byte[] out = grpcDo.getProtoRep();
    DescriptorProto inputDesc = null;
    DescriptorProto outputDesc = null;
    if (in != null && in.length > 0 && out != null && out.length > 0) {
      try {
        inputDesc = DescriptorProto.parseFrom(in);
        outputDesc = DescriptorProto.parseFrom(out);
        return new ImmutablePair<Descriptor, Descriptor>(inputDesc.getDescriptorForType(),
            outputDesc.getDescriptorForType());
      } catch (InvalidProtocolBufferException e) {
        LOG.error(e.getMessage(), e);
        throw new RpcBizException("protobuf service definition is invalid,the input type is: "
            + inputDesc + " the output ytpe is:" + outputDesc, e);
      }
    }
    return null;
  }



}
