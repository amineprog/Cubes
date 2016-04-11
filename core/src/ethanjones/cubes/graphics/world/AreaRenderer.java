package ethanjones.cubes.graphics.world;

import ethanjones.cubes.block.Block;
import ethanjones.cubes.core.system.Pools;
import ethanjones.cubes.core.util.BlockFace;
import ethanjones.cubes.side.Sided;
import ethanjones.cubes.side.common.Cubes;
import ethanjones.cubes.world.client.WorldClient;
import ethanjones.cubes.world.light.SunLight;
import ethanjones.cubes.world.reference.AreaReference;
import ethanjones.cubes.world.storage.Area;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import sun.security.provider.Sun;

import java.util.ArrayList;

import static ethanjones.cubes.graphics.world.AreaMesh.SAFE_VERTICES;
import static ethanjones.cubes.graphics.world.FaceVertices.*;
import static ethanjones.cubes.world.storage.Area.*;

public class AreaRenderer implements RenderableProvider, Disposable, Pool.Poolable {

  public static final int MIN_AREA = 0;
  public static final int MAX_AREA = SIZE_BLOCKS - 1;
  public static int MAX_REFRESH_PER_FRAME = 8;

  private static int refreshedThisFrame = 0;

  public boolean refresh = true;
  Vector3 offset = new Vector3();
  private Area area;
  private int ySection;
  private ArrayList<AreaMesh> meshs = new ArrayList<AreaMesh>();

