package ethanjones.cubes.world.client;

import ethanjones.cubes.core.settings.Settings;
import ethanjones.cubes.entity.Entity;
import ethanjones.cubes.graphics.world.AreaRenderer;
import ethanjones.cubes.side.common.Cubes;
import ethanjones.cubes.world.CoordinateConverter;
import ethanjones.cubes.world.World;
import ethanjones.cubes.world.reference.AreaReference;
import ethanjones.cubes.world.reference.multi.MultiAreaReference;
import ethanjones.cubes.world.storage.Area;
import ethanjones.cubes.world.thread.GenerationTask;
import ethanjones.cubes.world.thread.WorldRequestParameter;

import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

public class WorldClient extends World {

  private ArrayList<Area> removed = new ArrayList<Area>();
  private AreaReference playerArea = new AreaReference();
  private final int renderDistance = Settings.getIntegerSettingValue(Settings.GRAPHICS_VIEW_DISTANCE); //keep 3 extra

  public WorldClient() {
    super(null);
  }

  @Override
  public void tick() {
    updateLock.writeLock();
    super.tick();

    playerArea.setFromPositionVector3(Cubes.getClient().player.position);
    removed.clear();

    map.lock.writeLock();
    Iterator<Area> areaIterator = map.iterator();
    while (areaIterator.hasNext()) {
      Area area = areaIterator.next();
      int dist = Math.max(Math.abs(area.areaX - playerArea.areaX), Math.abs(area.areaZ - playerArea.areaZ));
      if (dist > renderDistance + 3) {
        removed.add(area);
        areaIterator.remove();
      } else if (dist > renderDistance + 1) {
        AreaRenderer.free(area.areaRenderer);
      }
    }
    map.lock.writeUnlock();

    for (Area area : removed) {
      area.unload();
    }
    
    entities.lock.writeLock();
    Iterator<Entry<UUID, Entity>> entityIterator = entities.entrySet().iterator();
    while (entityIterator.hasNext()) {
      Entry<UUID, Entity> entry = entityIterator.next();
      Entity entity = entry.getValue();
      int aX = CoordinateConverter.area(entity.position.x);
      int aZ = CoordinateConverter.area(entity.position.z);
      int dist = Math.max(Math.abs(aX - playerArea.areaX), Math.abs(aZ - playerArea.areaZ));
      if (dist > renderDistance + 1) {
        entity.dispose();
        entityIterator.remove();
      }
    }
    entities.lock.writeUnlock();
    updateLock.writeUnlock();
  }

  public Color getSkyColour() {
    float light = getSunlight();
    if (light <= 0.3f) return Color.BLACK;
    if (light >= 0.7f) return Color.SKY;
    return Color.BLACK.cpy().lerp(Color.SKY, (light - 0.3f) * 2.5f);
  }

  @Override
  public void dispose() {
    super.dispose();
  }

  @Override
  public GenerationTask requestRegion(MultiAreaReference references, WorldRequestParameter parameter) {
    // not needed, server sends needed areas automatically
    return null;
  }
}
