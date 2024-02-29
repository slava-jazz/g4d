package today.geojutsu;

import java.util.*;
import java.util.Map.Entry;

/**
 * represents any non point geometry. dependently on context it can be a poly-line or interior/exterior ring of a polygon
 */
public class G4d<TCustom> extends AABB
{

  final double length;   // absolute length of the feature
  final V4d[] shape;    // geometrical shape of the feature
  final Edge[] edges;    // edges of the feature's shape
  final MSegment[] segments; // monotonic segments of the feature's shape
  final TCustom customData; // custom data

  public interface InputAdapter<TCustomGeometry>
  {
    AnAssociation<V4d[], Double> convert(final TCustomGeometry _geometry);
  }

  enum TangentOrder
  {
    Stable,
    Ascending,
    Descending
  }

  /**
   * single edge of the geometrical shape.
   * actually it introduces only one physical property - the tangent
   * but, since it provides many additional functionality, I decided
   * to create the class.
   */
  public class Edge extends AABB
  {

    public final int firstIndex;   // first index of the edge
    public final double tangent;   // edge's tangent as angle in radians

    public Edge(final int _firstIndex, final double _tangent)
    {
      super(shape[_firstIndex], shape[_firstIndex + 1]);
      firstIndex = _firstIndex;
      tangent = _tangent;
    }

    /**
     * @return first vertex of the edge in the native digitalising order
     */
    public V4d getFirstVertex()
    {
      return shape[firstIndex];
    }

    /**
     * @return last vertex of the edge in the native digitalising order
     */
    public V4d getLastVertex()
    {
      return shape[firstIndex + 1];
    }

    public boolean isLast()
    {
      return (firstIndex + 1) == edges.length;
    }

    public double calcEuclidHeading()
    {
      return Euclid.calcHeading(this);
    }

    public double calcGeodeticHeading()
    {
      return Geodetic.calcHeading(getFirstVertex(),getLastVertex());
    }

    /**
     * @return absolute length of the edge
     */
    public double getLength()
    {
      return parametric2absolute(shape[firstIndex + 1].o - shape[firstIndex].o);
    }

    /**
     * linear interpolate V4d for specific offset provided as parameter
     *
     * @param _offset           paramedical offset, must be inside edge's range
     * @param _offset_tolerance tolerance to choose existing shape point
     * @return the V4d on the edge or null if passed offset is out of edge's range
     */
    public V4d lerpVertex(final double _offset, final double _offset_tolerance)
    {
      return Euclid.lerpVertex(shape[firstIndex], shape[firstIndex + 1], _offset, _offset_tolerance);
    }

    public G4d<TCustom> getFeature()
    {
      return G4d.this;
    }

    @Override
    public String toString()
    {
      return "{" + firstIndex + " [" + getFirstVertex() + "; " + getLastVertex() + "], t:" + tangent + '}';
    }
  }

  /**
   * monotonic segment
   */
  public class MSegment extends AABB
  {

    final Edge firstEdge;
    final Edge lastEdge;
    final double[] tangentRange;
    final TangentOrder tangentOrder;

    MSegment(final Edge _firstEdge, final Edge _lastEdge, final TangentOrder _order)
    {
      super(_firstEdge);
      extend(_lastEdge);
      tangentOrder = _order;
      firstEdge = _firstEdge;
      lastEdge = _lastEdge;
      tangentRange = new double[] {Math.min(firstEdge.tangent, lastEdge.tangent), Math.max(firstEdge.tangent, lastEdge.tangent)};
    }

    /**
     * @return first edge of the segment in the native digitalising order
     */
    public Edge getFirstEdge()
    {
      return firstEdge;
    }

    public Edge getEdge(final int _index)
    {
      return edges[_index + firstEdge.firstIndex];
    }

    public V4d getVertex(final int _index)
    {
      return shape[_index + firstEdge.firstIndex];
    }

    /**
     * @return last edge of the segment in the native digitalising order
     */
    public Edge getLastEdge()
    {
      return lastEdge;
    }

