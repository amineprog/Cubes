package ethanjones.modularworld.graphics.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import ethanjones.modularworld.ModularWorld;
import ethanjones.modularworld.block.Block;
import ethanjones.modularworld.core.util.DebugHud;
import ethanjones.modularworld.graphics.GameModel;
import ethanjones.modularworld.world.coordinates.AreaCoordinates;
import ethanjones.modularworld.world.coordinates.BlockCoordinates;
import ethanjones.modularworld.world.storage.Area;

public class BlockRenderer {
  
  public Vector3 position;
  public static int RENDERING_DISTANCE_AREAS = 2;
  
  public Environment lights;
  public PerspectiveCamera camera;
  
  private Renderer renderer;
  
  public BlockRenderer(Renderer renderer) {
    this.renderer = renderer;
    
    position = new Vector3(40, 10, 40);
    
    lights = new Environment();
    lights.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
    lights.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
    
    setupCamera();
    
  }
  
  public void setupCamera() {
    camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    camera.position.set(position);
    camera.near = 1f;
    camera.far = 300f;
    camera.update();
  }
  
  public void render() {
    ModularWorld.instance.player.updateRotation();
    DebugHud.lineB = ModularWorld.instance.player.angleX + " " + ModularWorld.instance.player.angleY;
    
    long l = System.currentTimeMillis();
    int r = 0;
    BlockCoordinates pos = new BlockCoordinates(position.x, position.y, position.z);
    for (int areaX = pos.areaX - RENDERING_DISTANCE_AREAS; areaX < pos.areaX + RENDERING_DISTANCE_AREAS; areaX++) {
      for (int areaY = pos.areaY - RENDERING_DISTANCE_AREAS; areaY < pos.areaY + RENDERING_DISTANCE_AREAS; areaY++) {
        for (int areaZ = pos.areaZ - RENDERING_DISTANCE_AREAS; areaZ < pos.areaZ + RENDERING_DISTANCE_AREAS; areaZ++) {
          if (areaY < 0) {
            continue;
          }
          AreaCoordinates areaC = new AreaCoordinates(areaX, areaY, areaZ);
          Area area = ModularWorld.instance.world.getArea(areaC);
          for (int x = 0; x < Area.S; x++) {
            for (int y = 0; y < Area.S; y++) {
              for (int z = 0; z < Area.S; z++) {
                Block b = area.getBlock(x, y, z);
                if (b != null && !Block.isCovered(x, y, z)) {
                  GameModel i = b.getModelInstance().setPos(x + area.minBlockX, y + area.minBlockY, z + area.minBlockZ);
                  if (isVisible(i)) {
                    renderer.modelBatch.render(i, lights);
                    r++;
                  }
                }
              }
            }
          }
        }
      }
    }
    long t = System.currentTimeMillis() - l;
    DebugHud.lineA = t + " " + (1000d / t) + " " + r;
  }
  
  protected boolean isVisible(GameModel model) {
    return true;// camera.frustum.boundsInFrustum(model.bounds);
  }
  
}
