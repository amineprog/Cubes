package ethanjones.cubes.common.networking.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import ethanjones.cubes.common.networking.packet.Packet;
import ethanjones.cubes.common.Sided;
import ethanjones.cubes.Cubes;

public class PacketBlockChanged extends Packet {

  public int x;
  public int y;
  public int z;
  public int block;

  @Override
  public void write(DataOutputStream dataOutputStream) throws Exception {
    dataOutputStream.writeInt(x);
    dataOutputStream.writeInt(y);
    dataOutputStream.writeInt(z);
    dataOutputStream.writeInt(block);
  }

  @Override
  public void read(DataInputStream dataInputStream) throws Exception {
    x = dataInputStream.readInt();
    y = dataInputStream.readInt();
    z = dataInputStream.readInt();
    block = dataInputStream.readInt();
  }

  @Override
  public void handlePacket() {
    Cubes.getClient().world.setBlock(Sided.getBlockManager().toBlock(block), x, y, z);
  }
}
