package ethanjones.modularworld.graphics.assets;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.utils.GdxRuntimeException;
import ethanjones.modularworld.core.compatibility.Compatibility;
import ethanjones.modularworld.core.logging.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Assets {
  public static final String CORE = "core";
  public static HashMap<String, AssetManager> assetManagers = new HashMap<String, AssetManager>();
  public static PackedTextureSheet blockPackedTextureSheet;

  public static AssetManager getCoreAssetManager() {
    return getAssetManager(CORE);
  }

  public static AssetManager getAssetManager(String name) {
    return assetManagers.get(name);
  }

  public static Asset getAsset(String name) {
    int index = name.indexOf(":");
    if (index == -1) return null;
    String assetManagerName = name.substring(0, index);
    AssetManager assetManager = getAssetManager(assetManagerName);
    if (assetManager == null) return null;
    String assetName = name.substring(index + 1);
    Asset asset = assetManager.getAsset(assetName);
    return asset;
  }

  public static Texture getTexture(String assetName) {
    Asset asset = getAsset(assetName);
    if (asset == null) return null;
    return new Texture(asset.getFileHandle());
  }

  public static TextureRegion getTextureRegion(String assetName) {
    Texture texture = getTexture(assetName);
    if (texture == null) return null;
    return new TextureRegion(texture);
  }

  public static Material getMaterial(String assetName) {
    Texture texture = getTexture(assetName);
    if (texture == null) return null;
    return new Material(TextureAttribute.createDiffuse(texture));
  }

  public static void init() {
    Compatibility.get().setupAssets();
    blockPackedTextureSheet = getPackedTextureSheet(AssetType.block);
  }

  public static PackedTexture getBlockTexture(String name) {
    PackedTexture packedTexture = blockPackedTextureSheet.getPackedTextures().get(name);
    return packedTexture;
  }

  private static PackedTextureSheet getPackedTextureSheet(AssetType assetType) {
    TexturePacker texturePacker = new TexturePacker(2048, 2048, 0);
    for (Map.Entry<String, AssetManager> entry : assetManagers.entrySet()) {
      ArrayList<Asset> assets = entry.getValue().getAssets(assetType.name() + "/");
      for (Asset asset : assets) {
        try {
          if (!asset.getFileHandle().extension().equals("png")) continue;
          texturePacker.insertImage(entry.getKey() + ":" + asset.getPath(), new Pixmap(asset.getFileHandle()));
        } catch (Exception e) {
          Log.error("Failed to read file: " + asset.getPath(), e);
        }
      }
    }
    FileHandle fileHandle = Compatibility.get().getBaseFolder().child("assets").child("PackedTextures");
    fileHandle.mkdirs();
    fileHandle = fileHandle.child(assetType.name() + ".cim");

    try {
      PixmapIO.writeCIM(fileHandle, texturePacker.getPixmap());
    } catch (GdxRuntimeException e) {
      Log.error("Failed to write packed image", e);
    }

    Texture texture = new Texture(fileHandle);
    texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    PackedTextureSheet packedTextureSheet = new PackedTextureSheet(new Material(TextureAttribute.createDiffuse(texture)));

    Map<String, TexturePacker.PackRectangle> rectangles = texturePacker.getRectangles();
    int num = 0;
    for (Map.Entry<String, TexturePacker.PackRectangle> entry : rectangles.entrySet()) {
      num++;
      PackedTexture packedTexture = new PackedTexture(texture, new TextureRegion(texture, entry.getValue().x, entry.getValue().y, entry.getValue().width, entry.getValue().height));
      packedTextureSheet.getPackedTextures().put(entry.getKey(), packedTexture);
    }
    return packedTextureSheet;
  }
}
