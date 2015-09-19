package ethanjones.cubes.core.mod;

import ethanjones.cubes.core.mod.event.ModEvent;
import ethanjones.cubes.graphics.assets.AssetManager;

import com.badlogic.gdx.files.FileHandle;

import java.util.List;

public abstract class ModInstance {

  protected final String name;
  protected final FileHandle file;
  protected final AssetManager assetManager;

  protected ModInstance(String name, FileHandle file, AssetManager assetManager) {
    this.name = name;
    this.file = file;
    this.assetManager = assetManager;
  }

  protected abstract void init() throws Exception;

  protected abstract void event(ModEvent modEvent) throws Exception;

  protected abstract void addState(ModState modState);

  public abstract List<ModState> getModStates();

  public abstract Object getMod();

  public FileHandle getFile() {
    return file;
  }

  public String getName() {
    return name;
  }

  public AssetManager getAssetManager() {
    return assetManager;
  }

  public String toString() {
    return this.getClass().getSimpleName() + ": " + name;
  }

}
