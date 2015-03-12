package ethanjones.cubes.common.core.mod;

import com.badlogic.gdx.files.FileHandle;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ethanjones.cubes.common.core.logging.Log;
import ethanjones.cubes.common.core.mod.ModLoader.ModType;
import ethanjones.cubes.common.core.mod.event.ModEvent;
import ethanjones.cubes.common.core.mod.java.JavaModInstance;
import ethanjones.cubes.common.core.mod.json.JsonModInstance;
import ethanjones.cubes.platform.Compatibility;
import ethanjones.cubes.common.core.system.CubesException;
import ethanjones.cubes.client.graphics.assets.AssetFinder;
import ethanjones.cubes.client.graphics.assets.AssetManager;
import ethanjones.cubes.client.graphics.assets.Assets;

public class ModManager {

  private static boolean init = false;
  private static List<ModInstance> mods;

  public synchronized static void init() {
    if (init) return;
    init = true;
    Log.debug("Loading mods");
    ArrayList<ModInstance> mods = new ArrayList<ModInstance>();
    ModLoader modLoader = Compatibility.get().getModLoader();
    FileHandle temp = Compatibility.get().getBaseFolder().child("mods").child("temp");
    temp.deleteDirectory();
    temp.mkdirs();
    for (FileHandle fileHandle : getModFiles()) {
      FileHandle classFile = null;
      String className = null;
      String name = "";
      List<FileHandle> jsonFiles = new ArrayList<FileHandle>();
      FileHandle modAssets = Assets.assetsFolder.child(fileHandle.name());
      try {
        InputStream inputStream = new FileInputStream(fileHandle.file());
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
          if (!entry.isDirectory()) {
            String entryName = entry.getName().toLowerCase();
            FileHandle f = temp.child(fileHandle.name()).child(entry.getName());
            if (entryName.equals("mod.jar")) {
              if (modLoader.getType() == ModType.jar) {
                writeToFile(f, zipInputStream);
                classFile = f;
              }
            } else if (entryName.equals("mod.dex")) {
              if (modLoader.getType() == ModType.dex) {
                writeToFile(f, zipInputStream);
                classFile = f;
              }
            } else if (entryName.equals("mod.properties")) {
              Properties properties = new Properties();
              properties.load(zipInputStream);
              className = properties.getProperty("modClass");
              name = properties.getProperty("modName");
            } else if (entryName.startsWith("assets/")) {
              writeToFile(modAssets.child(entry.getName().substring(7)), zipInputStream);
            } else if (entryName.endsWith(".json")) { //if a json file not in assets
              writeToFile(f, zipInputStream);
              jsonFiles.add(f);
            }
          }
        }
        if (name == null) {
          Log.error("Mod " + fileHandle.name() + " does not contain a properties file");
          continue;
        }
        if (jsonFiles.isEmpty()) {
          if (className == null) {
            Log.error("Mod " + fileHandle.name() + " does not contain a \"mod.properties\" with a \"className\" or json files");
            continue;
          }
          if (classFile == null) {
            Log.error("Mod " + fileHandle.name() + " does not contain a " + modLoader.getType());
            continue;
          }
        } else {
          if (className == null && classFile != null) {
            Log.error("Mod " + fileHandle.name() + " does not contain a \"mod.properties\" with a \"className\"");
          } else if (className != null && classFile == null) {
            Log.error("Mod " + fileHandle.name() + " does not contain a " + modLoader.getType());
          }
        }
      } catch (Exception e) {
        Log.error("Failed to load mod: " + name, e);
      }
      try {
        Log.debug("Mod file: \"" + fileHandle.name() + "\" Name: \"" + name + "\"");
        AssetManager assetManager;
        if (modAssets.exists() && modAssets.isDirectory()) {
          Log.debug("Loading assets for " + name);
          assetManager = AssetFinder.findAssets(modAssets, name);
        } else {
          Log.debug("No assets detected for " + name);
          assetManager = null;
        }
        jsonFiles = Collections.unmodifiableList(jsonFiles);
        if (!jsonFiles.isEmpty()) {
          Log.debug("Initialising JsonModInstance");
          JsonModInstance jsonModInstance = new JsonModInstance(name, fileHandle, assetManager, jsonFiles);
          ((ModInstance) jsonModInstance).init();
          mods.add(jsonModInstance);
          Log.info("Loaded Json mod " + name);
        }
        if (className != null) {
          Log.info("Trying to load Java mod " + name);
          Class<?> c = modLoader.loadClass(classFile, className);
          Log.debug("Creating instance of Java mod " + name);
          Object mod = c.newInstance();
          Log.debug("Initialising JavaModInstance");
          JavaModInstance javaModInstance = new JavaModInstance(name, fileHandle, assetManager, mod);
          ((ModInstance) javaModInstance).init();
          mods.add(javaModInstance);
          Log.info("Loaded Java mod " + name);
        }

      } catch (Exception e) {
        Log.debug("Failed to make instance of mod: " + className, e);
      }
    }
    ModManager.mods = Collections.unmodifiableList(mods);
  }

  private static FileHandle[] getModFiles() {
    FileHandle base = Compatibility.get().getBaseFolder().child("mods");
    base.mkdirs();
    return base.list(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        String s = pathname.getName().toLowerCase();
        return s.endsWith(".cm");
      }
    });
  }

  private static void writeToFile(FileHandle file, ZipInputStream zipInputStream) throws Exception {
    FileOutputStream fileOutputStream = null;
    try {
      file.parent().mkdirs();
      file.file().createNewFile();
      fileOutputStream = new FileOutputStream(file.file());
      int len = 0;
      byte[] buffer = new byte[2048];
      while ((len = zipInputStream.read(buffer)) > 0) {
        fileOutputStream.write(buffer, 0, len);
      }
    } catch (Exception e) {
      throw e;
    } finally {
      if (fileOutputStream != null) {
        fileOutputStream.close();
      }
    }
  }

  public synchronized static List<ModInstance> getMods() {
    return mods;
  }

  public synchronized static void postModEvent(ModEvent modEvent) {
    Log.debug("Posting " + modEvent.getClass().getSimpleName());
    for (ModInstance mod : mods) {
      try {
        mod.event(modEvent);
      } catch (Exception e) {
        mod.addState(ModState.Error);
        throw new CubesException("Exception while posting " + modEvent.getClass().getSimpleName() + " to mod " + mod.name, e);
      }
    }
    Log.debug("Finished posting " + modEvent.getClass().getSimpleName());
  }
}
