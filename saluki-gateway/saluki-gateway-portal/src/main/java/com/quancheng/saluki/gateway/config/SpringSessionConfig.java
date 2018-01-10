package com.quancheng.saluki.gateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(prefix = "bootdo", name = "spring-session-open", havingValue = "true")
public class SpringSessionConfig {

}
