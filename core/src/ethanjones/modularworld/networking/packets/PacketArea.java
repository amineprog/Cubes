package ethanjones.modularworld.networking.packets;

import ethanjones.modularworld.core.data.Data;
import ethanjones.modularworld.core.data.DataGroup;
import ethanjones.modularworld.networking.common.packet.Packet;
import ethanjones.modularworld.side.client.ModularWorldClient;
import ethanjones.modularworld.world.reference.AreaReference;
import ethanjones.modularworld.world.storage.Area;

public class PacketArea extends Packet {

  public static AreaReference areaReference = new AreaReference();

  public int areaX;
  public int areaY;
  public int areaZ;
  public DataGroup area;

  @Override
  public void handlePacket() {
    areaReference.setFromArea(areaX, areaY, areaZ);
    Area a = new Area(areaX, areaY, areaZ);
    a.read(area);
    ModularWorldClient.instance.world.setAreaInternal(areaReference, a);
  }

  @Override
  public Data write() {
    DataGroup dataGroup = new DataGroup();
    dataGroup.setInteger("areaX", areaX);
    dataGroup.setInteger("areaY", areaY);
    dataGroup.setInteger("areaZ", areaZ);
    dataGroup.setGroup("area", area);
    return dataGroup;
  }

  @Override
  public void read(Data data) {
    DataGroup dataGroup = (DataGroup) data;
    areaX = dataGroup.getInteger("areaX");
    areaY = dataGroup.getInteger("areaY");
    areaZ = dataGroup.getInteger("areaZ");
    area = dataGroup.getGroup("area");
  }
}
