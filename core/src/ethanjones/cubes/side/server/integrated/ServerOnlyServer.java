package ethanjones.cubes.side.server.integrated;

import ethanjones.cubes.networking.server.ClientIdentifier;
import ethanjones.cubes.networking.socket.SocketMonitor;
import ethanjones.cubes.world.save.Save;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ServerOnlyServer extends IntegratedServer {

  private final HashMap<SocketMonitor, ClientIdentifier> clients = new HashMap<SocketMonitor, ClientIdentifier>();
  private final ArrayList<ClientIdentifier> disconnected = new ArrayList<ClientIdentifier>();

  public ServerOnlyServer(Save save) {
    super(save);
  }

  @Override
  public void update() {
    super.update();

    synchronized (clients) {
      Iterator<ClientIdentifier> iterator = disconnected.iterator();
      while (iterator.hasNext()) {
        ClientIdentifier next = iterator.next();
        next.getPlayerManager().disconnected();
        clients.remove(next.getSocketMonitor());
        iterator.remove();
      }
    }
  }

  @Override
  public List<ClientIdentifier> getAllClients() {
    synchronized (clients) {
      List<ClientIdentifier> list = new ArrayList<ClientIdentifier>(clients.size());
      list.addAll(clients.values());
      return list;
    }
  }

  @Override
  public ClientIdentifier getClient(SocketMonitor socketMonitor) {
    synchronized (clients) {
      return clients.get(socketMonitor);
    }
  }

  @Override
  public ClientIdentifier getClient(String username) {
    synchronized (clients) {
      for (ClientIdentifier clientIdentifier : clients.values()) {
        if (clientIdentifier.getPlayer().username.equals(username)) {
          return clientIdentifier;
        }
      }
    }
    return null;
  }

  @Override
  public void addClient(ClientIdentifier clientIdentifier) {
    synchronized (clients) {
      clients.put(clientIdentifier.getSocketMonitor(), clientIdentifier);
    }
  }

  @Override
  public void removeClient(SocketMonitor socketMonitor) {
    synchronized (clients) {
      ClientIdentifier clientIdentifier = clients.get(socketMonitor);
      if (clientIdentifier != null && !disconnected.contains(clientIdentifier)) disconnected.add(clientIdentifier);
    }
  }

}
