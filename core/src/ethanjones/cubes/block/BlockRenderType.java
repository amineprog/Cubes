package ethanjones.cubes.block;

import ethanjones.cubes.core.util.BlockFace;
import ethanjones.cubes.graphics.world.BlockTextureHandler;
import ethanjones.cubes.graphics.world.CrossFaceVertices;
import ethanjones.cubes.world.storage.Area;

import com.badlogic.gdx.math.Vector3;

import static ethanjones.cubes.graphics.world.FaceVertices.*;
import static ethanjones.cubes.world.light.SunLight.MAX_SUNLIGHT;
import static ethanjones.cubes.world.storage.Area.*;

public abstract class BlockRenderType {
  
  public static final BlockRenderType DEFAULT = new BlockRenderType() {
    static final int MIN_AREA = 0;
    static final int MAX_AREA = SIZE_BLOCKS - 1;
    
    @Override
    public int render(float[] vertices, int vertexOffset, Vector3 areaOffset, Block block, int meta, BlockTextureHandler textureHandler, Area area, int x, int y, int z, int i, Area maxX, Area minX, Area maxZ, Area minZ) {
      if (x < SIZE_BLOCKS - 1) {
        if (block.renderFace(BlockFace.posX, area.blocks[i + MAX_X_OFFSET])) { //light: byte is signed (-128 to 127) so & 0xFF to convert to 0-255
          vertexOffset = createMaxX(areaOffset, textureHandler.getSide(BlockFace.posX), x, y, z, area.light[i + MAX_X_OFFSET] & 0xFF, vertices, vertexOffset);
        }
      } else if (maxX == null || y > maxX.maxY) {
        vertexOffset = createMaxX(areaOffset, textureHandler.getSide(BlockFace.posX), x, y, z, MAX_SUNLIGHT, vertices, vertexOffset);
      } else if (block.renderFace(BlockFace.posX, maxX.blocks[getRef(MIN_AREA, y, z)])) {
        vertexOffset = createMaxX(areaOffset, textureHandler.getSide(BlockFace.posX), x, y, z, maxX.light[getRef(MIN_AREA, y, z)] & 0xFF, vertices, vertexOffset);
      }
      
      if (x > 0) {
        if (block.renderFace(BlockFace.negX, area.blocks[i + MIN_X_OFFSET])) {
          vertexOffset = createMinX(areaOffset, textureHandler.getSide(BlockFace.negX), x, y, z, area.light[i + MIN_X_OFFSET] & 0xFF, vertices, vertexOffset);
        }
      } else if (minX == null || y > minX.maxY) {
        vertexOffset = createMinX(areaOffset, textureHandler.getSide(BlockFace.negX), x, y, z, MAX_SUNLIGHT, vertices, vertexOffset);
      } else if (block.renderFace(BlockFace.negX, minX.blocks[getRef(MAX_AREA, y, z)])) {
        vertexOffset = createMinX(areaOffset, textureHandler.getSide(BlockFace.negX), x, y, z, minX.light[getRef(MAX_AREA, y, z)] & 0xFF, vertices, vertexOffset);
      }
      
      if (y < area.maxY) {
        if (block.renderFace(BlockFace.posY, area.blocks[i + MAX_Y_OFFSET])) {
          vertexOffset = createMaxY(areaOffset, textureHandler.getSide(BlockFace.posY), x, y, z, area.light[i + MAX_Y_OFFSET] & 0xFF, vertices, vertexOffset);
        }
      } else {
        vertexOffset = createMaxY(areaOffset, textureHandler.getSide(BlockFace.posY), x, y, z, MAX_SUNLIGHT, vertices, vertexOffset); //FIXME fix the light at the top and bottom of an area
      }
      
      if (y > 0) {
        if (block.renderFace(BlockFace.negY, area.blocks[i + MIN_Y_OFFSET])) {
          vertexOffset = createMinY(areaOffset, textureHandler.getSide(BlockFace.negY), x, y, z, area.light[i + MIN_Y_OFFSET] & 0xFF, vertices, vertexOffset);
        }
      } else {
        vertexOffset = createMinY(areaOffset, textureHandler.getSide(BlockFace.negY), x, y, z, 0, vertices, vertexOffset); //FIXME fix the light at the top and bottom of an area
      }
      
      if (z < SIZE_BLOCKS - 1) {
        if (block.renderFace(BlockFace.posZ, area.blocks[i + MAX_Z_OFFSET])) {
          vertexOffset = createMaxZ(areaOffset, textureHandler.getSide(BlockFace.posZ), x, y, z, area.light[i + MAX_Z_OFFSET] & 0xFF, vertices, vertexOffset);
        }
      } else if (maxZ == null || y > maxZ.maxY) {
        vertexOffset = createMaxZ(areaOffset, textureHandler.getSide(BlockFace.posZ), x, y, z, MAX_SUNLIGHT, vertices, vertexOffset);
      } else if (block.renderFace(BlockFace.posZ, maxZ.blocks[getRef(x, y, MIN_AREA)])) {
        vertexOffset = createMaxZ(areaOffset, textureHandler.getSide(BlockFace.posZ), x, y, z, maxZ.light[getRef(x, y, MIN_AREA)] & 0xFF, vertices, vertexOffset);
      }
      
      if (z > 0) {
        if (block.renderFace(BlockFace.negZ, area.blocks[i + MIN_Z_OFFSET])) {
          vertexOffset = createMinZ(areaOffset, textureHandler.getSide(BlockFace.negZ), x, y, z, area.light[i + MIN_Z_OFFSET] & 0xFF, vertices, vertexOffset);
        }
      } else if (minZ == null || y > minZ.maxY) {
        vertexOffset = createMinZ(areaOffset, textureHandler.getSide(BlockFace.negZ), x, y, z, MAX_SUNLIGHT, vertices, vertexOffset);
      } else if (block.renderFace(BlockFace.negZ, minZ.blocks[getRef(x, y, MAX_AREA)])) {
        vertexOffset = createMinZ(areaOffset, textureHandler.getSide(BlockFace.negZ), x, y, z, minZ.light[getRef(x, y, MAX_AREA)] & 0xFF, vertices, vertexOffset);
      }
      return vertexOffset;
    }
  };
  
