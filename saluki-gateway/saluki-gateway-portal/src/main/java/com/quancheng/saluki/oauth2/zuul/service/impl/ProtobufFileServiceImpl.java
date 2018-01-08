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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.web.multipart.MultipartFile;

import com.quancheng.saluki.oauth2.zuul.service.ProtobufFileService;

/**
 * @author liushiming
 * @version ProtobufFileServiceImpl.java, v 0.0.1 2018年1月8日 下午4:16:18 liushiming
 */
public class ProtobufFileServiceImpl implements ProtobufFileService {

  private static final String PROTOBUF_FILE_DIRECYTORY = "/var/protofiles/";


  @Override
  public byte[] protobufService(MultipartFile multipartFile, String serviceFileName) {
    return null;
  }

  @Override
  public byte[] protobufInputOutput(MultipartFile inputFile, MultipartFile outputFile) {
    return null;
  }

  private String uploadZipFile(InputStream fileStream, String serviceName) throws IOException {
    String unZipRealPath = PROTOBUF_FILE_DIRECYTORY + serviceName;
    File unZipFile = new File(unZipRealPath);
    if (!unZipFile.exists()) {
      unZipFile.mkdirs();
    }
    ZipInputStream zipInputStream = null;
    try {
      zipInputStream = new ZipInputStream(fileStream);
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

  // public static void main(String[] args) {
  // File file = new File(
  // "/Users/liushiming/project/java/saluki/saluki-example/saluki-example-api/src/main/proto.zip");
  // ProtobufFileServiceImpl service = new ProtobufFileServiceImpl();
  // try {
  // service.uploadZipFile(new FileInputStream(file), "helloService");
  // } catch (FileNotFoundException e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // } catch (IOException e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // }
  //
  // }

}
