package ethanjones.cubes.world;

import com.badlogic.gdx.utils.Pool;
import java.util.HashMap;

import ethanjones.cubes.core.system.Threads;
import ethanjones.cubes.world.generator.WorldGenerator;
import ethanjones.cubes.world.reference.AreaReference;
import ethanjones.cubes.world.storage.Area;
import ethanjones.cubes.world.thread.GenerateWorldCallable;

public class WorldServer extends World {

  private static class Key {

    int x, y, z;

    public Key set(AreaReference areaReference) {
      this.x = areaReference.areaX;
      this.y = areaReference.areaY;
      this.z = areaReference.areaZ;
      return this;
    }

    @Override
    public int hashCode() {
      return x ^ y ^ z;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Key) {
        Key key = (Key) obj;
        return key.x == x && key.y == y && key.z == z;
      }
      return false;
    }
  }

  private static class KeyPool extends Pool<Key> {

    @Override
    public synchronized Key newObject() {
      return new Key();
    }
  }
  private final WorldGenerator worldGenerator;
  private final HashMap<Key, Area> areaMap;
  private final KeyPool keyPool;

  public WorldServer(WorldGenerator worldGenerator) {
    super();
    this.worldGenerator = worldGenerator;
    areaMap = new HashMap<Key, Area>();
    keyPool = new KeyPool();
  }

  public Area getAreaInternal(AreaReference areaReference, boolean request, boolean returnBlank) {
    Key key;
    Area area;
    synchronized (keyPool) {
      key = keyPool.obtain().set(areaReference);
    }
    synchronized (this) {
      area = areaMap.get(key);
    }
    if (area != null) {
      synchronized (keyPool) {
        keyPool.free(key);
      }
      return area;
    } else if (request) {
      requestArea(areaReference);
    }
    return returnBlank ? BLANK_AREA : null;
  }

  @Override
  public void dispose() {

  }

  public WorldGenerator getWorldGenerator() {
    return worldGenerator;
  }

  public boolean setAreaInternal(AreaReference areaReference, Area area) {
    Key key;
    synchronized (keyPool) {
      key = keyPool.obtain().set(areaReference);
    }
    synchronized (this) {
      areaMap.put(key, area);
    }
    return true;
  }

  public void requestArea(AreaReference areaReference) {
    setAreaInternal(areaReference, World.BLANK_AREA);
    Threads.execute(new GenerateWorldCallable(areaReference, this));
  }








}
