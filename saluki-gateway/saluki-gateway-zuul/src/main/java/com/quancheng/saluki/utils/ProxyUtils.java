package com.quancheng.saluki.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;


public class ProxyUtils {

  private static final Set<String> SHOULD_NOT_PROXY_HOP_BY_HOP_HEADERS =
      ImmutableSet.of(HttpHeaderNames.CONNECTION.toString().toLowerCase(Locale.US),
          HttpHeaderNames.PROXY_AUTHENTICATE.toString().toLowerCase(Locale.US),
          HttpHeaderNames.PROXY_AUTHORIZATION.toString().toLowerCase(Locale.US),
          HttpHeaderNames.TE.toString().toLowerCase(Locale.US),
          HttpHeaderNames.TRAILER.toString().toLowerCase(Locale.US),
          HttpHeaderNames.UPGRADE.toString().toLowerCase(Locale.US));

  private static final Logger LOG = LoggerFactory.getLogger(ProxyUtils.class);

  private static final TimeZone GMT = TimeZone.getTimeZone("GMT");


  private static final Splitter COMMA_SEPARATED_HEADER_VALUE_SPLITTER =
      Splitter.on(',').trimResults().omitEmptyStrings();


  private static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

  private static Pattern HTTP_PREFIX = Pattern.compile("^https?://.*", Pattern.CASE_INSENSITIVE);


  public static String stripHost(final String uri) {
    if (!HTTP_PREFIX.matcher(uri).matches()) {
      return uri;
    }
    final String noHttpUri = StringUtils.substringAfter(uri, "://");
    final int slashIndex = noHttpUri.indexOf("/");
    if (slashIndex == -1) {
      return "/";
    }
    final String noHostUri = noHttpUri.substring(slashIndex);
    return noHostUri;
  }


  public static String formatDate(final Date date) {
    return formatDate(date, PATTERN_RFC1123);
  }


