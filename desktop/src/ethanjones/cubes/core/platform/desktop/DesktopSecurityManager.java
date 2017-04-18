package ethanjones.cubes.core.platform.desktop;

import ethanjones.cubes.core.logging.Log;
import ethanjones.cubes.core.logging.LogLevel;
import ethanjones.cubes.side.common.Cubes;

import java.security.Permission;

public class DesktopSecurityManager extends SecurityManager {

  protected static void setup() {
    try {
      System.setSecurityManager(new DesktopSecurityManager());
    } catch (SecurityException e) {
      System.out.println("Failed to set the Security Manager");
      if (System.getSecurityManager() != null) {
        System.out.println("Security Manager class: \"" + System.getSecurityManager().getClass() + "\"");
      }
      e.printStackTrace();
      System.exit(1);
    }
  }

  @Override
  public synchronized void checkPermission(Permission perm) {
    if (perm.getName().startsWith("exitVM.")) {
      if (!Cubes.cubesSetup()) return;
      if (getClassContext().length < 5) throw new SecurityException();
      String c = getClassContext()[4].getName();
      if (c.startsWith("com.badlogic.gdx.backends.lwjgl.Lwjgl") || c.equals("ethanjones.cubes.side.common.Cubes") || c.equals("ethanjones.cubes.core.system.Debug") || c.equals("ethanjones.cubes.core.platform.Adapter") || c.equals("ethanjones.cubes.core.platform.desktop.DesktopCompatibility")) {
        Log.debug("Allowing class \"" + c + "\" to exit");
        stackTrace(LogLevel.debug);
      } else {
        Log.error("Class \"" + c + "\" tried to exit");
        stackTrace(LogLevel.error);
        throw new SecurityException("Cannot exit");
      }
    } else if ("setSecurityManager".equals(perm.getName())) {
      if (getClassContext().length < 4) throw new SecurityException();
      String c = getClassContext()[3].getName();
      Log.error("Class \"" + c + "\" tried to set the security manager");
      stackTrace(LogLevel.error);
      throw new SecurityException("Cannot replace the security manager");
    } else if ("createSecurityManager".equals(perm.getName())) {
      if (getClassContext().length < 3) throw new SecurityException();
      String c = getClassContext()[2].getName();
      Log.error("Class \"" + c + "\" tried to create a security manager");
      stackTrace(LogLevel.error);
      throw new SecurityException("Cannot create a security manager");
    }
  }

  private static void stackTrace(LogLevel logLevel) {
    Log.log(logLevel, "Stack trace:");
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    for (int i = 3; i < stackTrace.length; i++) {
      StackTraceElement stackTraceElement = stackTrace[i];
      Log.log(logLevel, "  " + stackTraceElement.toString());
    }
  }
}