    public int getEdgesQty()
    {
      return lastEdge.firstIndex - firstEdge.firstIndex + 1;
    }

    public int getVertexesQty()
    {
      return getEdgesQty() + 1;
    }

    public boolean containsOffset(final double _o)
    {
      return firstEdge.getFirstVertex().o <= _o && lastEdge.getLastVertex().o >= _o;
    }

    /**
     * @return tangent's range [min,max]
     */
    public double[] getTangentRange()
    {
      return tangentRange;
    }

    public G4d<TCustom> getFeature()
    {
      return G4d.this;
    }

    @Override
    public String toString()
    {
      String custom = getCustomData() == null ? "" : "@" + getCustomData();
      return "{" + firstEdge + " -> " + lastEdge + ", tg=" + Arrays.toString(tangentRange) + " " + tangentOrder + '}' + custom;
    }

    public V4d findProjection(final V4d _v0, final AABB _tolerance_box, final double [] _d2d)
    {
      double min_d = Double.MAX_VALUE;
      double o = Double.NaN;
      for(int i = firstEdge.firstIndex; i <= lastEdge.firstIndex; i++)
      {
        Edge e = edges[i];
        if(_tolerance_box.calcIntersectStatus(e.getFirstVertex(),e.getLastVertex(),null) != IntersectionTest.outside)
        {
          double [] od = Euclid.calcProjectionOffsetAndSquareDistance(e.getFirstVertex(),e.getLastVertex(),_v0,_d2d);
          if(od != null && od[1] < min_d)
          {
            min_d = od[1];
            o = od[0];
          }
          else if(_tolerance_box.contains(e.getFirstVertex()))
          {
            double d = Euclid.calcDistanceSq(_v0,e.getFirstVertex());
            if(d < min_d)
            {
              min_d = d;
              o = e.getFirstVertex().o;
            }
          }
        }
      }
      if(_tolerance_box.contains(getLastVertex()) && Euclid.calcDistanceSq(_v0,getLastVertex()) < min_d)
      {
        o = getLastVertex().o;
      }
      return Double.isNaN(o) ? null:calculatePoint(o,0);
    }
  }

  public static <TData> G4d<TData> join(final G4d<?> _head, final G4d<?> _tail, final TData _custom)
  {
    double len = _head.length + _tail.length;
    double head_factor = _head.length / len;
    double tail_factor = _tail.length / len;
    V4d[] shape = new V4d[_head.getShape().length + _tail.getShape().length - 1]; // length - 1 to include joint once
    for (int i = 0; i < _head.shape.length; i++)
    {
      V4d v = _head.shape[i];
      shape[i] = new V4d(v, v.o * head_factor);
    }
    int tail_offset = _head.shape.length - 1;
    for (int i = 1; i < _tail.shape.length; i++)
    {
      V4d v = _tail.shape[i];
      shape[tail_offset + i] = new V4d(v, head_factor + v.o * tail_factor);
    }

    return build(len, shape, _custom);
  }

  /**
   * build g4d from custom feature using specific adapter
   *
   * @param _adapter     adapter to convert
   * @param _custom_geom custom feature
   * @param <TGeom>      custom feature's type
   * @return g4d feature
   */
  public static <TGeom, TData> G4d<TData> build(final InputAdapter<TGeom> _adapter, final TGeom _custom_geom, final TData _custom_data)
  {
    AnAssociation<V4d[], Double> data = _adapter.convert(_custom_geom);
    return build(data.b, data.a, _custom_data);
  }

  public static final double __SEGMENT_TANGENT_BUILD_TOLERANCE = 0.05; // todo make it adjustable

