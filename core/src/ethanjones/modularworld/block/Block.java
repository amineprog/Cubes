package ethanjones.modularworld.block;

import com.badlogic.gdx.math.collision.BoundingBox;
import ethanjones.modularworld.core.data.DataGroup;
import ethanjones.modularworld.graphics.world.BlockTextureHandler;

public abstract class Block {
  protected BlockTextureHandler textureHandler;
  String mainMaterial;

  public Block(String mainMaterial) {
    this.mainMaterial = mainMaterial;
  }

  public void loadGraphics() {
    textureHandler = new BlockTextureHandler(mainMaterial);
  }

  public BlockTextureHandler getTextureHandler(DataGroup data) {
    return textureHandler;
  }

  /**
   * @return custom bounding box
   */
  public BoundingBox getBoundingBox() {
    return null;
  }

}
