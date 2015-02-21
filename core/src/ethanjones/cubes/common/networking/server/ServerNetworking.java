package ethanjones.cubes.common.networking.server;

import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.Array;

import ethanjones.cubes.common.core.logging.Log;
import ethanjones.cubes.common.networking.Networking;
import ethanjones.cubes.common.networking.packet.Packet;
import ethanjones.cubes.common.networking.packet.PacketQueue;
import ethanjones.cubes.common.networking.socket.SocketMonitor;
import ethanjones.cubes.common.Side;
import ethanjones.cubes.Cubes;

public class ServerNetworking extends Networking {

  private final ServerNetworkingParameter serverNetworkingParameter;
  private Array<SocketMonitor> sockets;
  private ServerSocketMonitor serverSocketMonitor;

  public ServerNetworking(ServerNetworkingParameter serverNetworkingParameter) {
    super();
    this.serverNetworkingParameter = serverNetworkingParameter;
    sockets = new Array<SocketMonitor>();
  }

  public synchronized void preInit() throws Exception {
    setNetworkingState(NetworkingState.Starting);
    serverSocketMonitor = new ServerSocketMonitor(serverNetworkingParameter.port, this);
  }

  @Override
  public void init() {
    Log.info("Starting Server Networking");
    serverSocketMonitor.start();
    setNetworkingState(NetworkingState.Running);
  }

  @Override
  public synchronized void update() {

  }

  @Override
  public synchronized void stop() {
    if (getNetworkingState() != NetworkingState.Running) return;
    setNetworkingState(NetworkingState.Stopping);
    Log.info("Stopping Server Networking");
    serverSocketMonitor.dispose();
    for (int i = 0; i < sockets.size; i++) {
      sockets.pop().dispose();
    }
  }

  @Override
  public void sendPacketToClient(Packet packet, ClientIdentifier clientIdentifier) throws UnsupportedOperationException {
    if (getNetworkingState() != NetworkingState.Running) {
      Log.warning("Cannot send " + packet.toString() + " as " + getNetworkingState().name());
      return;
    }
    clientIdentifier.getSocketMonitor().getSocketOutput().getPacketQueue().add(packet);
  }

  @Override
  public synchronized void disconnected(SocketMonitor socketMonitor, Exception e) {
    if (getNetworkingState() == NetworkingState.Stopping) return;
    Log.info("Disconnected from " + socketMonitor.getSocket().getRemoteAddress(), e);
    Cubes.getServer().removeClient(socketMonitor);
  }

  public void processPackets() {
    for (SocketMonitor socketMonitor : sockets) {
      Packet packet = null;
      PacketQueue packetQueue = socketMonitor.getSocketInput().getPacketQueue();
      while ((packet = packetQueue.get()) != null) {
        packet.handlePacket();
      }
    }
  }

  protected synchronized void accepted(Socket socket) {
    sockets.add(new SocketMonitor(socket, this, Side.Server));
    Log.info("Successfully connected to " + socket.getRemoteAddress());
  }
}
