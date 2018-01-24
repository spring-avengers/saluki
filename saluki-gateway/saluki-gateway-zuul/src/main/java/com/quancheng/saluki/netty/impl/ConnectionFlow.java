package com.quancheng.saluki.netty.impl;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.quancheng.saluki.netty.impl.support.ConnectionState;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;


class ConnectionFlow {
  private Queue<ConnectionFlowStep> steps = new ConcurrentLinkedQueue<ConnectionFlowStep>();

  private final ClientToProxyConnection clientConnection;
  private final ProxyToServerConnection serverConnection;
  private final Object connectLock;
  private volatile ConnectionFlowStep currentStep;
  private volatile boolean suppressInitialRequest = false;



  ConnectionFlow(ClientToProxyConnection clientConnection, ProxyToServerConnection serverConnection,
      Object connectLock) {
    super();
    this.clientConnection = clientConnection;
    this.serverConnection = serverConnection;
    this.connectLock = connectLock;
  }

  ConnectionFlow then(ConnectionFlowStep step) {
    steps.add(step);
    return this;
  }

  void read(Object msg) {
    if (this.currentStep != null) {
      this.currentStep.read(this, msg);
    }
  }

  void start() {
    clientConnection.serverConnectionFlowStarted(serverConnection);
    advance();
  }


  void advance() {
    currentStep = steps.poll();
    if (currentStep == null) {
      succeed();
    } else {
      processCurrentStep();
    }
  }

  private void processCurrentStep() {
    final ProxyConnection<?> connection = currentStep.getConnection();
    final ProxyConnectionLogger LOG = connection.getLOG();
    LOG.debug("Processing connection flow step: {}", currentStep);
    connection.become(currentStep.getState());
    suppressInitialRequest = suppressInitialRequest || currentStep.shouldSuppressInitialRequest();

    if (currentStep.shouldExecuteOnEventLoop()) {
      connection.ctx.executor().submit(new Runnable() {
        @Override
        public void run() {
          doProcessCurrentStep(LOG);
        }
      });
    } else {
      doProcessCurrentStep(LOG);
    }
  }

  @SuppressWarnings("unchecked")
  private void doProcessCurrentStep(final ProxyConnectionLogger LOG) {
    currentStep.execute().addListener(new GenericFutureListener<Future<?>>() {
      public void operationComplete(io.netty.util.concurrent.Future<?> future) throws Exception {
        synchronized (connectLock) {
          if (future.isSuccess()) {
            LOG.debug("ConnectionFlowStep succeeded");
            currentStep.onSuccess(ConnectionFlow.this);
          } else {
            LOG.debug("ConnectionFlowStep failed", future.cause());
            fail(future.cause());
          }
        }
      };
    });
  }


  void succeed() {
    synchronized (connectLock) {
      serverConnection.getLOG().debug("Connection flow completed successfully: {}", currentStep);
      serverConnection.connectionSucceeded(!suppressInitialRequest);
      notifyThreadsWaitingForConnection();
    }
  }


  @SuppressWarnings({"unchecked", "rawtypes"})
  void fail(final Throwable cause) {
    final ConnectionState lastStateBeforeFailure = serverConnection.getCurrentState();
    serverConnection.disconnect().addListener(new GenericFutureListener() {

      @Override
      public void operationComplete(Future future) throws Exception {
        synchronized (connectLock) {
          if (!clientConnection.serverConnectionFailed(serverConnection, lastStateBeforeFailure,
              cause)) {
            serverConnection.become(ConnectionState.DISCONNECTED);
            notifyThreadsWaitingForConnection();
          }
        }
      }


    });
  }


  void fail() {
    fail(null);
  }


  private void notifyThreadsWaitingForConnection() {
    connectLock.notifyAll();
  }

}
