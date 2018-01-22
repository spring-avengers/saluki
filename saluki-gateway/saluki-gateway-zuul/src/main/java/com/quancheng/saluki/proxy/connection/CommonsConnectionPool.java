package com.quancheng.saluki.proxy.connection;

import java.net.URL;

import com.quancheng.saluki.proxy.IllegalRouteException;

public class CommonsConnectionPool implements ConnectionPool {

  @Override
  public Connection borrow(URL routeHost) throws IllegalRouteException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void release(Connection channel) {
    // TODO Auto-generated method stub

  }

  @Override
  public void destroy(Connection channel) {
    // TODO Auto-generated method stub

  }


}
