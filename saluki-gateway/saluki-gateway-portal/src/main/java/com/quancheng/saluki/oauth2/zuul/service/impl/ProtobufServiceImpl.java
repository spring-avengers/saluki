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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.os72.protocjar.Protoc;
import com.google.common.collect.ImmutableList;
import com.quancheng.saluki.oauth2.common.BDException;
import com.quancheng.saluki.oauth2.zuul.service.ProtobufService;

/**
 * @author liushiming
 * @version ProtobufFileServiceImpl.java, v 0.0.1 2018年1月8日 下午4:16:18 liushiming
 */
@Service
public class ProtobufServiceImpl implements ProtobufService {

  @Value("${saluki.gateway.protoFile}")
  private String protoFileDirectory;

  @Override
  public byte[] compileDirectoryProto(InputStream directoryZipStream, String serviceFileName) {
    String fileDirectory = null;
    try {
      fileDirectory = this.uploadZipFile(directoryZipStream, serviceFileName);
      ProtoFileServiceFilter filter = new ProtoFileServiceFilter(serviceFileName);
      Files.walkFileTree(Paths.get(fileDirectory), filter);
      String protoFilePath = filter.getProtoFilePath();
      return this.runProtoc(fileDirectory, protoFilePath);
    } catch (IOException e) {
      throw new BDException(e.getMessage(), e);
    } finally {
      if (fileDirectory != null) {
        try {
          Files.deleteIfExists(Paths.get(fileDirectory));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  public byte[] compileFileProto(InputStream inputStream, String fileName) {
    String filePath = null;
    try {
      filePath = this.uploadSimpleFile(inputStream, fileName);
      return this.runProtoc(null, filePath);
    } catch (IOException e) {
      throw new BDException(e.getMessage(), e);
    } finally {
      if (filePath != null) {
        try {
          Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

  }

  private byte[] runProtoc(String includePath, String protoFilePath) {
    try {
      Path descriptorPath = Files.createTempFile("descriptor", ".pb.bin");
      ImmutableList.Builder<String> builder = ImmutableList.<String>builder();
      if (includePath != null) {
        builder.add("-I" + includePath);
      }
      builder.add("--descriptor_set_out=" + descriptorPath.toAbsolutePath().toString());
      ImmutableList<String> protocArgs = builder.add(protoFilePath).build();
      int status = Protoc.runProtoc(protocArgs.toArray(new String[0]));
      if (status != 0) {
        throw new IllegalArgumentException(
            String.format("Got exit code [%d] from protoc with args [%s]", status, protocArgs));
      }
      return Files.readAllBytes(descriptorPath);
    } catch (IOException | InterruptedException e) {
      throw new BDException(e.getMessage(), e);
    }
  }


  private String uploadSimpleFile(InputStream argStream, String fileName) throws IOException {
    String argRealPath = Paths.get(protoFileDirectory, "argsProtos").toFile().getAbsolutePath();
    File argDirectory = new File(argRealPath);
    if (!argDirectory.exists()) {
      argDirectory.mkdirs();
    }
    Path filePath = Paths.get(argRealPath, fileName);
    Files.copy(argStream, filePath, StandardCopyOption.REPLACE_EXISTING);
    return filePath.toFile().getAbsolutePath();
  }

  private String uploadZipFile(InputStream serviceStream, String serviceName) throws IOException {
    String unZipRealPath = Paths.get(protoFileDirectory, serviceName).toFile().getAbsolutePath();
    File unZipFile = new File(unZipRealPath);
    if (!unZipFile.exists()) {
      unZipFile.mkdirs();
    }
    ZipInputStream zipInputStream = null;
    try {
      zipInputStream = new ZipInputStream(serviceStream);
      ZipEntry zipEntry;
      while ((zipEntry = zipInputStream.getNextEntry()) != null) {
        String zipEntryName = zipEntry.getName();
        String outPath = unZipRealPath + "/" + zipEntryName;
        File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
        if (!file.exists()) {
          file.mkdirs();
        }
        if (new File(outPath).isDirectory()) {
          continue;
        }
        OutputStream outputStream = new FileOutputStream(outPath);
        byte[] bytes = new byte[4096];
        int len;
        while ((len = zipInputStream.read(bytes)) > 0) {
          outputStream.write(bytes, 0, len);
        }
        outputStream.close();
        zipInputStream.closeEntry();
      }
    } catch (IOException e) {
      throw e;
    } finally {
      try {
        if (zipInputStream != null)
          zipInputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return unZipRealPath;
  }


  private static class ProtoFileServiceFilter extends SimpleFileVisitor<Path> {

    private final String protoFileName;

    private String protoFilePath;

    public ProtoFileServiceFilter(String protoFileName) {
      this.protoFileName = protoFileName;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      if (file.getFileName().startsWith(protoFileName)) {
        protoFilePath = file.toFile().getAbsolutePath();
      }
      return FileVisitResult.CONTINUE;
    }

    public String getProtoFilePath() {
      return protoFilePath;
    }

  }


}
