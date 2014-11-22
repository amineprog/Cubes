package ethanjones.cubes.core.event;

import ethanjones.cubes.core.logging.Log;
import ethanjones.cubes.core.system.CubesException;
import ethanjones.cubes.side.Sided;

public class Event {

  private final boolean cancelable;
  private final boolean threaded;
  private boolean canceled;

  public Event(boolean cancelable, boolean threaded) {
    this.cancelable = cancelable;
    this.threaded = threaded;
    this.canceled = false;
  }

  public boolean isCancelable() {
    return cancelable;
  }

  public boolean isThreaded() {
    return threaded;
  }

  public boolean isCanceled() {
    return canceled;
  }

  public void setCanceled(boolean canceled) {
    if (!cancelable) {
      Log.error(new CubesException("Event is not cancelable"));
    }
    this.canceled = canceled;
  }

  public void post() {
    Sided.getEventBus().post(this);
  }
}