  public static <T> G4d<T> build(final double _length, final V4d[] _shape, final T _custom_data)
  {
    final G4d<T>.Edge[] edges = new G4d.Edge[Math.max(_shape.length - 1, 0)];
    final double[] tangents = new double[edges.length];
    TangentOrder order = TangentOrder.Stable;
    List<Entry<TangentOrder, Integer>> segmentPos = new LinkedList<>();
    boolean dxp = false;
    boolean dyp = false;

    double t_start = Double.NaN;
    for (int i = 0; i < tangents.length; i++)
    {
      double t_end = tangents[i] = Euclid.calcTangent(_shape[i], _shape[i + 1]);
      boolean dx = (_shape[i + 1].xLon - _shape[i].xLon) > 0;
      boolean dy = (_shape[i + 1].yLat - _shape[i].yLat) > 0;
      if (i > 0)
      {
        boolean same_sign = dxp == dx && dyp == dy;
        double dt = tangents[i] - tangents[i - 1];
        double dts = t_end - t_start;
        if (Math.abs(dts) < __SEGMENT_TANGENT_BUILD_TOLERANCE && Math.abs(dt) < __SEGMENT_TANGENT_BUILD_TOLERANCE)
        {
          dt = 0;
        }
        TangentOrder o = dt < 0 ? TangentOrder.Descending : dt > 0 ? TangentOrder.Ascending : TangentOrder.Stable;
        if (!same_sign)
        {
          segmentPos.add(new AbstractMap.SimpleImmutableEntry<>(order, i));
          order = TangentOrder.Stable;
        }
        else if (o != TangentOrder.Stable)
        {
          if (order == TangentOrder.Stable)
          {
            order = o;
          }
          else if (order != o)
          {
            segmentPos.add(new AbstractMap.SimpleImmutableEntry<>(order, i));
            t_start = t_end;
            order = TangentOrder.Stable;
          }
        }
      }
      else
      {
        t_start = t_end;
      }
      dxp = dx;
      dyp = dy;
    }
    segmentPos.add(new AbstractMap.SimpleImmutableEntry<>(order, edges.length));
    G4d<T>.MSegment[] segments = new G4d.MSegment[segmentPos.size()];

    // time to create the output feature
    G4d<T> feature = new G4d<T>(_length, _shape, edges, segments, _custom_data);
    for (int i = 0; i < tangents.length; i++)
    {
      edges[i] = feature.new Edge(i, tangents[i]);
      feature.extend(edges[i].getFirstVertex());
      feature.extend(edges[i].getLastVertex());
    }
    int i = 0;
    int first_edge_index = 0;
    for (Entry<TangentOrder, Integer> e : segmentPos)
    {
      segments[i] = feature.new MSegment(edges[first_edge_index], edges[e.getValue() - 1], e.getKey());
      i++;
      first_edge_index = e.getValue();
    }

    return feature;
  }

  public static <T> G4d<T> buildDummy(final T _custom_data)
  {
    return new G4d<>(0, null, null, null, _custom_data);
  }

  /**
   * protected constructor for internal usage only, the static "build" methods must be used
   *
   * @param _length
   * @param _shape
   * @param _edges
   * @param _segments
   */
  private G4d(final double _length, final V4d[] _shape, final Edge[] _edges, final MSegment[] _segments, final TCustom _data)
  {
    length = _length;
    shape = _shape;
    edges = _edges;
    segments = _segments;
    customData = _data;
  }

  public V4d getFirstVertex()
  {
    return shape[0];
  }

  public V4d getLastVertex()
  {
    return shape[shape.length - 1];
  }

  public V4d getClosetVertex(final double _offset)
  {
    int i = findEdge(_offset);
    double d1 = _offset - shape[i].o;
    double d2 = shape[i+1].o - _offset;
    return  d1 < d2 ? shape[i]:shape[i+1];
  }

  public int findEdge(final double _o)
  {
    if(_o <= 0)
    {
      return  0;
    }
    if(_o >= 1)
    {
      return edges.length - 1;
    }
    int i = (int)(edges.length * _o);
    int min = 0;
    int max = edges.length - 1;

    while (true)
    {
      if(_o < edges[i].getFirstVertex().o)
      {
        max = i - 1;
      }
      else if(_o > edges[i].getLastVertex().o)
      {
        min = i + 1;
      }
      else
      {
        if(_o == edges[i].getLastVertex().o)
        {
          i++;
        }
        break;
      }
      if(max > min)
      {
        i = (min+max)/2;
      }
      else
      {
        i = min;
        break;
      }
    }
    return i;
  }

