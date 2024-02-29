package today.geojutsu;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * common geometrical algorithms in euclidean space
 */
public class Euclid
{
  // one more time,  all about Pi
  public static final double PI_2 = Math.PI / 2.;
  public static final double PI_34 = PI_2 + PI_2 / 2.;
  public static final double _2PI = Math.PI + Math.PI;

  /**
   * check, with tolerance provided, if two double values the same
   * @param _v1 first value
   * @param _v2 second value
   * @param _tolerance tolerance
   * @return true if the same with tolerance
   */
  public static boolean theSame(final double _v1, double _v2, double _tolerance)
  {
    return Math.abs(_v2 - _v1) <= _tolerance;
  }

  /**
   * linear interpolate vertex between two neighbors
   * @param _v1 first neighbor (vertex before)
   * @param _v2 second neighbor (vertex after)
   * @param _offset paramedical offset, must be inside edge's range
   * @param _offset_tolerance tolerance to choose existing shape point
   * @return the V4d on the edge or null if passed offset is out of edge's range
   * */
  public static V4d lerpVertex(final V4d _v1, final V4d _v2, final double _offset, final double _offset_tolerance)
  {
    V4d res = theSame(_v1.o, _offset, _offset_tolerance) ? _v1 :
        theSame(_v2.o, _offset, _offset_tolerance) ? _v2 : null;
    if (res == null && _offset >= _v1.o && _offset <= _v2.o)
    {
      double factor = (_offset - _v1.o) / (_v2.o - _v1.o);
      double dx = _v2.xLon - _v1.xLon;
      double dy = _v2.yLat - _v1.yLat;
      double dz = _v2.zAlt - _v1.zAlt;
      res = new V4d(
          _v1.xLon + dx * factor,
          _v1.yLat + dy * factor,
          _v1.zAlt + dz * factor,
          _offset
      );
    }
    return res;
  }

  public static double lerpValue(final double _val1, final V4d _v1, final double _val2, final V4d _v2, final double _o0)
  {
    return lerpValue(_val1, _v1.o, _val2, _v2.o, _o0);
  }

  public static double lerpValue(final double _val1, final double _o1, final double _val2, final double _o2, final double _o0)
  {
    double factor = (_o0 - _o1) / (_o2 - _o1);
    double dv = _val2 - _val1;
    return _val1 + dv * factor;
  }

  /**
   * linear interpolate vertex between two neighbors
   * @param _v1 first neighbor (vertex before)
   * @param _v2 second neighbor (vertex after)
   * @param _offset paramedical offset, must be inside edge's range
   * @return the V4d on the edge or null if passed offset is out of edge's range
   * */
  public static V4d lerpVertex(final V4d _v1, final V4d _v2, final double _offset)
  {
    return lerpVertex(_v1, _v2, _offset, 0);
  }

  public static double calcOffset(final G4d<?>.Edge _e, final V4d _v0)
  {
    return calcOffset(_e.getFirstVertex(), _e.getLastVertex(), _v0);
  }

  public static double calcOffset(final V4d _v1, final V4d _v2, final V4d _v0)
  {
    return calcOffset(_v1, _v2, _v0.xLon, _v0.yLat);
  }

  public static double calcOffset(final V4d _v1, final V4d _v2, final double _x0, final double _y0)
  {
    final double Dx = _v2.xLon - _v1.xLon;
    final double Dy = _v2.yLat - _v1.yLat;
    final double factor = (Math.abs(Dx) > Math.abs(Dy)) ? (_x0 - _v1.xLon) / Dx : (_y0 - _v1.yLat) / Dy;
    return Tools.clamp(_v1.o + (_v2.o - _v1.o) * factor, _v1.o, _v2.o); // clamp to avoid out of range errors because of double precision calculation
  }

  public static double calcOffset(final V4d _v1, final V4d _v2, final double[] _v0)
  {
    return calcOffset(_v1, _v2, _v0[0], _v0[1]);
  }

  /**
   * calculate euclidean heading angle of the passed edge in range [0, 2Pi]
   * @param _e edge to calculate
   * @return euclidean heading
   */
  public static double calcHeading(final G4d<?>.Edge _e)
  {
    V4d v1 = _e.getFirstVertex();
    V4d v2 = _e.getLastVertex();
    return Math.atan2(v2.yLat - v1.yLat, v2.xLon - v1.xLon);
  }

