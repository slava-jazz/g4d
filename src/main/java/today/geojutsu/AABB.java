package today.geojutsu;

import today.geojutsu.match.RangeReference;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/*
   2d axis-aligned minimum bounding box
 */
public class AABB implements Serializable
{

  final double[] xyxy;  // unbreakable memory chunk

  public AABB(final V4d _v1, final V4d _v2)
  {
    this(_v1.xLon, _v1.yLat, _v2.xLon, _v2.yLat);
  }

  public AABB(final double _x1, final double _y1, final double _x2, final double _y2)
  {
    xyxy = new double[]
        {
            Math.min(_x1, _x2), Math.min(_y1, _y2),
            Math.max(_x1, _x2), Math.max(_y1, _y2)
        };
  }

  public AABB(final AABB _aabb)
  {
    xyxy = new double[]
        {
            _aabb.xyxy[0], _aabb.xyxy[1], _aabb.xyxy[2], _aabb.xyxy[3]
        };
  }

  public AABB(final V4d _v, final double _extend_x, final double _extend_y)
  {
    xyxy = new double[]
        {
            _v.xLon - _extend_x,
            _v.yLat - _extend_y,
            _v.xLon + _extend_x,
            _v.yLat + _extend_y
        };
  }

  public AABB(final AABB _aabb, final double _extend_x, final double _extend_y)
  {
    xyxy = new double[]
        {
            _aabb.xyxy[0] - _extend_x,
            _aabb.xyxy[1] - _extend_y,
            _aabb.xyxy[2] + _extend_x,
            _aabb.xyxy[3] + _extend_y
        };
  }

  public AABB()
  {
    xyxy = new double[]
        {
            Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY
        };
  }

  public boolean isOk()
  {
    return !Double.isInfinite(xyxy[0]);
  }

  public AABB reset(final AABB _aabb)
  {
    xyxy[0] = _aabb.xyxy[0];
    xyxy[1] = _aabb.xyxy[1];
    xyxy[2] = _aabb.xyxy[2];
    xyxy[3] = _aabb.xyxy[3];
    return this;
  }

  public AABB reset(final double _x1, final double _y1, final double _x2, final double _y2)
  {
    xyxy[0] = _x1;
    xyxy[1] = _y1;
    xyxy[2] = _x2;
    xyxy[3] = _y2;
    return this;
  }


  public AABB reset(final V4d _v)
  {
    xyxy[2] = xyxy[0] = _v.xLon;
    xyxy[3] = xyxy[1] = _v.yLat;
    return this;
  }

  public AABB reset(final V4d _v, final double _dx, final double _dy)
  {
    reset(_v);
    surroundBy(_dx, _dy);
    return this;
  }

  public double getXMin()
  {
    return xyxy[0];
  }

  public double getYMin()
  {
    return xyxy[1];
  }

  public double getXMax()
  {
    return xyxy[2];
  }

  public double getYMax()
  {
    return xyxy[3];
  }

  public double getDX()
  {
    return xyxy[2] - xyxy[0];
  }

  public double getDY()
  {
    return xyxy[3] - xyxy[1];
  }

  public AABB extend(final AABB _aabb)
  {
    if (_aabb.xyxy[0] < xyxy[0])
    {
      xyxy[0] = _aabb.xyxy[0];
    }
    if (_aabb.xyxy[1] < xyxy[1])
    {
      xyxy[1] = _aabb.xyxy[1];
    }
    if (_aabb.xyxy[2] > xyxy[2])
    {
      xyxy[2] = _aabb.xyxy[2];
    }
    if (_aabb.xyxy[3] > xyxy[3])
    {
      xyxy[3] = _aabb.xyxy[3];
    }
    return this;
  }

  public AABB surroundBy(final double _dx, final double _dy)
  {
    xyxy[0] -= _dx;
    xyxy[1] -= _dy;
    xyxy[2] += _dx;
    xyxy[3] += _dy;
    return this;
  }

  public AABB extend(final double _x, final double _y)
  {
    if (_x < xyxy[0])
    {
      xyxy[0] = _x;
    }
    if (_y < xyxy[1])
    {
      xyxy[1] = _y;
    }
    if (_x > xyxy[2])
    {
      xyxy[2] = _x;
    }
    if (_y > xyxy[3])
    {
      xyxy[3] = _y;
    }
    return this;
  }

  public void extend(final V4d _v)
  {
    extend(_v.xLon, _v.yLat);
  }

