package ethanjones.modularworld.core.platform.desktop;

public class HeadlessLauncher {
  public static void main(String[] arg) {
    DesktopSecurityManager.setup();
    new HeadlessCompatibility(arg).startModularWorld();
  }
}
