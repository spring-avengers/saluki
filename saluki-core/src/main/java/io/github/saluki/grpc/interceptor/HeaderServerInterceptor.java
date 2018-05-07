/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package io.github.saluki.grpc.interceptor;

import java.net.InetSocketAddress;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;
import io.github.saluki.common.Constants;
import io.github.saluki.common.RpcContext;
import io.github.saluki.grpc.util.GrpcUtil;
import io.github.saluki.grpc.util.SerializerUtil;

import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

/**
 * @author shimingliu 2016年12月14日 下午10:29:37
 * @version HeaderServerInterceptor.java, v 0.0.1 2016年12月14日 下午10:29:37 shimingliu
 */
public class HeaderServerInterceptor implements ServerInterceptor {

  private static final Logger log = LoggerFactory.getLogger(HeaderServerInterceptor.class);


  public static ServerInterceptor instance() {
    return new HeaderServerInterceptor();
  }

  private HeaderServerInterceptor() {}

  @Override
  public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
      final Metadata headers, ServerCallHandler<ReqT, RespT> next) {
    final class ServerCallHandlerWrap implements ServerCallHandler<ReqT, RespT> {

      private final ServerCallHandler<ReqT, RespT> handler;

      public ServerCallHandlerWrap(ServerCallHandler<ReqT, RespT> handler) {
        this.handler = handler;
      }

      @Override
      public Listener<ReqT> startCall(ServerCall<ReqT, RespT> call, Metadata headers) {
        try {
          contextCopy(call, headers);
          return handler.startCall(call, headers);
        } finally {
          RpcContext.removeContext();
        }
      }

    }

    final class ListenerWrap extends Listener<ReqT> {

      private final Listener<ReqT> listener;

      public ListenerWrap(Listener<ReqT> listener) {
        this.listener = listener;
      }

      @Override
      public void onMessage(ReqT message) {
        listener.onMessage(message);
      }

      @Override
      public void onHalfClose() {
        try {
          contextCopy(call, headers);
          listener.onHalfClose();
        } finally {
          RpcContext.removeContext();
        }
      }

      @Override
      public void onCancel() {
        listener.onCancel();
      }

      @Override
      public void onComplete() {
        listener.onComplete();
      }

      @Override
      public void onReady() {
        listener.onReady();
      }

    };
    return new ListenerWrap(new ServerCallHandlerWrap(next).startCall(call, headers));
  }


  private void contextCopy(ServerCall<?, ?> call, final Metadata headers) {
    copyMetadataToThreadLocal(headers);
    InetSocketAddress remoteAddress =
        (InetSocketAddress) call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
    RpcContext.getContext().setAttachment(Constants.REMOTE_ADDRESS, remoteAddress.getHostString());
  }

  private void copyMetadataToThreadLocal(Metadata headers) {
    String attachments = headers.get(GrpcUtil.GRPC_CONTEXT_ATTACHMENTS);
    String values = headers.get(GrpcUtil.GRPC_CONTEXT_VALUES);
    try {
      if (attachments != null) {
        Map<String, String> attachmentsMap =
            SerializerUtil.fromJson(attachments, new TypeToken<Map<String, String>>() {}.getType());
        RpcContext.getContext().setAttachments(attachmentsMap);
      }
      if (values != null) {
        Map<String, Object> valuesMap =
            SerializerUtil.fromJson(values, new TypeToken<Map<String, Object>>() {}.getType());
        for (Map.Entry<String, Object> entry : valuesMap.entrySet()) {
          RpcContext.getContext().set(entry.getKey(), entry.getValue());
        }
      }
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
    }
  }
}
