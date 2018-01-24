package com.quancheng.saluki.netty;

import java.net.InetSocketAddress;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.saluki.netty.filter.HttpRequestFilter;
import com.quancheng.saluki.netty.filter.HttpRequestFilterChain;
import com.quancheng.saluki.netty.filter.HttpResponseFilterChain;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;


public class HttpFiltersRunner extends HttpFiltersAdapter {

  private static Logger logger = LoggerFactory.getLogger(HttpFiltersRunner.class);
  private final HttpRequestFilterChain httpRequestFilterChain = new HttpRequestFilterChain();
  private final HttpResponseFilterChain httpResponseFilterChain = new HttpResponseFilterChain();

  public HttpFiltersRunner(HttpRequest originalRequest, ChannelHandlerContext ctx) {
    super(originalRequest, ctx);
  }

  @Override
  public HttpResponse clientToProxyRequest(HttpObject httpObject) {
    HttpResponse httpResponse = null;
    try {
      ImmutablePair<Boolean, HttpRequestFilter> immutablePair =
          httpRequestFilterChain.doFilter(originalRequest, httpObject, ctx);
      if (immutablePair.left) {
        httpResponse = createResponse(HttpResponseStatus.FORBIDDEN, originalRequest);
      }
    } catch (Exception e) {
      httpResponse = createResponse(HttpResponseStatus.BAD_GATEWAY, originalRequest);
      logger.error("client's request failed", e.getCause());
    }
    return httpResponse;
  }

  @Override
  public HttpObject proxyToClientResponse(HttpObject httpObject) {
    if (httpObject instanceof HttpResponse) {
      httpResponseFilterChain.doFilter(originalRequest, (HttpResponse) httpObject);
    }
    return httpObject;
  }

  @Override
  public void proxyToServerResolutionSucceeded(String serverHostAndPort,
      InetSocketAddress resolvedRemoteAddress) {
    if (resolvedRemoteAddress == null) {
      ctx.writeAndFlush(createResponse(HttpResponseStatus.BAD_GATEWAY, originalRequest));
    }
  }

  private HttpResponse createResponse(HttpResponseStatus httpResponseStatus,
      HttpRequest originalRequest) {
    HttpHeaders httpHeaders = new DefaultHttpHeaders();
    httpHeaders.add("Transfer-Encoding", "chunked");
    HttpResponse httpResponse =
        new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus);
    httpResponse.headers().add(httpHeaders);
    return httpResponse;
  }

}