  @Override
  public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
    if (area == null) return;
    if (refresh) {
      free(meshs);
      if (refreshedThisFrame < MAX_REFRESH_PER_FRAME) {
        if (calculateVertices()) {
          refreshedThisFrame++;
          refresh = false;
        } else {
          return;
        }
      } else {
        return;
      }
    }
    if (meshs.size() <= 0) return;
    for (AreaMesh mesh : meshs) {
      renderables.add(mesh.renderable(pool));
    }
  }

  public boolean calculateVertices() {
    if (area == null) return false;
    float[] vertices = AreaMesh.vertices;

    WorldClient worldClient = (WorldClient) Cubes.getClient().world;
    worldClient.lock.readLock();
    AreaReference areaReference = new AreaReference();
    Area maxX = worldClient.map.get(areaReference.setFromAreaCoordinates(area.areaX + 1, area.areaZ));
    Area minX = worldClient.map.get(areaReference.setFromAreaCoordinates(area.areaX - 1, area.areaZ));
    Area maxZ = worldClient.map.get(areaReference.setFromAreaCoordinates(area.areaX, area.areaZ + 1));
    Area minZ = worldClient.map.get(areaReference.setFromAreaCoordinates(area.areaX, area.areaZ - 1));
    worldClient.lock.readUnlock();
    if (maxX == null || minX == null || maxZ == null || minZ == null) return false;

    int i = ySection * SIZE_BLOCKS_CUBED;
    int vertexOffset = 0;
    area.lock.readLock();

    for (int y = ySection * SIZE_BLOCKS; y < (ySection + 1) * SIZE_BLOCKS; y++) {
      for (int z = 0; z < SIZE_BLOCKS; z++) {
        for (int x = 0; x < SIZE_BLOCKS; x++, i++) {
          int blockInt = area.blocks[i];
          if (blockInt > 0) {
            Block block = Sided.getIDManager().toBlock(blockInt);
            if (block == null) continue;
            BlockTextureHandler textureHandler = block.getTextureHandler();
            if (x < SIZE_BLOCKS - 1) {
              if (area.blocks[i + MAX_X_OFFSET] == 0) {
                vertexOffset = createMaxX(offset, textureHandler.getSide(BlockFace.posX), x, y, z, area.light[i + MAX_X_OFFSET], vertices, vertexOffset);
              }
            } else if (maxX == null || maxX.getBlock(MIN_AREA, y, z) == null) {
              vertexOffset = createMaxX(offset, textureHandler.getSide(BlockFace.posX), x, y, z, (maxX == null ? 0 : maxX.getLightRaw(MIN_AREA, y, z)), vertices, vertexOffset);
            }
            if (x > 0) {
              if (area.blocks[i + MIN_X_OFFSET] == 0) {
                vertexOffset = createMinX(offset, textureHandler.getSide(BlockFace.negX), x, y, z, area.light[i + MIN_X_OFFSET], vertices, vertexOffset);
              }
            } else if (minX == null || minX.getBlock(MAX_AREA, y, z) == null) {
              vertexOffset = createMinX(offset, textureHandler.getSide(BlockFace.negX), x, y, z, (minX == null ? 0 : minX.getLightRaw(MAX_AREA, y, z)), vertices, vertexOffset);
            }
            if (y < area.maxY) {
              if (area.blocks[i + MAX_Y_OFFSET] == 0) {
                vertexOffset = createMaxY(offset, textureHandler.getSide(BlockFace.posY), x, y, z, area.light[i + MAX_Y_OFFSET], vertices, vertexOffset);
              }
            } else {
              vertexOffset = createMaxY(offset, textureHandler.getSide(BlockFace.posY), x, y, z, SunLight.MAX_SUNLIGHT, vertices, vertexOffset); //FIXME fix the light at the top and bottom of an area
            }
            if (y > 0) {
              if (area.blocks[i + MIN_Y_OFFSET] == 0) {
                vertexOffset = createMinY(offset, textureHandler.getSide(BlockFace.negY), x, y, z, area.light[i + MIN_Y_OFFSET], vertices, vertexOffset);
              }
            } else {
              vertexOffset = createMinY(offset, textureHandler.getSide(BlockFace.negY), x, y, z, 0, vertices, vertexOffset); //FIXME fix the light at the top and bottom of an area
            }
            if (z < SIZE_BLOCKS - 1) {
              if (area.blocks[i + MAX_Z_OFFSET] == 0) {
                vertexOffset = createMaxZ(offset, textureHandler.getSide(BlockFace.posZ), x, y, z, area.light[i + MAX_Z_OFFSET], vertices, vertexOffset);
              }
            } else if (maxZ == null || maxZ.getBlock(x, y, MIN_AREA) == null) {
              vertexOffset = createMaxZ(offset, textureHandler.getSide(BlockFace.posZ), x, y, z, (maxZ == null ? 0 : maxZ.getLightRaw(x, y, MIN_AREA)), vertices, vertexOffset);
            }
            if (z > 0) {
              if (area.blocks[i + MIN_Z_OFFSET] == 0) {
                vertexOffset = createMinZ(offset, textureHandler.getSide(BlockFace.negZ), x, y, z, area.light[i + MIN_Z_OFFSET], vertices, vertexOffset);
              }
            } else if (minZ == null || minZ.getBlock(x, y, MAX_AREA) == null) {
              vertexOffset = createMinZ(offset, textureHandler.getSide(BlockFace.negZ), x, y, z, (minZ == null ? 0 : minZ.getLightRaw(x, y, MAX_AREA)), vertices, vertexOffset);
            }
            if (vertexOffset >= SAFE_VERTICES) {
              save(vertexOffset);
              vertexOffset = 0;
            }
          }
        }
      }
    }
    if (vertexOffset > 0) save(vertexOffset);
    area.lock.readUnlock();
    return true;
  }

  private void save(int vertexCount) {
    AreaMesh areaMesh = Pools.obtain(AreaMesh.class);
    areaMesh.saveVertices(vertexCount);
    meshs.add(areaMesh);
  }

  @Override
  public void dispose() {
    free(meshs);
  }

  @Override
  public void reset() {
    if (area != null) area.areaRenderer[ySection] = null;
    free(meshs);
    area = null;
    refresh = true;
  }

  public AreaRenderer set(Area area, int ySection) {
    this.area = area;
    this.ySection = ySection;
    this.area.areaRenderer[ySection] = this;
    this.refresh = true;
    this.offset.set(area.minBlockX, 0, area.minBlockZ);
    return this;
  }

  public static void free(AreaRenderer areaRenderer) {
    if (areaRenderer != null) Pools.free(AreaRenderer.class, areaRenderer);
  }

  public static void free(AreaRenderer[] areaRenderer) {
    if (areaRenderer == null) return;
    for (AreaRenderer renderer : areaRenderer) {
      if (renderer == null) continue;
      Pools.free(AreaRenderer.class, renderer);
    }
  }

  public static void free(ArrayList<AreaMesh> areaMeshs) {
    if (areaMeshs == null) return;
    for (AreaMesh areaMesh : areaMeshs) {
      if (areaMesh == null) continue;
      Pools.free(AreaMesh.class, areaMesh);
    }
    areaMeshs.clear();
  }

  public static void newFrame() {
    refreshedThisFrame = 0;
  }
}
