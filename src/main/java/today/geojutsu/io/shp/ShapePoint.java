package today.geojutsu.io.shp;

import java.nio.ByteBuffer;

public class ShapePoint
{
  final double [] coordinates;

  private ShapePoint(final double _x, final double _y)
  {
    coordinates = new double[] {_x,_y};
  }

  private ShapePoint(final double _x, final double _y, final double _z)
  {
    coordinates = new double[] {_x,_y, _z};
  }

  private ShapePoint(final double _x, final double _y, final double _z, final double _m)
  {
    coordinates = new double[] {_x,_y,_z,_m};
  }

  static ShapePoint read2d(final ShapeType _type, final ByteBuffer _bb)
  {
    if(_type.hasZ())
    {
      return new ShapePoint(_bb.getDouble(),_bb.getDouble(),Double.NaN);
    }
    else if (_type.hasM())
    {
      return new ShapePoint(_bb.getDouble(),_bb.getDouble(),Double.NaN,Double.NaN);
    }
    return new ShapePoint(_bb.getDouble(),_bb.getDouble());
  }

  static ShapePoint read(final ShapeType _type, final ByteBuffer _bb)
  {
    if(_type.hasZ())
    {
      return new ShapePoint(_bb.getDouble(),_bb.getDouble(),_bb.getDouble());
    }
    else if (_type.hasM())
    {
      return new ShapePoint(_bb.getDouble(),_bb.getDouble(),_bb.getDouble(),_bb.getDouble());
    }
    return new ShapePoint(_bb.getDouble(),_bb.getDouble());
  }

  void addZ(final ByteBuffer _bb)
  {
    coordinates[2] = _bb.getDouble();
  }

  void addM(final ByteBuffer _bb)
  {
    coordinates[3] = _bb.getDouble();
  }

  public double getX()
  {
    return coordinates[0];
  }

  public double getY()
  {
    return coordinates[1];
  }

  public double getZ()
  {
    return coordinates.length > 3 ? Double.NaN:coordinates[2];
  }

  public double getM()
  {
    return coordinates.length > 4 ? Double.NaN:coordinates[3];
  }

}
