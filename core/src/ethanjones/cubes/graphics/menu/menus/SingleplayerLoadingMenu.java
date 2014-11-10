package ethanjones.cubes.graphics.menu.menus;

import ethanjones.cubes.core.adapter.GraphicalAdapter;
import ethanjones.cubes.core.localization.Localization;
import ethanjones.cubes.core.settings.Settings;
import ethanjones.cubes.networking.client.ClientNetworkingParameter;
import ethanjones.cubes.networking.server.ServerNetworkingParameter;
import ethanjones.cubes.side.Side;
import ethanjones.cubes.side.Sided;
import ethanjones.cubes.side.client.CubesClient;
import ethanjones.cubes.side.server.CubesServer;

public class SingleplayerLoadingMenu extends InfoMenu {

  public SingleplayerLoadingMenu() {
    super(Localization.get("menu.general.loading"), false);
  }

  public void render() {
    super.render();
    if (Sided.isSetup(Side.Server)) return;
    CubesClient client = new CubesClient(new ClientNetworkingParameter("localhost", Settings.getIntegerSettingValue(Settings.NETWORKING_PORT)));
    client.wait = new Object(); //Makes sure server networking starts first
    GraphicalAdapter.instance.setCubes(new CubesServer(new ServerNetworkingParameter()), client);
    GraphicalAdapter.instance.setMenu(null);
  }
}
