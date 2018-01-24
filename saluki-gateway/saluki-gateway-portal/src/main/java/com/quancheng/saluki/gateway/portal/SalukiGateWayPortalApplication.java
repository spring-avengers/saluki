package com.quancheng.saluki.gateway.portal;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@MapperScan(value = {"com.quancheng.saluki.gateway.portal.*.dao",
    "com.quancheng.saluki.gateway.persistence.*.dao"})
@SpringBootApplication
public class SalukiGateWayPortalApplication {
  public static void main(String[] args) {
    SpringApplication.run(SalukiGateWayPortalApplication.class, args);
  }

}