  public static final BlockRenderType CROSS = new BlockRenderType() {
    @Override
    public int render(float[] vertices, int vertexOffset, Vector3 areaOffset, Block block, int meta, BlockTextureHandler textureHandler, Area area, int x, int y, int z, int i, Area maxX, Area minX, Area maxZ, Area minZ) {
      vertexOffset = CrossFaceVertices.createMinXMaxZ(areaOffset, textureHandler.getSide(null), x, y, z, area.light[i + MIN_Z_OFFSET] & 0xFF, vertices, vertexOffset);
      vertexOffset = CrossFaceVertices.createMaxZMinX(areaOffset, textureHandler.getSide(null), x, y, z, area.light[i + MIN_Z_OFFSET] & 0xFF, vertices, vertexOffset);
      vertexOffset = CrossFaceVertices.createMaxXMinZ(areaOffset, textureHandler.getSide(null), x, y, z, area.light[i + MIN_Z_OFFSET] & 0xFF, vertices, vertexOffset);
      vertexOffset = CrossFaceVertices.createMinZMaxX(areaOffset, textureHandler.getSide(null), x, y, z, area.light[i + MIN_Z_OFFSET] & 0xFF, vertices, vertexOffset);
      return vertexOffset;
    }
  };
  
  public static final BlockRenderType CROSS_STRETCHED = new BlockRenderType() {
    @Override
    public int render(float[] vertices, int vertexOffset, Vector3 areaOffset, Block block, int meta, BlockTextureHandler textureHandler, Area area, int x, int y, int z, int i, Area maxX, Area minX, Area maxZ, Area minZ) {
      vertexOffset = CrossFaceVertices.createMinXMaxZStretched(areaOffset, textureHandler.getSide(null), x, y, z, area.light[i + MIN_Z_OFFSET] & 0xFF, vertices, vertexOffset);
      vertexOffset = CrossFaceVertices.createMaxZMinXStretched(areaOffset, textureHandler.getSide(null), x, y, z, area.light[i + MIN_Z_OFFSET] & 0xFF, vertices, vertexOffset);
      vertexOffset = CrossFaceVertices.createMaxXMinZStretched(areaOffset, textureHandler.getSide(null), x, y, z, area.light[i + MIN_Z_OFFSET] & 0xFF, vertices, vertexOffset);
      vertexOffset = CrossFaceVertices.createMinZMaxXStretched(areaOffset, textureHandler.getSide(null), x, y, z, area.light[i + MIN_Z_OFFSET] & 0xFF, vertices, vertexOffset);
      return vertexOffset;
    }
  };
  
  public abstract int render(float[] vertices, int vertexOffset, Vector3 areaOffset, Block block, int meta, BlockTextureHandler textureHandler, Area area, int x, int y, int z, int i, Area maxX, Area minX, Area maxZ, Area minZ);
}