  /**
   *  2d subtract second passed vector from the first one
   * @param _a original vector
   * @param _b vector to subtract
   * @return subtract result
   */
  public static V4d sub2d(final V4d _a, final V4d _b)
  {
    return new V4d(_a.xLon - _b.xLon, _a.yLat - _b.yLat);
  }

  /**
   * 2d vector summa
   * @param _a first vector
   * @param _b second vector
   * @return vector summa
   */
  public static V4d sum2d(final V4d _a, final V4d _b)
  {
    return new V4d(_a.xLon + _b.xLon, _a.yLat + _b.yLat);
  }

  /**
   * 2d vector scale
   * @param _v vector to scale
   * @param _f scale factor
   * @return scaled vector
   */
  public static V4d mul2d(final V4d _v, final double _f)
  {
    return new V4d(_v.xLon * _f, _v.yLat * _f);
  }

  /**
   * 2d dot product
   * @param _a first vector
   * @param _b second vector
   * @return dot product
   */
  public static double dot2d(final V4d _a, final V4d _b)
  {
    return _a.xLon * _b.xLon + _a.yLat * _b.yLat;
  }

  /**
   * 2d cross product
   * @param _v1 first vector
   * @param _v2 second vector
   * @return cross product
   */
  public static double cross2d(final V4d _v1, final V4d _v2)
  {
    return _v1.xLon * _v2.yLat - _v1.yLat * _v2.xLon;
  }

  /**
   * 2d triangle area
   * @param _v1 first vertex of the triangle
   * @param _v2 second vertex of the triangle
   * @param _v3 last vertex of the triangle
   * @return 2d area of the triangle
   */
  public static double area2d(final V4d _v1, final V4d _v2, final V4d _v3)
  {
    return 0.5 * Math.abs(cross2d(sub2d(_v3, _v2), sub2d(_v1, _v2)));
  }

  /**
   * calculate tangent angle for passed edge
   * @param _e edge
   * @return tangent angle in radian
   */
  public static double calcTangent(final G4d.Edge _e)
  {
    return calcTangent(_e.getFirstVertex(), _e.getLastVertex());
  }

  /**
   * calculate tangent angle for two passed vertexes
   * @param _v1 start vertex
   * @param _v2 end vertex
   * @return tangent angle in radian
   */
  public static double calcTangent(final V4d _v1, final V4d _v2)
  {
    double dx = _v2.xLon - _v1.xLon;
    double dy = _v2.yLat - _v1.yLat;
    return dy != 0. ? Math.atan(dx / dy) : PI_2;  // use pi/2 for vertical line
  }

  /**
   * calculate square 2d distance between two vertexes passed
   * @param _v1 first vertex
   * @param _v2 second vertex
   * @return square 2d distance
   */
  public static double calcDistanceSq(final V4d _v1, final V4d _v2)
  {
    double dx = _v2.xLon - _v1.xLon;
    double dy = _v2.yLat - _v1.yLat;
    return dx * dx + dy * dy;
  }

  public static double calcDistanceSq(final double _x1, final double _y1, final double _x2, final double _y2)
  {
    double dx = _x2 - _x1;
    double dy = _y2 - _y1;
    return dx * dx + dy * dy;
  }

  public static double calcDistanceSq(final double[] _v1, final V4d _v2)
  {
    double dx = _v2.xLon - _v1[0];
    double dy = _v2.yLat - _v1[1];
    return dx * dx + dy * dy;
  }

  public static double calcLineEquation(final double _x1, final double _y1, final double _x2, final double _y2,
      final double _x, final double _y)
  {
    return calcLineEquationFromDeltas(_x1, _y1, _x2 - _x1, _y2 - _y1, _x, _y);
  }

  private static double calcLineEquationFromDeltas(final double _x1, final double _y1, final double _dx, final double _dy,
      final double _x, final double _y)
  {
    return _dy * (_x - _x1) - (_y - _y1) * _dx;
  }

  public static double calcDistanceToLineSq(final double _x1, final double _y1, final double _x2, final double _y2,
      final double _x, final double _y)
  {
    final double dx = (_x2 - _x1);
    final double dy = (_y2 - _y1);
    final double normalizer = dx * dx + dy * dy;
    final double le = calcLineEquationFromDeltas(_x1, _y1, dx, dy, _x, _y);
    return le * le / normalizer;
  }

