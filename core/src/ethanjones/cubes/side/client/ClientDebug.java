package ethanjones.cubes.side.client;

import ethanjones.cubes.core.platform.Compatibility;
import ethanjones.cubes.core.system.Branding;
import ethanjones.cubes.core.util.PerSecond;
import ethanjones.cubes.entity.living.player.Player;
import ethanjones.cubes.graphics.world.AreaRenderer;
import ethanjones.cubes.side.common.Cubes;
import ethanjones.cubes.world.CoordinateConverter;
import ethanjones.cubes.world.storage.Area;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.WindowedMean;

import java.text.DecimalFormat;

public class ClientDebug {

  private static final String lineSeparator = System.getProperty("line.separator");
  static WindowedMean ms = new WindowedMean(50);
  private static PerSecond fps = new PerSecond(10);
  private static StringBuilder builder = new StringBuilder(250).append(Branding.DEBUG).append(lineSeparator);
  private static int brandingDebugLength = builder.length();
  private static DecimalFormat twoDP = new DecimalFormat("0.00");
  private static DecimalFormat oneDP = new DecimalFormat("0.0");
  
  protected static void frame() {
    fps.tick();
  }

  public static String getDebugString() {
    Vector3 p = Cubes.getClient().player.position;
    ms.addValue(Gdx.graphics.getRawDeltaTime() * 1000f);

    builder.setLength(brandingDebugLength);
    builder.append("FPS:").append(Gdx.graphics.getFramesPerSecond()).append(" MS:").append(twoDP.format(ms.getMean())).append(" MEM:").append(Compatibility.get().getFreeMemory()).append("MB").append(lineSeparator);
    builder.append("TPS C:").append(Cubes.getClient().ticksPerSecond.last()).append(" A:").append(oneDP.format(Cubes.getClient().ticksPerSecond.average()));
    if (Cubes.getServer() != null) builder.append(" S:").append(Cubes.getServer().ticksPerSecond.last()).append(" A:").append(oneDP.format(Cubes.getServer().ticksPerSecond.average()));
    builder.append(lineSeparator);
    builder.append("POS X:").append(twoDP.format(p.x)).append("(").append(CoordinateConverter.area(p.x)).append(")").append(" Y:").append(twoDP.format(p.y)).append("(").append(CoordinateConverter.area(p.y)).append(")").append(" Z:").append(twoDP.format(p.z)).append("(").append(CoordinateConverter.area(p.z)).append(")").append(lineSeparator);
    builder.append("DIR X:").append(twoDP.format(Cubes.getClient().player.angle.x)).append(" Y:").append(twoDP.format(Cubes.getClient().player.angle.y)).append(" Z:").append(twoDP.format(Cubes.getClient().player.angle.z)).append(lineSeparator);
    builder.append("R A:").append(AreaRenderer.renderedThisFrame).append(" M:").append(AreaRenderer.renderedMeshesThisFrame).append(lineSeparator);
    builder.append("W B:").append(getBlockLight()).append(" S:").append(getSunlight()).append(" T:").append(Cubes.getClient().world.time);
    return builder.toString();
  }

  private static int getBlockLight() {
    Player player = Cubes.getClient().player;
    Area area = Cubes.getClient().world.getArea(CoordinateConverter.area(player.position.x), CoordinateConverter.area(player.position.z));
    if (area != null) {
      int x = CoordinateConverter.block(player.position.x);
      int y = CoordinateConverter.block(player.position.y - player.height);
      int z = CoordinateConverter.block(player.position.z);
      if (y > area.maxY || y < 0) return 0;
      return area.getLight(x - area.minBlockX, y, z - area.minBlockZ);
    }
    return 0;
  }

  private static int getSunlight() {
    Player player = Cubes.getClient().player;
    Area area = Cubes.getClient().world.getArea(CoordinateConverter.area(player.position.x), CoordinateConverter.area(player.position.z));
    if (area != null) {
      int x = CoordinateConverter.block(player.position.x);
      int y = CoordinateConverter.block(player.position.y - player.height);
      int z = CoordinateConverter.block(player.position.z);
      if (y > area.maxY || y < 0) return 0;
      return area.getSunlight(x - area.minBlockX, y, z - area.minBlockZ);
    }
    return 0;
  }
}
