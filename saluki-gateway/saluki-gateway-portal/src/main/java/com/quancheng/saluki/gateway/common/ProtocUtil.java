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
package com.quancheng.saluki.gateway.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.os72.protocjar.Protoc;
import com.google.common.collect.ImmutableList;

/**
 * @author liushiming
 * @version ProtocUtil.java, v 0.0.1 2018年1月7日 下午12:56:06 liushiming
 */
public class ProtocUtil {

  private ProtocUtil() {}


  public static byte[] compileProtoFile(String protoDirectyPath, String protoPath) {
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
}
