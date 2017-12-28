package com.quancheng.saluki.oauth2.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(prefix = "bootdo", name = "spring-session-open", havingValue = "true")
public class SpringSessionConfig {

}
