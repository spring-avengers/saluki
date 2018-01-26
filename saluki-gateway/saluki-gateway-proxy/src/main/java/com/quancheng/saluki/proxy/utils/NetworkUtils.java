package com.quancheng.saluki.proxy.utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.apache.commons.lang3.StringUtils;


public class NetworkUtils {

  public static InetAddress getLocalHost() throws UnknownHostException {
    return InetAddress.getLocalHost();
  }


  public static Boolean equalAddress(InetSocketAddress address, String hostAndPort) {
    String hostAnPortCopy = StringUtils.replaceChars(hostAndPort, "localhost", "127.0.0.1");
    String addressStr = address.getHostName() + ":" + address.getPort();
    return addressStr.equals(hostAnPortCopy) || addressStr.equals(hostAndPort);
  }


  public static InetAddress firstLocalNonLoopbackIpv4Address() {
    try {
      Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
      while (networkInterfaces.hasMoreElements()) {
        NetworkInterface networkInterface = networkInterfaces.nextElement();
        if (networkInterface.isUp()) {
          for (InterfaceAddress ifAddress : networkInterface.getInterfaceAddresses()) {
            if (ifAddress.getNetworkPrefixLength() > 0 && ifAddress.getNetworkPrefixLength() <= 32
                && !ifAddress.getAddress().isLoopbackAddress()) {
              return ifAddress.getAddress();
            }
          }
        }
      }
      return null;
    } catch (SocketException se) {
      return null;
    }
  }

}
