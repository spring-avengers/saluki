/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package io.github.saluki.grpc.client;

import java.lang.reflect.Method;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;

import io.github.saluki.common.Constants;
import io.github.saluki.common.GrpcURL;
import io.github.saluki.grpc.annotation.GrpcMethodType;
import io.github.saluki.grpc.exception.RpcFrameworkException;
import io.github.saluki.grpc.util.GrpcUtil;
import io.github.saluki.utils.ReflectUtils;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;

/**
 * @author shimingliu 2016年12月14日 下午5:51:01
 * @version GrpcRequest.java, v 0.0.1 2016年12月14日 下午5:51:01 shimingliu
 */
public abstract class GrpcRequest {

  public Class<?> getResponseType() {
    return null;
  }

  public MethodDescriptor<Message, Message> getMethodDescriptor() {
    return null;
  }

  public Channel getChannel() {
    return null;
  }

  public String getServiceName() {
    return null;
  }

  public String getMethodName() {
    return null;
  }

  public Object getRequestParam() {
    return null;
  }

  public Object getResponseOberver() {
    return null;
  }

  public GrpcURL getRefUrl() {
    return null;
  }

  public int getCallType() {
    return 0;
  }

  public int getCallTimeout() {
    return 0;
  }

  public io.grpc.MethodDescriptor.MethodType getMethodType() {
    return null;
  }


  public static class Dynamic extends GrpcRequest {

    private final GrpcURL refUrl;

    private final String methodName;

    private final Channel channel;

    private final int callType;

    private final int callTimeout;

    private final MethodDescriptor<Message, Message> methodDesc;

    private final DynamicMessage message;

    public Dynamic(GrpcURL refUrl, GrpcProtocolClient.ChannelCall channelPool,
        MethodDescriptor<Message, Message> methodDesc, DynamicMessage message, int callType,
        int callTimeout) {
      super();
      this.refUrl = refUrl;
      this.methodName = refUrl.getParameter(Constants.METHOD_KEY);
      this.channel = channelPool.getChannel(refUrl);
      this.callType = callType;
      this.callTimeout = callTimeout;
      this.methodDesc = methodDesc;
      this.message = message;
    }


    @Override
    public MethodDescriptor<Message, Message> getMethodDescriptor() {
      return this.methodDesc;
    }

    @Override
    public Channel getChannel() {
      return this.channel;
    }

    @Override
    public String getServiceName() {
      return refUrl.getServiceInterface();
    }

    @Override
    public String getMethodName() {
      return this.methodName;
    }

    @Override
    public Object getRequestParam() {
      return message;
    }

    @Override
    public Class<?> getResponseType() {
      return Message.class;
    }

    @Override
    public GrpcURL getRefUrl() {
      return this.refUrl;
    }

    @Override
    public int getCallType() {
      return this.callType;
    }

    @Override
    public int getCallTimeout() {
      return this.callTimeout;
    }

    @Override
    public io.grpc.MethodDescriptor.MethodType getMethodType() {
      return methodDesc.getType();
    }


  }


  public static class Default extends GrpcRequest {


    private final GrpcURL refUrl;

    private final Channel channel;

    private final String methodName;

    private final Object[] args;

    private final int callType;

    private final int callTimeout;

    private final GrpcMethodType grpcMethodType;

    public Default(GrpcURL refUrl, GrpcProtocolClient.ChannelCall chanelPool, String methodName,
        Object[] args, int callType, int callTimeout) {
      super();
      this.refUrl = refUrl.addParameter(Constants.METHOD_KEY, methodName);
      this.channel = chanelPool.getChannel(refUrl);
      this.methodName = methodName;
      if (args.length > 2) {
        throw new IllegalArgumentException(
            "grpc not support multiple args,args is " + args + " length is " + args.length);
      } else {
        this.args = args;
      }
      this.callType = callType;
      this.callTimeout = callTimeout;
      try {
        Class<?> service = ReflectUtils.forName(this.getServiceName());
        Method method = ReflectUtils.findMethodByMethodName(service, this.getMethodName());
        grpcMethodType = method.getAnnotation(GrpcMethodType.class);
      } catch (Exception e) {
        RpcFrameworkException framworkException = new RpcFrameworkException(e);
        throw framworkException;
      }
    }

    @Override
    public Object getRequestParam() {
      return args[0];
    }

    @Override
    public MethodDescriptor<Message, Message> getMethodDescriptor() {
      return GrpcUtil.createMethodDescriptor(this.getServiceName(), methodName, grpcMethodType);
    }

    @Override
    public Class<?> getResponseType() {
      return grpcMethodType.responseType();
    }

    @Override
    public Channel getChannel() {
      return channel;
    }

    @Override
    public String getServiceName() {
      return refUrl.getServiceInterface();
    }

    @Override
    public GrpcURL getRefUrl() {
      return this.refUrl;
    }

    @Override
    public String getMethodName() {
      return this.methodName;
    }

    @Override
    public int getCallType() {
      return this.callType;
    }

    @Override
    public int getCallTimeout() {
      return this.callTimeout;
    }

    @Override
    public io.grpc.MethodDescriptor.MethodType getMethodType() {
      return this.grpcMethodType.methodType();
    }


    @Override
    public Object getResponseOberver() {
      return args[1];
    }

  }

}
