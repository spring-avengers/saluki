package io.github.saluki.example.client;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saluki.example.model.First;
import com.saluki.example.model.Second;

import io.github.examples.model.hello.HelloReply;
import io.github.examples.model.hello.HelloRequest;
import io.github.examples.service.HelloService;
import io.github.saluki.boot.SalukiReference;
import io.github.saluki.common.RpcContext;
import io.grpc.stub.StreamObserver;

@RestController
@RequestMapping("/proxy")
public class ProxyServiceController {


  @SalukiReference(retries = 3, validatorGroups = {First.class, Second.class})
  private HelloService helloService;


  @RequestMapping("/hello")
  public HelloReply hello(@RequestParam(value = "name", required = false) String name) {
    return call(name);
  }


  @RequestMapping("/serverstream")
  public HelloReply serverstream(@RequestParam(value = "name", required = false) String name) {
    HelloRequest request = new HelloRequest();
    request.setName(name);
    helloService.sayHelloServerStream(request, responseObserver());
    return null;
  }

  @RequestMapping("/clientstream")
  public HelloReply clientstream(@RequestParam(value = "name", required = false) String name) {
    HelloRequest request = new HelloRequest();
    request.setName(name);
    RpcContext.getContext().set("123", "123");
    StreamObserver<io.github.examples.model.hello.HelloRequest> requestObserver =
        helloService.sayHelloClientStream(responseObserver());
    try {
      for (int i = 0; i < 10; i++) {
        requestObserver.onNext(request);
      }
    } catch (Exception e) {
      requestObserver.onError(e);
    }
    requestObserver.onCompleted();
    return null;
  }

  @RequestMapping("/bodistream")
  public HelloReply bodistream(@RequestParam(value = "name", required = false) String name) {
    HelloRequest request = new HelloRequest();
    request.setName(name);
    StreamObserver<io.github.examples.model.hello.HelloRequest> requestObserver =
        helloService.sayHelloBidiStream(responseObserver());
    try {
      for (int i = 0; i < 10; i++) {
        requestObserver.onNext(request);
      }
    } catch (Exception e) {
      requestObserver.onError(e);
    }
    requestObserver.onCompleted();
    return null;
  }


  private StreamObserver<io.github.examples.model.hello.HelloReply> responseObserver() {
    StreamObserver<io.github.examples.model.hello.HelloReply> responseObserver =
        new StreamObserver<io.github.examples.model.hello.HelloReply>() {
          @Override
          public void onNext(io.github.examples.model.hello.HelloReply summary) {
            System.out.println(summary.getMessage());
          }

          @Override
          public void onError(Throwable t) {
            t.printStackTrace();
          }

          @Override
          public void onCompleted() {

        }
        };
    return responseObserver;
  }



  private HelloReply call(final String name) {
    RpcContext.getContext().set("123", "123");
    HelloRequest request = new HelloRequest();
    request.setName(name);
    HelloReply reply = helloService.sayHello(request);
    // System.out.println(RpcContext.getContext().get("123"));
    return reply;
  }


}
