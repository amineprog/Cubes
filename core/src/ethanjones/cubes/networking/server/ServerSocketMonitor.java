package ethanjones.cubes.networking.server;

import ethanjones.cubes.core.logging.Log;
import ethanjones.cubes.networking.Networking;
import ethanjones.cubes.side.common.Side;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.Disposable;

import java.util.concurrent.atomic.AtomicBoolean;

public class ServerSocketMonitor implements Runnable, Disposable {

  protected final AtomicBoolean running;
  private final ServerNetworking serverNetworking;
  private ServerSocket serverSocket;
  private Thread thread;
  private int port;

  public ServerSocketMonitor(int port, ServerNetworking serverNetworking) {
    this.port = port;
    this.serverNetworking = serverNetworking;
    serverSocket = Gdx.net.newServerSocket(Protocol.TCP, port, Networking.serverSocketHints);
    running = new AtomicBoolean(true);
  }

  @Override
  public void run() {
    Side.setSide(Side.Server);
    while (running.get()) {
      try {
        Socket accept = serverSocket.accept(Networking.socketHints);
        ServerConnectionInitializer.check(accept);
      } catch (Exception e) {
        if (running.get()) Log.error(e);
      }
    }
    dispose();
  }

  @Override
  public void dispose() {
    running.set(false);
    serverSocket.dispose();
    getThread().interrupt();
  }

  protected Thread getThread() {
    return thread;
  }

  public Thread start() {
    if (thread != null) return thread;
    thread = new Thread(this);
    thread.setDaemon(true);
    thread.setName("Server Socket Monitor: " + port);
    thread.start();
    return thread;
  }
}
