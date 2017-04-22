package ethanjones.cubes.world.thread;

import ethanjones.cubes.core.event.world.generation.AreaLoadedEvent;
import ethanjones.cubes.core.event.world.generation.FeaturesEvent;
import ethanjones.cubes.core.event.world.generation.GenerationEvent;
import ethanjones.cubes.core.gwt.FakeAtomic.AtomicReference;
import ethanjones.cubes.world.light.SunLight;
import ethanjones.cubes.world.reference.AreaReference;
import ethanjones.cubes.world.reference.multi.MultiAreaReference;
import ethanjones.cubes.world.save.Save;
import ethanjones.cubes.world.server.WorldServer;
import ethanjones.cubes.world.storage.Area;
import ethanjones.cubes.world.storage.AreaMap;
import ethanjones.cubes.world.storage.WorldStorage;

public class WorldTasks {

  private static final WorldGenerationRunnable gen = new WorldGenerationRunnable();

  static {
    gen.start();
  }

  public static GenerationTask request(WorldServer worldServer, MultiAreaReference references, WorldRequestParameter parameter) {
    WorldGenerationTask generationTask = new WorldGenerationTask(worldServer, references, parameter);
    gen.queue.add(generationTask);
    return generationTask;
  }
  
  public static void save(Save s, AreaMap areas) {
    for (Area area : areas) {
      if (area.modifiedSinceSave(null)) {
        WorldStorage.storeChangedBlocks(area.areaX, area.areaZ, area.changedBlockList);
        area.saveModCount();
      }
    }
  }

  public static boolean currentlySaving() {
    return false;
  }

  public static boolean waitSaveFinish() {
    return true;
  }

  protected static int generate(AreaReference areaReference, WorldServer world) {
    Area area = world.getArea(areaReference, false);
    if (area != null) return 0;
    area = world.save.readArea(areaReference.areaX, areaReference.areaZ);
    if (area != null) {
      world.setArea(area);
      if (area.features.get() != null) new AreaLoadedEvent(area, areaReference).post();
      return 1;
    }

    area = new Area(areaReference.areaX, areaReference.areaZ);
    world.getTerrainGenerator().generate(area);
    new GenerationEvent(area, areaReference).post();
    area.modify();

    world.setArea(area);
    return 2;
  }

  protected static int features(AreaReference areaReference, WorldServer world) {
    Area area = world.getArea(areaReference, false);
    if (area == null) return 0;

    AtomicReference<Object> features = area.features;

    if (features.compareAndSet(null, true)) {
      world.getTerrainGenerator().features(area, world);
      new FeaturesEvent(area, areaReference).post();
      area.initialUpdate();
      SunLight.initialSunlight(area);
      WorldStorage.requestChangedBlocks(area.areaX, area.areaZ);
      new AreaLoadedEvent(area, areaReference).post();
      return 1;
    }
    return 0;
  }
}
