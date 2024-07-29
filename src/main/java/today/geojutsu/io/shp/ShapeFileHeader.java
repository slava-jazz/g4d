package today.geojutsu.io.shp;

import today.geojutsu.AABB;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ShapeFileHeader
{
  private final AABB aabb;
  private final double [] zRange;
  private final double [] mRange;
  private final ShapeType type;
  private DbfFieldHeader [] semanticHeader;

  private ShapeFileHeader(final AABB _aabb, final double[] _zRange, final double[] _mRange, final ShapeType _type)
  {
    aabb = _aabb;
    zRange = _zRange;
    mRange = _mRange;
    type = _type;
  }

  public static ShapeFileHeader read(final ByteBuffer _bb)
  {
    ShapeFileHeader h = null;
    _bb.order(ByteOrder.BIG_ENDIAN);
    if (_bb.getInt(0) == MAGIC_NUMBER)
    {
      int file_len = _bb.getInt(24);
      _bb.order(ByteOrder.LITTLE_ENDIAN);
      if (_bb.getInt(28) == THE_VERSION)
      {
        ShapeType type = ShapeType.fromInt(_bb.getInt(32));
        AABB aabb = new AABB(_bb.getDouble(36),_bb.getDouble(44),_bb.getDouble(52),_bb.getDouble(60));
        double [] z_range = new double[] {_bb.getDouble(68),_bb.getDouble(76)};
        double [] m_range = new double[] {_bb.getDouble(84),_bb.getDouble(92)};
        h = new ShapeFileHeader(aabb,z_range,m_range,type);
        _bb.position(100);
      }
    }
    return h;
  }

  public AABB getAabb()
  {
    return aabb;
  }

  public double[] getZRange()
  {
    return zRange;
  }

  public double[] getMRange()
  {
    return mRange;
  }

  public ShapeType getType()
  {
    return type;
  }

  public DbfFieldHeader[] getSemanticHeader()
  {
    return semanticHeader;
  }

  public ShapeFileHeader setSemanticHeader(final DbfFieldHeader[] _semanticHeader)
  {
    semanticHeader = _semanticHeader;
    return this;
  }

  private final static int MAGIC_NUMBER = 9994;
  private final static int THE_VERSION = 1000;

}
