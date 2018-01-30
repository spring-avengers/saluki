package com.quancheng.saluki.proxy.netty.filter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;
import com.quancheng.saluki.proxy.config.SpringContextHolder;
import com.quancheng.saluki.proxy.netty.filter.request.BlackIpHttpRequesFilter;
import com.quancheng.saluki.proxy.netty.filter.request.BlackURLHttpRequestFilter;
import com.quancheng.saluki.proxy.netty.filter.request.BlackCookieHttpRequestFilter;
import com.quancheng.saluki.proxy.netty.filter.request.DubboAdapterHttpRequestFilter;
import com.quancheng.saluki.proxy.netty.filter.request.GrpcAdapterHttpRequestFilter;
import com.quancheng.saluki.proxy.netty.filter.request.HttpRequestFilter;
import com.quancheng.saluki.proxy.netty.filter.request.RateLimitHttpRequestFilter;
import com.quancheng.saluki.proxy.netty.filter.request.ScannerHttpRequestFilter;
import com.quancheng.saluki.proxy.netty.filter.request.URLParamHttpRequestFilter;
import com.quancheng.saluki.proxy.netty.filter.request.BlackUaHttpRequestFilter;
import com.quancheng.saluki.proxy.routerules.GroovyFilterComponent;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public class HttpRequestFilterChain {
  public static List<HttpRequestFilter> filters = Lists.newLinkedList();

  private static HttpRequestFilterChain filterChain = new HttpRequestFilterChain();

  static {
    filters.add(BlackIpHttpRequesFilter.newFilter());
    filters.add(BlackCookieHttpRequestFilter.newFilter());
    filters.add(RateLimitHttpRequestFilter.newFilter());
    filters.add(ScannerHttpRequestFilter.newFilter());
    filters.add(BlackUaHttpRequestFilter.newFilter());
    filters.add(BlackURLHttpRequestFilter.newFilter());
    filters.add(URLParamHttpRequestFilter.newFilter());
    filters.add(DubboAdapterHttpRequestFilter.newFilter());
    filters.add(GrpcAdapterHttpRequestFilter.newFilter());
  }

  public static HttpRequestFilterChain requestFilterChain() {
    GroovyFilterComponent filterComponent =
        SpringContextHolder.getBean(GroovyFilterComponent.class);
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
      // 如果一个filter有返回值，将会中断下一个filter，这里需要注意filter的顺序，默认grpc->dubbo
      if (response != null) {
        return response;
      }
    }
    return null;
  }
}
