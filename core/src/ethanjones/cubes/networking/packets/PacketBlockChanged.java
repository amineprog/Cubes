package ethanjones.cubes.networking.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import ethanjones.cubes.networking.packet.Packet;
import ethanjones.cubes.side.client.CubesClient;
import ethanjones.cubes.side.common.Cubes;
import ethanjones.cubes.world.reference.AreaReference;

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
    Cubes.getClient().world.getArea(new AreaReference().setFromBlockCoordinates(x, y, z)).handleChange(this);
  }
}