  public boolean theSame(final AABB _aabb)
  {
    return xyxy[0] == _aabb.xyxy[0] && xyxy[1] == _aabb.xyxy[1] && xyxy[2] == _aabb.xyxy[2] && xyxy[3] == _aabb.xyxy[3];
  }

  public boolean edgeOverlaps(final AABB _aabb)
  {
    return xyxy[0] == _aabb.xyxy[0] || xyxy[1] == _aabb.xyxy[1] || xyxy[2] == _aabb.xyxy[2] || xyxy[3] == _aabb.xyxy[3];
  }


  public boolean overlaps(final AABB _aabb)
  {
    return xyxy[2] >= _aabb.xyxy[0] && xyxy[0] <= _aabb.xyxy[2] && xyxy[3] >= _aabb.xyxy[1] && xyxy[1] <= _aabb.xyxy[3];
  }

  public boolean contains(AABB _aabb)
  {
    return xyxy[2] >= _aabb.xyxy[2] && xyxy[0] <= _aabb.xyxy[0] && xyxy[3] >= _aabb.xyxy[3] && xyxy[1] <= _aabb.xyxy[1];
  }

  public boolean contains(V4d _v)
  {
    return xyxy[2] >= _v.xLon && xyxy[0] <= _v.xLon && xyxy[3] >= _v.yLat && xyxy[1] <= _v.yLat;
  }

  public boolean contains(final double _x, final double _y)
  {
    return xyxy[2] >= _x && xyxy[0] <= _x && xyxy[3] >= _y && xyxy[1] <= _y;
  }

  public boolean containsWithoutMax(V4d _v)
  {
    return xyxy[2] > _v.xLon && xyxy[0] <= _v.xLon && xyxy[3] > _v.yLat && xyxy[1] <= _v.yLat;
  }

  public boolean containedBy(AABB _aabb)
  {
    return _aabb.contains(this);
  }

  public double distanceSq(final double _x, final double _y)
  {
    double ds = 0;
    double temp = xyxy[0] - _x;
    if (temp < 0)
    {
      temp = _x - xyxy[2];
    }
    if (temp > 0)
    {
      ds += (temp * temp);
    }
    temp = xyxy[1] - _y;
    if (temp < 0)
    {
      temp = _y - xyxy[3];
    }
    if (temp > 0)
    {
      ds += (temp * temp);
    }
    return ds;
  }

  public double distance(final double _x, final double _y)
  {
    return Math.sqrt(distanceSq(_x, _y));
  }

  public double distanceSq(final V4d _v)
  {
    return distanceSq(_v.xLon, _v.yLat);
  }

  public double distance(final V4d _v)
  {
    return distance(_v.xLon, _v.yLat);
  }

  public boolean touchedMin(final V4d _v)
  {
    return _v.xLon == xyxy[0] || _v.yLat == xyxy[1];
  }

  public boolean touchedMax(final V4d _v)
  {
    return _v.xLon == xyxy[2] || _v.yLat == xyxy[3];
  }

  public double distanceSq(final AABB _aabb)
  {
    double ds = 0;
    double min = Math.max(xyxy[0], _aabb.xyxy[0]);
    double max = Math.min(xyxy[2], _aabb.xyxy[2]);
    if (min > max)
    {
      ds += ((min - max) * (min - max));
    }
    min = Math.max(xyxy[1], _aabb.xyxy[1]);
    max = Math.min(xyxy[3], _aabb.xyxy[3]);
    if (min > max)
    {
      ds += ((min - max) * (min - max));
    }
    return ds;
  }

  public double distance(final AABB _aabb)
  {
    return Math.sqrt(distanceSq(_aabb));
  }

  public AABB calcCross(final AABB _aabb, final double _buffer_x, final double _buffer_y)
  {
    double x_min = Math.max(xyxy[0], _aabb.xyxy[0]) - _buffer_x;
    double x_max = Math.min(xyxy[2], _aabb.xyxy[2]) + _buffer_x;
    double y_min = Math.max(xyxy[1], _aabb.xyxy[1]) - _buffer_y;
    double y_max = Math.min(xyxy[3], _aabb.xyxy[3]) + _buffer_y;

    return (x_max >= x_min && y_max >= y_min) ? new AABB(x_min, y_min, x_max, y_max) : null;
  }

  public boolean isPoint()
  {
    return xyxy[0] == xyxy[2] && xyxy[1] == xyxy[3];
  }

  public double calcEnlargement(final AABB _aabb)
  {
    double ret = 0;
    if (!contains(_aabb))
    {
      double new_area = (Math.max(xyxy[2], _aabb.xyxy[2]) - Math.min(xyxy[0], _aabb.xyxy[0])) *
          (Math.max(xyxy[3], _aabb.xyxy[3]) - Math.min(xyxy[1], _aabb.xyxy[1]));

      ret = new_area - calcArea();
    }
    return ret;
  }

