package ethanjones.cubes.graphics.rendering;

import ethanjones.cubes.core.logging.Log;
import ethanjones.cubes.core.settings.Settings;
import ethanjones.cubes.core.system.Debug;
import ethanjones.cubes.core.system.Pools;
import ethanjones.cubes.graphics.world.AreaRenderStatus;
import ethanjones.cubes.graphics.world.AreaRenderer;
import ethanjones.cubes.graphics.world.AreaRendererPool;
import ethanjones.cubes.input.CameraController;
import ethanjones.cubes.side.client.CubesClient;
import ethanjones.cubes.side.common.Cubes;
import ethanjones.cubes.world.CoordinateConverter;
import ethanjones.cubes.world.reference.AreaReference;
import ethanjones.cubes.world.storage.Area;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.utils.Disposable;

import static ethanjones.cubes.graphics.Graphics.modelBatch;

public class WorldRenderer implements Disposable {

  public Environment environment;
  public PerspectiveCamera camera;

  static {
    Pools.registerType(AreaRenderer.class, new AreaRendererPool());
  }

  public WorldRenderer() {
    environment = new Environment();
    environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
    environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.9f, -0.8f));

    camera = new PerspectiveCamera(Settings.getIntegerSettingValue(Settings.GRAPHICS_FOV), Gdx.graphics.getWidth(), Gdx.graphics.getHeight()) {
      @Override
      public void update(boolean b) {
        viewportWidth = Gdx.graphics.getWidth();
        viewportHeight = Gdx.graphics.getHeight();
        super.update(b);
      }
    };
    camera.near = 0.1f;
    camera.far = 300f;

    Cubes.getClient().inputChain.cameraController = new CameraController(camera);
  }

  public void render() {
    modelBatch.begin(camera);

    int renderDistance = Settings.getIntegerSettingValue(Settings.GRAPHICS_VIEW_DISTANCE);

    AreaReference pos = Pools.obtainAreaReference().setFromPositionVector3(Cubes.getClient().player.position);
    int yPos = CoordinateConverter.area(Cubes.getClient().player.position.y);
    for (int areaX = pos.areaX - renderDistance; areaX <= pos.areaX + renderDistance; areaX++) {
      for (int areaZ = pos.areaZ - renderDistance; areaZ <= pos.areaZ + renderDistance; areaZ++) {
        Area area = CubesClient.getClient().world.getArea(areaX, areaZ);
        if (area == null || area.isBlank()) continue;
        if (!areaInFrustum(area, camera.frustum)) {
          AreaRenderer.free(area.areaRenderer);
          continue;
        }
        for (int ySection = Math.max(yPos - renderDistance, 0); ySection <= yPos + renderDistance; ySection++) {
          if (ySection >= area.height) break;
          if (areaInFrustum(area, ySection, camera.frustum) && shouldRender(area, ySection)) {
            if (area.areaRenderer[ySection] == null) {
              Pools.obtain(AreaRenderer.class).set(area, ySection);
            }
            modelBatch.render(area.areaRenderer[ySection], environment);
          } else if (area.areaRenderer[ySection] != null) {
            AreaRenderer.free(area.areaRenderer[ySection]);
          }
        }
      }
    }

    modelBatch.end();
  }

  public boolean areaInFrustum(Area area, Frustum frustum) {
    return frustum.boundsInFrustum(area.minBlockX + Area.HALF_SIZE_BLOCKS, Area.MAX_Y / 2f, area.minBlockZ + Area.HALF_SIZE_BLOCKS, Area.HALF_SIZE_BLOCKS, Area.MAX_Y / 2f, Area.HALF_SIZE_BLOCKS);
  }

  public boolean areaInFrustum(Area area, int ySection, Frustum frustum) {
    return frustum.boundsInFrustum(area.minBlockX + Area.HALF_SIZE_BLOCKS, (ySection * Area.SIZE_BLOCKS) + Area.HALF_SIZE_BLOCKS, area.minBlockZ + Area.HALF_SIZE_BLOCKS, Area.HALF_SIZE_BLOCKS, Area.HALF_SIZE_BLOCKS, Area.HALF_SIZE_BLOCKS);
  }

  public boolean shouldRender(Area area, int ySection) {
    int status = area.renderStatus[ySection];
    if (status == AreaRenderStatus.UNKNOWN) status = AreaRenderStatus.update(area, ySection);
    if (status == AreaRenderStatus.EMPTY) return false;
    if (status == AreaRenderStatus.COMPLETE) {
      if (ySection > 0 && (area.renderStatus[ySection - 1] & AreaRenderStatus.COMPLETE_MAX_Y) != AreaRenderStatus.COMPLETE_MAX_Y)
        return true;
      if (ySection < area.renderStatus.length - 1 && (area.renderStatus[ySection + 1] & AreaRenderStatus.COMPLETE_MIN_Y) != AreaRenderStatus.COMPLETE_MIN_Y)
        return true;
      Area maxX = Cubes.getClient().world.getArea(area.areaX + 1, area.areaZ);
      if (maxX != null && !maxX.isBlank())
        if (maxX.renderStatus.length < ySection - 1 || (maxX.renderStatus[ySection] & AreaRenderStatus.COMPLETE_MIN_X) != AreaRenderStatus.COMPLETE_MIN_X)
          return true;
      Area minX = Cubes.getClient().world.getArea(area.areaX - 1, area.areaZ);
      if (minX != null && !minX.isBlank())
        if (minX.renderStatus.length < ySection - 1 || (minX.renderStatus[ySection] & AreaRenderStatus.COMPLETE_MAX_X) != AreaRenderStatus.COMPLETE_MAX_X)
          return true;
      Area maxZ = Cubes.getClient().world.getArea(area.areaX, area.areaZ + 1);
      if (maxZ != null && !maxZ.isBlank())
        if (maxZ.renderStatus.length < ySection - 1 || (maxZ.renderStatus[ySection] & AreaRenderStatus.COMPLETE_MIN_Z) != AreaRenderStatus.COMPLETE_MIN_Z)
          return true;
      Area minZ = Cubes.getClient().world.getArea(area.areaX, area.areaZ - 1);
      if (minZ != null && !minZ.isBlank())
        if (minZ.renderStatus.length < ySection - 1 || (minZ.renderStatus[ySection] & AreaRenderStatus.COMPLETE_MAX_Z) != AreaRenderStatus.COMPLETE_MAX_Z)
          return true;
      return false;
    }
    return true;
  }

  @Override
  public void dispose() {

  }
}
