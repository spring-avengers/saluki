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
package com.quancheng.saluki.oauth2.zuul.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.github.os72.protocjar.Protoc;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

/**
 * @author liushiming
 * @version ProtocService.java, v 0.0.1 2018年1月5日 下午1:00:00 liushiming
 */
@Component
public class ProtocService {
  private static final Logger logger = LoggerFactory.getLogger(ProtocService.class);


  public byte[] compileProtoFile(String protoDirectyPath, String protoPath) {
    try {
      Path descriptorPath = Files.createTempFile("descriptor", ".pb.bin");
      ImmutableList.Builder<String> builder = ImmutableList.<String>builder()//
          .add("--include_std_types")//
          .add("-I" + protoDirectyPath)
          .add("--descriptor_set_out=" + descriptorPath.toAbsolutePath().toString())//
      ;
      ImmutableList<String> protocArgs = builder.add(protoPath).build();
      System.out.println(protocArgs);
      int status;
      try {
        status = Protoc.runProtoc(protocArgs.toArray(new String[0]));
      } catch (IOException | InterruptedException e) {
        throw new IllegalArgumentException("Unable to execute protoc binary", e);
      }
      if (status != 0) {
        throw new IllegalArgumentException(
            String.format("Got exit code [%d] from protoc with args [%s]", status, protocArgs));
      }
      return Files.readAllBytes(descriptorPath);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }


  // public static final Message json2Protobuf(byte[] protoBytes, String jsonStr) throws IOException
  // {
  // FileDescriptorSet protoCollection = FileDescriptorSet.parseFrom(protoBytes);
  // FileDescriptorProto protoFile = protoCollection.getFileList().get(0);
  // Message.Builder messageBuilder = DynamicMessage.newBuilder(protoFile);
  // protoBufJsonFormat.merge(new ByteArrayInputStream(jsonStr.getBytes()), messageBuilder);
  // return messageBuilder.build();
  // }

  public static void main(String[] args) throws IOException {
    ProtocService service = new ProtocService();
    byte[] protoBytes = service.compileProtoFile(
        "/Users/liushiming/project/java/saluki/saluki-example/saluki-example-api/src/main/proto",
        "/Users/liushiming/project/java/saluki/saluki-example/saluki-example-api/src/main/proto/example/hello.proto");
    // try {
    // String jsonFormat = "{name:'liushiming'}";
    // Message message = ProtocService.json2Protobuf(protoBytes, jsonFormat);
    // System.out.println(message.getDefaultInstanceForType());
    // } catch (InvalidProtocolBufferException e) {
    // e.printStackTrace();
    // }
  }
}
