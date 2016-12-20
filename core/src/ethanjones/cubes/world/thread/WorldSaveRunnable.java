package ethanjones.cubes.world.thread;

import ethanjones.cubes.core.logging.Log;
import ethanjones.cubes.core.system.CubesException;
import ethanjones.cubes.side.common.Side;
import ethanjones.cubes.world.save.SaveAreaIO;
import ethanjones.cubes.world.storage.Area;

import java.util.concurrent.LinkedBlockingQueue;

public class WorldSaveRunnable implements Runnable {
  public LinkedBlockingQueue<WorldSaveTask> queue = new LinkedBlockingQueue<WorldSaveTask>();

  @Override
  public void run() {
    while (!Thread.interrupted()) {
      try {
        WorldSaveTask task = queue.peek();
        while (task == null) {
          try {
            Thread.sleep(25);
          } catch (InterruptedException e) {
            return;
          }
          task = queue.peek();
        }

        Area area = task.saveQueue.poll();
        while (area != null) {
          if (SaveAreaIO.write(task.save, area)) {
            int written = task.written.incrementAndGet();
            if (written % 100 == 0) Log.debug("Written " + written + " areas");
          }
          area = task.saveQueue.poll();
        }

        if (queue.remove(task)) {
          Log.debug("Saved areas: wrote " + task.written + " total " + task.length);
        }
      } catch (CubesException e) {
        if (e.className.equals(Side.class.getName())) {
          queue.clear();
        } else {
          throw e;
        }
      }
    }
  }
}
