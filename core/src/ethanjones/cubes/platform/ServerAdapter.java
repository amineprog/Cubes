package ethanjones.cubes.platform;

import ethanjones.cubes.common.localization.Localization;
import ethanjones.cubes.common.logging.Log;
import ethanjones.cubes.common.Debug;
import ethanjones.cubes.client.graphics.menu.Menu;
import ethanjones.cubes.common.networking.NetworkingManager;
import ethanjones.cubes.common.networking.server.ServerNetworkingParameter;
import ethanjones.cubes.common.Side;
import ethanjones.cubes.client.CubesClient;
import ethanjones.cubes.Cubes;
import ethanjones.cubes.server.CubesServer;
import ethanjones.cubes.server.dedicated.DedicatedServer;

public class ServerAdapter implements AdapterInterface {

  private DedicatedServer cubesServer;
  private Thread thread;

  public ServerAdapter() {
    Adapter.setInterface(this);
  }

  @Override
  public void create() {
    try {
      thread = Thread.currentThread();
      thread.setName(getSide().name());
      Cubes.setup(this);
      NetworkingManager.serverPreInit(new ServerNetworkingParameter());
      cubesServer = new DedicatedServer();
      cubesServer.create();
      Log.info(Localization.get("server.server_loaded"));
    } catch (Exception e) {
      Debug.crash(e);
    }
  }

  @Override
  public void resize(int width, int height) {

  }

  @Override
  public void render() {
    try {
      Compatibility.get().render();
      cubesServer.render();
    } catch (Exception e) {
      Debug.crash(e);
    }
  }

  @Override
  public void pause() {

  }

  @Override
  public void resume() {

  }

  @Override
  public void dispose() {
    Adapter.dispose();
  }

  @Override
  public Thread getThread() {
    return thread;
  }

  @Override
  public void setClient(CubesClient cubesClient) throws UnsupportedOperationException {
    //throw new UnsupportedOperationException("Cannot set client");
  }

  @Override
  public void setServer(CubesServer cubesServer) throws UnsupportedOperationException {
    //throw new UnsupportedOperationException("Cannot set server");
  }

  @Override
  public void setMenu(Menu menu) throws UnsupportedOperationException {
    //throw new UnsupportedOperationException("Cannot set menu");
  }

  @Override
  public CubesClient getClient() {
    return null;
  }

  @Override
  public CubesServer getServer() {
    return cubesServer;
  }

  @Override
  public Menu getMenu() {
    return null;
  }

  @Override
  public Side getSide() {
    return Side.Server;
  }
}
