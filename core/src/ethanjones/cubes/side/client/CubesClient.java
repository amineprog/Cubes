package ethanjones.cubes.side.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import ethanjones.cubes.block.Blocks;
import ethanjones.cubes.core.compatibility.Compatibility;
import ethanjones.cubes.core.mod.ModManager;
import ethanjones.cubes.core.mod.event.StartingClientEvent;
import ethanjones.cubes.core.mod.event.StoppingClientEvent;
import ethanjones.cubes.core.system.CubesException;
import ethanjones.cubes.entity.living.player.Player;
import ethanjones.cubes.graphics.rendering.Renderer;
import ethanjones.cubes.input.InputChain;
import ethanjones.cubes.input.keyboard.KeyboardHelper;
import ethanjones.cubes.networking.NetworkingManager;
import ethanjones.cubes.networking.client.ClientNetworkingParameter;
import ethanjones.cubes.side.Side;
import ethanjones.cubes.side.common.Cubes;
import ethanjones.cubes.world.WorldClient;

public class CubesClient extends Cubes implements ApplicationListener {

  public static CubesClient instance;
  private final ClientNetworkingParameter clientNetworkingParameter;

  public Player player;
  public InputChain inputChain;
  public Renderer renderer;
  private boolean disposed;

  public CubesClient(ClientNetworkingParameter clientNetworkingParameter) {
    super(Side.Client);
    if (Compatibility.get().isHeadless()) throw new CubesException("Client requires Graphics ");
    this.clientNetworkingParameter = clientNetworkingParameter;
  }

  @Override
  public void create() {
    super.create();
    NetworkingManager.connectClient(clientNetworkingParameter);

    inputChain = new InputChain();
    renderer = new Renderer();
    player = new Player(renderer.block.camera);
    Blocks.loadGraphics();

    inputChain.setup();
    Gdx.input.setInputProcessor(InputChain.getInputMultiplexer());

    world = new WorldClient();

    ModManager.postModEvent(new StartingClientEvent());
  }

  @Override
  public void render() {
    if (KeyboardHelper.isKeyDown(Input.Keys.ESCAPE)) {
      Cubes.quit(false);
      return;
    }
    super.render();
    inputChain.beforeRender();
    if (renderer.hud.isDebugEnabled()) ClientDebug.update();
    renderer.render();
    inputChain.afterRender();
    player.update();
  }

  @Override
  public void dispose() {
    if (disposed) return;
    ModManager.postModEvent(new StoppingClientEvent());
    super.dispose();
    renderer.dispose();
    inputChain.dispose();
    disposed = true;
  }

  @Override
  public void tick() {
    super.tick();
  }

  @Override
  public void resize(int width, int height) {
    renderer.resize();
  }

  @Override
  public void pause() {

  }

  @Override
  public void resume() {

  }
}