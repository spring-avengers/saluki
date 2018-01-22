package com.quancheng.saluki.zuul.filter;

import com.quancheng.saluki.zuul.api.HttpRequestMessage;

/**
 * @author liushiming
 * @version AbstractZuulPreFilter.java, v 0.0.1 2018年1月22日 下午3:39:18 liushiming
 */
public abstract class AbstractZuulPreFilter implements ZuulPreFilter {

  private final String type = "pre";
  private final int order;

  public AbstractZuulPreFilter(int order) {
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
  public boolean shouldFilter(HttpRequestMessage request) {
    return true;
  }

  @Override
  public int compareTo(ZuulFilter o) {
    return order - o.order();
  }

}
