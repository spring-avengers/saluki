package com.quancheng.saluki.proxy.protocol.grpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;

public class ServiceResolver {
  private static final Logger logger = LoggerFactory.getLogger(ServiceResolver.class);
  private final ImmutableList<FileDescriptor> fileDescriptors;

  public static ServiceResolver fromFileDescriptorSet(FileDescriptorSet descriptorSet) {
    ImmutableMap<String, FileDescriptorProto> descriptorProtoIndex =
        computeDescriptorProtoIndex(descriptorSet);
    Map<String, FileDescriptor> descriptorCache = new HashMap<>();

    ImmutableList.Builder<FileDescriptor> result = ImmutableList.builder();
    for (FileDescriptorProto descriptorProto : descriptorSet.getFileList()) {
      try {
        result.add(descriptorFromProto(descriptorProto, descriptorProtoIndex, descriptorCache));
      } catch (DescriptorValidationException e) {
        logger.warn("Skipped descriptor " + descriptorProto.getName() + " due to error", e);
        continue;
      }
    }
    return new ServiceResolver(result.build());
  }

  public Iterable<ServiceDescriptor> listServices() {
    ArrayList<ServiceDescriptor> serviceDescriptors = new ArrayList<ServiceDescriptor>();
    for (FileDescriptor fileDescriptor : fileDescriptors) {
      serviceDescriptors.addAll(fileDescriptor.getServices());
    }
    return serviceDescriptors;
  }

  private ServiceResolver(Iterable<FileDescriptor> fileDescriptors) {
    this.fileDescriptors = ImmutableList.copyOf(fileDescriptors);
  }


  public MethodDescriptor resolveServiceMethod(ProtoMethodName method) {
    return resolveServiceMethod(method.getServiceName(), method.getMethodName(),
        method.getPackageName());
  }

  private MethodDescriptor resolveServiceMethod(String serviceName, String methodName,
      String packageName) {
    ServiceDescriptor service = findService(serviceName, packageName);
    MethodDescriptor method = service.findMethodByName(methodName);
    if (method == null) {
      throw new IllegalArgumentException(
          "Unable to find method " + methodName + " in service " + serviceName);
    }
    return method;
  }

  private ServiceDescriptor findService(String serviceName, String packageName) {
    for (FileDescriptor fileDescriptor : fileDescriptors) {
      if (!fileDescriptor.getPackage().equals(packageName)) {
        continue;
      }

      ServiceDescriptor serviceDescriptor = fileDescriptor.findServiceByName(serviceName);
      if (serviceDescriptor != null) {
        return serviceDescriptor;
      }
    }
    throw new IllegalArgumentException("Unable to find service with name: " + serviceName);
  }


  private static ImmutableMap<String, FileDescriptorProto> computeDescriptorProtoIndex(
      FileDescriptorSet fileDescriptorSet) {
    ImmutableMap.Builder<String, FileDescriptorProto> resultBuilder = ImmutableMap.builder();
    for (FileDescriptorProto descriptorProto : fileDescriptorSet.getFileList()) {
      resultBuilder.put(descriptorProto.getName(), descriptorProto);
    }
    return resultBuilder.build();
  }


  private static FileDescriptor descriptorFromProto(FileDescriptorProto descriptorProto,
      ImmutableMap<String, FileDescriptorProto> descriptorProtoIndex,
      Map<String, FileDescriptor> descriptorCache) throws DescriptorValidationException {
    String descritorName = descriptorProto.getName();
    if (descriptorCache.containsKey(descritorName)) {
      return descriptorCache.get(descritorName);
    }

    ImmutableList.Builder<FileDescriptor> dependencies = ImmutableList.builder();
    for (String dependencyName : descriptorProto.getDependencyList()) {
      if (!descriptorProtoIndex.containsKey(dependencyName)) {
        throw new IllegalArgumentException("Could not find dependency: " + dependencyName);
      }
      FileDescriptorProto dependencyProto = descriptorProtoIndex.get(dependencyName);
      dependencies.add(descriptorFromProto(dependencyProto, descriptorProtoIndex, descriptorCache));
    }

    FileDescriptor[] empty = new FileDescriptor[0];
    return FileDescriptor.buildFrom(descriptorProto, dependencies.build().toArray(empty));
  }
}
