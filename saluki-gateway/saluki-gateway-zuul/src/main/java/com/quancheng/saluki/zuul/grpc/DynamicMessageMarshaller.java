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

import java.io.IOException;
import java.io.InputStream;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.ExtensionRegistryLite;

import io.grpc.MethodDescriptor.Marshaller;

/**
 * @author liushiming
 * @version DynamicMessageMarshaller.java, v 0.0.1 2018年1月5日 下午3:27:24 liushiming
 */
public class DynamicMessageMarshaller implements Marshaller<DynamicMessage> {
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
