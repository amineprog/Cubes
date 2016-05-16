package ethanjones.cubes.core.system;

import ethanjones.cubes.side.Side;
import ethanjones.cubes.side.Sided;

public class ThreadPool {

  private final String name;
  private final Runnable runnable;
  private final Thread[] threads;
  private Side side = null;
  private int priority = Thread.NORM_PRIORITY;
  private boolean daemon = false;
  private ThreadGroup group;

  public ThreadPool(String name, Runnable runnable) {
    this(name, runnable, Runtime.getRuntime().availableProcessors());
  }

  public ThreadPool(String name, Runnable runnable, int numThreads) {
    this.name = name;
    this.runnable = runnable;
    this.threads = new Thread[numThreads];
  }

  public ThreadPool setSide(Side side) {
    this.side = side;
    return this;
  }

  public ThreadPool setDaemon(boolean daemon) {
    this.daemon = daemon;
    return this;
  }

  public ThreadPool setPriority(int priority) {
    this.priority = priority;
    return this;
  }

  public ThreadPool start() {
    group = new ThreadGroup(name);
    for (int i = 0; i < threads.length; i++) {
      Thread thread = new Thread(group, new Runnable() {
        @Override
        public void run() {
          if (side != null) Sided.setSide(side);
          runnable.run();
        }
      });
      thread.setDaemon(daemon);
      thread.setPriority(priority);
      thread.setName(name + "-" + (i + 1));
      thread.setUncaughtExceptionHandler(Debug.UncaughtExceptionHandler.instance);
      thread.start();
      threads[i] = thread;
    }
    return this;
  }

  public ThreadPool stop() {
    for (Thread thread : threads) {
      if (thread.isAlive()) {
        thread.interrupt();
      }
    }
    for (Thread thread : threads) {
      if (thread.isAlive()) {
        try {
          thread.join();
        } catch (InterruptedException e) {
          Debug.crash(e);
        }
      }
    }
    return this;
  }
}
