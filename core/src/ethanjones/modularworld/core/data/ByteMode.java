package ethanjones.modularworld.core.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class ByteMode {

  public static final byte MODE_BASE = 16;
  public final byte mode;
  public final byte modeByte;

  private ByteMode(byte mode) {
    this.mode = mode;
    modeByte = (byte) (mode * MODE_BASE);
  }

  public static ByteMode read(byte modeNotdivided, DataInput input) throws IOException {
    byte mode = (byte) (modeNotdivided / MODE_BASE);
    switch (mode) {
      case 0:
        return new Normal(input);
      case 1:
        return new Named(input);
      case 2:
        return new Numbered(input);
    }
    return null;
  }

  public static void write(ByteBase bb, DataOutput output) throws IOException {
    output.writeByte(bb.mode.modeByte + bb.getID());
    bb.mode.writeData(output);
  }

  public abstract void writeData(DataOutput output) throws IOException;

  public static final class Normal extends ByteMode {
    public Normal() {
      super((byte) 0);
    }

    protected Normal(DataInput input) {
      this();
    }

    @Override
    public void writeData(DataOutput output) throws IOException {

    }
  }

  public static final class Named extends ByteMode {
    public final String name;

    public Named(String name) {
      super((byte) 1);
      this.name = name;
    }

    protected Named(DataInput input) throws IOException {
      this(input.readUTF());
    }

    @Override
    public void writeData(DataOutput output) throws IOException {
      output.writeUTF(name);
    }

  }

  public static final class Numbered extends ByteMode {
    public final int number;

    public Numbered(int number) {
      super((byte) 2);
      this.number = number;
    }

    protected Numbered(DataInput input) throws IOException {
      this(input.readInt());
    }

    @Override
    public void writeData(DataOutput output) throws IOException {
      output.writeInt(number);
    }

  }
}
