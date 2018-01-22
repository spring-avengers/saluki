package com.quancheng.saluki.zuul.filter;

import com.quancheng.saluki.zuul.api.HttpRequestMessage;
import com.quancheng.saluki.zuul.api.HttpResponseMessage;
import com.quancheng.saluki.zuul.api.HttpResponseHandler;

/**
 * 
 * @author liushiming
 * @version ZuulPostFilter.java, v 0.0.1 2018年1月22日 下午3:39:26 liushiming
 */
public interface ZuulPostFilter extends ZuulFilter, HttpResponseHandler {

  boolean shouldFilter(HttpRequestMessage request, HttpResponseMessage response);

}
