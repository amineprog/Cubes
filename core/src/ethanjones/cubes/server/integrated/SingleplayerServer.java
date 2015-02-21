package ethanjones.cubes.server.integrated;

import java.util.ArrayList;
import java.util.List;

import ethanjones.cubes.common.networking.server.ClientIdentifier;
import ethanjones.cubes.common.networking.socket.SocketMonitor;

public class SingleplayerServer extends IntegratedServer {

  private SingleplayerClientIdentifier singleplayerClientIdentifier;

  public void create() {
    super.create();
    singleplayerClientIdentifier = new SingleplayerClientIdentifier();
  }

  @Override
  public List<ClientIdentifier> getAllClients() {
    List<ClientIdentifier> list = new ArrayList<ClientIdentifier>(1);
    list.add(singleplayerClientIdentifier);
    return list;
  }

  @Override
  public ClientIdentifier getClient(SocketMonitor socketMonitor) {
    return singleplayerClientIdentifier;
  }

  @Override
  public ClientIdentifier getClient(String username) {
    if (username.equals(singleplayerClientIdentifier.getPlayer().username)) return singleplayerClientIdentifier;
    return null;
  }

  @Override
  public void addClient(ClientIdentifier clientIdentifier) {

  }

  @Override
  public void removeClient(SocketMonitor socketMonitor) {

  }
  
}
