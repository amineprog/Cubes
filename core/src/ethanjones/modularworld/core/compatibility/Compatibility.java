package ethanjones.modularworld.core.compatibility;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import ethanjones.modularworld.core.adapter.GraphicalAdapter;
import ethanjones.modularworld.core.adapter.HeadlessAdapter;
import ethanjones.modularworld.core.logging.Log;
import ethanjones.modularworld.core.mod.ModLoader;
import ethanjones.modularworld.core.system.Debug;
import ethanjones.modularworld.core.system.ModularWorldException;
import ethanjones.modularworld.graphics.assets.AssetFinder;
import ethanjones.modularworld.graphics.assets.Assets;
import ethanjones.modularworld.side.Side;
import ethanjones.modularworld.side.Sided;

public abstract class Compatibility {

  private static Compatibility compatibility;
  public final Application.ApplicationType applicationType;

  protected Compatibility(Application.ApplicationType applicationType) {
    this.applicationType = applicationType;
  }

  public static Compatibility get() {
    return compatibility;
  }

  public void init(Side side) {
    if (side != null) Sided.getEventBus().register(this);
  }

  public boolean isHeadless() {
    return false;
  }

  public FileHandle getBaseFolder() {
    return Gdx.files.absolute(System.getProperty("user.dir"));
  }

  public FileHandle getWorkingFolder() {
    return Gdx.files.absolute(System.getProperty("user.dir"));
  }

  /**
   * Gets assets using FileHandles by default
   */
  public void setupAssets() {
    AssetFinder.findAssets(Gdx.files.internal("assets"), Assets.CORE);
  }

  public void logEnvironment() {

  }

  public boolean isTouchScreen() {
    return false;
  }

  protected abstract void run(ApplicationListener applicationListener);

  public void startModularWorld() {
    compatibility = this;

    Debug.UncaughtExceptionHandler uncaughtExceptionHandler = new Debug.UncaughtExceptionHandler();
    Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
    Thread.currentThread().setUncaughtExceptionHandler(uncaughtExceptionHandler);

    try {
      if (isHeadless()) {
        run(new HeadlessAdapter());
      } else {
        run(new GraphicalAdapter());
      }
    } catch (Exception e) {
      try {
        Log.error("Failed to start", ModularWorldException.getModularWorldException(e));
      } catch (Exception ex) {
        if (ex instanceof ModularWorldException) {
          throw (ModularWorldException) ex;
        } else {
          throw ModularWorldException.getModularWorldException(e);
        }
      }
    }
  }

  public abstract ModLoader getModLoader();
}
