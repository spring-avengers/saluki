package com.quancheng.saluki.gateway.portal;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@ServletComponentScan
@MapperScan("com.quancheng.saluki.gateway.portal.*.dao")
@SpringBootApplication
public class SalukiGateWayPortalApplication {
  public static void main(String[] args) {
    SpringApplication.run(SalukiGateWayPortalApplication.class, args);
  }

}
