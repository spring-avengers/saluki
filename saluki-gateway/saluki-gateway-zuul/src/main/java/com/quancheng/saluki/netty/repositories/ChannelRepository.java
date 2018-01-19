package com.quancheng.saluki.netty.repositories;

import org.springframework.stereotype.Repository;

import com.quancheng.saluki.servo.jsr166e.ConcurrentHashMapV8;

import io.netty.channel.Channel;

/**
 * Created by Krisztian on 2016. 10. 31..
 */
@Repository
public class ChannelRepository {
  private final ConcurrentHashMapV8<String, Channel> channelCache = new ConcurrentHashMapV8<>();

  public ChannelRepository put(String key, Channel value) {
    channelCache.put(key, value);
    return this;
  }

  public Channel get(String key) {
    return channelCache.get(key);
  }

  public void remove(String key) {
    this.channelCache.remove(key);
  }

  public int size() {
    return this.channelCache.size();
  }

  public ConcurrentHashMapV8<String, Channel> getChannelCache() {
    return channelCache;
  }
}
