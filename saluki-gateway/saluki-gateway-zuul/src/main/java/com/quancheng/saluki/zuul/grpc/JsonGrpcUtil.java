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

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;

/**
 * @author liushiming
 * @version JsonGrpcUtil.java, v 0.0.1 2018年1月5日 下午3:39:00 liushiming
 */
public final class JsonGrpcUtil {
  private JsonGrpcUtil() {

  }

  private static final JsonFormat protoBufJsonFormat = new JsonFormat();


  public static final Message json2Protobuf(byte[] protoBytes, String jsonStr) throws IOException {
    FileDescriptorSet protoCollection = FileDescriptorSet.parseFrom(protoBytes);
    FileDescriptorProto protoFile = protoCollection.getFileList().get(0);
    Message.Builder messageBuilder = DynamicMessage.newBuilder(protoFile);
    protoBufJsonFormat.merge(new ByteArrayInputStream(jsonStr.getBytes()), messageBuilder);
    return messageBuilder.build();
  }

}
