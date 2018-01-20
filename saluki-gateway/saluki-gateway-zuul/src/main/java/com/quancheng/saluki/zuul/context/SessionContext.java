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
package com.quancheng.saluki.zuul.context;

import static org.junit.Assert.assertEquals;

import java.io.NotSerializableException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.zuul.filters.FilterError;
import com.netflix.zuul.stats.Timings;
import com.quancheng.saluki.zuul.message.http.HttpResponseMessage;
import com.quancheng.saluki.zuul.util.DeepCopy;

/**
 * Represents the context between client and origin server for the duration of the dedicated
 * connection/session between them. But we're currently still only modelling single request/response
 * pair per session.
 *
 * NOTE: Not threadsafe, and not intended to be used concurrently.
 */
public class SessionContext extends HashMap<String, Object> implements Cloneable {
  private static final long serialVersionUID = -6857896034895613998L;
  private static final int INITIAL_SIZE = DynamicPropertyFactory.getInstance()
      .getIntProperty("com.netflix.zuul.context.SessionContext.initialSize", 60).get();

  private boolean brownoutMode = false;
  private boolean shouldStopFilterProcessing = false;
  private boolean shouldSendErrorResponse = false;
  private boolean errorResponseSent = false;
  private boolean debugRouting = false;
  private boolean debugRequest = false;
  private boolean debugRequestHeadersOnly = false;
  private boolean cancelled = false;
  private Timings timings = new Timings();
  private static final String KEY_UUID = "_uuid";
  private static final String KEY_VIP = "routeVIP";
  private static final String KEY_ENDPOINT = "_endpoint";
  private static final String KEY_STATIC_RESPONSE = "_static_response";
  private static final String KEY_EVENT_PROPS = "eventProperties";
  private static final String KEY_FILTER_ERRORS = "_filter_errors";
  private static final String KEY_FILTER_EXECS = "_filter_executions";

  public SessionContext() {
    super(INITIAL_SIZE);
    put(KEY_FILTER_EXECS, new StringBuilder());
    put(KEY_EVENT_PROPS, new HashMap<String, Object>());
    put(KEY_FILTER_ERRORS, new ArrayList<FilterError>());
  }


  @Override
  public SessionContext clone() {
    return (SessionContext) super.clone();
  }

  public String getString(String key) {
    return (String) get(key);
  }


  public boolean getBoolean(String key) {
    return getBoolean(key, false);
  }


  public boolean getBoolean(String key, boolean defaultResponse) {
    Boolean b = (Boolean) get(key);
    if (b != null) {
      return b.booleanValue();
    }
    return defaultResponse;
  }

  public void set(String key) {
    put(key, Boolean.TRUE);
  }


  public void set(String key, Object value) {
    if (value != null)
      put(key, value);
    else
      remove(key);
  }

  public SessionContext copy() {
    SessionContext copy = new SessionContext();
    copy.brownoutMode = brownoutMode;
    copy.cancelled = cancelled;
    copy.shouldStopFilterProcessing = shouldStopFilterProcessing;
    copy.shouldSendErrorResponse = shouldSendErrorResponse;
    copy.errorResponseSent = errorResponseSent;
    copy.debugRouting = debugRouting;
    copy.debugRequest = debugRequest;
    copy.debugRequestHeadersOnly = debugRequestHeadersOnly;
    copy.timings = timings;

    Iterator<String> it = keySet().iterator();
    String key = it.next();
    while (key != null) {
      Object orig = get(key);
      try {
        Object copyValue = DeepCopy.copy(orig);
        if (copyValue != null) {
          copy.set(key, copyValue);
        } else {
          copy.set(key, orig);
        }
      } catch (NotSerializableException e) {
        copy.set(key, orig);
      }
      if (it.hasNext()) {
        key = it.next();
      } else {
        key = null;
      }
    }
    return copy;
  }

  public String getUUID() {
    return getString(KEY_UUID);
  }

  public void setUUID(String uuid) {
    set(KEY_UUID, uuid);
  }

