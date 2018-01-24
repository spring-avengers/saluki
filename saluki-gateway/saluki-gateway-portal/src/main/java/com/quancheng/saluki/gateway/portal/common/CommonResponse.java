package com.quancheng.saluki.gateway.portal.common;

import java.util.HashMap;
import java.util.Map;

public class CommonResponse extends HashMap<String, Object> {
  private static final long serialVersionUID = 1L;

  public static int SUCCESS = 1;
  public static int ERROR = 0;

  public CommonResponse() {
    put("code", 0);
    put("msg", "操作成功");
  }

  public static CommonResponse error() {
    return error(1, "操作失败");
  }

  public static CommonResponse error(String msg) {
    return error(500, msg);
  }

  public static CommonResponse error(int code, String msg) {
    CommonResponse r = new CommonResponse();
    r.put("code", code);
    r.put("msg", msg);
    return r;
  }

  public static CommonResponse ok(String msg) {
    CommonResponse r = new CommonResponse();
    r.put("msg", msg);
    return r;
  }

  public static CommonResponse ok(Map<String, Object> map) {
    CommonResponse r = new CommonResponse();
    r.putAll(map);
    return r;
  }

  public static CommonResponse ok() {
    return new CommonResponse();
  }

  @Override
  public CommonResponse put(String key, Object value) {
    super.put(key, value);
    return this;
  }
}
