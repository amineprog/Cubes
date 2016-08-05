package ethanjones.cubes.graphics.menus;

import ethanjones.cubes.core.localization.Localization;
import ethanjones.cubes.core.logging.Log;
import ethanjones.cubes.core.platform.Adapter;
import ethanjones.cubes.core.system.Branding;
import ethanjones.cubes.graphics.assets.Assets;
import ethanjones.cubes.graphics.menu.Fonts;
import ethanjones.cubes.graphics.menu.Menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import static ethanjones.cubes.graphics.GUI.HEIGHT;
import static ethanjones.cubes.graphics.GUI.WIDTH;

public class MainMenu extends Menu {

  private static Value cellHeight = new Value() {
    @Override
    public float get(Actor context) {
      return HEIGHT / 8;
    }
  };
  private static Value cellWidth = new Value() {
    @Override
    public float get(Actor context) {
      return WIDTH / 3 * 2;
    }
  };
  Image logo;
  Label version;
  Label author;
  Table buttons;
  TextButton singleplayer;
  TextButton multiplayer;
  TextButton serveronly;
  TextButton settings;
  TextButton quit;

  public MainMenu() {
    super();
    logo = new Image(new TextureRegionDrawable(Assets.getTextureRegion("core:logo.png")), Scaling.fillY, Align.center);
    version = new Label(Branding.DEBUG, new Label.LabelStyle(Fonts.smallHUD, Color.WHITE));
    author = new Label(Branding.AUTHOR, new Label.LabelStyle(Fonts.smallHUD, Color.WHITE));
    buttons = new Table();
    buttons.defaults().height(cellHeight).width(cellWidth).pad(5).fillX().fillY();
    buttons.add(singleplayer = new TextButton(Localization.get("menu.main.singleplayer"), skin)).row();
    singleplayer.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        Adapter.setMenu(new SingleplayerSavesMenu());
      }
    });
    buttons.add(multiplayer = new TextButton(Localization.get("menu.main.multiplayer"), skin)).row();
    multiplayer.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        Adapter.setMenu(new MultiplayerConnectMenu());
      }
    });
    buttons.add(serveronly = new TextButton(Localization.get("menu.main.serveronly"), skin)).row();
    serveronly.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        Adapter.setMenu(new ServerSetupMenu());
      }
    });
    buttons.add(settings = new TextButton(Localization.get("menu.main.settings"), skin)).row();
    settings.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        Adapter.setMenu(new SettingsMenu());
      }
    });
    buttons.add(quit = new TextButton(Localization.get("menu.main.quit"), skin)).row();
    quit.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        Log.debug("Quit pressed");
        Adapter.quit();
      }
    });

    stage.addActor(logo);
    stage.addActor(version);
    stage.addActor(author);
    stage.addActor(buttons);
  }

  @Override
  public void resize(float width, float height) {
    super.resize(width, height);
    logo.setBounds(0, height / 6 * 5, width, height / 6);
    version.setBounds(0, 0, author.getPrefWidth(), author.getPrefHeight());
    version.setAlignment(Align.left);
    author.setBounds(width - author.getPrefWidth(), 0, author.getPrefWidth(), author.getPrefHeight());
    author.setAlignment(Align.right);
    buttons.setBounds(0, 0, width, height / 6 * 5);
    buttons.align(Align.top);
    buttons.layout();
  }
}
