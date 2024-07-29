package today.geojutsu.io.shp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ShapeRecordHeader
{
  public final int recordNumber;
  public final int recordLength;
  public final ShapeType type;

  public ShapeRecordHeader(final int _recordNumber, final int _recordLength, final ShapeType _type)
  {
    recordNumber = _recordNumber;
    recordLength = _recordLength;
    type = _type;
  }

  static ShapeRecordHeader read(final ByteBuffer _bb)
  {
    _bb.order(ByteOrder.BIG_ENDIAN);
    int rnr = _bb.getInt();
    int rl = _bb.getInt();
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    ShapeType type = ShapeType.read(_bb);
    return  new ShapeRecordHeader(rnr, rl, type);
  }
}
