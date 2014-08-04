package ethanjones.modularworld.networking.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.ServerSocket;
import ethanjones.modularworld.core.logging.Log;
import ethanjones.modularworld.networking.NetworkUtil;
import ethanjones.modularworld.networking.NetworkingManager;
import ethanjones.modularworld.networking.common.SocketMonitorBase;

public class SocketMonitorServer extends SocketMonitorBase {

  ServerSocket serverSocket;

  public SocketMonitorServer(int port) {
    super("localhost:" + port);
    serverSocket = Gdx.net.newServerSocket(NetworkUtil.protocol, port, NetworkUtil.serverSocketHints);
  }

  @Override
  public void run() {
    while (running) {
      try {
        NetworkingManager.serverNetworking.accepted(serverSocket.accept(NetworkUtil.socketHints));
      } catch (Exception e) {
        if (running) Log.error(e);
      }
    }
    dispose();
  }

  @Override
  public void dispose() {
    running = false;
    serverSocket.dispose();
    getThread().interrupt();
  }
}
