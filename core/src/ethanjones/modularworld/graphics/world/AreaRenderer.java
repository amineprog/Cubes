package ethanjones.modularworld.graphics.world;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import ethanjones.modularworld.block.Block;
import ethanjones.modularworld.core.util.Direction;
import ethanjones.modularworld.graphics.GraphicsHelper;
import ethanjones.modularworld.world.storage.Area;

import static ethanjones.modularworld.graphics.world.FaceVertices.*;
import static ethanjones.modularworld.world.storage.Area.SIZE_BLOCKS;
import static ethanjones.modularworld.world.storage.Area.SIZE_BLOCKS_CUBED;

public class AreaRenderer implements RenderableProvider {

  public static final int topOffset = SIZE_BLOCKS * SIZE_BLOCKS;
  public static final int bottomOffset = -SIZE_BLOCKS * SIZE_BLOCKS;
  public static final int leftOffset = -1;
  public static final int rightOffset = 1;
  public static final int frontOffset = -SIZE_BLOCKS;
  public static final int backOffset = SIZE_BLOCKS;
  public static final int VERTEX_SIZE = 8; //3 for position, 3 for normal, 2 for texture coordinates;

  private static short[] indices;
  private static float vertices[];

  static {
    int len = SIZE_BLOCKS_CUBED * 6 * 6 / 3;
    indices = new short[len];
    short j = 0;
    for (int i = 0; i < len; i += 6, j += 4) {
      indices[i + 0] = (short) (j + 0);
      indices[i + 1] = (short) (j + 1);
      indices[i + 2] = (short) (j + 2);
      indices[i + 3] = (short) (j + 2);
      indices[i + 4] = (short) (j + 3);
      indices[i + 5] = (short) (j + 0);
    }

    vertices = new float[VERTEX_SIZE * 6 * SIZE_BLOCKS_CUBED];
  }

  public Mesh mesh;
  public boolean dirty = true;
  Vector3 offset = new Vector3();
  private int numVertices = 0;
  private Camera camera;
  private Area area;

  public AreaRenderer(Area area) {
    this.area = area;
    this.offset.set(area.minBlockX, area.minBlockY, area.minBlockZ);
    mesh = new Mesh(true, SIZE_BLOCKS_CUBED * 8 * 4, SIZE_BLOCKS_CUBED * 36 / 3, GraphicsHelper.vertexAttributes);
    mesh.setIndices(indices);
  }

  public AreaRenderer set(Camera camera) {
    this.camera = camera;
    return this;
  }

  @Override
  public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
    if (dirty) {
      int numVerts = calculateVertices(vertices);
      numVertices = numVerts / 4 * 6;
      mesh.setVertices(vertices, 0, numVerts * VERTEX_SIZE);
      dirty = false;
    }
    if (numVertices == 0) return;
    Renderable renderable = pool.obtain();
    renderable.material = GraphicsHelper.getBlockTextureSheet();
    renderable.mesh = mesh;
    renderable.meshPartOffset = 0;
    renderable.meshPartSize = numVertices;
    renderable.primitiveType = GL20.GL_TRIANGLES;
    renderables.add(renderable);
  }

  public int calculateVertices(float[] vertices) {
    int i = 0;
    int vertexOffset = 0;
    for (int y = 0; y < SIZE_BLOCKS; y++) {
      for (int z = 0; z < SIZE_BLOCKS; z++) {
        for (int x = 0; x < SIZE_BLOCKS; x++, i++) {
          Block block = area.blocks[i];
          if (block == null) continue;
          BlockTextureHandler textureHandler = block.getTextureHandler();
          if (y < SIZE_BLOCKS - 1) {
            if (area.blocks[i + topOffset] == null)
              vertexOffset = createMaxY(offset, textureHandler.getSide(Direction.posY).textureRegion, x, y, z, vertices, vertexOffset);
          } else {
            vertexOffset = createMaxY(offset, textureHandler.getSide(Direction.posY).textureRegion, x, y, z, vertices, vertexOffset);
          }
          if (y > 0) {
            if (area.blocks[i + bottomOffset] == null)
              vertexOffset = createMinY(offset, textureHandler.getSide(Direction.negY).textureRegion, x, y, z, vertices, vertexOffset);
          } else {
            vertexOffset = createMinY(offset, textureHandler.getSide(Direction.negY).textureRegion, x, y, z, vertices, vertexOffset);
          }
          if (x > 0) {
            if (area.blocks[i + leftOffset] == null)
              vertexOffset = createMinX(offset, textureHandler.getSide(Direction.negX).textureRegion, x, y, z, vertices, vertexOffset);
          } else {
            vertexOffset = createMinX(offset, textureHandler.getSide(Direction.negX).textureRegion, x, y, z, vertices, vertexOffset);
          }
          if (x < SIZE_BLOCKS - 1) {
            if (area.blocks[i + rightOffset] == null)
              vertexOffset = createMaxX(offset, textureHandler.getSide(Direction.posX).textureRegion, x, y, z, vertices, vertexOffset);
          } else {
            vertexOffset = createMaxX(offset, textureHandler.getSide(Direction.posX).textureRegion, x, y, z, vertices, vertexOffset);
          }
          if (z > 0) {
            if (area.blocks[i + frontOffset] == null)
              vertexOffset = createMinZ(offset, textureHandler.getSide(Direction.negZ).textureRegion, x, y, z, vertices, vertexOffset);
          } else {
            vertexOffset = createMinZ(offset, textureHandler.getSide(Direction.negZ).textureRegion, x, y, z, vertices, vertexOffset);
          }
          if (z < SIZE_BLOCKS - 1) {
            if (area.blocks[i + backOffset] == null)
              vertexOffset = createMaxZ(offset, textureHandler.getSide(Direction.posZ).textureRegion, x, y, z, vertices, vertexOffset);
          } else {
            vertexOffset = createMaxZ(offset, textureHandler.getSide(Direction.posZ).textureRegion, x, y, z, vertices, vertexOffset);
          }
        }
      }
    }
    return vertexOffset / VERTEX_SIZE;
  }
}
