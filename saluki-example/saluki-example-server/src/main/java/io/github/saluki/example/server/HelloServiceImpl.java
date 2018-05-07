package io.github.saluki.example.server;

import io.github.examples.model.hello.HelloReply;
import io.github.examples.model.hello.HelloRequest;
import io.github.examples.service.HelloService;
import io.github.saluki.boot.SalukiService;
import io.github.saluki.common.RpcContext;
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
    System.out.println(RpcContext.getContext().get("123"));
    return new StreamObserver<HelloRequest>() {


      private StringBuilder sb = new StringBuilder();

      @Override
      public void onNext(HelloRequest value) {
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
