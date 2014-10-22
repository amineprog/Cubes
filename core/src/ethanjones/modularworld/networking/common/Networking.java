package ethanjones.modularworld.networking.common;

import ethanjones.modularworld.networking.common.packet.Packet;
import ethanjones.modularworld.networking.common.packet.PacketBuffer;
import ethanjones.modularworld.networking.common.socket.SocketMonitor;
import ethanjones.modularworld.side.Side;

public abstract class Networking {

  private final Side side;
  private PacketBuffer packetBuffer;
  private volatile NetworkingState networkingState;

  public Networking(Side side) {
    this.side = side;
    this.packetBuffer = new PacketBuffer();
  }

  public NetworkingState getNetworkingState() {
    return networkingState;
  }

  protected void setNetworkingState(NetworkingState networkingState) {
    this.networkingState = networkingState;
  }

  public abstract void start();

  public abstract void tick();

  public abstract void stop();

  /**
   * @param e may be null
   */
  public abstract void disconnected(SocketMonitor socketMonitor, Exception e);

  public final void received(Packet packet) {
    packetBuffer.addPacket(packet, side);
  }

  /**
   * Call on main thread
   */
  public final void processPackets() {
    packetBuffer.process();
  }

  public final Side getSide() {
    return side;
  }
}
