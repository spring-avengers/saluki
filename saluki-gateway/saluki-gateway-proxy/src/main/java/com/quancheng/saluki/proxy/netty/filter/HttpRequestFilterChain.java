package com.quancheng.saluki.proxy.netty.filter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;
import com.quancheng.saluki.proxy.config.SpringContextHolder;
import com.quancheng.saluki.proxy.netty.filter.request.BlackIpHttpRequesFilter;
import com.quancheng.saluki.proxy.netty.filter.request.CookieHttpRequestFilter;
import com.quancheng.saluki.proxy.netty.filter.request.DubboAdapterHttpRequestFilter;
import com.quancheng.saluki.proxy.netty.filter.request.GrpcAdapterHttpRequestFilter;
import com.quancheng.saluki.proxy.netty.filter.request.HttpRequestFilter;
import com.quancheng.saluki.proxy.netty.filter.request.RateLimitHttpRequestFilter;
import com.quancheng.saluki.proxy.netty.filter.request.ScannerHttpRequestFilter;
import com.quancheng.saluki.proxy.netty.filter.request.URLParamHttpRequestFilter;
import com.quancheng.saluki.proxy.netty.filter.request.UaHttpRequestFilter;
import com.quancheng.saluki.proxy.netty.filter.request.WriteIpHttpRequestFilter;
import com.quancheng.saluki.proxy.netty.filter.request.WriteURLHttpRequestFilter;
import com.quancheng.saluki.proxy.rule.DynamicsFilterComponent;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public class HttpRequestFilterChain {
  public static List<HttpRequestFilter> filters = Lists.newLinkedList();

  private static HttpRequestFilterChain filterChain = new HttpRequestFilterChain();

  static {
    filters.add(BlackIpHttpRequesFilter.newFilter());
    filters.add(CookieHttpRequestFilter.newFilter());
    filters.add(RateLimitHttpRequestFilter.newFilter());
    filters.add(ScannerHttpRequestFilter.newFilter());
    filters.add(UaHttpRequestFilter.newFilter());
    filters.add(WriteIpHttpRequestFilter.newFilter());
    filters.add(WriteURLHttpRequestFilter.newFilter());
    filters.add(URLParamHttpRequestFilter.newFilter());
    filters.add(DubboAdapterHttpRequestFilter.newFilter());
    filters.add(GrpcAdapterHttpRequestFilter.newFilter());
  }

  public static HttpRequestFilterChain requestFilterChain() {
    DynamicsFilterComponent filterComponent =
        SpringContextHolder.getBean(DynamicsFilterComponent.class);
    if (filterComponent.requestChanged()) {
      List<String> groovyFilters = filterComponent.loadRequestGroovyCode();
      for (String groovyFilter : groovyFilters) {
        Class<?> clazz = GroovyCompiler.compile(groovyFilter);
        try {
          HttpRequestFilter requestFilter = (HttpRequestFilter) clazz.newInstance();
          filters.add(requestFilter);
        } catch (Throwable e) {
          e.printStackTrace();
        }
      }
      Collections.sort(filters, new Comparator<HttpRequestFilter>() {
        @Override
        public int compare(HttpRequestFilter o1, HttpRequestFilter o2) {
          return o1.filterOrder() - o2.filterOrder();
        }
      });
    }
    return filterChain;
  }


  public HttpResponse doFilter(HttpRequest originalRequest, HttpObject httpObject,
      ChannelHandlerContext channelHandlerContext) {
    for (HttpRequestFilter filter : filters) {
      HttpResponse response = filter.doFilter(originalRequest, httpObject, channelHandlerContext);
      if (response != null) {
        return response;
      }
    }
    return null;
  }
}
