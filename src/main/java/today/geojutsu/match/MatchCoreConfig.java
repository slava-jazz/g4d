package today.geojutsu.match;

import today.geojutsu.Geodetic;
import today.geojutsu.G4d;

/**
 * basic constants
 */
public class MatchCoreConfig
{
  public static MatchCoreConfig STD = new MatchCoreConfig(
      G4d.__SEGMENT_TANGENT_BUILD_TOLERANCE * 1.8,
      0.08,
      0.000006,
      0.000006,
      0.2,
      0.01,
      3);

  public final double tangentTolerance;
  public final double headingTolerance;
  public final double xLonTolerance;
  public final double yLatTolerance;
  public final double distanceSquareTolerance;
  public final double rangeToleranceInMeters;
  public final double x3;
  public final double y3;
  public final double minimalLinkLengthToBeShared;
  public final int maxChildren;

  public MatchCoreConfig(final double _tangentTolerance, final double _headingTolerance, final double _xLonTolerance, final double _yLatTolerance,
      final double _minimalLinkLengthToBeShared, final double _rangeToleranceInMeters, final int _maxChildren)
  {
    tangentTolerance = _tangentTolerance;
    headingTolerance = _headingTolerance;
    xLonTolerance = _xLonTolerance;
    yLatTolerance = _yLatTolerance;
    distanceSquareTolerance = 0.5 * (xLonTolerance * xLonTolerance + yLatTolerance * yLatTolerance);
    x3 = xLonTolerance * 6;
    y3 = yLatTolerance * 6;
    rangeToleranceInMeters = _rangeToleranceInMeters;
    minimalLinkLengthToBeShared = _minimalLinkLengthToBeShared;
    maxChildren = _maxChildren;
  }

  /**
   * calculate tolerance in gard for specific scope
   * @param _x_lon_scope_center x/lon coordinate of the scope's center
   * @param _y_lat_scope_center y/lat coordinate of the scope's center
   * @param _tolerance_meter  tolerance in meters to convert in grad
   * @return array [x_tolerance, y_tolerance]
   */
  public static double[]  calcXLonYLatToleranceFromDistanceInMeters(
      final double _x_lon_scope_center,
      final double _y_lat_scope_center,
      final double _tolerance_meter)
  {
    double dx = Geodetic.calcDistanceInMeters(_y_lat_scope_center,_x_lon_scope_center,_y_lat_scope_center ,_x_lon_scope_center + D_GARD);
    double dy = Geodetic.calcDistanceInMeters(_y_lat_scope_center,_x_lon_scope_center,_y_lat_scope_center + D_GARD,_x_lon_scope_center);
    return new double[] {_tolerance_meter * D_GARD/dx, _tolerance_meter * D_GARD/dy};
  }

  private static final double D_GARD = 0.001;
}
