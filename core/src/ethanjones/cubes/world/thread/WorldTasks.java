package ethanjones.cubes.world.thread;

import ethanjones.cubes.core.event.world.generation.AreaGeneratedEvent;
import ethanjones.cubes.core.event.world.generation.FeaturesEvent;
import ethanjones.cubes.core.event.world.generation.GenerationEvent;
import ethanjones.cubes.core.logging.Log;
import ethanjones.cubes.core.system.CubesException;
import ethanjones.cubes.core.system.ThreadPool;
import ethanjones.cubes.side.Side;
import ethanjones.cubes.world.reference.AreaReference;
import ethanjones.cubes.world.reference.multi.MultiAreaReference;
import ethanjones.cubes.world.server.WorldServer;
import ethanjones.cubes.world.storage.Area;

import java.util.concurrent.atomic.AtomicReference;

public class WorldTasks {

  private static final WorldGenerationThread gen = new WorldGenerationThread();
  private static final ThreadPool threadPool = new ThreadPool("WorldGeneration", gen, 1).setSide(Side.Server).setDaemon(true).start();

  public static void request(WorldServer worldServer, MultiAreaReference references) {
    gen.queue.add(new WorldGenerationTask(worldServer, references));
  }

  protected static void generate(AreaReference areaReference, WorldServer world) {
    Area area = world.getArea(areaReference, false);
    if (area != null) return;

    area = new Area(areaReference.areaX, areaReference.areaZ);
    world.getTerrainGenerator().generate(area);
    new GenerationEvent(area, areaReference).post();
    area.updateAll();

    world.setAreaInternal(area);
  }

  protected static void features(AreaReference areaReference, WorldServer world) {
    Area area = world.getArea(areaReference, false);
    if (area == null) return;

    AtomicReference<Object> features = area.features;

    if (features.compareAndSet(null, Thread.currentThread())) {
      world.getTerrainGenerator().features(area, world);
      new FeaturesEvent(area, areaReference).post();
      area.updateAll();
      new AreaGeneratedEvent(area, areaReference).post();
    }
  }
}
