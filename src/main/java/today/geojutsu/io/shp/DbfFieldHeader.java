package today.geojutsu.io.shp;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;


public class DbfFieldHeader
{
  /** field-length in bytes (used for reading from the bytebuffer).*/
  public static final int HEADER_SIZE = 32;

  public final int index;
  public final String name;
  public final Type type;
  public final int displacement;
  public final int length;
  public final byte flag;
  public final int autoIncNext;
  public final byte autoIncStep;

  public DbfFieldHeader(final ByteBuffer _bb, final int _index)
  {
    index = _index;
    byte[] buffer = new byte[11];  //0-11
    _bb.get(buffer);
    String name_raw = new String(buffer, StandardCharsets.ISO_8859_1);
    name = name_raw.substring(0, name_raw.indexOf('\0'));
    type = Type.byID((char) _bb.get());
    displacement = _bb.getInt();
    length = _bb.get() & 0xFF;
    flag = _bb.get();
    autoIncNext = _bb.getInt();
    autoIncStep = _bb.get();
  }

  public String getName()
  {
    return name;
  }

  public Type getType()
  {
    return type;
  }

  public int getLength()
  {
    return length;
  }

  public int getIndex()
  {
    return index;
  }

  public int getDisplacement()
  {
    return displacement;
  }

  public byte getFlag()
  {
    return flag;
  }

  public int getAutoIncNext()
  {
    return autoIncNext;
  }

  public byte getAutoIncStep()
  {
    return autoIncStep;
  }

  public enum Type
  {
    Character('C'),
    Date('D'),
    Numeric('N'),
    Logical('L'),
    Memo('M'),
    Undefined('\0');

    private final char id;

    Type(char _id)
    {
      id = _id;
    }

    public static Type byID(char ID)
    {
      for (Type type : Type.values())
      {
        if (type.id == ID)
        {
          return type;
        }
      }
      return Undefined;
    }

    public char getId()
    {
      return id;
    }
  }
}
