package io.github.saluki.monitor.rest;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import io.github.saluki.boot.SalukiReference;
import io.github.saluki.boot.SalukiService;
import io.github.saluki.boot.autoconfigure.GrpcProperties;
import io.github.saluki.boot.common.GrpcAop;
import io.github.saluki.monitor.dao.domain.GrpcServiceTestModel;
import io.github.saluki.monitor.jaket.Jaket;
import io.github.saluki.monitor.jaket.model.GenericInvokeMetadata;
import io.github.saluki.monitor.jaket.model.MetadataType;
import io.github.saluki.monitor.jaket.model.MethodDefinition;
import io.github.saluki.monitor.jaket.model.ServiceDefinition;
import io.github.saluki.monitor.jaket.util.GenericInvokeUtils;
import io.github.saluki.common.RpcContext;
import io.github.saluki.grpc.service.GenericService;
import io.github.saluki.utils.ReflectUtils;

@RestController
@RequestMapping("service")
public class TestController {

  private final Gson gson = new Gson();

  @Autowired
  private GrpcProperties prop;

  @Autowired
  private AbstractApplicationContext applicationContext;

  @SalukiReference(group = "default", version = "1.0.0")
  private GenericService genricService;

  @RequestMapping(value = "getAllMethod", method = RequestMethod.GET)
  public List<MethodDefinition> getAllMethod(
      @RequestParam(value = "service", required = true) String service)
      throws ClassNotFoundException {
    try {
      Class<?> clazz = ReflectUtils.name2class(service);
      ServiceDefinition sd = Jaket.build(clazz);
      return sd.getMethods();
    } catch (ClassNotFoundException e) {
      throw e;
    }
  }

  @RequestMapping(value = "getAllService", method = RequestMethod.GET)
  public List<Map<String, Object>> getAllService() throws Exception {
    List<Map<String, Object>> services = Lists.newArrayList();
    try {
      Collection<Object> instances = getTypedBeansWithAnnotation(SalukiService.class);
      for (Object instance : instances) {
        Object target = GrpcAop.getTarget(instance);
        Class<?>[] interfaces = ClassUtils.getAllInterfacesForClass(target.getClass());
        Class<?> clzz = interfaces[0];
        Map<String, Object> serviceMap = Maps.newHashMap();
        serviceMap.put("simpleName", clzz.getSimpleName());
        serviceMap.put("name", clzz.getName());
        ServiceDefinition sd = Jaket.build(clzz);
        List<MethodDefinition> methodDefines = sd.getMethods();
        List<String> functions = Lists.newArrayList();
        for (MethodDefinition methodDefine : methodDefines) {
          functions.add(methodDefine.getName());
        }
        serviceMap.put("functions", functions);
        services.add(serviceMap);
      }
      return services;
    } catch (Exception e) {
      throw e;
    }
  }

  @RequestMapping(value = "getMethod", method = RequestMethod.GET)
  public GenericInvokeMetadata getMethod(
      @RequestParam(value = "service", required = true) String service,
      @RequestParam(value = "method", required = true) String method)
      throws ClassNotFoundException {
    try {
      Class<?> clazz = ReflectUtils.name2class(service);
      ServiceDefinition serviceMeta = Jaket.build(clazz);
      if (!method.contains("~")) {
        for (MethodDefinition methodDef : serviceMeta.getMethods()) {
          if (methodDef.getName().equals(method)) {
            method = method + "~" + methodDef.getParameterTypes()[0];
            break;
          }
        }
      }
      GenericInvokeMetadata meta = GenericInvokeUtils.getGenericInvokeMetadata(serviceMeta, method,
          MetadataType.DEFAULT_VALUE);
      return meta;
    } catch (ClassNotFoundException e) {
      throw e;
    }
  }

  @RequestMapping(value = "testLocal", method = RequestMethod.POST)
  public Object testLocalService(
      @RequestParam(value = "routerRule", required = true) String routerRule,
      @RequestBody GrpcServiceTestModel model) throws ClassNotFoundException {
    try {
      Class<?> requestClass = ReflectUtils.name2class(model.getParameterType());
      Object request = gson.fromJson(model.getParameter(), requestClass);
      Object[] args = new Object[] {request};
      if (StringUtils.isNotBlank(routerRule)) {
        RpcContext.getContext().setAttachment("routerRule", routerRule);
      } else {
        RpcContext.getContext().removeAttachment("routerRule");
      }
      Object reply =
          genricService.$invoke(model.getService(), getAnnotation(model.getService()).getLeft(),
              getAnnotation(model.getService()).getRight(), model.getMethod(), args);
      return reply;
    } catch (ClassNotFoundException e) {
      throw e;
    }
  }

  @RequestMapping(value = "test", method = RequestMethod.POST)
  public Object testService(@RequestBody GrpcServiceTestModel model) throws ClassNotFoundException {
    try {
      Class<?> requestClass = ReflectUtils.name2class(model.getParameterType());
      Object request = gson.fromJson(model.getParameter(), requestClass);
      Object[] args = new Object[] {request};
      Object reply =
          genricService.$invoke(model.getService(), getAnnotation(model.getService()).getLeft(),
              getAnnotation(model.getService()).getRight(), model.getMethod(), args);
      return reply;
    } catch (ClassNotFoundException e) {
      throw e;
    }
  }

  private Pair<String, String> getAnnotation(String className) throws ClassNotFoundException {
    Class<?> beanType = ReflectUtils.name2class(className);
    Map<String, ?> beanMap = applicationContext.getBeansOfType(beanType);
    String group = null;
    String version = null;
    for (Map.Entry<String, ?> entry : beanMap.entrySet()) {
      Object obj = entry.getValue();
      SalukiService salukiAnnotation = obj.getClass().getAnnotation(SalukiService.class);
      group = salukiAnnotation.group();
      version = salukiAnnotation.version();
      break;
    }
    if (StringUtils.isBlank(group) || StringUtils.isBlank(version)) {
      group = prop.getGroup();
      version = prop.getVersion();
    }
    return new ImmutablePair<String, String>(group, version);

  }

  private Collection<Object> getTypedBeansWithAnnotation(Class<? extends Annotation> annotationType)
      throws Exception {
    return Stream.of(applicationContext.getBeanNamesForAnnotation(annotationType)).filter(name -> {
      BeanDefinition beanDefinition = applicationContext.getBeanFactory().getBeanDefinition(name);
      if (beanDefinition.getSource() instanceof StandardMethodMetadata) {
        StandardMethodMetadata metadata = (StandardMethodMetadata) beanDefinition.getSource();
        return metadata.isAnnotated(annotationType.getName());
      }
      return null != applicationContext.getBeanFactory().findAnnotationOnBean(name, annotationType);
    }).map(name -> applicationContext.getBeanFactory().getBean(name)).collect(Collectors.toList());

  }
}
