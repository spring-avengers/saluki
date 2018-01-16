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
package com.quancheng.saluki.gateway.zuul.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.os72.protocjar.Protoc;
import com.google.common.collect.ImmutableList;
import com.quancheng.saluki.gateway.common.BDException;
import com.quancheng.saluki.gateway.zuul.service.ProtobufService;

/**
 * @author liushiming
 * @version ProtobufFileServiceImpl.java, v 0.0.1 2018年1月8日 下午4:16:18 liushiming
 */
@Service
public class ProtobufServiceImpl implements ProtobufService {

  private String protoFileDirectory;

  @PostConstruct
  public void init() {
    try {
      Path protosTempDirectory = Files.createTempDirectory("protos");
      protoFileDirectory = protosTempDirectory.toFile().getAbsolutePath();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public byte[] compileDirectoryProto(MultipartFile directoryZipStream, String serviceFileName) {
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
        FileUtils.deleteQuietly(new File(fileDirectory));
      }
    }
  }

  @Override
  public byte[] compileFileProto(MultipartFile inputStream, String fileName) {
    String filePath = null;
    try {
      filePath = this.uploadSimpleFile(inputStream, fileName);
      return this.runProtoc(null, filePath);
    } catch (IOException e) {
      throw new BDException(e.getMessage(), e);
    } finally {
      if (filePath != null) {
        FileUtils.deleteQuietly(new File(filePath));
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


  private String uploadSimpleFile(MultipartFile protoFile, String fileName) throws IOException {
    Path protoFileDistPath =
        Paths.get(protoFileDirectory, "argProtos", protoFile.getOriginalFilename());
    File protoFileDist = protoFileDistPath.toFile();
    FileUtils.forceMkdirParent(protoFileDist);
    protoFile.transferTo(protoFileDist);
    return protoFileDist.getAbsolutePath();
  }

  private String uploadZipFile(MultipartFile zipFile, String serviceName) throws IOException {
    Path zipFileDistPath =
        Paths.get(protoFileDirectory, serviceName, zipFile.getOriginalFilename());
    File zipFileDist = zipFileDistPath.toFile();
    FileUtils.forceMkdirParent(zipFileDist);
    zipFile.transferTo(zipFileDist);
    ZipInputStream zipInputStream = null;
    try {
      zipInputStream = new ZipInputStream(new FileInputStream(zipFileDist));
      ZipEntry zipEntry;
      while ((zipEntry = zipInputStream.getNextEntry()) != null) {
        String zipEntryName = zipEntry.getName();
        String outPath = zipFileDistPath.getParent() + "/" + zipEntryName;
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
    return zipFileDist.getParent();
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