  public double calcArea()
  {
    return calcArea(xyxy[0], xyxy[1], xyxy[2], xyxy[3]);
  }

  private static final Set<V4d> __EMPTY_V4D_SET = Collections.emptyNavigableSet();
  private static final Comparator<V4d> __SORT_BY_OFFSET = new Comparator<V4d>()
  {
    @Override
    public int compare(final V4d _o1, final V4d _o2)
    {
      return Double.compare(_o1.o, _o2.o);
    }
  };

  /**
   * calculate intersections of the passed feature which the AABB borders
   * @param _geom    the feature to test
   * @param _buffer  temporary buffer at least 6 doubles long
   * @return  intersections in digitization order
   */
  public Iterable<V4d> findIntersections(final G4d<?> _geom, final double [] _buffer)
  {
    Set<V4d> res = __EMPTY_V4D_SET;
    if(!contains(_geom) && overlaps(_geom))
    {
      for(int i = 0; i < _geom.segments.length; i++)
      {
        G4d<?>.MSegment sm = _geom.segments[i];
        if(!contains(sm) && overlaps(sm))
        {
          for(int j = 0; j < sm.getEdgesQty(); j++)
          {
            G4d<?>.Edge ed = sm.getEdge(j);
            if(Euclid.liesOnBorder(this, ed))
            {
              // the simplest way to get deterministic intersection point :)
              if(res == __EMPTY_V4D_SET)
              {
                res = new TreeSet<>(__SORT_BY_OFFSET);
              }
              else
              {
                res.remove(ed.getFirstVertex());
              }
              res.add(ed.getLastVertex());
            }
            else
            {
              IntersectionTest test = calcIntersectStatus(ed.getFirstVertex(),ed.getLastVertex(),_buffer);
              if(test == IntersectionTest.intersect || test == IntersectionTest.doubleIntersect)
              {
                if(res == __EMPTY_V4D_SET)
                {
                  res = new TreeSet<>(__SORT_BY_OFFSET);
                }
                res.add(calcVertex(ed,_buffer[0],_buffer[1]));
                if(test == IntersectionTest.doubleIntersect)
                {
                  // because of algorithm, second point in buffer may be the same as the first one.
                  if(_buffer[0] != _buffer[2])
                  {
                    res.add(calcVertex(ed,_buffer[2],_buffer[3]));
                  }
                  else if(!Double.isNaN(_buffer[4]))
                  {
                    res.add(calcVertex(ed,_buffer[4],_buffer[5]));
                  }
                }
              }
            }
          }
        }
      }
    }
    return res;
  }

  /**
   * calculate with ranges of the passed feature located in the AABB scope
   * @param _feature    the feature to test
   * @param _buffer     temporary buffer at least 6 doubles long
   * @return  ranges of the feature inside the scope (partition in fact)
   */
  public TreeSet<RangeReference.Range>  findPartsInScope(G4d<?> _feature, final double [] _buffer )
  {
    Iterable<V4d> intersections = findIntersections(_feature,_buffer);
    TreeSet<RangeReference.Range> res = new TreeSet<>();
    double o = 0;
    for (V4d v : intersections)
    {
      if(containsWithoutMax(_feature.calculatePoint(0.5*(o +v.o),0)))
      {
        res.add(new RangeReference.Range(o,v.o));
      }
      o = v.o;
    }
    if(containsWithoutMax(_feature.calculatePoint(0.5*(o +1.),0)))
    {
      res.add(new RangeReference.Range(o,1));
    }
    return res;
  }


  private static V4d calcVertex(final G4d<?>.Edge _ed, final double _x, final double _y)
  {
    double dx = Math.abs(_ed.getLastVertex().xLon - _ed.getFirstVertex().xLon);
    double dy = Math.abs(_ed.getLastVertex().yLat - _ed.getFirstVertex().yLat);
    double factor = dx > dy ? Math.abs(_x - _ed.getFirstVertex().xLon) / dx : Math.abs(_y - _ed.getFirstVertex().yLat) / dy;
    double o = _ed.getFirstVertex().o + (_ed.getLastVertex().o - _ed.getFirstVertex().o) * factor;
    double z = _ed.getFirstVertex().zAlt + (_ed.getLastVertex().zAlt - _ed.getFirstVertex().zAlt) * factor;
    return new V4d(_x, _y, z, o);
  }

