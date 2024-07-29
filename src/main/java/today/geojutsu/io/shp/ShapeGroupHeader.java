package today.geojutsu.io.shp;

import today.geojutsu.AABB;

import java.nio.ByteBuffer;

public class ShapeGroupHeader
{
  protected final AABB aabb;
  protected final double [] zRange;
  protected final double [] mRange;
  protected final int membersQty;

  private ShapeGroupHeader(final AABB _aabb, final double[] _zRange, final double[] _mRange, final int _membersQty)
  {
    aabb = _aabb;
    zRange = _zRange;
    mRange = _mRange;
    membersQty = _membersQty;
  }

  public static ShapeGroupHeader read(final ByteBuffer _bb)
  {
    AABB aabb = new AABB(_bb.getDouble(),_bb.getDouble(),_bb.getDouble(),_bb.getDouble());
    int size = _bb.getInt();
    double [] z_range = new double[] {Double.NaN,Double.NaN};
    double [] m_range = new double[] {Double.NaN,Double.NaN};
    return new ShapeGroupHeader(aabb,z_range,m_range,size);
  }

  void updateZRange(final ByteBuffer _bb)
  {
    zRange[0] = _bb.getDouble();
    zRange[1] = _bb.getDouble();
  }
  void updateMRange(final ByteBuffer _bb)
  {
    mRange[0] = _bb.getDouble();
    mRange[1] = _bb.getDouble();
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

  public int getMembersQty()
  {
    return membersQty;
  }
}
