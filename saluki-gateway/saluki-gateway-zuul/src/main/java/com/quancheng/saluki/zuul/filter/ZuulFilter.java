package com.quancheng.saluki.zuul.filter;

/**
 */
public interface ZuulFilter extends Ordered, Comparable<ZuulFilter> {

  /**
   * @return
   */
  String type();

}
