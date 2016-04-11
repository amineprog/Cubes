package ethanjones.cubes.world.light;

import ethanjones.cubes.side.Side;
import ethanjones.cubes.side.Sided;
import ethanjones.cubes.world.CoordinateConverter;
import ethanjones.cubes.world.World;
import ethanjones.cubes.world.storage.Area;

class LightWorldSection {
  public final int initialAreaX;
  public final int initialAreaZ;
  public final Area[][] areas = new Area[3][3];
  private final World world;
  private final int ySection;

  LightWorldSection(Area initial, int ySection) {
    this.world = initial.world;
    this.ySection = ySection;
    initialAreaX = initial.areaX;
    initialAreaZ = initial.areaZ;

    areas[0][0] = world.getArea(initialAreaX - 1, initialAreaZ - 1);
    areas[0][1] = world.getArea(initialAreaX - 1, initialAreaZ);
    areas[0][2] = world.getArea(initialAreaX - 1, initialAreaZ + 1);
    areas[1][0] = world.getArea(initialAreaX, initialAreaZ - 1);
    areas[1][1] = initial;
    areas[1][2] = world.getArea(initialAreaX, initialAreaZ + 1);
    areas[2][0] = world.getArea(initialAreaX + 1, initialAreaZ - 1);
    areas[2][1] = world.getArea(initialAreaX + 1, initialAreaZ);
    areas[2][2] = world.getArea(initialAreaX + 1, initialAreaZ + 1);

    for (Area[] areaArr : areas) {
      for (Area area : areaArr) {
        area.lock.writeLock();
      }
    }
  }

//    protected int getLight(int x, int y, int z) {
//      Area a = getArea(CoordinateConverter.area(x), CoordinateConverter.area(z));
//      return (a.light[getRef(x - a.minBlockX, y, z - a.minBlockZ)]) & 0xF;
//    }
//
//    protected void setLight(int x, int y, int z, int l) {
//      Area a = getArea(CoordinateConverter.area(x), CoordinateConverter.area(z));
//      int ref = getRef(x - a.minBlockX, y, z - a.minBlockZ);
//      a.light[ref] = (byte) ((a.light[ref] & 0xF0) | l);
//    }

  protected int maxY(int x, int z) {
    return getArea(CoordinateConverter.area(x), CoordinateConverter.area(z)).maxY;
  }

  protected Area getArea(int areaX, int areaZ) {
    int dX = areaX - initialAreaX;
    int dZ = areaZ - initialAreaZ;
    return areas[dX + 1][dZ + 1];
  }

  protected void unlock() {
    boolean isClient = Sided.getSide() == Side.Client;
    for (Area[] areaArr : areas) {
      for (Area area : areaArr) {
        if (ySection != -1) {
          if (isClient && ySection - 1 < area.height) area.updateRender(ySection - 1);
          if (isClient && ySection < area.height) area.updateRender(ySection);
          if (isClient && ySection + 1 < area.height) area.updateRender(ySection + 1);
        }

        area.lock.writeUnlock();
      }
    }
  }
}
