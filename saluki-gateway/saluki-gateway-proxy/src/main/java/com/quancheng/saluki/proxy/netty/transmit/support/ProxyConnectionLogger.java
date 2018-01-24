package   com.quancheng.saluki.proxy.netty.transmit.support;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.LocationAwareLogger;

import com.quancheng.saluki.proxy.netty.transmit.connection.ProxyConnection;


public class ProxyConnectionLogger {
  private final ProxyConnection<?> connection;
  private final LogDispatch dispatch;
  private final Logger logger;
  private final String fqcn = this.getClass().getCanonicalName();

  public ProxyConnectionLogger(ProxyConnection<?> connection) {
    this.connection = connection;
    final Logger lg = LoggerFactory.getLogger(connection.getClass());
    if (lg instanceof LocationAwareLogger) {
      dispatch = new LocationAwareLogggerDispatch((LocationAwareLogger) lg);
    } else {
      dispatch = new LoggerDispatch();
    }
    logger = lg;
  }

  public void error(String message, Object... params) {
    if (logger.isErrorEnabled()) {
      dispatch.doLog(LocationAwareLogger.ERROR_INT, message, params, null);
    }
  }

  public void error(String message, Throwable t) {
    if (logger.isErrorEnabled()) {
      dispatch.doLog(LocationAwareLogger.ERROR_INT, message, null, t);
    }
  }

  public void warn(String message, Object... params) {
    if (logger.isWarnEnabled()) {
      dispatch.doLog(LocationAwareLogger.WARN_INT, message, params, null);
    }
  }

  public void warn(String message, Throwable t) {
    if (logger.isWarnEnabled()) {
      dispatch.doLog(LocationAwareLogger.WARN_INT, message, null, t);
    }
  }

  public void info(String message, Object... params) {
    if (logger.isInfoEnabled()) {
      dispatch.doLog(LocationAwareLogger.INFO_INT, message, params, null);
    }
  }

  public void info(String message, Throwable t) {
    if (logger.isInfoEnabled()) {
      dispatch.doLog(LocationAwareLogger.INFO_INT, message, null, t);
    }
  }

  public void debug(String message, Object... params) {
    if (logger.isDebugEnabled()) {
      dispatch.doLog(LocationAwareLogger.DEBUG_INT, message, params, null);
    }
  }

  public void debug(String message, Throwable t) {
    if (logger.isDebugEnabled()) {
      dispatch.doLog(LocationAwareLogger.DEBUG_INT, message, null, t);
    }
  }

  public void log(int level, String message, Object... params) {
    if (level != LocationAwareLogger.DEBUG_INT || logger.isDebugEnabled()) {
      dispatch.doLog(level, message, params, null);
    }
  }

  public void log(int level, String message, Throwable t) {
    if (level != LocationAwareLogger.DEBUG_INT || logger.isDebugEnabled()) {
      dispatch.doLog(level, message, null, t);
    }
  }

  private interface LogDispatch {
    void doLog(int level, String message, Object[] params, Throwable t);
  }

  private String fullMessage(String message) {
    String stateMessage = connection.getCurrentState().toString();
    if (connection.isTunneling()) {
      stateMessage += " {tunneling}";
    }
    String messagePrefix = "(" + stateMessage + ")";
    if (connection.channel != null) {
      messagePrefix = messagePrefix + " " + connection.channel;
    }
    return messagePrefix + ": " + message;
  }


  private class LoggerDispatch implements LogDispatch {
    @Override
    public void doLog(int level, String message, Object[] params, Throwable t) {
      String formattedMessage = fullMessage(message);

      final Object[] paramsWithThrowable;

      if (t != null) {
        if (params == null) {
          paramsWithThrowable = new Object[1];
          paramsWithThrowable[0] = t;
        } else {
          paramsWithThrowable = Arrays.copyOf(params, params.length + 1);
          paramsWithThrowable[params.length] = t;
        }
      } else {
        paramsWithThrowable = params;
      }
      switch (level) {
        case LocationAwareLogger.TRACE_INT:
          logger.trace(formattedMessage, paramsWithThrowable);
          break;
        case LocationAwareLogger.DEBUG_INT:
          logger.debug(formattedMessage, paramsWithThrowable);
          break;
        case LocationAwareLogger.INFO_INT:
          logger.info(formattedMessage, paramsWithThrowable);
          break;
        case LocationAwareLogger.WARN_INT:
          logger.warn(formattedMessage, paramsWithThrowable);
          break;
        case LocationAwareLogger.ERROR_INT:
        default:
          logger.error(formattedMessage, paramsWithThrowable);
          break;
      }
    }
  }


  private class LocationAwareLogggerDispatch implements LogDispatch {

    private LocationAwareLogger log;

    public LocationAwareLogggerDispatch(LocationAwareLogger log) {
      this.log = log;
    }

    @Override
    public void doLog(int level, String message, Object[] params, Throwable t) {
      String formattedMessage = fullMessage(message);
      if (params != null && params.length > 0) {
        formattedMessage = MessageFormatter.arrayFormat(formattedMessage, params).getMessage();
      }
      log.log(null, fqcn, level, formattedMessage, null, t);
    }
  }
}
