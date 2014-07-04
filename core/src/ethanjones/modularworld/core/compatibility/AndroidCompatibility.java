package ethanjones.modularworld.core.compatibility;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import ethanjones.modularworld.core.Branding;
import ethanjones.modularworld.core.events.EventHandler;
import ethanjones.modularworld.core.events.setting.AfterProcessSettingEvent;
import ethanjones.modularworld.core.settings.Settings;

public class AndroidCompatibility extends Compatibility {

  protected AndroidCompatibility() {
    super(Application.ApplicationType.Android);
  }

  @EventHandler
  public void compatibilitySettings(AfterProcessSettingEvent event) {
    Settings.renderer_block_viewDistance.getIntegerSetting().setValue(0);
  }

  @Override
  public FileHandle getBaseFolder() {
    return Gdx.files.external(Branding.NAME);
  }

  @Override
  public FileHandle getWorkingFolder() {
    return Gdx.files.internal(".");
  }
}
