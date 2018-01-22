package com.quancheng.saluki.proxy.connection;

import java.net.URL;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

public class Connection {

  private final URL routeHost;
  private final ChannelFuture channelFuture;

  public Connection(URL routeHost, ChannelFuture channelFuture) {
    this.routeHost = routeHost;
    this.channelFuture = channelFuture;
  }

  public URL getRouteHost() {
    return routeHost;
  }

  public Channel getChannel() {
    return channelFuture.channel();
  }

  public ChannelFuture getChannelFuture() {
    return channelFuture;
  }


}