  // see trapezoid formula in https://en.wikipedia.org/wiki/Shoelace_formula
  public static double calcPolySquare2d(final G4d<?> _poly)
  {
    double res = 0;
    for (int i = 0; i < _poly.shape.length; i++)
    {
      V4d v1 = i > 0 ? _poly.shape[i - 1] : _poly.shape[_poly.shape.length - 1];
      V4d v2 = _poly.shape[i];
      res += (v1.xLon - v2.xLon) * (v1.yLat + v2.yLat);
    }
    return Math.abs(res) / 2.; // use abs to provide positive area for clock wise orientation
  }

  /**
   * compute center of circle defined by three vertexes
   * @param _v0 first vertex on circle's arc
   * @param _v1 second vertex on circle's arc
   * @param _v2 third  vertex on circle's arc
   * @return 2d center position
   */
  private static V4d computeCircleCenter(final V4d _v0, final V4d _v1, final V4d _v2)
  {
    double offset = _v1.xLon * _v1.xLon + _v1.yLat * _v1.yLat;
    double bc = (_v0.xLon * _v0.xLon + _v0.yLat * _v0.yLat - offset) / 2.0;
    double cd = (offset - _v2.xLon * _v2.xLon - _v2.yLat * _v2.yLat) / 2.0;
    double det = (_v0.xLon - _v1.xLon) * (_v1.yLat - _v2.yLat) - (_v1.xLon - _v2.xLon) * (_v0.yLat - _v1.yLat);
    double inv_det = 1 / det;
    double cx = (bc * (_v1.yLat - _v2.yLat) - cd * (_v0.yLat - _v1.yLat)) * inv_det;
    double cy = (cd * (_v0.xLon - _v1.xLon) - bc * (_v1.xLon - _v2.xLon)) * inv_det;
    return new V4d(cx, cy);
  }

  /**
   * calculate projection of passed vertx on passed edge
   * @param _e target edge
   * @param _v0 vertex to project
   * @param _buffer output buffer minimum two doubles long, MUST be allocated! this buffer will populate
   * @return projected point in allocated buffer or null if passed vertex can't be projected on the edge
   */
  public static double[] calcProjectionPoint(final G4d.Edge _e, final V4d _v0, final double[] _buffer)
  {
    return calcProjectionPoint(_e.getFirstVertex(), _e.getLastVertex(), _v0, _buffer);
  }


  /**
   * calculate projection of passed vertx on passed edge defined by two vertexes
   * @param _v1 first vertex of the target edge
   * @param _v2 second vertex of the target edge
   * @param _v0 vertex to project
   * @param _buffer output buffer minimum two doubles long, MUST be allocated! this buffer will populate
   * @return projected point in allocated buffer or null if passed vertex can't be projected on the edge
   * @return
   */
  public static double[] calcProjectionOffsetAndSquareDistance(final V4d _v1, final V4d _v2, final V4d _v0, final double[] _buffer)
  {
    double [] xy = calcProjectionPoint(_v1.xLon, _v1.yLat, _v2.xLon, _v2.yLat, _v0.xLon, _v0.yLat, _buffer);
    if(xy != null)
    {
      double dx = Math.abs(_v2.xLon - _v1.xLon);
      double dy = Math.abs(_v2.yLat - _v1.yLat);
      double factor = dx > dy ? Math.abs(xy[0] - _v1.xLon)/dx : Math.abs(xy[1] - _v1.yLat)/dy;
      double o = _v1.o + (_v2.o - _v1.o) * factor;
      double d = calcDistanceSq(xy[0],xy[1],_v0.xLon,_v0.yLat);
      xy[0] = o;
      xy[1] = d;
    }
    return xy;
  }


  /**
   * calculate projection of passed vertx on passed edge defined by two vertexes
   * @param _v1 first vertex of the target edge
   * @param _v2 second vertex of the target edge
   * @param _v0 vertex to project
   * @param _buffer output buffer minimum two doubles long, MUST be allocated! this buffer will populate
   * @return projected point in allocated buffer or null if passed vertex can't be projected on the edge
   * @return
   */
  public static double[] calcProjectionPoint(final V4d _v1, final V4d _v2, final V4d _v0, final double[] _buffer)
  {
    return calcProjectionPoint(_v1.xLon, _v1.yLat, _v2.xLon, _v2.yLat, _v0.xLon, _v0.yLat, _buffer);
  }

