package ethanjones.cubes.entity;

import ethanjones.cubes.block.Block;
import ethanjones.cubes.core.system.Debug;
import ethanjones.cubes.core.util.VectorUtil;
import ethanjones.cubes.side.Side;
import ethanjones.cubes.side.Sided;
import ethanjones.cubes.side.common.Cubes;
import ethanjones.cubes.world.CoordinateConverter;
import ethanjones.cubes.world.World;
import ethanjones.data.DataGroup;
import ethanjones.data.DataParser;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

import java.util.UUID;

public class Entity implements DataParser, Disposable {

  public UUID uuid;
  public float height = 0f;
  public final Vector3 position;
  public final Vector3 angle;
  public final Vector3 motion;
  public final String id;

  public Entity(String id) {
    this(UUID.randomUUID(), id, new Vector3(), new Vector3(1f, 0f, 0f));
  }

  public Entity(String id, Vector3 position, Vector3 angle) {
    this(UUID.randomUUID(), id, position, angle);
  }

  public Entity(UUID uuid, String id, Vector3 position, Vector3 angle) {
    this.uuid = uuid;
    this.id = id;
    this.position = position;
    this.motion = new Vector3();
    this.angle = angle;
  }

  /**
   * @return true to be removed
   */
  public boolean update() {
    if (Sided.getSide() == Side.Server) {
      World world = Sided.getCubes().world;
      if (world.getArea(CoordinateConverter.area(position.x), CoordinateConverter.area(position.z)) == null)
        return false;
      float f = position.y - height;
      int y = CoordinateConverter.block(f - 0.01f);
      Block b = world.getBlock(CoordinateConverter.block(position.x), y, CoordinateConverter.block(position.z));
      if (b == null || f > y + 1.01f) {
        position.y -= Math.max(0.1f, f - (y + 1));
        world.syncEntity(uuid);
      } else {
        if (motion.y < 0) motion.y = 0;
        float newY = y + 1 + height;
        if (position.y != newY) {
          position.y = y + 1 + height;
          world.syncEntity(uuid);
        }
      }
      if (!motion.isZero()) {
        float scl = Cubes.tickMS / 1000f * 4f;
        position.add(motion.x * scl, motion.y * scl, motion.z * scl);
        motion.scl(0.9f);
        if (motion.len2() < 0.01f) motion.set(0f, 0f, 0f);
        world.syncEntity(uuid);
      }
    }
    return false;
  }

  @Override
  public DataGroup write() {
    DataGroup dataGroup = new DataGroup();
    dataGroup.put("id", id);
    dataGroup.put("uuid", uuid);
    dataGroup.put("pos", VectorUtil.array(position));
    dataGroup.put("motion", VectorUtil.array(motion));
    dataGroup.put("ang", VectorUtil.array(angle));
    return dataGroup;
  }

  @Override
  public void read(DataGroup data) {
    this.position.set(VectorUtil.array(data.getArray("pos", Float.class)));
    this.motion.set(VectorUtil.array(data.getArray("motion", Float.class)));
    this.angle.set(VectorUtil.array(data.getArray("ang", Float.class)));
    this.uuid = (UUID) data.get("uuid");
    if (!this.id.equals(data.getString("id")))
      throw new IllegalArgumentException(this.id + "!=" + data.getString("id"));
  }

  public static Entity readType(DataGroup data) {
    try {
      Class<? extends Entity> c = EntityManager.toClass(data.getString("id"));
      Entity entity = c.newInstance();
      entity.read(data);
      return entity;
    } catch (Exception e) {
      Debug.crash(e);
      return null;
    }
  }

  @Override
  public void dispose() {

  }

  @Override
  public String toString() {
    return id + " " + uuid.toString();
  }
}
