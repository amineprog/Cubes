package ethanjones.cubes.world.generator;

import ethanjones.cubes.block.Block;
import ethanjones.cubes.block.data.BlockData;
import ethanjones.cubes.side.Sided;
import ethanjones.cubes.world.storage.Area;

public abstract class TerrainGenerator {

  public abstract void generate(Area area);

  protected void set(Area area, Block block, int x, int y, int z) {
    set(area, block, block == null ? null : block.getBlockData(), x, y, z);
  }

  protected void set(Area area, Block block, BlockData blockData, int x, int y, int z) {
    int ref = area.getRef(x, y, z);
    area.checkArrays();
    synchronized (area) {
      area.blocks[ref] = Sided.getBlockManager().toInt(block);
      area.blockData[ref] = blockData;
    }
  }
}
