package ethanjones.cubes.core.mod;

import com.badlogic.gdx.files.FileHandle;
import java.io.*;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ethanjones.cubes.core.compatibility.Compatibility;
import ethanjones.cubes.core.logging.Log;

public class ModManager { //TODO: Pack assets. Make modes useful

  public static void init() {
    ModLoader modLoader = Compatibility.get().getModLoader();
    FileHandle temp = Compatibility.get().getBaseFolder().child("modTemp");
    temp.deleteDirectory();
    temp.mkdirs();
    FileHandle modAssets = Compatibility.get().getBaseFolder().child("modAssets");
    modAssets.deleteDirectory();
    modAssets.mkdirs();
    for (FileHandle fileHandle : getModFiles()) {
      FileHandle classFile = null;
      String className = null;
      String modName = "";
      try {
        modName = fileHandle.name();
        InputStream inputStream = new FileInputStream(fileHandle.file());
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
          FileHandle f = temp.child(fileHandle.name()).child(entry.getName());
          if (!entry.isDirectory() && entry.getName().toLowerCase().equals("mod.jar")) {
            if (modLoader.supports(ModType.jar)) {
              writeToFile(f, zipInputStream);
              classFile = f;
            }
          } else if (!entry.isDirectory() && entry.getName().toLowerCase().equals("mod.dex")) {
            if (modLoader.supports(ModType.dex)) {
              writeToFile(f, zipInputStream);
              classFile = f;
            }
          } else if (!entry.isDirectory() && entry.getName().toLowerCase().equals("mod.properties")) {
            Properties properties = new Properties();
            properties.load(zipInputStream);
            className = properties.getProperty("modClass");
          } else if (!entry.isDirectory() && entry.getName().toLowerCase().startsWith("assets/")) {
            writeToFile(modAssets.child(fileHandle.name()).child(entry.getName().substring(7)), zipInputStream);
          }
        }
        if (classFile == null) {
          Log.error("Mod " + modName + " does not contain a jar/dex");
          continue;
        }
        if (className == null) {
          Log.error("Mod " + modName + " does not contain a properties file");
          continue;
        }
      } catch (Exception e) {
        Log.error("Failed to load mod: " + modName, e);
      }
      try {
        Log.info("Trying to load mod " + modName);
        Class<? extends Mod> c = modLoader.loadClass(classFile, className).asSubclass(Mod.class);
        Log.debug("Creating instance of mod " + modName);
        Mod mod = c.newInstance();
        Log.debug("Calling create on mod " + modName);
        mod.create();
        Log.info("Loaded mod " + modName);
      } catch (Exception e) {
        Log.debug("Failed to make instance of mod: " + className, e);
      }
    }
  }

  private static FileHandle[] getModFiles() {
    FileHandle base = Compatibility.get().getBaseFolder().child("mods");
    base.mkdirs();
    return base.list(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        String s = pathname.getName().toLowerCase();
        return s.endsWith(".mod");
      }
    });
  }

  public static void writeToFile(FileHandle file, ZipInputStream zipInputStream) throws Exception {
    file.parent().mkdirs();
    file.file().createNewFile();
    FileOutputStream fileOutputStream = new FileOutputStream(file.file());
    int len = 0;
    byte[] buffer = new byte[2048];
    while ((len = zipInputStream.read(buffer)) > 0) {
      fileOutputStream.write(buffer, 0, len);
    }
    fileOutputStream.close();
  }
}
