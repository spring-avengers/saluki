package com.quancheng.saluki.proxy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;


@Service
@Lazy(false)
public class SpringContextHolder implements ApplicationContextAware, DisposableBean {

  private static final Logger log = LoggerFactory.getLogger(SpringContextHolder.class);
  private static ApplicationContext applicationContext = null;


  public static ApplicationContext getApplicationContext() {
    return applicationContext;
  }


  @SuppressWarnings("unchecked")
  public static <T> T getBean(String name) {
    return (T) applicationContext.getBean(name);
  }


  public static <T> T getBean(Class<T> requiredType) {
    try {
      return applicationContext.getBean(requiredType);
    } catch (NoSuchBeanDefinitionException exception) {
      log.debug("not found bean in spring cotext", exception);
      return null;
    }

  }


  public static void clearHolder() {
    applicationContext = null;
  }


  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    SpringContextHolder.applicationContext = applicationContext;
  }

  @Override
  public void destroy() throws Exception {
    SpringContextHolder.clearHolder();
  }

}