  /**
   * calculate projection of passed vertx on passed edge defined by two vertexes
   * @param _x1 x of the first vertex of the target edge
   * @param _y1 y of the first vertex of the target edge
   * @param _x2 x of the second vertex of the target edge
   * @param _y2 y of the second vertex of the target edge
   * @param _x0 x of the vertex to project
   * @param _y0 y vertex to project
   * @param _buffer output buffer minimum two doubles long, MUST be allocated! this buffer will populate
   * @return projected point in allocated buffer or null if passed vertex can't be projected on the edge
   */
  private static double[] calcProjectionPoint(final double _x1, final double _y1, final double _x2, final double _y2, final double _x0,
      final double _y0, final double[] _buffer)
  {
    double[] ret = null;
    final double dx = _x2 - _x1;
    final double dy = _y2 - _y1;
    final double u = ((_x0 - _x1) * dx + (_y0 - _y1) * dy) / (dx * dx + dy * dy);
    if (u >= 0 && u <= 1)
    {
      ret = _buffer;
      ret[0] = _x1 + u * dx;
      ret[1] = _y1 + u * dy;
    }
    return ret;
  }

  /**
   * comparing two tangent angle with tolerance provided
   * @param _t1 first tangent angle to compare
   * @param _t2 second tangent angle to compare
   * @param _t comparison tolerance
   * @return {-1;0;1} -> {less,same,greatest}
   */
  public static int compareTangentWithTolerance(final double _t1, final double _t2, final double _t)
  {
    return calcDiffTangent(_t1,_t2)  <= _t ? 0 : _t1 > _t2 ? 1:-1;
  }

  /**
   * calculate difference between two tangent angles
   * @param _t1 first tangent angle
   * @param _t2 second tangent angle
   * @return difference
   */
  public static double calcDiffTangent(final double _t1, final double _t2)
  {
    final double d = Math.abs(_t1 - _t2);
    return Math.min(d, Math.abs(d - Math.PI));
  }

  public static boolean rayTest(final G4d<?> _ring, V4d _test_point)
  {
    int counter = 0;
    for (int i = 0; i < _ring.segments.length; i++)
    {
      G4d<?>.MSegment sb = _ring.segments[i];
      if (sb.getXMax() > _test_point.xLon && sb.getYMax() > _test_point.yLat && sb.getYMin() <= _test_point.yLat)
      {
        for (int j = 0; j < sb.getEdgesQty(); j++)
        {
          G4d<?>.Edge ed = sb.getEdge(j);
          if (ed.getXMax() > _test_point.xLon && ed.getYMax() > _test_point.yLat && ed.getYMin() <= _test_point.yLat)
          {
            V4d delta = sub2d(ed.getLastVertex(), ed.getFirstVertex());
            if (delta.yLat != 0)
            {
              double factor = delta.xLon / delta.yLat;
              double x = ed.getLastVertex().xLon - (ed.getLastVertex().yLat - _test_point.yLat) * factor;
              if (x > _test_point.xLon)
              {
                counter++;
              }
            }
            else
            {
              counter++;
            }
          }
        }
      }
    }
    return counter % 2 != 0;
  }

  public static AnAssociation<V4d, V4d> calcEdgesIntersection2d(G4d<?>.Edge _a, G4d<?>.Edge _b)
  {
    return calcEdgesIntersection2d(_a.getFirstVertex(), _a.getLastVertex(), _b.getFirstVertex(), _b.getLastVertex());
  }

  public static AnAssociation<V4d, V4d> calcEdgesIntersection2d(final V4d _vA0, final V4d _vA1, final V4d _vB0, final V4d _vB1)
  {
    // calc the matrix
    double s10_x = _vA1.xLon - _vA0.xLon;
    double s10_y = _vA1.yLat - _vA0.yLat;
    double s32_x = _vB1.xLon - _vB0.xLon;
    double s32_y = _vB1.yLat - _vB0.yLat;
    double denom = s10_x * s32_y - s32_x * s10_y;
    if (denom == 0) // todo: use tolerance there
    {
      return null;
    }

    boolean denom_positive = denom > 0;
    double s02_x = _vA0.xLon - _vB0.xLon;
    double s02_y = _vA0.yLat - _vB0.yLat;
    double s_numer = s10_x * s02_y - s10_y * s02_x;
    if ((s_numer < 0) == denom_positive)
    {
      return null;
    }

    double t_numer = s32_x * s02_y - s32_y * s02_x;
    if (
        (t_numer < 0) == denom_positive ||
            (((s_numer > denom) == denom_positive) || ((t_numer > denom) == denom_positive))
    )
    {
      return null;
    }

    double t = t_numer / denom;
    double x = _vA0.xLon + (t * s10_x);
    double y = _vA0.yLat + (t * s10_y);
    double oa = calcOffset(_vA0, _vA1, x, y);
    double ob = calcOffset(_vB0, _vB1, x, y);
    return new AnAssociation<>
        (
            new V4d(x, y, lerpValue(_vA0.zAlt, _vA0, _vA1.zAlt, _vA1, oa), oa),
            new V4d(x, y, lerpValue(_vB0.zAlt, _vB0, _vB1.zAlt, _vB1, ob), ob)
        );
  }