  public void setStaticResponse(HttpResponseMessage response) {
    set(KEY_STATIC_RESPONSE, response);
  }

  public HttpResponseMessage getStaticResponse() {
    return (HttpResponseMessage) get(KEY_STATIC_RESPONSE);
  }

  public Throwable getError() {
    return (Throwable) get("_error");

  }

  public void setError(Throwable th) {
    put("_error", th);

  }

  public String getErrorEndpoint() {
    return (String) get("_error-endpoint");
  }

  public void setErrorEndpoint(String name) {
    put("_error-endpoint", name);
  }

  public void setDebugRouting(boolean bDebug) {
    this.debugRouting = bDebug;
  }


  public boolean debugRouting() {
    return debugRouting;
  }


  public void setDebugRequestHeadersOnly(boolean bHeadersOnly) {
    this.debugRequestHeadersOnly = bHeadersOnly;
  }


  public boolean debugRequestHeadersOnly() {
    return this.debugRequestHeadersOnly;
  }


  public void setDebugRequest(boolean bDebug) {
    this.debugRequest = bDebug;
  }


  public boolean debugRequest() {
    return this.debugRequest;
  }

  public void removeRouteHost() {
    remove("routeHost");
  }


  public void setRouteHost(URL routeHost) {
    set("routeHost", routeHost);
  }

  public URL getRouteHost() {
    return (URL) get("routeHost");
  }

  public void addFilterExecutionSummary(String name, String status, long time) {
    StringBuilder sb = getFilterExecutionSummary();
    if (sb.length() > 0)
      sb.append(", ");
    sb.append(name).append('[').append(status).append(']').append('[').append(time).append("ms]");
  }


  public StringBuilder getFilterExecutionSummary() {
    return (StringBuilder) get(KEY_FILTER_EXECS);
  }

  public boolean shouldSendErrorResponse() {
    return this.shouldSendErrorResponse;
  }


  public void setShouldSendErrorResponse(boolean should) {
    this.shouldSendErrorResponse = should;
  }


  public boolean errorResponseSent() {
    return this.errorResponseSent;
  }

  public void setErrorResponseSent(boolean should) {
    this.errorResponseSent = should;
  }


  public boolean isInBrownoutMode() {
    return brownoutMode;
  }

  public void setInBrownoutMode() {
    this.brownoutMode = true;
  }


  public void stopFilterProcessing() {
    shouldStopFilterProcessing = true;
  }

  public boolean shouldStopFilterProcessing() {
    return shouldStopFilterProcessing;
  }


  public String getRouteVIP() {
    return (String) get(KEY_VIP);
  }


  public void setRouteVIP(String sVip) {
    set(KEY_VIP, sVip);
  }

  public void setEndpoint(String endpoint) {
    put(KEY_ENDPOINT, endpoint);
  }

  public String getEndpoint() {
    return (String) get(KEY_ENDPOINT);
  }

  public void setEventProperty(String key, Object value) {
    getEventProperties().put(key, value);
  }

  public Map<String, Object> getEventProperties() {
    return (Map<String, Object>) this.get(KEY_EVENT_PROPS);
  }

  public List<FilterError> getFilterErrors() {
    return (List<FilterError>) get(KEY_FILTER_ERRORS);
  }

  public Timings getTimings() {
    return timings;
  }

  public void setOriginReportedDuration(int duration) {
    put("_originReportedDuration", duration);
  }

  public int getOriginReportedDuration() {
    Object value = get("_originReportedDuration");
    if (value != null) {
      return (Integer) value;
    }
    return -1;
  }

  public boolean isCancelled() {
    return cancelled;
  }

  public void cancel() {
    this.cancelled = true;
  }

  @RunWith(MockitoJUnitRunner.class)
  public static class UnitTest {
    @Test
    public void testBoolean() {
      SessionContext context = new SessionContext();
      assertEquals(context.getBoolean("boolean_test"), Boolean.FALSE);
      assertEquals(context.getBoolean("boolean_test", true), true);

    }
  }
}
