package com.quancheng.saluki.utils;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;


public class NetworkUtils {

  public static InetAddress getLocalHost() throws UnknownHostException {
    return InetAddress.getLocalHost();
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
