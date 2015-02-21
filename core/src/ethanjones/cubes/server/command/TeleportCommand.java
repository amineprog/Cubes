package ethanjones.cubes.server.command;

import com.badlogic.gdx.math.Vector3;
import java.util.List;

import ethanjones.cubes.common.localization.Localization;
import ethanjones.cubes.common.entity.living.player.Player;
import ethanjones.cubes.common.networking.NetworkingManager;
import ethanjones.cubes.common.networking.packets.PacketPlayerInfo;
import ethanjones.cubes.common.networking.server.ClientIdentifier;

public class TeleportCommand {

  public static void init() {
    CommandBuilder tp = new CommandBuilder("tp").register();
    tp.add(CommandValue.coordinate).add(CommandValue.coordinate).add(CommandValue.coordinate).setCommandListener(new CommandListener() {
      @Override
      public void onCommand(CommandBuilder builder, List<CommandArgument> arguments, CommandSender sender) {
        if (sender instanceof Player) {
          float x = (Float) arguments.get(1).get();
          float y = (Float) arguments.get(2).get();
          float z = (Float) arguments.get(3).get();
          PacketPlayerInfo packetPlayerInfo = new PacketPlayerInfo();
          packetPlayerInfo.angle = ((Player) sender).angle;
          packetPlayerInfo.position = new Vector3().set(x, y, z);
          NetworkingManager.sendPacketToClient(packetPlayerInfo, ((Player) sender).clientIdentifier);
        } else {
          sender.print(Localization.get("command.common.onlyPlayer"));
        }
      }
    });
    tp.add(CommandValue.clientIdentifier).setCommandListener(new CommandListener() {
      @Override
      public void onCommand(CommandBuilder builder, List<CommandArgument> arguments, CommandSender sender) {
        if (sender instanceof Player) {
          PacketPlayerInfo packetPlayerInfo = new PacketPlayerInfo();
          packetPlayerInfo.angle = ((Player) sender).angle;
          packetPlayerInfo.position = new Vector3().set(((ClientIdentifier) arguments.get(1).get()).getPlayer().position);
          NetworkingManager.sendPacketToClient(packetPlayerInfo, ((Player) sender).clientIdentifier);
        } else {
          sender.print(Localization.get("command.common.onlyPlayer"));
        }
      }
    });
  }
  
}
