package com.quancheng.saluki.zuul.filter;

import com.quancheng.saluki.zuul.api.HttpRequestMessage;
import com.quancheng.saluki.zuul.api.HttpResponseMessage;

/**
 * 
 * @author liushiming
 * @version AbstractZuulPostFilter.java, v 0.0.1 2018年1月22日 下午3:39:11 liushiming
 */
public abstract class AbstractZuulPostFilter implements ZuulPostFilter {

  private final String type;
  private final int order;

  public AbstractZuulPostFilter(String type, int order) {
    this.type = type;
    this.order = order;
  }

  @Override
  public String type() {
    return type;
  }

  @Override
  public int order() {
    return order;
  }

  @Override
  public boolean shouldFilter(HttpRequestMessage request, HttpResponseMessage response) {
    return true;
  }

  @Override
  public int compareTo(ZuulFilter o) {
    return order - o.order();
  }

}
