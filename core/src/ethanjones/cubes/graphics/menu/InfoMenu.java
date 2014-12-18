package ethanjones.cubes.graphics.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

import ethanjones.cubes.graphics.gui.Fonts;
import ethanjones.cubes.graphics.gui.Gui;
import ethanjones.cubes.graphics.gui.MenuTools;
import ethanjones.cubes.graphics.gui.StageMenu;

public class InfoMenu extends StageMenu {

  protected Label text;
  protected TextButton button;

  public InfoMenu(String labelText, String buttonText) {
    super();
    text = new Label(labelText, new LabelStyle(Fonts.Size2, Color.WHITE));
    text.setAlignment(Align.center, Align.center);
    button = new TextButton(buttonText, Gui.skin);
    stage.addActor(button);
  }

  public InfoMenu(String labelText, boolean back) {
    super();
    text = new Label(labelText, new LabelStyle(Fonts.Size2, Color.WHITE));
    text.setAlignment(Align.center, Align.center);
    stage.addActor(text);
    if (back) {
      button = MenuTools.getBackButton(this);
      stage.addActor(button);
    }
  }

  @Override
  public void resize(int width, int height) {
    super.resize(width, height);
    if (button != null) {
      button.layout();
      text.layout();
      text.setBounds(0, button.getPrefHeight(), width, height - button.getPrefHeight());
      button.setBounds(width / 2 - button.getPrefWidth() / 2, height / 4, button.getPrefWidth(), button.getPrefHeight());
    } else {
      text.layout();
      text.setBounds(0, 0, width, height);
    }
  }

  public boolean addButtonListener(EventListener listener) {
    if (button == null) return false;
    return button.addListener(listener);
  }
}
