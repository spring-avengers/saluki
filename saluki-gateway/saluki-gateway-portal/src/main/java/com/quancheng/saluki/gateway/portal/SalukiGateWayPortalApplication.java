package com.quancheng.saluki.gateway.portal;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@MapperScan(basePackages = "com.quancheng.saluki.gateway.*.dao")
@SpringBootApplication
public class SalukiGateWayPortalApplication {
  public static void main(String[] args) {
    SpringApplication.run(SalukiGateWayPortalApplication.class, args);
  }

}
