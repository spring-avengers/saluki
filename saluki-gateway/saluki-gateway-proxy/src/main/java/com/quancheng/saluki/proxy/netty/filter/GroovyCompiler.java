/*
 * Copyright 2013 Netflix, Inc.
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

package com.quancheng.saluki.proxy.netty.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.lang.GroovyClassLoader;


public class GroovyCompiler {

  private static final Logger LOG = LoggerFactory.getLogger(GroovyCompiler.class);

  private static final GroovyClassLoader groovyClassLoader = new GroovyClassLoader();

  public static Class compile(String sCode) {
    LOG.warn("Compiling filter: " + sCode);
    Class groovyClass = groovyClassLoader.parseClass(sCode);
    return groovyClass;
  }


}