  public TCustom getCustomData()
  {
    return customData;
  }

  /**
   * @return length
   */
  public double getLength()
  {
    return length;
  }

  /**
   * @return shape
   */
  public V4d[] getShape()
  {
    return shape;
  }

  /**
   * @return edges
   */
  public Edge[] getEdges()
  {
    return edges;
  }


  public void addTolerance(final double _dx, final double _dy)
  {
    for (int i = 0; i < edges.length; i++)
    {
      edges[i].surroundBy(_dx, _dy);
    }
    for (int i = 0; i < segments.length; i++)
    {
      segments[i].surroundBy(_dx, _dy);
    }
    surroundBy(_dx, _dy);
  }

  /**
   * @return monotonic segments
   */
  public MSegment[] getSegments()
  {
    return segments;
  }

  /**
   * convert absolute distance along feature to the parametric offset
   *
   * @param _absolute_distance absolute distance to convert
   * @return parametric offset
   */
  public double absolute2parametric(final double _absolute_distance)
  {
    return _absolute_distance / length;
  }

  /**
   * convert parametric offset to the distance along feature
   *
   * @param _offset offset to convert
   * @return absolute distance along feature
   */
  public double parametric2absolute(final double _offset)
  {
    return _offset * length;
  }

  /**
   * calculate point on the feature
   *
   * @param _offset    offset of the point to calculate
   * @param _tolerance parametric tolerance to get shape point instead of interpolation
   * @return calculated point
   */
  public V4d calculatePoint(final double _offset, final double _tolerance)
  {
    return edges[findEdge(_offset)].lerpVertex(_offset, _tolerance);
  }

  public <T> G4d<T> extract(final double _o1, final double _o2, final double _tolerance, final T _data, final V4d _v_first, final V4d _v_last)
  {
    double min_offset = Math.max(Math.min(_o1, _o2), 0.);
    double max_offset = Math.min(Math.max(_o1, _o2), 1.);

    int pos0 = findEdge(min_offset);
    int posn = findEdge(max_offset);
    V4d v0 = edges[pos0].lerpVertex(min_offset, _tolerance);
    V4d vn = edges[posn].lerpVertex(max_offset, _tolerance);

    boolean lerp_start = edges[pos0].getLastVertex().o - v0.o > _tolerance;

    int qty = posn - pos0 + 1 + (lerp_start ? 1 : 0);
    double l = max_offset - min_offset;
    V4d[] shape = new V4d[qty];
    int i = 0;
    shape[i++] = new V4d(v0, 0);
    for (int p = lerp_start ? pos0 : pos0 + 1; p < posn; p++)
    {
      V4d v = edges[p].getLastVertex();
      shape[i++] = new V4d(v, (v.o - v0.o) / l);
    }
    shape[i] = new V4d(vn, 1);

    // replace first and lsat points if required
    if (_v_first != null)
    {
      shape[0] = new V4d(_v_first.xLon, _v_first.yLat, shape[0].zAlt, shape[0].o);
    }
    if (_v_last != null)
    {
      int li = shape.length - 1;
      shape[li] = new V4d(_v_last.xLon, _v_last.yLat, shape[li].zAlt, shape[li].o);
    }

    return build(length * l, shape, _data);
  }

  public G4d<TCustom> extract(final double _o1, final double _o2, final double _tolerance)
  {
    double min_offset = Math.min(_o1, _o2);
    double max_offset = Math.max(_o1, _o2);

    if (min_offset <= _tolerance && max_offset >= (1. - _tolerance))
    {
      return this;
    }

    int pos0 = findEdge(min_offset);
    int posn = findEdge(max_offset);
    V4d v0 = edges[pos0].lerpVertex(min_offset, _tolerance);
    V4d vn = edges[posn].lerpVertex(max_offset, _tolerance);

    boolean lerp_start = edges[pos0].getLastVertex().o - v0.o > _tolerance;
    int qty = posn - pos0 + 1 + (lerp_start ? 1 : 0);
    double l = max_offset - min_offset;
    V4d[] shape = new V4d[qty];
    int i = 0;
    shape[i++] = new V4d(v0, 0);
    for (int p = lerp_start ? pos0 : pos0 + 1; p < posn; p++)
    {
      V4d v = edges[p].getLastVertex();
      shape[i++] = new V4d(v, (v.o - v0.o) / l);
    }
    shape[i] = new V4d(vn, 1);

    return build(length * l, shape, customData);
  }

