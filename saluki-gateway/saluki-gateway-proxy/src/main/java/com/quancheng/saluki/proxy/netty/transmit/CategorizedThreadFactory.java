package  com.quancheng.saluki.proxy.netty.transmit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


public class CategorizedThreadFactory implements ThreadFactory {
  private static final Logger log = LoggerFactory.getLogger(CategorizedThreadFactory.class);

  private final String name;
  private final String category;
  private final int uniqueServerGroupId;

  private AtomicInteger threadCount = new AtomicInteger(0);

  private static final Thread.UncaughtExceptionHandler UNCAUGHT_EXCEPTION_HANDLER =
      new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
          log.error("Uncaught throwable in thread: {}", t.getName(), e);
        }
      };


  public CategorizedThreadFactory(String name, String category, int uniqueServerGroupId) {
    this.category = category;
    this.name = name;
    this.uniqueServerGroupId = uniqueServerGroupId;
  }

  @Override
  public Thread newThread(Runnable r) {
    Thread t = new Thread(r,
        name + "-" + uniqueServerGroupId + "-" + category + "-" + threadCount.getAndIncrement());

    t.setUncaughtExceptionHandler(UNCAUGHT_EXCEPTION_HANDLER);

    return t;
  }

}
