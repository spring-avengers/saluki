package com.quancheng.saluki.netty.impl;

import com.quancheng.saluki.netty.impl.support.ConnectionState;

import io.netty.util.concurrent.Future;


abstract class ConnectionFlowStep {
  private final ProxyConnectionLogger LOG;
  private final ProxyConnection<?> connection;
  private final ConnectionState state;


  ConnectionFlowStep(ProxyConnection<?> connection, ConnectionState state) {
    super();
    this.connection = connection;
    this.state = state;
    this.LOG = connection.getLOG();
  }

  ProxyConnection<?> getConnection() {
    return connection;
  }

  ConnectionState getState() {
    return state;
  }

  boolean shouldSuppressInitialRequest() {
    return false;
  }

  boolean shouldExecuteOnEventLoop() {
    return true;
  }

  @SuppressWarnings("rawtypes")
  protected abstract Future execute();

  void onSuccess(ConnectionFlow flow) {
    flow.advance();
  }

  void read(ConnectionFlow flow, Object msg) {
    LOG.debug("Received message while in the middle of connecting: {}", msg);
  }

  @Override
  public String toString() {
    return state.toString();
  }

}