  /**
   * calculate intersection status for the passed edge
   *
   * @param _p1  first vertex of the edge
   * @param _p2  second vertex of the edge
   * @param _buffer optional buffer to store intersection coordinates, may be null if not interested
   * @return intersection status
   */
  public IntersectionTest calcIntersectStatus(final V4d _p1, final V4d _p2, final double[] _buffer)
  {
    IntersectionTest res = contains(_p1) && contains(_p2) ? IntersectionTest.inside : IntersectionTest.outside;
    int buffer_capacity = _buffer == null ? -1 : _buffer.length / 2;
    if (_buffer != null)
    {
      for (int i = 0; i < _buffer.length; i++)
      {
        _buffer[i] = Double.NaN;
      }
    }
    if (res == IntersectionTest.outside)
    {
      double x_min = Math.min(_p1.xLon, _p2.xLon);
      double y_min = Math.min(_p1.yLat, _p2.yLat);
      double x_max = Math.max(_p1.xLon, _p2.xLon);
      double y_max = Math.max(_p1.yLat, _p2.yLat);
      if (x_min <= xyxy[2] && x_max >= xyxy[0] && y_min <= xyxy[3] || y_max >= xyxy[1])
      {
        int intersection_count = 0;
        double dx = _p2.xLon - _p1.xLon;
        double dy = _p2.yLat - _p1.yLat;
        if (dx == 0. && x_min >= xyxy[0] && x_max <= xyxy[2]) // vertical
        {
          if (y_min <= xyxy[1] && y_max >= xyxy[1])
          {
            // (x_min, xyxy[1]);
            if (buffer_capacity > intersection_count)
            {
              _buffer[0] = x_min;
              _buffer[1] = xyxy[1];
            }
            intersection_count++;
          }
          if (y_min <= xyxy[3] && y_max >= xyxy[3])
          {
            // (x_min, xyxy[3]);
            if (buffer_capacity > intersection_count)
            {
              _buffer[intersection_count * 2] = x_min;
              _buffer[intersection_count * 2 + 1] = xyxy[3];
            }
            intersection_count++;
          }
        }
        else if (dy == 0. && y_min >= xyxy[1] && y_max <= xyxy[3]) // horizontal
        {
          if (x_min <= xyxy[0] && x_max >= xyxy[0])
          {
            // (xyxy[0], y_min);
            if (buffer_capacity > intersection_count)
            {
              _buffer[0] = xyxy[0];
              _buffer[1] = y_min;
            }
            intersection_count++;
          }
          if (x_min <= xyxy[2] && x_max >= xyxy[2])
          {
            // (xyxy[2], y_min);
            if (buffer_capacity > intersection_count)
            {
              _buffer[intersection_count * 2] = xyxy[2];
              _buffer[intersection_count * 2 + 1] = y_min;
            }
            intersection_count++;
          }
        }
        else // common case
        {
          for (int i = 0; i < 3; i += 2) // check vertical borders
          {
            double x_test = xyxy[i];
            if (x_test >= x_min && x_test <= x_max)
            {
              double y_test = _p1.yLat + (x_test - _p1.xLon) / dx * dy;
              if (y_test >= y_min && y_test <= y_max && y_test >= xyxy[1] && y_test <= xyxy[3])
              {
                // (x_test, y_test);
                if (buffer_capacity > intersection_count)
                {
                  _buffer[intersection_count * 2] = x_test;
                  _buffer[intersection_count * 2 + 1] = y_test;
                }
                intersection_count++;
              }
            }
          }

          for (int i = 1; i < 4; i += 2) // check horizontal borders
          {
            double y_test = xyxy[i];
            if (y_test >= y_min && y_test <= y_max)
            {
              double x_test = _p1.xLon + (y_test - _p1.yLat) / dy * dx;
              if (x_test >= x_min && x_test <= x_max && x_test >= xyxy[0] && x_test <= xyxy[2])
              {
                // (x_test, y_test);
                if (buffer_capacity > intersection_count)
                {
                  _buffer[intersection_count * 2] = x_test;
                  _buffer[intersection_count * 2 + 1] = y_test;
                }
                intersection_count++;
              }
            }
          }
        }
        res = intersection_count == 1 ? IntersectionTest.intersect :
            intersection_count > 1 ? IntersectionTest.doubleIntersect :
                IntersectionTest.outside;
      }
    }
    return res;
  }

  public enum IntersectionTest
  {
    intersect, inside, outside, doubleIntersect
  }

  static public double calcArea(double _x1, double _y1, double _x2, double _y2)
  {
    return Math.abs((_x2 - _x1) * (_y2 - _y1));
  }

}
