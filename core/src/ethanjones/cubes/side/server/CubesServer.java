package ethanjones.cubes.side.server;

import java.util.List;
import java.util.concurrent.TimeUnit;

import ethanjones.cubes.block.Blocks;
import ethanjones.cubes.core.localization.Localization;
import ethanjones.cubes.core.logging.Log;
import ethanjones.cubes.core.mod.ModManager;
import ethanjones.cubes.core.mod.event.StartingServerEvent;
import ethanjones.cubes.core.mod.event.StoppingServerEvent;
import ethanjones.cubes.core.platform.Adapter;
import ethanjones.cubes.core.system.Executor;
import ethanjones.cubes.core.timing.TimeHandler;
import ethanjones.cubes.networking.NetworkingManager;
import ethanjones.cubes.networking.server.ClientIdentifier;
import ethanjones.cubes.networking.socket.SocketMonitor;
import ethanjones.cubes.side.Side;
import ethanjones.cubes.side.Sided;
import ethanjones.cubes.side.common.Cubes;
import ethanjones.cubes.side.server.command.CommandManager;
import ethanjones.cubes.world.generator.BasicTerrainGenerator;
import ethanjones.cubes.world.server.WorldServer;

public abstract class CubesServer extends Cubes implements TimeHandler {

  public CubesServer() {
    super(Side.Server);
  }

  @Override
  public void create() {
    if (state.isSetup()) return;
    super.create();
    CommandManager.reset();
    NetworkingManager.serverInit();

    world = new WorldServer(new BasicTerrainGenerator());

    Sided.getTiming().addHandler(this, 250);

    ModManager.postModEvent(new StartingServerEvent());

    state.setup();
  }

  @Override
  public void render() {
    if (shouldReturn()) return;
    super.render();
    for (ClientIdentifier clientIdentifier : getAllClients()) {
      clientIdentifier.getPlayerManager().update();
    }
  }

  @Override
  protected void stop() {
    if (state.hasStopped() || !state.isSetup()) return;
    ModManager.postModEvent(new StoppingServerEvent());
    super.stop();
    if (isDedicated()) Adapter.quit();
  }

  @Override
  public void time(int interval) {
    if (shouldReturn()) return;
    super.time(interval);
    if (interval != 250) return;
    world.setBlock(Blocks.dirt, (int) (Math.random() * 16), (int) (8 + (Math.random() * 7)), (int) (Math.random() * 16));
  }

  public abstract boolean isDedicated();

  public abstract ClientIdentifier getClient(SocketMonitor socketMonitor);

  public abstract ClientIdentifier getClient(String username);

  public abstract void addClient(ClientIdentifier clientIdentifier);

  public abstract void removeClient(SocketMonitor socketMonitor);

  public abstract List<ClientIdentifier> getAllClients();
}
