package ethanjones.modularworld.graphics;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import ethanjones.modularworld.ModularWorld;
import ethanjones.modularworld.core.ModularWorldException;
import ethanjones.modularworld.core.logging.Log;
import ethanjones.modularworld.graphics.rendering.Renderer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GraphicsHelper {

  public static PackedTexture.PackedMaterial blockPackedTextures;
  public static int usage = Usage.Position | Usage.Normal | Usage.TextureCoordinates;
  private static Array<TexturePacker> texturePackers = new Array<TexturePacker>();
  private static Array<Texture> packedTextures;
  private static HashMap<String, PackedTexture> textures = new HashMap<String, PackedTexture>();

  public static PackedTexture load(String name) {
    PackedTexture packedTextureWrapper = textures.get(stringToHashMap(name));
    if (packedTextureWrapper == null || packedTextureWrapper.packedTexture == null) {
      Log.error(new ModularWorldException("No such texture: " + name + " in map: " + Character.LINE_SEPARATOR + textures.toString()));
    }
    return packedTextureWrapper;
  }

  public static PackedTexture loadBlock(String material) {
    return load("Blocks/" + material + ".png");
  }

  public static Renderer getRenderer() {
    return ModularWorld.instance.renderer;
  }

  public static ModelBuilder getModelBuilder() {
    return getRenderer().modelBuilder;
  }

  public static void init(AssetManager assetManager) {
    FileHandle parent = ModularWorld.instance.baseFolder.child("PackedTextures");
    parent.mkdirs();
    for (String pastPackedTexture : parent.file().list()) {
      try {
        new File(pastPackedTexture).delete();
      } catch (Exception e) {

      }
    }
    AssetManager.AssetFolder assetFolderManager = assetManager.assets;
    Array<AssetManager.Asset> textureHandles = new Array<AssetManager.Asset>();

    AssetManager.AssetFolder blockFolderManager = assetManager.assets.folders.get("Blocks");
    Array<AssetManager.Asset> blockTextureHandles = new Array<AssetManager.Asset>();

    findTexture(blockFolderManager, null, blockTextureHandles);
    pack(blockTextureHandles);
    if (texturePackers.size > 1) {
      Log.error(new ModularWorldException("Only one sheet of block textures is allowed"));
    }
    findTexture(assetFolderManager, blockFolderManager, textureHandles);
    pack(textureHandles);

    packedTextures = new Array<Texture>(texturePackers.size);
    for (int i = 0; i < texturePackers.size; i++) {
      TexturePacker texturePacker = texturePackers.get(i);

      String filename = i + ".png";
      FileHandle fileHandle = parent.child(filename);

      try {
        PixmapIO.writePNG(fileHandle, texturePacker.getPixmap());
      } catch (GdxRuntimeException e) {
        Log.error("Failed to write packed image", e);
      }

      Texture texture = new Texture(fileHandle);
      texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
      PackedTexture.PackedMaterial material = new PackedTexture.PackedMaterial(TextureAttribute.createDiffuse(texture));
      packedTextures.add(texture);
      if (i == 0) {
        blockPackedTextures = material;
      }

      Map<String, TexturePacker.PackRectangle> rectangles = texturePacker.getRectangles();
      int num = 0;
      for (String str : rectangles.keySet()) {
        num++;
        TexturePacker.PackRectangle rectangle = rectangles.get(str);
        str = stringToHashMap(str);
        textures.put(str, new PackedTexture(texture, num, material, new TextureRegion(texture, rectangle.x, rectangle.y, rectangle.width, rectangle.height), str));
      }
    }
  }

  private static String stringToHashMap(String str) {
    return str.replace("\\", "/");
  }

  private static void pack(Array<AssetManager.Asset> files) {
    TexturePacker texturePacker = getTexturePacker();
    for (AssetManager.Asset asset : files) {
      try {
        if (!addToTexturePacker(texturePacker, asset)) {
          texturePackers.add(texturePacker);
          texturePacker = getTexturePacker();
          addToTexturePacker(texturePacker, asset);
        }
      } catch (IOException e) {
        Log.error("Failed to read file: " + asset.path, e);
      }
    }
    if (texturePacker.getRectangles().size() != 0) texturePackers.add(texturePacker);
  }

  private static boolean addToTexturePacker(TexturePacker texturePacker, AssetManager.Asset asset) throws IOException {
    return texturePacker.insertImage(asset.path, new Pixmap(asset.bytes, 0, asset.bytes.length));
  }

  private static TexturePacker getTexturePacker() {
    return new TexturePacker(2048, 2048, 0);
  }

  private static void findTexture(AssetManager.AssetFolder parent, AssetManager.AssetFolder exclude, Array<AssetManager.Asset> files) {
    if (parent == exclude) return;
    for (AssetManager.AssetFolder folder : parent.folders.values()) {
      findTexture(folder, exclude, files);
    }
    for (AssetManager.Asset file : parent.files.values()) {
      if (file.path.endsWith(".png")) files.add(file);
    }
  }

  private static void findTexture() {

  }

}
