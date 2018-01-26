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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.quancheng.saluki.core.grpc.exception.RpcBizException;
import com.quancheng.saluki.gateway.persistence.filter.domain.RpcDO;

/**
 * @author liushiming
 * @version GrpcRouteService.java, v 0.0.1 2018年1月7日 下午12:59:14 liushiming
 */
@Component
public class ProtobufComponent {

  private static final Logger LOG = LoggerFactory.getLogger(ProtobufComponent.class);


  public Pair<Descriptor, Descriptor> resolveServiceInputOutputType(final RpcDO rpcDo) {
    Pair<Descriptor, Descriptor> argsDesc = this.findSingleProtobuf(rpcDo);
    if (argsDesc == null) {
      argsDesc = this.findDirectyprotobuf(rpcDo);
    }
    return argsDesc;
  }


  private Pair<Descriptor, Descriptor> findDirectyprotobuf(final RpcDO rpcDo) {
    byte[] protoContent = rpcDo.getProtoContext();
    FileDescriptorSet descriptorSet = null;
    if (protoContent != null && protoContent.length > 0) {
      try {
        descriptorSet = FileDescriptorSet.parseFrom(protoContent);
        ServiceResolver serviceResolver = ServiceResolver.fromFileDescriptorSet(descriptorSet);
        ProtoMethodName protoMethodName = ProtoMethodName
            .parseFullGrpcMethodName(rpcDo.getServiceName() + "/" + rpcDo.getMethodName());
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

  private Pair<Descriptor, Descriptor> findSingleProtobuf(final RpcDO rpcDo) {
    byte[] in = rpcDo.getProtoReq();
    byte[] out = rpcDo.getProtoRep();
    FileDescriptorSet inputDescriptorSet = null;
    FileDescriptorSet outputDescriptorSet = null;
    if (in != null && in.length > 0 && out != null && out.length > 0) {
      try {
        inputDescriptorSet = FileDescriptorSet.parseFrom(in);
        outputDescriptorSet = FileDescriptorSet.parseFrom(out);
        DescriptorProto fileInputDesc = inputDescriptorSet.getFile(0).getMessageType(0);
        DescriptorProto fileOutputDesc = outputDescriptorSet.getFile(0).getMessageType(0);
        return new ImmutablePair<Descriptor, Descriptor>(fileInputDesc.getDescriptorForType(),
            fileOutputDesc.getDescriptorForType());
      } catch (InvalidProtocolBufferException e) {
        LOG.error(e.getMessage(), e);
        throw new RpcBizException("protobuf service definition is invalid,the input type is: "
            + inputDescriptorSet + " the output ytpe is:" + outputDescriptorSet, e);
      }
    }
    return null;
  }



}
