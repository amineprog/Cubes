package ethanjones.cubes.side.server;

import ethanjones.cubes.block.Blocks;
import ethanjones.cubes.core.event.EventHandler;
import ethanjones.cubes.core.event.entity.living.player.PlayerBreakBlockEvent;
import ethanjones.cubes.core.event.entity.living.player.PlayerPlaceBlockEvent;
import ethanjones.cubes.core.gwt.FakeAtomic.AtomicLong;
import ethanjones.cubes.core.gwt.Task;
import ethanjones.cubes.core.logging.Log;
import ethanjones.cubes.core.platform.Adapter;
import ethanjones.cubes.core.platform.Compatibility;
import ethanjones.cubes.core.timing.TimeHandler;
import ethanjones.cubes.core.util.BlockFace;
import ethanjones.cubes.core.util.VectorUtil;
import ethanjones.cubes.networking.server.ClientIdentifier;
import ethanjones.cubes.networking.socket.SocketMonitor;
import ethanjones.cubes.side.common.Cubes;
import ethanjones.cubes.side.common.Side;
import ethanjones.cubes.side.server.command.CommandManager;
import ethanjones.cubes.world.save.Save;
import ethanjones.cubes.world.server.WorldServer;
import ethanjones.cubes.world.storage.WorldStorage;
import ethanjones.cubes.world.storage.WorldStorage.ChangedBlockBatch;

import com.badlogic.gdx.math.Vector3;

import java.util.ArrayDeque;
import java.util.List;

public abstract class CubesServer extends Cubes implements TimeHandler {

  private static final int SAVE_TIME = 60000;
  private static final AtomicLong lastUpdateTime = new AtomicLong();
  private ArrayDeque<ChangedBlockBatch> changedBlocksBatches = new ArrayDeque<ChangedBlockBatch>();
  private final Save save;
  private  long nextTickTime = System.currentTimeMillis() + tickMS;
  private  int behindTicks = 0;

  public CubesServer(Save save) {
    super(Side.Server);
    this.save = save;
  }

  @Override
  public void create() {
    if (state.isSetup()) return;
    super.create();
    save.readIDManager();
    CommandManager.reset();

    world = new WorldServer(save);

    Side.getTiming().addHandler(this, SAVE_TIME);

    lastUpdateTime.set(System.currentTimeMillis());
    state.setup();
  }
  
  public void loop() {
    while (state.isRunning()) {
      long diff = nextTickTime - System.currentTimeMillis();
      if (diff < 0) {
        behindTicks += 1 + (-diff / tickMS);
      }
      if (behindTicks == 0) {
        throw new Task.TimelimitException();
      } else if (behindTicks >= (1000 / tickMS)) {
        Log.warning("Skipping " + behindTicks + " ticks");
        nextTickTime += behindTicks * tickMS;
        behindTicks = 0;
      } else {
        behindTicks--;
      }
      nextTickTime += tickMS;
      tick();
      update();
    }
  }
  
  @Override
  protected void tick() {
    super.tick();
    for (ClientIdentifier clientIdentifier : getAllClients()) {
      clientIdentifier.getPlayerManager().update();
    }
  }
  
  @Override
  protected void update() {
    lastUpdateTime.set(System.currentTimeMillis());
    super.update();
    Compatibility.get().update();
    while (!changedBlocksBatches.isEmpty()) {
      WorldStorage.processChangedBlocks(changedBlocksBatches.pop());
    }
  }
  
  @Override
  protected void stop() {
    if (state.hasStopped() || !state.isSetup()) return;
    super.stop();
    if (isDedicated()) Adapter.quit();
  }

  @Override
  public void time(int interval) {
    if (shouldReturn()) return;
    if (interval == SAVE_TIME) world.save();
  }

  @EventHandler
  public void placeMeta(PlayerPlaceBlockEvent event) {
    if (event.getBlock() == Blocks.log) {
      switch (event.getBlockIntersection().getBlockFace()) {
        case posY:
        case negY:
          event.setMeta(0);
          break;
        case posX:
        case negX:
          event.setMeta(1);
          break;
        case posZ:
        case negZ:
          event.setMeta(2);
          break;
      }
    } else if (event.getBlock() == Blocks.chest) {
      Vector3 pos = event.getPlayer().position.cpy();
      pos.sub(event.getBlockReference().asVector3());
      pos.nor();
      BlockFace blockFace = VectorUtil.directionXZ(pos);
      if (blockFace == null || blockFace == BlockFace.posX) {
        event.setMeta(0);
      } else if (blockFace == BlockFace.negX) {
        event.setMeta(1);
      } else if (blockFace == BlockFace.posZ) {
        event.setMeta(2);
      } else if (blockFace == BlockFace.negZ) {
        event.setMeta(3);
      }
    }
  }

  @EventHandler
  public void breakMeta(PlayerBreakBlockEvent event) {
    if (!(event.getBlock() == Blocks.log || event.getBlock() == Blocks.chest)) return;
    event.setMeta(0);
  }

  public abstract boolean isDedicated();

  public abstract List<ClientIdentifier> getAllClients();

  public abstract ClientIdentifier getClient(SocketMonitor socketMonitor);

  public abstract ClientIdentifier getClient(String username);

  public abstract void addClient(ClientIdentifier clientIdentifier);

  public abstract void removeClient(SocketMonitor socketMonitor);
  
  public static long lastUpdateTime() {
    return lastUpdateTime.get();
  }
  
  public void addChangedBlocksBatch(ChangedBlockBatch changedBlockBatch) {
    changedBlocksBatches.add(changedBlockBatch);
  }
}
