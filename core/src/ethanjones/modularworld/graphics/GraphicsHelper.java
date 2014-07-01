package ethanjones.modularworld.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Array;
import ethanjones.modularworld.ModularWorld;
import ethanjones.modularworld.core.ModularWorldException;
import ethanjones.modularworld.core.logging.Log;
import ethanjones.modularworld.graphics.rendering.Renderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GraphicsHelper {

  private static Array<String> textureFiles = new Array<String>();
  private static Array<String> blockTextureFiles = new Array<String>();
  private static Array<TexturePacker> texturePackers = new Array<TexturePacker>();
  private static Array<Texture> packedTextures;
  private static HashMap<String, PackedTexture> textures = new HashMap<String, PackedTexture>();

  public static int usage = Usage.Position | Usage.Normal | Usage.TextureCoordinates;
  public static Material test;

  public static PackedTexture load(String name) {
    PackedTexture packedTextureWrapper = textures.get(name);
    if (packedTextureWrapper == null || packedTextureWrapper.packedTexture == null) {
      Log.error(new ModularWorldException("No such texture: " + name));
    }
    return packedTextureWrapper;
  }

  public static PackedTexture loadBlock(String material) {
    return load("Blocks/" + material + ".png");
  }

  private static Renderer getRenderer() {
    return ModularWorld.instance.renderer;
  }

  public static ModelBuilder getModelBuilder() {
    return getRenderer().modelBuilder;
  }

  public static void init() {
    findTexture(ModularWorld.instance.compatibility.getWorkingFolder().child("Blocks").file(), null, blockTextureFiles);
    pack(blockTextureFiles);
    if (texturePackers.size < 0) {
      Log.error(new ModularWorldException("No block textures"));
    } else if (texturePackers.size > 1) {
      Log.error(new ModularWorldException("Only one sheet of block textures is allowed"));
    }
    findTexture(ModularWorld.instance.compatibility.getWorkingFolder().file(), ModularWorld.instance.compatibility.getWorkingFolder().child("Block").file(), textureFiles);
    pack(textureFiles);

    packedTextures = new Array<Texture>(texturePackers.size);
    FileHandle parent = ModularWorld.instance.baseFolder.child("PackedTextures");
    for (int i = 0; i < texturePackers.size; i++) {
      TexturePacker texturePacker = texturePackers.get(i);

      String filename = i + ".png";
      FileHandle fileHandle = parent.child(filename);
      fileHandle.mkdirs();
      File file = new File(parent.file(), filename);

      try {
        file.createNewFile();
        ImageIO.write(texturePacker.getImage(), "png", file);
      } catch (IOException e) {
        Log.error("Failed to write packed image", e);
      }

      Texture texture = new Texture(fileHandle);
      texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
      Material material = new Material(TextureAttribute.createDiffuse(texture));
      packedTextures.add(texture);

      Map<String, Rectangle> rectangles = texturePacker.getRectangles();
      for (String str : rectangles.keySet()) {
        Rectangle rectangle = rectangles.get(str);
        textures.put(str, new PackedTexture(texture, material, new TextureRegion(texture, rectangle.x, rectangle.y, rectangle.width, rectangle.height), str));
      }
    }

    test = loadBlock("Grass").material;
  }

  private static void pack(Array<String> filenames) {
    TexturePacker texturePacker = getTexturePacker();
    for (String str : filenames) {
      try {
        if (!addToTexturePacker(texturePacker, str)) {
          texturePackers.add(texturePacker);
          texturePacker = getTexturePacker();
          addToTexturePacker(texturePacker, str);
        }
      } catch (IOException e) {
        Log.error("Failed to read file: " + str, e);
      }
    }
    if (texturePacker.getRectangles().size() != 0) texturePackers.add(texturePacker);
  }

  private static TexturePacker getTexturePacker() {
    return new TexturePacker(2048, 2048, 0);
  }

  private static boolean addToTexturePacker(TexturePacker texturePacker, String path) throws IOException {
    return texturePacker.insertImage(path, ImageIO.read(Gdx.files.internal(path).file()));
  }

  private static void findTexture(File parent, File exclude, Array<String> filenames) {
    for (String string : parent.list()) {
      File file = new File(parent, string);
      if (exclude != null && file != exclude) return;
      if (file.isDirectory()) {
        findTexture(file, exclude, filenames);
      } else if (file.getName().endsWith(".png")) {
        filenames.add(file.getAbsolutePath());
      }
    }
  }

}
