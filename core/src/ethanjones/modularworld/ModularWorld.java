package ethanjones.modularworld;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import ethanjones.modularworld.block.factory.BlockFactories;
import ethanjones.modularworld.core.Branding;
import ethanjones.modularworld.core.compatibility.Compatibility;
import ethanjones.modularworld.core.debug.Debug;
import ethanjones.modularworld.core.events.EventBus;
import ethanjones.modularworld.core.logging.Log;
import ethanjones.modularworld.core.settings.Settings;
import ethanjones.modularworld.core.settings.SettingsManager;
import ethanjones.modularworld.core.timing.Timing;
import ethanjones.modularworld.entity.living.player.Player;
import ethanjones.modularworld.graphics.rendering.Renderer;
import ethanjones.modularworld.input.InputChain;
import ethanjones.modularworld.networking.Networking;
import ethanjones.modularworld.world.World;
import ethanjones.modularworld.world.generator.BasicWorldGenerator;

import java.io.File;

public class ModularWorld implements ApplicationListener {

  public static ModularWorld instance;

  public Player player;
  public InputChain inputChain;
  public Renderer renderer;
  public World world;

  public EventBus eventBus;
  public Compatibility compatibility;
  public SettingsManager settings;
  public Timing timing;

  public File baseFolder;

  public ModularWorld() {
    ModularWorld.instance = this;
  }

  @Override
  public void create() {
    Log.info(Branding.NAME, Branding.DEBUG);
    Debug.printProperties();

    eventBus = new EventBus().register(this);
    compatibility = Compatibility.getCompatibility();

    baseFolder = compatibility.getBaseFolder();
    Log.info("Base Folder: " + baseFolder.getAbsolutePath());

    player = new Player();

    BlockFactories.init();

    if (!compatibility.isHeadless()) {
      inputChain = new InputChain();
      renderer = new Renderer();
      BlockFactories.loadGraphics();
    }

    world = new World(new BasicWorldGenerator());

    if (!compatibility.isHeadless()) {
      Gdx.input.setInputProcessor(inputChain.init());
      Gdx.input.setCursorCatched(true);
    }

    settings = new SettingsManager();
    Settings.processAll();

    Networking.startServerNetworking();

    timing = new Timing();
  }

  @Override
  public void resize(int width, int height) {
    if (!compatibility.isHeadless()) {
      renderer.resize();
    }
  }

  @Override
  public void render() {
    long currentTimeMillis = System.currentTimeMillis();
    timing.update();
    if (!compatibility.isHeadless()) {
      inputChain.beforeRender();
      renderer.render();
      inputChain.afterRender();
      Debug.renderingLoop(System.currentTimeMillis() - currentTimeMillis);
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
    if (!compatibility.isHeadless()) {
      renderer.dispose();
    }
    world.dispose();
  }

}
