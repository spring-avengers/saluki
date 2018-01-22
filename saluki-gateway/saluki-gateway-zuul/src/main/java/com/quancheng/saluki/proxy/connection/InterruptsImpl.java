package com.quancheng.saluki.proxy.connection;


import static io.netty.handler.codec.http.HttpResponseStatus.MOVED_PERMANENTLY;
import static io.netty.handler.codec.http.HttpResponseStatus.TEMPORARY_REDIRECT;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import com.quancheng.saluki.zuul.api.Interrupts;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public class InterruptsImpl implements Interrupts {

  private final HttpRequest httpRequest;
  private final Channel channel;
  private boolean interrupted = false;

  public InterruptsImpl(HttpRequest httpRequest, Channel channel) {
    this.httpRequest = httpRequest;
    this.channel = channel;
  }

  @Override
  public void movedPermanently(String location) {
    HttpResponse response = new DefaultHttpResponse(HTTP_1_1, MOVED_PERMANENTLY);
    response.headers().add("Location", location);
    write(response);
  }

  @Override
  public void temporaryRedirect(String location) {
    HttpResponse response = new DefaultHttpResponse(HTTP_1_1, TEMPORARY_REDIRECT);
    response.headers().add("Location", location);
    write(response);
  }

  private void write(HttpResponse httpResponse) {
    ChannelFuture future = channel.write(httpResponse);
    if (!channel.isActive()) {
      future.addListener(ChannelFutureListener.CLOSE);
    }
    this.interrupted = true;
  }

  /**
   * An action was performed which requires that onward request processing is interrupted
   * 
   * @return onward processing should be interrupted
   */
  public boolean isInterrupted() {
    return interrupted;
  }

}
