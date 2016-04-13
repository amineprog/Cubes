package ethanjones.cubes.world;

import ethanjones.cubes.block.Block;
import ethanjones.cubes.core.event.world.generation.AreaGeneratedEvent;
import ethanjones.cubes.core.logging.Log;
import ethanjones.cubes.core.system.Pools;
import ethanjones.cubes.core.util.Lock;
import ethanjones.cubes.entity.Entity;
import ethanjones.cubes.side.common.Cubes;
import ethanjones.cubes.world.generator.TerrainGenerator;
import ethanjones.cubes.world.reference.AreaReference;
import ethanjones.cubes.world.reference.BlockReference;
import ethanjones.cubes.world.reference.multi.MultiAreaReference;
import ethanjones.cubes.world.reference.multi.WorldRegion;
import ethanjones.cubes.world.storage.Area;
import ethanjones.data.DataGroup;

import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class World implements Disposable {

  public static final int MAX_TIME = (1000 / Cubes.tickMS) * 60;

  public final Lock lock = new Lock();
  public final HashMap<AreaReference, Area> map;
  public final TerrainGenerator terrainGenerator;
  public final AtomicBoolean disposed = new AtomicBoolean(false);
  public final BlockReference spawnpoint = new BlockReference();
  public final HashMap<UUID, Entity> entities = new HashMap<UUID, Entity>();
  public int time;

  public World(TerrainGenerator terrainGenerator) {
    this.terrainGenerator = terrainGenerator;
    map = new HashMap<AreaReference, Area>(1024);
  }

  public Area setAreaInternal(Area area) {
    lock.writeLock();

    AreaReference areaReference = new AreaReference().setFromArea(area);

    if (map.containsKey(areaReference)) {
      Log.debug("World already contains " + area.areaX + "," + area.areaZ);
      return lock.writeUnlock(map.get(areaReference));
    }

    Area old = map.put(areaReference.clone(), area);

    lock.writeUnlock();

    //Must be after lock released to prevent dead locks
    if (area.features()) new AreaGeneratedEvent(area, areaReference.clone()).post();

    synchronized (map) {
      map.notifyAll();
    }

    if (old != null) {
      old.world = null;
      old.unload();
    }
    area.world = this;
    return area;
  }

  public Block getBlock(int x, int y, int z) {
    Area area = getArea(CoordinateConverter.area(x), CoordinateConverter.area(z));
    return area == null ? null : area.getBlock(x - area.minBlockX, y, z - area.minBlockZ);
  }

  public int getLight(int x, int y, int z) {
    Area area = getArea(CoordinateConverter.area(x), CoordinateConverter.area(z));
    return area == null || y < 0 || y > area.maxY ? 0 : area.getLight(x - area.minBlockX, y, z - area.minBlockZ);
  }

  public int getSunLight(int x, int y, int z) {
    Area area = getArea(CoordinateConverter.area(x), CoordinateConverter.area(z));
    return area == null || y < 0 || y > area.maxY ? 0 : area.getSunlight(x - area.minBlockX, y, z - area.minBlockZ);
  }

  public int getLightRaw(int x, int y, int z) {
    Area area = getArea(CoordinateConverter.area(x), CoordinateConverter.area(z));
    return area == null || y < 0 || y > area.maxY ? 0 : area.getLightRaw(x - area.minBlockX, y, z - area.minBlockZ);
  }

  public Area getArea(int areaX, int areaZ) {
    AreaReference areaReference = Pools.obtainAreaReference().setFromAreaCoordinates(areaX, areaZ);
    Area area = getArea(areaReference);
    Pools.free(AreaReference.class, areaReference);
    return area;
  }

  public Area getArea(AreaReference areaReference) {
    return getArea(areaReference, true);
  }

  public Area getArea(AreaReference areaReference, boolean request) {
    lock.readLock();
    Area area = map.get(areaReference);
    lock.readUnlock();

    if (area != null) {
      return area;
    } else if (request) {
      requestRegion(new WorldRegion(areaReference));
    }
    return null;
  }

  public abstract void requestRegion(MultiAreaReference references);

  public void setBlock(Block block, int x, int y, int z) {
    Area area = getArea(CoordinateConverter.area(x), CoordinateConverter.area(z));
    if (area != null) area.setBlock(block, x - area.minBlockX, y, z - area.minBlockZ);
  }

  public void tick() {
    lock.writeLock();
    Iterator<Entry<UUID, Entity>> iterator = entities.entrySet().iterator();
    while (iterator.hasNext()) {
      Entry<UUID, Entity> entry = iterator.next();
      if (entry.getValue().update()) {
        entry.getValue().dispose();
        UUID uuid = entry.getKey();
        iterator.remove();
        removeEntity(uuid);
      }
    }
    time++;
    if (time >= MAX_TIME) time = 0;
    lock.writeUnlock();
  }

  public float getSunlight() {
    lock.readLock();
    int t = time < MAX_TIME / 2 ? time : MAX_TIME - time;
    float f = ((float) t) / (((float) MAX_TIME) / 2f);
    lock.readUnlock();
    return f;
  }

  public TerrainGenerator getTerrainGenerator() {
    return terrainGenerator;
  }

  @Override
  public void dispose() {
    lock.writeLock();

    disposed.set(true);

    for (Entry<AreaReference, Area> entry : map.entrySet()) {
      entry.getValue().unload();
    }
    map.clear();
    for (Entity entity : entities.values()) {
      entity.dispose();
    }
    entities.clear();

    lock.writeUnlock();
  }

  public Entity getEntity(UUID uuid) {
    lock.readLock();
    return lock.readUnlock(entities.get(uuid));
  }

  public void addEntity(Entity entity) {
    lock.writeLock();
    entities.put(entity.uuid, entity);
    lock.writeUnlock();
  }

  public void removeEntity(UUID uuid) {
    lock.writeLock();
    Entity remove = entities.remove(uuid);
    if (remove != null) remove.dispose();
    lock.writeUnlock();
  }

  public void updateEntity(DataGroup data) {
    lock.writeLock();
    UUID uuid = (UUID) data.get("uuid");
    Entity entity = entities.get(uuid);
    if (entity != null) {
      entity.read(data);
    } else {
      Log.warning("No entity with uuid " + uuid.toString());
    }
    lock.writeUnlock();
  }

  public void syncEntity(UUID uuid) {

  }
}
