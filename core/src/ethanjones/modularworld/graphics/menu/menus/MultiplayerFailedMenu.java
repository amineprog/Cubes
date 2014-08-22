package ethanjones.modularworld.graphics.menu.menus;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import ethanjones.modularworld.core.adapter.GraphicalAdapter;
import ethanjones.modularworld.core.localization.Localization;

public class MultiplayerFailedMenu extends InfoMenu {

  private boolean firstRender = true;

  public MultiplayerFailedMenu(Exception e) {
    super(Localization.get("menu.multiplayer_connect.failed_exception") + System.lineSeparator() + e.getClass().getSimpleName(), Localization.get("menu.general.return_main_menu"));
  }

  public MultiplayerFailedMenu() {
    super(Localization.get("menu.multiplayer_connect.failed"), Localization.get("menu.general.return_main_menu"));
  }

  public void render() {
    super.render();
    if (!firstRender) return;
    addButtonListener(new EventListener() {
      @Override
      public boolean handle(Event event) {
        if (!(event instanceof ChangeListener.ChangeEvent)) return false;
        GraphicalAdapter.instance.gotoMainMenu();
        return true;
      }
    });
    GraphicalAdapter.instance.setModularWorld(null, null);
    firstRender = false;
  }
}
