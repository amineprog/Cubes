package ethanjones.cubes.core.platform.desktop;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.files.FileHandle;

import ethanjones.cubes.core.system.Branding;

public class ServerCompatibility extends DesktopCompatibility {

  protected ServerCompatibility(ServerLauncher serverLauncher, String[] arg) {
    super(serverLauncher, Application.ApplicationType.HeadlessDesktop, arg);
  }

  public boolean isServer() {
    return true;
  }

  @Override
  protected void run(ApplicationListener applicationListener) {
    new HeadlessApplication(applicationListener);
  }

  @Override
  public FileHandle getBaseFolder() {
    FileHandle fileHandle = getWorkingFolder();
    if (Branding.IS_DEBUG) {
      fileHandle = fileHandle.child("server");
    }
    return fileHandle;
  }
}
