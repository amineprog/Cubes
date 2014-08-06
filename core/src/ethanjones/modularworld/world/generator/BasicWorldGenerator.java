package ethanjones.modularworld.world.generator;

import ethanjones.modularworld.block.factory.BlockFactories;
import ethanjones.modularworld.world.storage.Area;

public class BasicWorldGenerator extends WorldGenerator {

  @Override
  public void generate(Area area) {
    if (area.y == 0) {
      for (int x = 0; x < Area.SIZE_BLOCKS; x++) {
        for (int z = 0; z < Area.SIZE_BLOCKS; z++) {
          area.setBlockFactory(BlockFactories.bedrock, x, 0, z);
          area.setBlockFactory(BlockFactories.stone, x, 1, z);
          area.setBlockFactory(BlockFactories.stone, x, 2, z);
          area.setBlockFactory(BlockFactories.dirt, x, 3, z);
          area.setBlockFactory(BlockFactories.grass, x, 4, z);
        }
      }
      if (area.x == 0 && area.z == 0) {
        area.setBlockFactory(BlockFactories.bedrock, 1, 4, 1);
        area.setBlockFactory(BlockFactories.bedrock, 1, 7, 1);
        area.setBlockFactory(BlockFactories.stone, 5, 5, 3);
        area.setBlockFactory(BlockFactories.grass, 3, 5, 5);
      }
    }
  }
}
