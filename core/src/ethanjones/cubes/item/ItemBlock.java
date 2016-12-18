package ethanjones.cubes.item;

import ethanjones.cubes.block.Block;
import ethanjones.cubes.core.event.entity.living.player.PlayerPlaceBlockEvent;
import ethanjones.cubes.core.util.BlockFace;
import ethanjones.cubes.entity.living.player.Player;
import ethanjones.cubes.input.ClickType;
import ethanjones.cubes.item.inv.InventoryHelper;
import ethanjones.cubes.side.Side;
import ethanjones.cubes.side.Sided;
import ethanjones.cubes.side.common.Cubes;
import ethanjones.cubes.world.collision.BlockIntersection;
import ethanjones.cubes.world.reference.BlockReference;

public class ItemBlock extends Item {
  public final Block block;

  public ItemBlock(Block block) {
    super(block.id);
    this.block = block;
  }

  public void loadGraphics() {
    this.texture = block.getTextureHandler(0).getSide(BlockFace.posX);
  }

  @Override
  public String getName() {
    return block.getName();
  }

  @Override
  public boolean onButtonPress(ClickType type, ItemStack itemStack, Player player, int stack) {
    if (Sided.getSide() == Side.Server && type == ClickType.place) {
      BlockIntersection blockIntersection = BlockIntersection.getBlockIntersection(player.position, player.angle, Cubes.getServer().world);
      if (blockIntersection == null || blockIntersection.getBlockFace() == null) return false;
      BlockReference blockReference = blockIntersection.getBlockReference();
      switch (blockIntersection.getBlockFace()) {
        case posX:
          blockReference.blockX++;
          break;
        case negX:
          blockReference.blockX--;
          break;
        case posY:
          blockReference.blockY++;
          break;
        case negY:
          blockReference.blockY--;
          break;
        case posZ:
          blockReference.blockZ++;
          break;
        case negZ:
          blockReference.blockZ--;
          break;
      }
      // check block would not be in player
      if (blockReference.equals(new BlockReference().setFromVector3(player.position))) return false;
      if (blockReference.equals(new BlockReference().setFromVector3(player.position.cpy().sub(0, player.height, 0))))
        return false;

      PlayerPlaceBlockEvent event = new PlayerPlaceBlockEvent(player, block, itemStack.meta, blockIntersection, blockReference);
      if (event.post().isCanceled()) return false;

      Cubes.getServer().world.setBlock(block, blockReference.blockX, blockReference.blockY, blockReference.blockZ, event.getMeta());

      InventoryHelper.reduceCount(player.getInventory(), stack);
      return true;
    }
    return false;
  }
}
