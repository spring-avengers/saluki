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
package com.quancheng.saluki.gateway.zuul.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author liushiming
 * @version ProtobufFileService.java, v 0.0.1 2018年1月8日 下午3:59:38 liushiming
 */
public interface ProtobufService {

  public byte[] compileDirectoryProto(MultipartFile directoryZipStream, String serviceFileName);

  public byte[] compileFileProto(MultipartFile inputStream, String fileName);


}
