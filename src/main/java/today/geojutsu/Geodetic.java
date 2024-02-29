package today.geojutsu;

import static java.lang.Math.*;

public class Geodetic
{

  public interface WGSCalculator
  {
    double calcDistanceInMeters(final double _lat1, final double _lon1, final double _lat2, final double _lon2);
  }

  public void setWGSCalculation(final WGSCalculator _the_calc)
  {
    __WGS_CALCULATOR = _the_calc;
  }

  private final static WGSCalculator __DEFAULT_WGS_CALCULATOR = new WGSCalculator()
  {
    @Override
    public double calcDistanceInMeters(final double _lat1, final double _lon1, final double _lat2, final double _lon2)
    {
      return distanceByBowring(_lat1, _lon1, _lat2, _lon2);
    }
  };
  private static WGSCalculator __WGS_CALCULATOR = __DEFAULT_WGS_CALCULATOR;

  public static double calcDistanceInMeters(final double _lat1, final double _lon1, final double _lat2, final double _lon2)
  {
    return __WGS_CALCULATOR.calcDistanceInMeters(_lat1, _lon1, _lat2, _lon2);
  }

  public static double calcDistanceInMeters(final V4d _v1, final V4d _v2)
  {
    return calcDistanceInMeters(_v1.yLat, _v1.xLon, _v2.yLat, _v2.xLon);
  }

  public static double calcDistanceInMeters(final G4d<?> _g)
  {
    double l = 0;
    for (int i = 1; i < _g.shape.length; i++)
    {
      l += calcDistanceInMeters(_g.shape[i - 1], _g.shape[i]);
    }
    return l;
  }


  public static double calcHeading(final V4d _from, final V4d _to)
  {
    final double lat1 = Math.toRadians(_from.yLat);
    final double lon1 = Math.toRadians(_from.xLon);
    final double lat2 = Math.toRadians(_to.yLat);
    final double lon2 = Math.toRadians(_to.xLon);

    double h = Math.atan2
        (
            Math.cos(lat2) * Math.sin(lon2 - lon1),
            Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)
        );
    if (h < 0)
    {
      h += Euclid._2PI;
    }
    return Math.toDegrees(h);
  }

  public static double calcSlope(final V4d _v1, final V4d _v2)
  {
    double run = calcDistanceInMeters(_v1, _v2);
    double rise = _v2.zAlt - _v1.zAlt;
    double angle = atan2(rise, run);
    return Math.toDegrees(angle);
  }

  public static double calcCurvature(final V4d _p1, final V4d _p2, final V4d _p3)
  {
    double dx1 = calcDistanceInMeters(_p2.yLat, _p2.xLon, _p2.yLat, _p1.xLon) * signum(_p2.xLon - _p1.xLon);
    double dy1 = calcDistanceInMeters(_p2.yLat, _p2.xLon, _p1.yLat, _p2.xLon) * signum(_p2.yLat - _p1.yLat);
    double dx2 = calcDistanceInMeters(_p3.yLat, _p3.xLon, _p3.yLat, _p1.xLon) * signum(_p3.xLon - _p1.xLon);
    double dy2 = calcDistanceInMeters(_p3.yLat, _p3.xLon, _p1.yLat, _p3.xLon) * signum(_p3.yLat - _p1.yLat);
    double area = dx1 * dy2 - dy1 * dx2;
    double len0 = calcDistanceInMeters(_p1, _p2);
    double len1 = calcDistanceInMeters(_p2, _p3);
    double len2 = calcDistanceInMeters(_p3, _p1);
    return 4 * area / (len0 * len1 * len2);
  }

  private static final double WGS84_SEMI_MINOR_AXIS = 6356752.3142;
  private static final double WGS84_SEMI_MAJOR_AXIS = 6378137.0;
  private static final double SECOND_ECCENTRICITY_SQUARED =
      (WGS84_SEMI_MAJOR_AXIS * WGS84_SEMI_MAJOR_AXIS) / (WGS84_SEMI_MINOR_AXIS * WGS84_SEMI_MINOR_AXIS) - 1;

  /**
   * The ellipsoidal distance between two points using Bowring (1981) formulas.
   * The accuracy is 1-2mm for 120km lines.
   * The accuracy is 3-4mm for 150km lines.
   * @param lat1 latitude of the first point
   * @param lon1 longitude of the first point
   * @param lat2 latitude of the second point
   * @param lon2 longitude of the second point
   * @return the distance in meters
   */
  public static double distanceByBowring(double lat1, double lon1, double lat2, double lon2)
  {
    double fi1 = toRadians(lat1);
    double fi2 = toRadians(lat2);
    double lambda1 = toRadians(lon1);
    double lambda2 = toRadians(lon2);

    double cosF1 = cos(fi1);

    // Common Equations
    double A = sqrt(1 + SECOND_ECCENTRICITY_SQUARED * Math.pow(cosF1, 4));
    double B = sqrt(1 + SECOND_ECCENTRICITY_SQUARED * Math.pow(cosF1, 2));
    double C = sqrt(1 + SECOND_ECCENTRICITY_SQUARED);
    double deltaFi = fi2 - fi1;
    double deltaLambda = lambda2 - lambda1;
    double w = A * deltaLambda / 2.0;

    // Inverse Problem Equations
    double t1 = 3 * SECOND_ECCENTRICITY_SQUARED * deltaFi * sin(2 * fi1 + 2. / 3. * deltaFi) / (4. * B * B);
    double D = deltaFi / (2. * B) * (1. + t1);
    double sinD = sin(D);
    double E = sinD * cos(w);
    double t2 = B * cosF1 * cos(D) - sin(fi1) * sinD;
    double F = 1. / A * sin(w) * t2;
    double sigma = 2 * asin(sqrt(E * E + F * F));
    return WGS84_SEMI_MAJOR_AXIS * C * sigma / (B * B);
  }
}
