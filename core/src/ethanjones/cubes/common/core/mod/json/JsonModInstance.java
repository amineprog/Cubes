package ethanjones.cubes.common.core.mod.json;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ethanjones.cubes.common.core.logging.Log;
import ethanjones.cubes.common.core.mod.ModInstance;
import ethanjones.cubes.common.core.mod.ModState;
import ethanjones.cubes.common.core.mod.event.ModEvent;
import ethanjones.cubes.common.core.mod.event.PostInitializationEvent;
import ethanjones.cubes.common.core.mod.event.PreInitializationEvent;
import ethanjones.cubes.platform.Adapter;
import ethanjones.cubes.client.graphics.assets.AssetManager;

public class JsonModInstance extends ModInstance {
  
  private final List<FileHandle> jsonFiles;
  public List<JsonBlockParameter> blockParameters = new ArrayList<JsonBlockParameter>();
  private List<ModState> modStates;

  public JsonModInstance(String name, FileHandle file, AssetManager assetManager, List<FileHandle> jsonFiles) {
    super(name, file, assetManager);
    this.jsonFiles = jsonFiles;
    List<ModState> modStates = new ArrayList<ModState>();
    modStates.add(ModState.Json);
    this.modStates = Collections.unmodifiableList(modStates);
  }

  @Override
  protected void init() throws Exception {
    for (FileHandle fileHandle : jsonFiles) {
      try {
        JsonValue jsonValue = new JsonReader().parse(fileHandle);
        JsonParser.parse(this, jsonValue);
      } catch (Exception e) {
        Log.error("Failed to read " + fileHandle.name() + " from " + name, e);
      }
    }
  }

  @Override
  protected void event(ModEvent modEvent) throws Exception {
    if (modEvent instanceof PreInitializationEvent) {
      for (JsonBlockParameter blockParameter : blockParameters) {
        blockParameter.init(this);
        blockParameter.register(this);
      }
    } else if (modEvent instanceof PostInitializationEvent) {
      if (Adapter.isDedicatedServer()) return;
      for (JsonBlockParameter blockParameter : blockParameters) {
        blockParameter.loadGraphics();
      }
    }
  }

  @Override
  protected void addState(ModState modState) {

  }

  @Override
  public List<ModState> getModStates() {
    return modStates;
  }

  @Override
  public Object getMod() {
    return this;
  }
}
