package ethanjones.modularworld;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import ethanjones.modularworld.block.factory.BlockFactories;
import ethanjones.modularworld.core.Branding;
import ethanjones.modularworld.core.compatibility.Compatibility;
import ethanjones.modularworld.core.events.EventBus;
import ethanjones.modularworld.core.logging.Log;
import ethanjones.modularworld.core.settings.Settings;
import ethanjones.modularworld.core.settings.SettingsManager;
import ethanjones.modularworld.core.timing.Timing;
import ethanjones.modularworld.entity.living.player.Player;
import ethanjones.modularworld.graphics.rendering.Renderer;
import ethanjones.modularworld.input.InputChain;
import ethanjones.modularworld.world.World;
import ethanjones.modularworld.world.generator.BasicWorldGenerator;

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

  public ModularWorld() {
    ModularWorld.instance = this;
  }

  @Override
  public void create() {
    Log.info(Branding.NAME, Branding.DEBUG);

    eventBus = new EventBus().register(this);
    compatibility = Compatibility.getCompatibility();

    player = new Player();

    BlockFactories.init();

    inputChain = new InputChain();

    renderer = new Renderer();
    BlockFactories.loadGraphics();

    world = new World(new BasicWorldGenerator());

    Gdx.input.setInputProcessor(inputChain.init());
    Gdx.input.setCursorCatched(true);

    settings = new SettingsManager();
    Settings.processAll();
    timing = new Timing();
  }

  @Override
  public void resize(int width, int height) {
    renderer.resize();
  }

  @Override
  public void render() {
    timing.update();
    inputChain.beforeRender();
    renderer.render();
    inputChain.afterRender();
  }

  @Override
  public void pause() {

  }

  @Override
  public void resume() {

  }

  @Override
  public void dispose() {
    renderer.dispose();
    world.dispose();
  }

}
