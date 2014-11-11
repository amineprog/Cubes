package ethanjones.cubes.core.platform;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

import ethanjones.cubes.core.logging.Log;
import ethanjones.cubes.core.system.Branding;
import ethanjones.cubes.core.system.Debug;
import ethanjones.cubes.core.system.Memory;
import ethanjones.cubes.graphics.menu.Fonts;
import ethanjones.cubes.graphics.menu.Menu;
import ethanjones.cubes.graphics.menu.MenuManager;
import ethanjones.cubes.graphics.menu.menus.MainMenu;
import ethanjones.cubes.side.Side;
import ethanjones.cubes.side.client.CubesClient;
import ethanjones.cubes.side.common.Cubes;
import ethanjones.cubes.side.server.CubesServer;
import ethanjones.cubes.side.server.CubesServerThread;

public class GraphicalAdapter implements AdapterInterface {

  private Menu menu;
  private CubesServerThread cubesServerThread;
  private CubesClient cubesClient;

  public GraphicalAdapter() {
    Adapter.setInterface(this);
  }

  @Override
  public void setClient(CubesClient cubesClient) throws UnsupportedOperationException {
    //CubesSecurity.checkSetCubes();
    if (cubesClient != null) {
      this.cubesClient = cubesClient;
      Log.debug("Client set");
      cubesClient.create();
      cubesClient.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    } else {
      this.cubesClient = null;
      Log.debug("Client set to null");
    }
  }

  @Override
  public void setServer(CubesServer cubesServer) throws UnsupportedOperationException {
    //CubesSecurity.checkSetCubes();
    if (cubesServer != null) {
      cubesServerThread = new CubesServerThread(cubesServer);
      Log.debug("Server set");
      cubesServerThread.start();
    } else {
      cubesServerThread = null;
      Log.debug("Server set to null");
    }
  }

  @Override
  public void setMenu(Menu menu) {
    //CubesSecurity.checkSetMenu();
    Menu old = this.menu;
    if (old != null) {
      old.hide();
    }
    this.menu = menu;
    if (menu != null) {
      Log.debug("Menu set to " + menu.getClass().getSimpleName());
      MenuManager.setMenu(menu);
      menu.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
      menu.show();
    } else {
      Log.debug("Menu set to null");
    }

  }

  @Override
  public CubesClient getClient() {
    return cubesClient;
  }

  @Override
  public CubesServer getServer() {
    return cubesServerThread != null ? cubesServerThread.server : null;
  }

  @Override
  public Menu getMenu() {
    return menu;
  }

  @Override
  public void create() {
    try {
      Gdx.graphics.setTitle(Branding.DEBUG);
      Thread.currentThread().setName(Side.Client.name());
      Cubes.setup(this);
      setMenu(new MainMenu());
    } catch (Exception e) {
      Debug.crash(e);
    }
  }

  @Override
  public void resize(int width, int height) {
    try {
      Fonts.resize();
      if (menu != null) menu.resize(width, height);
      if (cubesClient != null) cubesClient.resize(width, height);
    } catch (Exception e) {
      Debug.crash(e);
    }
  }

  @Override
  public void render() {
    try {
      Compatibility.get().render();
      Memory.update();
      Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
      Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
      if (menu == null) {
        Gdx.input.setCursorCatched(true);
      } else {
        Gdx.input.setCursorCatched(false);
      }
      if (cubesClient != null) cubesClient.render();
      if (menu != null) menu.render(); //Render menu over client
    } catch (Exception e) {
      Debug.crash(e);
    }
  }

  @Override
  public void pause() {
    try {
      if (cubesClient != null) cubesClient.pause();
    } catch (Exception e) {
      Debug.crash(e);
    }
  }

  @Override
  public void resume() {
    try {
      if (cubesClient != null) cubesClient.resume();
    } catch (Exception e) {
      Debug.crash(e);
    }
  }

  @Override
  public void dispose() {
    try {
      if (menu != null) {
        menu.hide();
      }
      if (cubesClient != null) cubesClient.dispose();
      if (cubesServerThread != null) {
        cubesServerThread.server.dispose();
        try {
          cubesServerThread.join(1000); //Wait for 1 second
          if (cubesServerThread.isAlive()) Log.error("Failed to stop server thread");
        } catch (InterruptedException e) {
        }
      }
    } catch (Exception e) {
      Debug.crash(e);
    }
  }
}
