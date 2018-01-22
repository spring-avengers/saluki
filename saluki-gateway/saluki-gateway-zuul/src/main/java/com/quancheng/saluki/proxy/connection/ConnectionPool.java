package com.quancheng.saluki.proxy.connection;

import java.net.URL;

import com.quancheng.saluki.proxy.IllegalRouteException;

public interface ConnectionPool {

  Connection borrow(URL routeHost) throws IllegalRouteException;

  void release(Connection channel);

  void destroy(Connection channel);

}
