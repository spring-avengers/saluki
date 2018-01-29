package com.quancheng.saluki.proxy.netty.filter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;
import com.quancheng.saluki.proxy.config.SpringContextHolder;
import com.quancheng.saluki.proxy.netty.filter.response.ClickjackHttpResponseFilter;
import com.quancheng.saluki.proxy.netty.filter.response.HttpResponseFilter;
import com.quancheng.saluki.proxy.routerules.GroovyFilterComponent;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;


public class HttpResponseFilterChain {
  public static final List<HttpResponseFilter> filters = Lists.newLinkedList();

  private static HttpResponseFilterChain filterChain = new HttpResponseFilterChain();

  static {
    filters.add(ClickjackHttpResponseFilter.newFilter());
  }

  public static HttpResponseFilterChain responseFilterChain() {
    GroovyFilterComponent filterComponent =
        SpringContextHolder.getBean(GroovyFilterComponent.class);
    if (filterComponent.responseChanged()) {
      List<String> groovyFilters = filterComponent.loadResponseGroovyCode();
      for (String groovyFilter : groovyFilters) {
        Class<?> clazz = GroovyCompiler.compile(groovyFilter);
        try {
          HttpResponseFilter requestFilter = (HttpResponseFilter) clazz.newInstance();
          filters.add(requestFilter);
        } catch (Throwable e) {
          e.printStackTrace();
        }
      }
      Collections.sort(filters, new Comparator<HttpResponseFilter>() {
        @Override
        public int compare(HttpResponseFilter o1, HttpResponseFilter o2) {
          return o1.filterOrder() - o2.filterOrder();
        }
      });
    }
    return filterChain;
  }


  public void doFilter(HttpRequest originalRequest, HttpResponse httpResponse) {
    for (HttpResponseFilter filter : filters) {
      filter.doFilter(originalRequest, httpResponse);
    }
  }
}
