package ethanjones.cubes.core.system;

import com.badlogic.gdx.utils.Pool;
import java.util.HashMap;

import ethanjones.cubes.world.reference.AreaReference;
import ethanjones.cubes.world.reference.AreaReferencePool;
import ethanjones.cubes.world.reference.BlockReference;
import ethanjones.cubes.world.reference.BlockReferencePool;

public class Pools {

  private static final HashMap<Class, Pool> pools = new HashMap<Class, Pool>();

  static {
    registerType(AreaReference.class, new AreaReferencePool());

    registerType(BlockReference.class, new BlockReferencePool());
  }

  public static void registerType(Class c, Pool pool) {
    Object obj = pool.obtain();
    Class<?> objClass = obj.getClass();
    pool.free(obj);
    if (objClass.equals(c)) {
      synchronized (pools) {
        if (!pools.containsKey(c)) {
          pools.put(c, pool);
        }
      }
    } else {
      throw new CubesException("Calling obtain on " + pool + " does not return " + c.getName());
    }
  }

  public static <T> T obtain(Class<T> c) {
    Pool<T> pool = pool(c);
    if (pool == null) return null;
    synchronized (pool) {
      return pool.obtain();
    }
  }

  public static <T> void free(Class<T> c, T obj) {
    Pool<T> pool = pool(c);
    if (pool == null) return;
    synchronized (pool) {
      pool.free(obj);
    }
  }

  private static <T> Pool<T> pool(Class<T> c) {
    synchronized (pools) {
      return pools.get(c);
    }
  }
  
}
