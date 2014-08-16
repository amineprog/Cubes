package ethanjones.modularworld.graphics.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import ethanjones.modularworld.graphics.GraphicsHelper;
import ethanjones.modularworld.input.InputChain;

public abstract class Menu {

  public static final Skin skin;

  private static SpriteBatch spriteBatch;
  private static ScreenViewport viewport;
  protected static Stage stage;

  static {
    skin = new Skin();
    skin.add("default", GraphicsHelper.getFont());
    skin.add("default", new Label.LabelStyle(skin.getFont("default"), Color.WHITE));

    NinePatch buttonDown = new NinePatch(GraphicsHelper.getTexture("hud/ButtonDown.png").textureRegion, 8, 8, 8, 8);
    NinePatch buttonUp = new NinePatch(GraphicsHelper.getTexture("hud/ButtonUp.png").textureRegion, 8, 8, 8, 8);
    skin.add("default", new TextButton.TextButtonStyle(new NinePatchDrawable(buttonUp), new NinePatchDrawable(buttonDown), null, skin.getFont("default")));

    NinePatch textBackground = new NinePatch(GraphicsHelper.getTexture("hud/TextBox.png").textureRegion, 8, 8, 8, 8);
    skin.add("default", new TextField.TextFieldStyle(
        skin.getFont("default"), //GraphicsHelper.getFont() Own copy of font because of ResizableTextField
        Color.BLACK,
        new TextureRegionDrawable(GraphicsHelper.getTexture("hud/TextCursor.png").textureRegion),
        new TextureRegionDrawable(GraphicsHelper.getTexture("hud/TextSelection.png").textureRegion),
        new NinePatchDrawable(textBackground))
    );

    spriteBatch = new SpriteBatch();
    viewport = new ScreenViewport();
    stage = new Stage(viewport, spriteBatch);
  }

  public static void staticDispose() {
    spriteBatch.dispose();
    stage.dispose();
  }

  public Array<Actor> actors;

  public Menu() {
    actors = new Array<Actor>();
  }

  /**
   * Have to add actors to the stage here
   */
  public abstract void addActors();

  public void resize(int width, int height) {
    viewport.update(width, height, true);
  }

  public void render() {
    stage.act();
    stage.draw();
  }

  public final void hide() {
    InputChain.getInputMultiplexer().removeProcessor(stage);
    stage.clear();
  }

  public final void show() {
    InputChain.getInputMultiplexer().addProcessor(0, stage);
    addActors();
  }
}