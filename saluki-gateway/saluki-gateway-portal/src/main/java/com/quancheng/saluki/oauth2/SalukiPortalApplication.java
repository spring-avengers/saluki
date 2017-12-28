package com.quancheng.saluki.oauth2;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@ServletComponentScan
@MapperScan("com.quancheng.saluki.oauth2.*.dao")
@SpringBootApplication
public class SalukiPortalApplication {
  public static void main(String[] args) {
    SpringApplication.run(SalukiPortalApplication.class, args);
  }

}
