package ethanjones.cubes.networking.packet;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

public class PacketQueue {

  private PriorityBlockingQueue<Packet> queue;

  public PacketQueue() {
    queue = new PriorityBlockingQueue<Packet>(16, PacketComparator.instance);
  }

  public void add(Packet packet) {
    if (packet == null || !packet.shouldSend()) return;
    queue.add(packet);
  }

  public Packet get() {
    return queue.poll();
  }

  public Packet waitAndGet() {
    try {
      return queue.poll(1, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      return null;
    }
  }
}
