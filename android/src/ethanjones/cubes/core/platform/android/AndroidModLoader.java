package ethanjones.cubes.core.platform.android;

import android.content.Context;
import com.badlogic.gdx.files.FileHandle;
import dalvik.system.DexClassLoader;

import ethanjones.cubes.core.mod.ModLoader;

public class AndroidModLoader implements ModLoader {

  private final AndroidCompatibility androidCompatibility;

  public AndroidModLoader(AndroidCompatibility androidCompatibility) {
    this.androidCompatibility = androidCompatibility;
  }

  @Override
  public boolean supports(ModType type) {
    return type == ModType.dex;
  }

  @Override
  public Class<?> loadClass(FileHandle classFile, String className) throws Exception {
    DexClassLoader classLoader = new DexClassLoader(classFile.file().getAbsolutePath(), androidCompatibility.androidLauncher.getDir("dex", Context.MODE_PRIVATE).getAbsolutePath(), null, AndroidModLoader.class.getClassLoader());
    return classLoader.loadClass(className);
  }
}
