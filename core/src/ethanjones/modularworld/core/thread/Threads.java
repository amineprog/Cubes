package ethanjones.modularworld.core.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Threads {

  private static ScheduledThreadPoolExecutor executor;

  public static void init() {
    executor = new ScheduledThreadPoolExecutor(8);
  }

  public static Future execute(Callable<?> task) {
    return executor.submit(task);
  }

  public static void dispose() {
    executor.shutdownNow();
  }

}
