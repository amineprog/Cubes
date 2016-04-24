package ethanjones.cubes.core.platform.android;

import ethanjones.cubes.core.logging.Log;
import ethanjones.cubes.core.logging.LogWriter;
import ethanjones.cubes.core.mod.ModLoader;
import ethanjones.cubes.core.platform.Adapter;
import ethanjones.cubes.core.platform.Compatibility;
import ethanjones.cubes.core.settings.Settings;
import ethanjones.cubes.core.system.Branding;
import ethanjones.cubes.graphics.menu.Menu;
import ethanjones.cubes.graphics.menu.MenuManager;
import ethanjones.cubes.graphics.menus.MainMenu;
import ethanjones.cubes.side.common.Cubes;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.os.Build;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;

public class AndroidCompatibility extends Compatibility {

  public AndroidLauncher androidLauncher;
  protected AndroidModLoader modLoader;
  protected boolean back = false;
  protected boolean modifier = false;

  protected ActivityManager activityManager;
  protected MemoryInfo memoryInfo;

  protected AndroidCompatibility(AndroidLauncher androidLauncher) {
    super(androidLauncher, Application.ApplicationType.Android);
    this.androidLauncher = androidLauncher;
    modLoader = new AndroidModLoader(this);

    activityManager = (ActivityManager) androidLauncher.getSystemService(Activity.ACTIVITY_SERVICE);
    memoryInfo = new MemoryInfo();
  }

  @Override
  public void postInit() {
    super.postInit();
    Settings.getIntegerSetting(Settings.GRAPHICS_VIEW_DISTANCE).rangeEnd = 4;
  }

  @Override
  public FileHandle getBaseFolder() {
    return Gdx.files.external(Branding.NAME);
  }

  @Override
  public FileHandle getWorkingFolder() {
    return Gdx.files.internal(".");
  }

  @Override
  public void setupAssets() {
    super.setupAssets();
  }

  @Override
  public void logEnvironment() {
    Log.debug("Android Version:    " + Build.VERSION.RELEASE);
    Log.debug("Android SDK:        " + Build.VERSION.SDK_INT);
    Log.debug("Brand:              " + Build.BRAND);
    Log.debug("Model:              " + Build.MODEL);
    Log.debug("Product:            " + Build.PRODUCT);
    activityManager.getMemoryInfo(memoryInfo);
    Log.debug("System Memory:      " + (int) (memoryInfo.totalMem / 1048576) + "MB");
  }

  @Override
  public boolean isTouchScreen() {
    return true;
  }

  @Override
  protected void run(ApplicationListener applicationListener) {
    AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
    config.useAccelerometer = false;
    config.useCompass = false;
    config.useWakelock = true;
    config.useImmersiveMode = true;
    androidLauncher.initialize(applicationListener, config);
  }

  @Override
  public ModLoader getModLoader() {
    return modLoader;
  }

  @Override
  public void render() {
    if (back) {
      back = false;
      Menu current = Adapter.getMenu();

      if (current instanceof MainMenu) {
        Adapter.quit();
        return;
      }

      if (Cubes.getClient() != null || Cubes.getServer() != null || current == null) {
        Adapter.gotoMainMenu();
        return;
      }

      Menu prev = MenuManager.getPrevious(current);
      if (prev == null) return;
      Adapter.setMenu(prev);
    }
  }

  @Override
  public int getFreeMemory() {
    activityManager.getMemoryInfo(memoryInfo);
    return (int) (memoryInfo.availMem / 1048576);
  }

  @Override
  public boolean functionModifier() {
    return modifier;
  }

  @Override
  public LogWriter getCustomLogWriter() {
    return new AndroidLogWriter();
  }
}
