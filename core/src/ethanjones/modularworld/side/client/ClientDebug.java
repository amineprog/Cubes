package ethanjones.modularworld.side.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import ethanjones.modularworld.core.system.Branding;
import ethanjones.modularworld.core.system.Memory;
import ethanjones.modularworld.core.util.LongAverage;
import ethanjones.modularworld.core.util.MathHelper;
import ethanjones.modularworld.graphics.menu.Fonts;

public class ClientDebug {

  private static final String lineSeparator = System.getProperty("line.separator");
  static LongAverage fps = new LongAverage();
  static LongAverage loop = new LongAverage();
  static long lastTime = System.currentTimeMillis();
  private static String debugString = "";

  public static void update() {
    Vector3 p = ModularWorldClient.instance.player.position;
    fps.add(Gdx.graphics.getFramesPerSecond());
    loop.add(System.currentTimeMillis() - lastTime);
    lastTime = System.currentTimeMillis();

    debugString = Branding.DEBUG + lineSeparator
      + "FPS:" + fps.getCurrent() + " AFPS:" + fps.getAverage() + lineSeparator
      + "RAM T:" + Memory.max + "MB F:" + Memory.totalFree + "MB U:" + Memory.used + "MB" + lineSeparator
      + lineSeparator
      + "P X:" + String.format("%.2f", p.x) + " Y:" + String.format("%.2f", p.y) + " Z:" + String.format("%.2f", p.z) + lineSeparator
      + "A X:" + MathHelper.area(p.x) + " Y:" + MathHelper.area(p.y) + " Z:" + MathHelper.area(p.z) + lineSeparator
      + "Z X:" + MathHelper.zone(MathHelper.area(p.x)) + " Z:" + MathHelper.zone(MathHelper.area(p.z)) + lineSeparator
      + "D X:" + String.format("%02f", ModularWorldClient.instance.player.angle.x) + " Y:" + String.format("%02f", ModularWorldClient.instance.player.angle.y) + " Z:" + String.format("%02f", ModularWorldClient.instance.player.angle.z)
      + lineSeparator
      + "L MS:" + String.format("%01d", loop.getCurrent()) + " AMS:" + String.format("%01d", loop.getAverage()) + lineSeparator
      + lineSeparator
      + "C:" + GLProfiler.calls + lineSeparator
      + "DC:" + GLProfiler.drawCalls + lineSeparator
      + "S:" + GLProfiler.shaderSwitches + lineSeparator
      + "T:" + GLProfiler.textureBindings + lineSeparator
      + "V:" + GLProfiler.vertexCount.latest + lineSeparator;

    GLProfiler.calls = 0;
    GLProfiler.drawCalls = 0;
    GLProfiler.shaderSwitches = 0;
    GLProfiler.textureBindings = 0;
  }

  public static class DebugLabel extends Label {

    static final LabelStyle style = new LabelStyle();

    static {
      style.font = Fonts.Size1;
    }

    public DebugLabel() {
      super(debugString, style);
    }

    public void validate() {
      setText(debugString);
      super.validate();
    }

  }
}
