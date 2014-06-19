package ethanjones.modularworld.core.events;

import ethanjones.modularworld.ModularWorld;
import ethanjones.modularworld.core.ModularWorldException;

public class Event {

  private final boolean cancelable;
  private boolean canceled;

  public Event(boolean cancelable) {
    this.cancelable = cancelable;
    this.canceled = false;
  }

  public boolean post() {
    return ModularWorld.instance.eventBus.post(this);
  }

  public boolean isCancelable() {
    return cancelable;
  }

  public boolean isCanceled() {
    return canceled;
  }

  public void setCanceled(boolean canceled) {
    if (!cancelable) {
      throw new ModularWorldException("Event is not cancelable");
    }
    this.canceled = canceled;
  }

}
