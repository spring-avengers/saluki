package com.quancheng.saluki.zuul.filter;

import com.quancheng.saluki.zuul.api.HttpRequestMessage;
import com.quancheng.saluki.zuul.api.HttpRequestHandler;

/**
 * 
 * @author liushiming
 * @version ZuulPreFilter.java, v 0.0.1 2018年1月22日 下午3:39:34 liushiming
 */
public interface ZuulPreFilter extends ZuulFilter, HttpRequestHandler {

  boolean shouldFilter(HttpRequestMessage request);

}
