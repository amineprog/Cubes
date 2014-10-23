package ethanjones.modularworld.side.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import ethanjones.modularworld.block.Blocks;
import ethanjones.modularworld.core.compatibility.Compatibility;
import ethanjones.modularworld.core.system.ModularWorldException;
import ethanjones.modularworld.entity.living.player.Player;
import ethanjones.modularworld.graphics.rendering.Renderer;
import ethanjones.modularworld.input.InputChain;
import ethanjones.modularworld.input.keyboard.KeyboardHelper;
import ethanjones.modularworld.networking.NetworkingManager;
import ethanjones.modularworld.networking.client.ClientNetworkingParameter;
import ethanjones.modularworld.side.Side;
import ethanjones.modularworld.side.common.ModularWorld;
import ethanjones.modularworld.world.WorldClient;

public class ModularWorldClient extends ModularWorld implements ApplicationListener {

  public static ModularWorldClient instance;
  private final ClientNetworkingParameter clientNetworkingParameter;

  public Player player;
  public InputChain inputChain;
  public Renderer renderer;
  private boolean disposed;

  public ModularWorldClient(ClientNetworkingParameter clientNetworkingParameter) {
    super(Side.Client);
    if (Compatibility.get().isHeadless()) throw new ModularWorldException("Client requires Graphics ");
    this.clientNetworkingParameter = clientNetworkingParameter;
  }

  @Override
  public void create() {
    super.create();

    ClientDebug.setup();
    NetworkingManager.connectClient(clientNetworkingParameter);

    inputChain = new InputChain();
    renderer = new Renderer();
    player = new Player(renderer.block.camera);
    Blocks.loadGraphics();

    inputChain.setup();
    Gdx.input.setInputProcessor(InputChain.getInputMultiplexer());

    world = new WorldClient();
  }

  @Override
  public void resize(int width, int height) {
    renderer.resize();
  }

  @Override
  public void render() {
    if (KeyboardHelper.isKeyDown(Input.Keys.ESCAPE)) {
      ModularWorld.quit(false);
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
  public void pause() {

  }

  @Override
  public void resume() {

  }

  @Override
  public void tick() {
    super.tick();
  }

  @Override
  public void dispose() {
    if (disposed) return;
    super.dispose();
    renderer.dispose();
    inputChain.dispose();
    disposed = true;
  }
}