  public G4d<TCustom>[] split(final double _o)
  {
    return split(_o,getCustomData(),getCustomData());
  }

  public G4d<TCustom>[] split(final double _o, final TCustom _c1, final TCustom _c2)
  {

    int split_edge_nr = findEdge(_o);
    V4d v0 = edges[split_edge_nr].lerpVertex(_o, 0);

    int qty_1 = split_edge_nr + 2;
    double l1 = _o;
    V4d[] shape1 = new V4d[qty_1];
    int i = 0;
    for (; i <= split_edge_nr; i++)
    {
      V4d v = edges[i].getFirstVertex();
      shape1[i] = new V4d(v, v.o / l1);
    }
    shape1[i] = new V4d(v0, 1);

    boolean lerp_start = edges[split_edge_nr].getLastVertex().o - v0.o > 0;
    int qty_2 = edges.length - split_edge_nr + (lerp_start ? 1 : 0);
    double l2 = 1 - _o;
    V4d[] shape2 = new V4d[qty_2];
    i = 0;
    shape2[i++] = new V4d(v0, 0);
    for (int p = lerp_start ? split_edge_nr : split_edge_nr + 1; p < edges.length; p++)
    {
      V4d v = edges[p].getLastVertex();
      shape2[i++] = new V4d(v, (v.o - v0.o) / l2);
    }
    return new G4d[] {build(length * l1, shape1, _c1), build(length * l2, shape2, _c2)};
  }

  public G4d<TCustom> extract(final double[] _range, final double _tolerance)
  {
    return extract(_range[0], _range[1], _tolerance);
  }

  public G4d<TCustom> reverse()
  {
    V4d[] s = new V4d[shape.length];
    for (int i = 0; i < shape.length; i++)
    {
      V4d v = shape[shape.length - 1 - i];
      s[i] = new V4d(v, 1. - v.o);
    }
    return build(length, s, customData);
  }

  public boolean isRing()
  {
    V4d v1 = shape[0];
    V4d v2 = shape[shape.length - 1];
    return v1.xLon == v2.xLon && v1.yLat == v2.yLat;
  }

  public Optional<G4d<TCustom>> rewind(final int _vertex_index)
  {
    if(isRing())
    {
      if(_vertex_index != 0 && _vertex_index != shape.length - 1)
      {
        double o_base = shape[_vertex_index].o;
        V4d[] s = new V4d[shape.length];
        int last_index = shape.length - 1;
        s[0] = new V4d(shape[_vertex_index], 0);
        s[last_index] = new V4d(shape[_vertex_index], 1);
        for (int i = 0; i < last_index; i++)
        {
          V4d v_old = shape[i];
          if(i < _vertex_index)
          {
            int i_new = last_index - _vertex_index + i;
            double o_new =  1 - o_base + v_old.o;
            s[i_new] = new V4d(v_old, o_new);
          }
          else if (i > _vertex_index)
          {
            int i_new = i -_vertex_index;
            double o_new =  v_old.o - o_base;
            s[i_new] = new V4d(v_old, o_new);
          }
        }
        return Optional.of(build(length, s, customData));
      }
      return Optional.of(this);
    }
    return Optional.empty();
  }

  public String toWKTString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("LINESTRING (");
    for (int i = 0; i < shape.length; i++)
    {
      sb.append(shape[i].xLon).append(" ").append(shape[i].yLat);
      if (i < shape.length - 1)
      {
        sb.append(",");
      }
    }
    sb.append(")");
    return sb.toString();
  }

  @Override
  public String toString()
  {
    return customData != null ? customData.toString() : super.toString();
  }

}
