
package com.quancheng.saluki.proxy.protocol.grpc;

import com.google.common.base.Joiner;

public class ProtoMethodName {
  private final String packageName;
  private final String serviceName;
  private final String methodName;

  public static ProtoMethodName parseFullGrpcMethodName(String fullMethodName) {
    String fullServiceName = io.grpc.MethodDescriptor.extractFullServiceName(fullMethodName);
    if (fullServiceName == null) {
      throw new IllegalArgumentException("Could not extract full service from " + fullMethodName);
    }

    int serviceLength = fullServiceName.length();
    if (serviceLength + 1 >= fullMethodName.length()
        || fullMethodName.charAt(serviceLength) != '/') {
      throw new IllegalArgumentException("Could not extract method name from " + fullMethodName);
    }
    String methodName = fullMethodName.substring(fullServiceName.length() + 1);

    int index = fullServiceName.lastIndexOf('.');
    if (index == -1) {
      throw new IllegalArgumentException("Could not extract package name from " + fullServiceName);
    }
    String packageName = fullServiceName.substring(0, index);

    if (index + 1 >= fullServiceName.length() || fullServiceName.charAt(index) != '.') {
      throw new IllegalArgumentException("Could not extract service from " + fullServiceName);
    }
    String serviceName = fullServiceName.substring(index + 1);

    return new ProtoMethodName(packageName, serviceName, methodName);
  }

  private ProtoMethodName(String packageName, String serviceName, String methodName) {
    this.packageName = packageName;
    this.serviceName = serviceName;
    this.methodName = methodName;
  }

  /** Returns the full package name of the method. */
  public String getPackageName() {
    return packageName;
  }

  /** Returns the (unqualified) service name of the method. */
  public String getServiceName() {
    return serviceName;
  }

  /** Returns the fully qualified service name of the method. */
  public String getFullServiceName() {
    return Joiner.on(".").join(packageName, serviceName);
  }

  /** Returns the (unqualified) method name of the method. */
  public String getMethodName() {
    return methodName;
  }
}