  private static final Collection<AnAssociation<V4d, V4d>> NO_INTERSECTIONS_FOUND = Collections.unmodifiableList(new LinkedList<>());

  public static Collection<AnAssociation<V4d, V4d>> findIntersections(final G4d<?> _a, final G4d<?> _b)
  {
    Collection<AnAssociation<V4d, V4d>> res = NO_INTERSECTIONS_FOUND;
    AABB search_scope = _a.calcCross(_b, 0, 0);
    if (search_scope != null)
    {
      LinkedList<G4d<?>.MSegment> bf = new LinkedList<>();
      for (int i = 0; i < _b.segments.length; i++)
      {
        G4d<?>.MSegment sb = _b.segments[i];
        if (search_scope.overlaps(sb))
        {
          bf.add(sb);
        }
      }
      for (int i = 0; i < _a.segments.length; i++)
      {
        G4d<?>.MSegment sa = _a.segments[i];
        if (search_scope.overlaps(sa))
        {
          for (G4d<?>.MSegment sb : bf)
          {
            AnAssociation<V4d, V4d> intesection = findIntersection(sa, sb);
            if (intesection != null)
            {
              if (res == NO_INTERSECTIONS_FOUND)
              {
                res = new LinkedList<>();
              }
              res.add(intesection);
            }
          }
        }
      }
    }
    return res;
  }

  public static Collection<AnAssociation<V4d, V4d>> findSelfIntersections(final G4d<?> _geom)
  {
    Collection<AnAssociation<V4d, V4d>> res = NO_INTERSECTIONS_FOUND;
    for (int i = 0; i < _geom.segments.length; i++)
    {
      G4d<?>.MSegment sa = _geom.segments[i];
      for (int j = i + 2; j < _geom.segments.length; j++) // two adjacent monotonic segments can't introduce self intersection
      {
        G4d<?>.MSegment sb = _geom.segments[j];
        if (sa.overlaps(sb))// todo: re-think  && calcSweepOrder(sa) != calcSweepOrder(sb))
        {
          AnAssociation<V4d, V4d> intesection = findIntersection(sa, sb);
          if (intesection != null)
          {
            if (res == NO_INTERSECTIONS_FOUND)
            {
              res = new LinkedList<>();
            }
            res.add(intesection);
          }
        }
      }
    }
    return res;
  }

  public static AnAssociation<V4d, V4d> findIntersection(final G4d<?>.MSegment _sa, final G4d<?>.MSegment _sb)
  {
    AABB cross = _sa.calcCross(_sb, 0, 0);
    AnAssociation<V4d, V4d> intersection = null;
    if (cross != null)
    {
      for (int m = 0; intersection == null && m < _sa.getEdgesQty(); m++)
      {
        G4d<?>.Edge ea = _sa.getEdge(m);
        if (ea.overlaps(cross))
        {
          for (int n = 0; intersection == null && n < _sb.getEdgesQty(); n++)
          {
            G4d<?>.Edge eb = _sb.getEdge(n);
            if (eb.overlaps(cross) && eb.overlaps(ea))
            {
              intersection = calcEdgesIntersection2d(ea, eb);
            }
          }
        }
      }
    }
    return intersection;
  }

  public static boolean liesOnBorder(AABB _a, final G4d<?>.Edge _e)
  {
    return
        (_e.getFirstVertex().xLon == _e.getLastVertex().xLon && (_e.getLastVertex().xLon == _a.xyxy[2] || _e.getLastVertex().xLon == _a.xyxy[0])) ||
            (_e.getFirstVertex().yLat == _e.getLastVertex().yLat && (_e.getLastVertex().yLat == _a.xyxy[3] || _e.getLastVertex().yLat == _a.xyxy[1]));
  }
}