  public static String formatDate(final Date date, final String pattern) {
    if (date == null)
      throw new IllegalArgumentException("date is null");
    if (pattern == null)
      throw new IllegalArgumentException("pattern is null");

    final SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.US);
    formatter.setTimeZone(GMT);
    return formatter.format(date);
  }


  public static boolean isLastChunk(final HttpObject httpObject) {
    return httpObject instanceof LastHttpContent;
  }


  public static boolean isChunked(final HttpObject httpObject) {
    return !isLastChunk(httpObject);
  }


  public static String parseHostAndPort(final HttpRequest httpRequest) {
    final String uriHostAndPort = parseHostAndPort(httpRequest.uri());
    return uriHostAndPort;
  }


  public static String parseHostAndPort(final String uri) {
    final String tempUri;
    if (!HTTP_PREFIX.matcher(uri).matches()) {
      tempUri = uri;
    } else {
      tempUri = StringUtils.substringAfter(uri, "://");
    }
    final String hostAndPort;
    if (tempUri.contains("/")) {
      hostAndPort = tempUri.substring(0, tempUri.indexOf("/"));
    } else {
      hostAndPort = tempUri;
    }
    return hostAndPort;
  }


  public static HttpResponse copyMutableResponseFields(final HttpResponse original) {

    HttpResponse copy = null;
    if (original instanceof DefaultFullHttpResponse) {
      ByteBuf content = ((DefaultFullHttpResponse) original).content();
      copy = new DefaultFullHttpResponse(original.protocolVersion(), original.status(), content);
    } else {
      copy = new DefaultHttpResponse(original.protocolVersion(), original.status());
    }
    final Collection<String> headerNames = original.headers().names();
    for (final String name : headerNames) {
      final List<String> values = original.headers().getAll(name);
      copy.headers().set(name, values);
    }
    return copy;
  }


  public static void addVia(HttpMessage httpMessage, String alias) {
    String newViaHeader = new StringBuilder().append(httpMessage.protocolVersion().majorVersion())
        .append('.').append(httpMessage.protocolVersion().minorVersion()).append(' ').append(alias)
        .toString();
    final List<String> vias;
    if (httpMessage.headers().contains(HttpHeaderNames.VIA)) {
      List<String> existingViaHeaders = httpMessage.headers().getAll(HttpHeaderNames.VIA);
      vias = new ArrayList<String>(existingViaHeaders);
      vias.add(newViaHeader);
    } else {
      vias = Collections.singletonList(newViaHeader);
    }

    httpMessage.headers().set(HttpHeaderNames.VIA, vias);
  }


  public static boolean isTrue(final String val) {
    return checkTrueOrFalse(val, "true", "on");
  }


  public static boolean isFalse(final String val) {
    return checkTrueOrFalse(val, "false", "off");
  }

  public static boolean extractBooleanDefaultFalse(final Properties props, final String key) {
    final String throttle = props.getProperty(key);
    if (StringUtils.isNotBlank(throttle)) {
      return throttle.trim().equalsIgnoreCase("true");
    }
    return false;
  }

  public static boolean extractBooleanDefaultTrue(final Properties props, final String key) {
    final String throttle = props.getProperty(key);
    if (StringUtils.isNotBlank(throttle)) {
      return throttle.trim().equalsIgnoreCase("true");
    }
    return true;
  }

  public static int extractInt(final Properties props, final String key) {
    return extractInt(props, key, -1);
  }

  public static int extractInt(final Properties props, final String key, int defaultValue) {
    final String readThrottleString = props.getProperty(key);
    if (StringUtils.isNotBlank(readThrottleString) && NumberUtils.isCreatable(readThrottleString)) {
      return Integer.parseInt(readThrottleString);
    }
    return defaultValue;
  }

  public static boolean isCONNECT(HttpObject httpObject) {
    return httpObject instanceof HttpRequest
        && HttpMethod.CONNECT.equals(((HttpRequest) httpObject).method());
  }


  public static boolean isHEAD(HttpRequest httpRequest) {
    return HttpMethod.HEAD.equals(httpRequest.method());
  }

  private static boolean checkTrueOrFalse(final String val, final String str1, final String str2) {
    final String str = val.trim();
    return StringUtils.isNotBlank(str)
        && (str.equalsIgnoreCase(str1) || str.equalsIgnoreCase(str2));
  }

  public static boolean isContentAlwaysEmpty(HttpMessage msg) {
    if (msg instanceof HttpResponse) {
      HttpResponse res = (HttpResponse) msg;
      int code = res.status().code();
      if (code >= 100 && code < 200) {
        return true;
      }
      switch (code) {
        case 204:
        case 205:
        case 304:
          return true;
      }
    }
    return false;
  }


  public static boolean isResponseSelfTerminating(HttpResponse response) {
    if (isContentAlwaysEmpty(response)) {
      return true;
    }
    List<String> allTransferEncodingHeaders =
        getAllCommaSeparatedHeaderValues(HttpHeaderNames.TRANSFER_ENCODING.toString(), response);
    if (!allTransferEncodingHeaders.isEmpty()) {
      String finalEncoding = allTransferEncodingHeaders.get(allTransferEncodingHeaders.size() - 1);
      return HttpHeaderValues.CHUNKED.toString().equals(finalEncoding);
    }
    String contentLengthHeader = response.headers().get(HttpHeaderNames.CONTENT_LENGTH);
    if (contentLengthHeader != null && !contentLengthHeader.isEmpty()) {
      return true;
    }
    return false;
  }


  public static List<String> getAllCommaSeparatedHeaderValues(String headerName,
      HttpMessage httpMessage) {
    List<String> allHeaders = httpMessage.headers().getAll(headerName);
    if (allHeaders.isEmpty()) {
      return Collections.emptyList();
    }

    ImmutableList.Builder<String> headerValues = ImmutableList.builder();
    for (String header : allHeaders) {
      List<String> commaSeparatedValues = splitCommaSeparatedHeaderValues(header);
      headerValues.addAll(commaSeparatedValues);
    }

    return headerValues.build();
  }


  public static HttpResponse duplicateHttpResponse(HttpResponse originalResponse) {
    DefaultHttpResponse newResponse =
        new DefaultHttpResponse(originalResponse.protocolVersion(), originalResponse.status());
    newResponse.headers().add(originalResponse.headers());
    return newResponse;
  }


  public static String getHostName() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (IOException e) {
      LOG.debug("Ignored exception", e);
    } catch (RuntimeException e) {
      LOG.debug("Ignored exception", e);
    }
    LOG.info("Could not lookup localhost");
    return null;
  }


  public static boolean shouldRemoveHopByHopHeader(String headerName) {
    return SHOULD_NOT_PROXY_HOP_BY_HOP_HEADERS.contains(headerName.toLowerCase(Locale.US));
  }


  public static List<String> splitCommaSeparatedHeaderValues(String headerValue) {
    return ImmutableList.copyOf(COMMA_SEPARATED_HEADER_VALUE_SPLITTER.split(headerValue));
  }



  public static FullHttpResponse createFullHttpResponse(HttpVersion httpVersion,
      HttpResponseStatus status, String body) {
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    ByteBuf content = Unpooled.copiedBuffer(bytes);

    return createFullHttpResponse(httpVersion, status, "text/html; charset=utf-8", content,
        bytes.length);
  }

  public static FullHttpResponse createFullHttpResponse(HttpVersion httpVersion,
      HttpResponseStatus status) {
    return createFullHttpResponse(httpVersion, status, null, null, 0);
  }


  public static FullHttpResponse createFullHttpResponse(HttpVersion httpVersion,
      HttpResponseStatus status, String contentType, ByteBuf body, int contentLength) {
    DefaultFullHttpResponse response;

    if (body != null) {
      response = new DefaultFullHttpResponse(httpVersion, status, body);
      response.headers().set(HttpHeaderNames.CONTENT_LENGTH, contentLength);
      response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
    } else {
      response = new DefaultFullHttpResponse(httpVersion, status);
    }

    return response;
  }

  public static void removeSdchEncoding(HttpHeaders headers) {
    List<String> encodings = headers.getAll(HttpHeaderNames.ACCEPT_ENCODING);
    headers.remove(HttpHeaderNames.ACCEPT_ENCODING);
    for (String encoding : encodings) {
      if (encoding != null) {
        encoding = encoding.replaceAll(",? *(sdch|SDCH)", "").replaceFirst("^ *, *", "");
        if (StringUtils.isNotBlank(encoding)) {
          headers.add(HttpHeaderNames.ACCEPT_ENCODING, encoding);
        }
      }
    }
  }
}
