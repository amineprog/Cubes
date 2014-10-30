package ethanjones.cubes.graphics.menu.menus;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ethanjones.cubes.core.adapter.GraphicalAdapter;
import ethanjones.cubes.core.localization.Localization;

public class MultiplayerFailedMenu extends InfoMenu {

  private boolean firstRender = true;

  public MultiplayerFailedMenu(Exception e) {
    super(Localization.get("menu.multiplayer.failed_exception") + System.lineSeparator() + e.getClass().getSimpleName(), Localization.get("menu.general.return_main_menu"));
  }

  public MultiplayerFailedMenu() {
    super(Localization.get("menu.multiplayer.failed"), Localization.get("menu.general.return_main_menu"));
  }

  public void render() {
    super.render();
    if (!firstRender) return;
    addButtonListener(new EventListener() {
      @Override
      public boolean handle(Event event) {
        if (!(event instanceof ChangeListener.ChangeEvent)) return false;
        GraphicalAdapter.instance.setMenu(null);
        GraphicalAdapter.instance.setCubes(null, null);
        return true;
      }
    });
    GraphicalAdapter.instance.setCubes(null, null);
    firstRender = false;
  }
}
