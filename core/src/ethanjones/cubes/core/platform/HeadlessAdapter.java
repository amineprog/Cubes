package ethanjones.cubes.core.platform;

import ethanjones.cubes.core.system.Debug;
import ethanjones.cubes.core.system.Memory;
import ethanjones.cubes.graphics.menu.Menu;
import ethanjones.cubes.networking.server.ServerNetworkingParameter;
import ethanjones.cubes.side.Side;
import ethanjones.cubes.side.client.CubesClient;
import ethanjones.cubes.side.common.Cubes;
import ethanjones.cubes.side.server.CubesServer;

public class HeadlessAdapter implements AdapterInterface {

  private CubesServer cubesServer;

  public HeadlessAdapter() {
    Adapter.setInterface(this);
  }

  @Override
  public void create() {
    try {
      Thread.currentThread().setName(Side.Server.name());
      Cubes.setup(this);
      cubesServer = new CubesServer(new ServerNetworkingParameter());
      cubesServer.create();
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
      Memory.update();
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
    try {
      cubesServer.dispose();
    } catch (Exception e) {
      Debug.crash(e);
    }
  }

  @Override
  public void setClient(CubesClient cubesClient) throws UnsupportedOperationException {
    throw new UnsupportedOperationException("Headless adapter cannot set client");
  }

  @Override
  public void setServer(CubesServer cubesServer) throws UnsupportedOperationException {
    throw new UnsupportedOperationException("Headless adapter cannot set server");
  }

  @Override
  public void setMenu(Menu menu) throws UnsupportedOperationException {
    throw new UnsupportedOperationException("Headless adapter cannot set menu");
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
}
