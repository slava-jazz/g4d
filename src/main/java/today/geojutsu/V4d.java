package today.geojutsu;

/**
 * basic vertex with parametric offset
 * any instance is not mutable since it can be shared between many geometrical objects
 */
import java.io.Serializable;

/**
 * basic vertex with parametric offset
 * any instance is not mutable since it can be shared between many geometrical objects
 */
public class V4d implements Comparable<V4d>
{

  // all properties are public due best performance
  public final double xLon;
  public final double yLat;
  public final double zAlt;
  public final double o; // parametric offset along specific shape

  public V4d(final double _xLon, final double _yLat, final double _zAlt, final double _o)
  {
    xLon = _xLon;
    yLat = _yLat;
    zAlt = _zAlt;
    o = _o;
  }

  public V4d(final double _xLon, final double _yLat, final double _zAlt)
  {
    this(_xLon, _yLat, _zAlt, Double.NaN);
  }

  public V4d(final double _xLon, final double _yLat)
  {
    this(_xLon, _yLat, Double.NaN, Double.NaN);
  }

  public V4d(final V4d _v, final double _o)
  {
    xLon = _v.xLon;
    yLat = _v.yLat;
    zAlt = _v.zAlt;
    o = _o;
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    return sb.append('(').append(xLon).append(',').append(yLat).append(',').append(zAlt).append(':').append(o).append(')').toString();
  }

  @Override
  public int compareTo(final V4d _o)
  {
    int res = Double.compare(xLon, _o.xLon);
    if (res == 0)
    {
      if ((res = Double.compare(yLat, _o.yLat)) == 0)
      {
        res = Double.compare(zAlt, _o.zAlt);
      }
    }
    return res;
  }

  public static boolean isEqual2d(final V4d _a, final V4d _b, final double _tolerance)
  {
    return Math.abs(_a.xLon - _b.xLon) <= _tolerance && Math.abs(_a.yLat - _b.yLat) <= _tolerance;
  }
}
