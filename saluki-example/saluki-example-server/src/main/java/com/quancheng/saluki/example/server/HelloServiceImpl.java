package com.quancheng.saluki.example.server;

import com.quancheng.examples.model.hello.HelloReply;
import com.quancheng.examples.model.hello.HelloRequest;
import com.quancheng.examples.service.HelloService;
import com.quancheng.saluki.boot.SalukiService;
import com.quancheng.saluki.core.common.RpcContext;

import io.grpc.stub.StreamObserver;

@SalukiService
public class HelloServiceImpl implements HelloService {

  @Override
  public HelloReply sayHello(HelloRequest request) {
    HelloReply reply = new HelloReply();
    reply.setMessage(request.getName());
    System.out.println(RpcContext.getContext().get("123"));
    // int registryPort = 0;
    // Preconditions.checkState(registryPort != 0, "RegistryPort can not be null", registryPort);
    return reply;
  }


  @Override
  public void sayHelloServerStream(HelloRequest hellorequest,
      StreamObserver<HelloReply> responseObserver) {
    System.out.println(RpcContext.getContext().get("123"));
    try {
      for (int i = 0; i < 10; i++) {
        HelloReply reply = new HelloReply();
        reply.setMessage(hellorequest.getName());
        responseObserver.onNext(reply);
      }
    } catch (Exception e) {
      responseObserver.onError(e);
    }
    responseObserver.onCompleted();
  }



  @Override
  public StreamObserver<HelloRequest> sayHelloClientStream(
      StreamObserver<HelloReply> responseObserver) {
    return new StreamObserver<HelloRequest>() {


      private StringBuilder sb = new StringBuilder();

      @Override
      public void onNext(HelloRequest value) {
        System.out.println(RpcContext.getContext().get("123"));
        sb.append(value.getName() + ", ");
      }

      @Override
      public void onError(Throwable t) {
        t.printStackTrace();
      }

      @Override
      public void onCompleted() {
        HelloReply reply = new HelloReply();
        reply.setMessage(sb.toString());
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
      }

    };


  }



  @Override
  public StreamObserver<HelloRequest> sayHelloBidiStream(
      StreamObserver<HelloReply> responseObserver) {
    System.out.println(RpcContext.getContext().get("123"));
    return new StreamObserver<HelloRequest>() {

      private int requestCount;

      @Override
      public void onNext(HelloRequest value) {
        requestCount++;
        HelloReply reply = new HelloReply();
        reply.setMessage(value.getName());
        responseObserver.onNext(reply);
      }

      @Override
      public void onError(Throwable t) {
        t.printStackTrace();
      }

      @Override
      public void onCompleted() {
        System.out.println(requestCount);
        responseObserver.onCompleted();
      }

    };
  }

}
