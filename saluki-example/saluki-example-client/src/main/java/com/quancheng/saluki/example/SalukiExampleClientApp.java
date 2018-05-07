package io.github.saluki.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.examples.model.hello.HelloRequest;
import io.github.examples.service.HelloService;
import io.github.saluki.boot.SalukiReference;

@SpringBootApplication
public class SalukiExampleClientApp implements CommandLineRunner {

  @SalukiReference
  private HelloService helloService;

  private int threads = 50;
  private int rounds = 10000;

  public static void main(String[] args) {
    SpringApplication.run(SalukiExampleClientApp.class, args);
  }

  @Override
  public void run(String... arg0) throws Exception {
    // Thread.sleep(1000 * 10);
    // long start = System.currentTimeMillis();
    // Thread[] ts = new Thread[threads];
    // for (int i = 0; i < threads; i++) {
    // ts[i] = new Thread(new Runnable() {
    //
    // @Override
    // public void run() {
    // for (int i = 0; i < rounds; i++) {
    // HelloRequest request = new HelloRequest();
    // request.setName("liushiming");
    // helloService.sayHello(request);
    // }
    // }
    //
    // });
    // }
    // for (int i = 0; i < threads; i++) {
    // ts[i].start();
    // }
    // for (int i = 0; i < threads; i++) {
    // ts[i].join();
    // }
    // System.out.println(
    // " qps=" + ((long) threads * rounds * 1000 / (System.currentTimeMillis() - start + 1)));
    // double ms = (System.currentTimeMillis() - start + 1) / ((double) threads * rounds);
    // System.out.println("ms=" + ms);
  }


}
