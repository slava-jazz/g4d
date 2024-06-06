package today.geojutsu;

import today.geojutsu.match.MatchCoreConfig;

public class Ecl2GeoTransformer
{
  public final double latFactor;
  public final double distanceFactor;


  public static Ecl2GeoTransformer compute(final V4d _scope_center)
  {
    double[] xy = MatchCoreConfig.calcXLonYLatToleranceFromDistanceInMeters(_scope_center.xLon, _scope_center.yLat, 1);
    return new Ecl2GeoTransformer(xy[0] / xy[1], 1. / xy[0]);
  }

  private Ecl2GeoTransformer(final double _latFactor, final double _distanceFactor)
  {
    latFactor = _latFactor;
    distanceFactor = _distanceFactor;
  }

  public V4d toEuclid(final V4d _v_wgs)
  {
    return new V4d(_v_wgs.xLon, _v_wgs.yLat * latFactor);
  }

  public V4d toWgs(final V4d _v_ecl)
  {
    return new V4d(_v_ecl.xLon, _v_ecl.yLat / latFactor);
  }

  public <T> G4d<T> toEuclid(final G4d<T> _g_wgs)
  {
    V4d[] shape = _g_wgs.getShape();
    V4d[] new_shape = new V4d[shape.length];
    for (int index = 0; index < shape.length; index++)
    {
      new_shape[index] = new V4d(shape[index].xLon, shape[index].yLat * latFactor, shape[index].zAlt, shape[index].o);
    }
    return G4d.build(_g_wgs.getLength(), new_shape, _g_wgs.getCustomData());
  }

  public <T> G4d<T> toWgs(final G4d<T> _g_ecl)
  {
    V4d[] shape = _g_ecl.getShape();
    V4d[] new_shape = new V4d[shape.length];
    for (int index = 0; index < shape.length; index++)
    {
      new_shape[index] = new V4d(shape[index].xLon, shape[index].yLat / latFactor, shape[index].zAlt, shape[index].o);
    }
    return G4d.build(_g_ecl.getLength(), new_shape, _g_ecl.getCustomData());
  }


  public V4d calcTranslation(final V4d _v, final double _dx_meters, final double _dy_meters, final double _o)
  {
    return new V4d
      (
        _v.xLon + _dx_meters / distanceFactor,
        _v.yLat + _dy_meters / distanceFactor,
        0,
        _o
      );
  }
}
