package ethanjones.cubes.graphics.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import java.util.ArrayList;
import java.util.Map;

import ethanjones.cubes.core.localization.Localization;
import ethanjones.cubes.core.platform.Adapter;
import ethanjones.cubes.core.settings.SettingGroup;
import ethanjones.cubes.core.settings.Settings;
import ethanjones.cubes.core.settings.SettingsSaveEvent;
import ethanjones.cubes.core.settings.VisualSettingManager;
import ethanjones.cubes.core.system.CubesException;
import ethanjones.cubes.graphics.gui.Gui;
import ethanjones.cubes.graphics.gui.MenuTools;
import ethanjones.cubes.graphics.gui.StageMenu;

public class SettingsMenu extends StageMenu implements VisualSettingManager {

  private static class ListObject {

    public final Label label;
    public final Actor actor;

    public ListObject(Label label, Actor actor) {
      this.label = label;
      this.actor = actor;
      if (!(actor instanceof Layout)) throw new CubesException("Settings actor must implement Layout");
    }
  }

  static final Value CELL_PADDING = new Value() {
    @Override
    public float get(Actor context) {
      return 5;
    }
  };
  static final Value CELL_WIDTH = new Value() {

    @Override
    public float get(Actor context) {
      return (Gdx.graphics.getWidth() / 2) - (CELL_PADDING.get(context) * 2);
    }
  };
  static final Value CELL_HEIGHT = new Value() {
    @Override
    public float get(Actor context) {
      return (Gdx.graphics.getHeight() / 10) - (CELL_PADDING.get(context) * 2);
    }
  };

  private final SettingGroup settingGroup;
  Label title;
  ScrollPane scrollPane;
  Table table;
  TextButton back;
  ArrayList<ListObject> listObjects = new ArrayList<ListObject>();

  public SettingsMenu() {
    this(Settings.getBaseSettingGroup());
  }

  public SettingsMenu(SettingGroup settingGroup) {
    this.settingGroup = settingGroup;

    title = new Label(Localization.get("menu.settings.title"), Gui.skin.get("title", Label.LabelStyle.class));

    table = new Table(Gui.skin);

    for (Map.Entry<String, SettingGroup> entry : settingGroup.getChildGroups().entrySet()) {
      Label name = new Label(Settings.getLocalisedSettingGroupName(entry.getKey()), Gui.skin);
      name.setAlignment(Align.left, Align.left);

      Actor actor = entry.getValue().getActor(this);

      listObjects.add(new ListObject(name, actor));
    }

    for (String str : settingGroup.getChildren()) {
      Label name = new Label(Settings.getLocalisedSettingGroupName(str), Gui.skin);
      name.setAlignment(Align.left, Align.left);

      Actor actor = Settings.getSetting(str).getActor(this);

      listObjects.add(new ListObject(name, actor));
    }

    scrollPane = new ScrollPane(table, Gui.skin);
    scrollPane.setScrollingDisabled(true, false);

    back = MenuTools.getBackButton(this);

    stage.addActor(title);
    stage.addActor(scrollPane);
    stage.addActor(back);
  }

  @Override
  public void resize(int width, int height) {
    super.resize(width, height);

    scrollPane.setBounds(0, height / 6, Gdx.graphics.getWidth(), height / 6 * 4);

    table.setWidth(width);
    table.clearChildren();

    for (int i = 0; i < listObjects.size(); i++) {
      ListObject listObject = listObjects.get(i);

      table.add(listObject.label).width(CELL_WIDTH).height(CELL_HEIGHT).pad(CELL_PADDING).fillX().fillY();
      table.add(listObject.actor).width(CELL_WIDTH).height(CELL_HEIGHT).pad(CELL_PADDING).fillX().fillY();
      table.row();
    }

    scrollPane.layout();

    MenuTools.setTitle(title);
    back.setBounds(width / 4, 0, width / 2, height / 6);
  }

  public void hide() {
    super.hide();
    for (ListObject listObject : listObjects) {
      listObject.actor.fire(new SettingsSaveEvent());
    }
    Settings.write();
  }

  @Override
  public void setSettingGroup(SettingGroup settingGroup) {
    Adapter.setMenu(new SettingsMenu(settingGroup));
  }
}
