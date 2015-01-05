package ethanjones.cubes.side;

import java.util.concurrent.atomic.AtomicBoolean;

public final class State {

  private AtomicBoolean setup = new AtomicBoolean(false);
  private AtomicBoolean stopping = new AtomicBoolean(false);
  private AtomicBoolean stopped = new AtomicBoolean(false);

  public synchronized void setup() {
    setup.set(true);
  }

  public synchronized void stopping() {
    stopping.set(true);
  }

  public synchronized void stopped() {
    stopped.set(true);
  }

  public synchronized boolean isSetup() {
    return setup.get();
  }

  public synchronized boolean canDispose() {
    return !stopping.get() && !stopped.get();
  }

  public synchronized boolean isStopping() {
    return stopping.get();
  }

  public synchronized boolean hasStopped() {
    return stopped.get();
  }

  public synchronized boolean canRun() {
    return setup.get() && !stopping.get() && !stopped.get();
  }
}
