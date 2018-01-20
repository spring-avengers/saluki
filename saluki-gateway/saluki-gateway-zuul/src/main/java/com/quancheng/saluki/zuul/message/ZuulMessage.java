/*
 * Copyright 2018 Netflix, Inc.
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

package com.quancheng.saluki.zuul.message;

import com.netflix.zuul.context.SessionContext;
import com.quancheng.saluki.zuul.filters.ZuulFilter;

import io.netty.handler.codec.http.HttpContent;

/**
 * User: Mike Smith Date: 7/16/15 Time: 12:22 AM
 */
public interface ZuulMessage extends Cloneable {
  SessionContext getContext();

  Headers getHeaders();

  void setHeaders(Headers newHeaders);

  boolean hasBody();

  void setHasBody(boolean hasBody);

  byte[] getBody();

  int getBodyLength();

  void setBody(byte[] body);

  void setBodyAsText(String bodyText);

  void bufferBodyContents(HttpContent chunk);

  Iterable<HttpContent> getBodyContents();

  boolean finishBufferedBodyIfIncomplete();

  boolean hasCompleteBody();

  void runBufferedBodyContentThroughFilter(ZuulFilter filter);

  void disposeBufferedBody();

  String getBodyAsText();

  int getMaxBodySize();

  ZuulMessage clone();

  String getInfoForLogging();

}
