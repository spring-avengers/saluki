package  com.quancheng.saluki.proxy.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;


public class HttpFiltersSourceAdapter {

  public HttpFiltersRunner filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
    return new HttpFiltersRunner(originalRequest, ctx);
  }

  public int getMaximumRequestBufferSizeInBytes() {
    return 0;
  }

  public int getMaximumResponseBufferSizeInBytes() {
    return 0;
  }

}
