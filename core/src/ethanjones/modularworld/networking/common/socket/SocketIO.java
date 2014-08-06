package ethanjones.modularworld.networking.common.socket;

import com.badlogic.gdx.utils.Disposable;

public abstract class SocketIO implements Runnable, Disposable {

  protected final SocketMonitor socketMonitor;
  private Thread thread;

  public SocketIO(SocketMonitor socketMonitor) {
    this.socketMonitor = socketMonitor;
  }

  public Thread start(String name) {
    if (thread != null) return thread;
    thread = new Thread(this);
    thread.setDaemon(true);
    thread.setName(name);
    thread.start();
    return thread;
  }

  protected Thread getThread() {
    return thread;
  }

}
