package ethanjones.modularworld.side.client;

import com.badlogic.gdx.Gdx;
import ethanjones.modularworld.block.factory.BlockFactories;
import ethanjones.modularworld.entity.living.player.Player;
import ethanjones.modularworld.graphics.GraphicsHelper;
import ethanjones.modularworld.graphics.asset.AssetManager;
import ethanjones.modularworld.graphics.rendering.Renderer;
import ethanjones.modularworld.input.InputChain;
import ethanjones.modularworld.networking.NetworkingManager;
import ethanjones.modularworld.networking.common.packet.Packet;
import ethanjones.modularworld.networking.common.socket.SocketMonitor;
import ethanjones.modularworld.side.client.debug.Debug;
import ethanjones.modularworld.side.common.ModularWorld;
import ethanjones.modularworld.world.WorldClient;

public class ModularWorldClient extends ModularWorld {

  public static ModularWorldClient instance;

  public Player player;
  public InputChain inputChain;
  public Renderer renderer;

  public ModularWorldClient() {
    ModularWorldClient.instance = this;
  }

  @Override
  public void create() {
    super.create();
    //TODO: Rewrite settings, have two classes "Client" and "Server"

    NetworkingManager.connectClient();

    player = new Player();

    assetManager = new AssetManager();
    compatibility.getAssets(assetManager);
    GraphicsHelper.init(assetManager);
    inputChain = new InputChain();
    renderer = new Renderer();
    BlockFactories.loadGraphics();
    Gdx.input.setInputProcessor(inputChain.init());
    Gdx.input.setCursorCatched(true);

    world = new WorldClient();
  }

  @Override
  public void resize(int width, int height) {
    renderer.resize();
  }

  @Override
  public void render() {
    long currentTimeMillis = System.currentTimeMillis();
    inputChain.beforeRender();
    renderer.render();
    inputChain.afterRender();
    Debug.ram();
    Debug.loop(System.currentTimeMillis() - currentTimeMillis);
  }

  @Override
  public void dispose() {
    renderer.dispose();
    super.dispose();
  }

  @Override
  public void received(Packet packet, SocketMonitor socketMonitor) {

  }
}
