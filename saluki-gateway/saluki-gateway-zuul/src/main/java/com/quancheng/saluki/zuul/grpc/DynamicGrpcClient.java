/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.quancheng.saluki.zuul.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.DynamicMessage;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor.MethodType;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;

/**
 * @author liushiming
 * @version DynamicGrpcClient.java, v 0.0.1 2018年1月5日 下午3:25:51 liushiming
 */
public class DynamicGrpcClient {
  private static final Logger logger = LoggerFactory.getLogger(DynamicGrpcClient.class);
  private final MethodDescriptor protoMethodDescriptor;
  private final Channel channel;

  /** Creates a client for the supplied method, talking to the supplied endpoint. */
  public static DynamicGrpcClient create(MethodDescriptor protoMethod, Channel channel) {
    return new DynamicGrpcClient(protoMethod, channel);
  }

  @VisibleForTesting
  DynamicGrpcClient(MethodDescriptor protoMethodDescriptor, Channel channel) {
    this.protoMethodDescriptor = protoMethodDescriptor;
    this.channel = channel;
  }

  /**
   * Makes an rpc to the remote endpoint and respects the supplied callback. Returns a future which
   * terminates once the call has ended. For calls which are single-request, this throws
   * {@link IllegalArgumentException} if the size of {@code requests} is not exactly 1.
   */
  public ListenableFuture<Void> call(ImmutableList<DynamicMessage> requests,
      StreamObserver<DynamicMessage> responseObserver, CallOptions callOptions) {
    Preconditions.checkArgument(!requests.isEmpty(), "Can't make call without any requests");
    MethodType methodType = getMethodType();
    long numRequests = requests.size();
    if (methodType == MethodType.UNARY) {
      logger.info("Making unary call");
      Preconditions.checkArgument(numRequests == 1,
          "Need exactly 1 request for unary call, but got: " + numRequests);
      return callUnary(requests.get(0), responseObserver, callOptions);
    } else if (methodType == MethodType.SERVER_STREAMING) {
      logger.info("Making server streaming call");
      Preconditions.checkArgument(numRequests == 1,
          "Need exactly 1 request for server streaming call, but got: " + numRequests);
      return callServerStreaming(requests.get(0), responseObserver, callOptions);
    } else if (methodType == MethodType.CLIENT_STREAMING) {
      logger.info("Making client streaming call with " + requests.size() + " requests");
      return callClientStreaming(requests, responseObserver, callOptions);
    } else {
      // Bidi streaming.
      logger.info("Making bidi streaming call with " + requests.size() + " requests");
      return callBidiStreaming(requests, responseObserver, callOptions);
    }
  }

  private ListenableFuture<Void> callBidiStreaming(ImmutableList<DynamicMessage> requests,
      StreamObserver<DynamicMessage> responseObserver, CallOptions callOptions) {
    DoneObserver<DynamicMessage> doneObserver = new DoneObserver<>();
    StreamObserver<DynamicMessage> requestObserver = ClientCalls.asyncBidiStreamingCall(
        createCall(callOptions), CompositeStreamObserver.of(responseObserver, doneObserver));
    requests.forEach(requestObserver::onNext);
    requestObserver.onCompleted();
    return doneObserver.getCompletionFuture();
  }

  private ListenableFuture<Void> callClientStreaming(ImmutableList<DynamicMessage> requests,
      StreamObserver<DynamicMessage> responseObserver, CallOptions callOptions) {
    DoneObserver<DynamicMessage> doneObserver = new DoneObserver<>();
    StreamObserver<DynamicMessage> requestObserver = ClientCalls.asyncClientStreamingCall(
        createCall(callOptions), CompositeStreamObserver.of(responseObserver, doneObserver));
    requests.forEach(requestObserver::onNext);
    requestObserver.onCompleted();
    return doneObserver.getCompletionFuture();
  }

  private ListenableFuture<Void> callServerStreaming(DynamicMessage request,
      StreamObserver<DynamicMessage> responseObserver, CallOptions callOptions) {
    DoneObserver<DynamicMessage> doneObserver = new DoneObserver<>();
    ClientCalls.asyncServerStreamingCall(createCall(callOptions), request,
        CompositeStreamObserver.of(responseObserver, doneObserver));
    return doneObserver.getCompletionFuture();
  }

  private ListenableFuture<Void> callUnary(DynamicMessage request,
      StreamObserver<DynamicMessage> responseObserver, CallOptions callOptions) {
    DoneObserver<DynamicMessage> doneObserver = new DoneObserver<>();
    ClientCalls.asyncUnaryCall(createCall(callOptions), request,
        CompositeStreamObserver.of(responseObserver, doneObserver));
    return doneObserver.getCompletionFuture();
  }

  private ClientCall<DynamicMessage, DynamicMessage> createCall(CallOptions callOptions) {
    return channel.newCall(createGrpcMethodDescriptor(), callOptions);
  }

  private io.grpc.MethodDescriptor<DynamicMessage, DynamicMessage> createGrpcMethodDescriptor() {
    return io.grpc.MethodDescriptor.<DynamicMessage, DynamicMessage>create(getMethodType(),
        getFullMethodName(), new DynamicMessageMarshaller(protoMethodDescriptor.getInputType()),
        new DynamicMessageMarshaller(protoMethodDescriptor.getOutputType()));
  }

  private String getFullMethodName() {
    String serviceName = protoMethodDescriptor.getService().getFullName();
    String methodName = protoMethodDescriptor.getName();
    return io.grpc.MethodDescriptor.generateFullMethodName(serviceName, methodName);
  }

  /** Returns the appropriate method type based on whether the client or server expect streams. */
  private MethodType getMethodType() {
    boolean clientStreaming = protoMethodDescriptor.toProto().getClientStreaming();
    boolean serverStreaming = protoMethodDescriptor.toProto().getServerStreaming();

    if (!clientStreaming && !serverStreaming) {
      return MethodType.UNARY;
    } else if (!clientStreaming && serverStreaming) {
      return MethodType.SERVER_STREAMING;
    } else if (clientStreaming && !serverStreaming) {
      return MethodType.CLIENT_STREAMING;
    } else {
      return MethodType.BIDI_STREAMING;
    }
  }
}
