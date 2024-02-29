package today.geojutsu;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tools
{
  public static class GeoJsonComposer
  {
    private final StringBuilder sb;

    public GeoJsonComposer()
    {
      sb = new StringBuilder();
    }

    public void add(final V4d _v, final GeoJsonOptions _o)
    {
      if (sb.length() != 0)
      {
        sb.append("\n,");
      }
      sb.append("{\"type\": \"Feature\", \"geometry\": {\"type\": \"Point\", \"coordinates\": [").append(_v.xLon).append(", ").append(_v.yLat)
          .append("]}");
      if (_o != null)
      {
        _o.apply(sb);
      }
      sb.append('}');
    }

    public void add(final AABB _s, final GeoJsonOptions _o)
    {
      if (sb.length() != 0)
      {
        sb.append("\n,");
      }
      sb.append("{\"type\": \"Feature\", \"geometry\": {\"type\": \"LineString\", \"coordinates\": [");
      sb.append("[").append(_s.getXMin()).append(",").append(_s.getYMin()).append("],");
      sb.append("[").append(_s.getXMin()).append(",").append(_s.getYMax()).append("],");
      sb.append("[").append(_s.getXMax()).append(",").append(_s.getYMax()).append("],");
      sb.append("[").append(_s.getXMax()).append(",").append(_s.getYMin()).append("],");
      sb.append("[").append(_s.getXMin()).append(",").append(_s.getYMin()).append("]");
      sb.append("]}");
      if (_o != null)
      {
        _o.apply(sb);
      }
      sb.append('}');
    }

    public void add(final G4d<?> _s, final GeoJsonOptions _o)
    {
      if (sb.length() != 0)
      {
        sb.append("\n,");
      }
      sb.append("{\"type\": \"Feature\", \"geometry\": {\"type\": \"LineString\", \"coordinates\": [");
      for (int i = 0; i < _s.shape.length; i++)
      {
        V4d v = _s.shape[i];
        if (i > 0)
        {
          sb.append(',');
        }
        sb.append("[").append(v.xLon).append(",").append(v.yLat).append("]");
      }
      sb.append("]}");
      if (_o != null)
      {
        _o.apply(sb);
      }
      sb.append('}');
    }

    public void add(final G4d<?>.MSegment _s, final GeoJsonOptions _o)
    {
      if (sb.length() != 0)
      {
        sb.append("\n,");
      }
      sb.append("{\"type\": \"Feature\", \"geometry\": {\"type\": \"LineString\", \"coordinates\": [");

      boolean first = true;
      for (int i = _s.firstEdge.firstIndex; i <= _s.lastEdge.firstIndex + 1; i++)
      {
        V4d v = _s.getFeature().shape[i];
        if (!first)
        {
          sb.append(',');
        }
        else
        {
          first = false;
        }
        sb.append("[").append(v.xLon).append(",").append(v.yLat).append("]");
      }
      sb.append("]}");
      if (_o != null)
      {
        _o.apply(sb);
      }
      sb.append('}');
    }

    public String get()
    {
      return "{ \"type\": \"FeatureCollection\",\n" +
          "  \"features\": [" + sb + "  ]\n" +
          "}";
    }

    @Override
    public String toString()
    {
      return get();
    }
  }

  public static class GeoJsonOptions
  {
    private String name;
    private String strokeColor;
    private String fillColor;
    private int strokeWidth;
    private final Map<String, String> keyValue;

    public GeoJsonOptions()
    {
      strokeWidth = -1;
      keyValue = new TreeMap<>();
    }

    private boolean smartComma(final StringBuilder _sb, final boolean _not_first)
    {
      if (_not_first)
      {
        _sb.append(',');
      }
      return true;
    }

    private boolean smartProperty(final StringBuilder _sb, final boolean _not_first, final String _key, final String _value)
    {
      boolean not_first = _not_first;
      if (_key != null && _value != null)
      {
        not_first = smartComma(_sb, not_first);
        _sb.append('"').append(_key).append("\":\"").append(_value).append("\"");
      }
      return not_first;
    }

    void apply(final StringBuilder _sb)
    {
      boolean not_first = false;
      if (name != null || !keyValue.isEmpty())
      {
        if (getStrokeColor() != null)
        {
          _sb.append(",\"style\":{\"color\":\"").append(getStrokeColor()).append("\"}");
        }
        if (!keyValue.isEmpty())
        {
          _sb.append(',');
          for (Map.Entry<String, String> e : keyValue.entrySet())
          {
            not_first = smartProperty(_sb, not_first, e.getKey(), e.getValue());
          }
          not_first = false;
        }
        _sb.append(",\"properties\": {");
        for (Map.Entry<String, String> e : keyValue.entrySet())
        {
          not_first = smartProperty(_sb, not_first, e.getKey(), e.getValue());
        }
        not_first = smartProperty(_sb, not_first, "name", getName());
        not_first = smartProperty(_sb, not_first, "stroke", getStrokeColor());
        not_first = smartProperty(_sb, not_first, "fill", getFillColor());
        smartProperty(_sb, not_first, "stroke-width", getStrokeWidth() == -1 ? null : "" + getStrokeWidth());
        _sb.append('}');
      }
    }

    public String getName()
    {
      return name;
    }

    public GeoJsonOptions setName(final String _name)
    {
      name = _name;
      return this;
    }

    public String getStrokeColor()
    {
      return strokeColor;
    }

    public GeoJsonOptions setStrokeColor(final String _strokeColor)
    {
      strokeColor = _strokeColor;
      return this;
    }

    public String getFillColor()
    {
      return fillColor;
    }

    public GeoJsonOptions setFillColor(final String _fillColor)
    {
      fillColor = _fillColor;
      return this;
    }

    public int getStrokeWidth()
    {
      return strokeWidth;
    }

    public GeoJsonOptions setStrokeWidth(final int _strokeWidth)
    {
      strokeWidth = _strokeWidth;
      return this;
    }

    public GeoJsonOptions addKeyValue(final String _key, final String _value)
    {
      keyValue.put(_key, _value);
      return this;
    }
  }

  private static final Pattern CONTENT_IN_BRACKETS = Pattern.compile("(?<=\\().+?(?=\\))");

  public static <T> List<G4d<T>> convertFromMULTILINESTRING(final String _in, final boolean _wgs_data, final T _custom_data)
  {
    List<G4d<T>> res = new LinkedList<>();
    if (_in.startsWith("MULTILINESTRING"))
    {
      for (Matcher lm = CONTENT_IN_BRACKETS.matcher(_in); lm.find(); )
      {
        String s = lm.group();
        res.add(G4d.build(_wgs_data ? GeoADAPTER : EclADAPTER, parseLine(s.charAt(0) == '(' ? s.substring(1) : s), _custom_data));
      }
    }
    return res;
  }

  public static <T> G4d<T> convertFromLINESTRING(final String _in, final boolean _wgs_data, final T _custom_data)
  {
    G4d<T> res = null;
    if (_in.startsWith("LINESTRING"))
    {
      Matcher lm = CONTENT_IN_BRACKETS.matcher(_in);
      if (lm.find())
      {
        res = G4d.build(_wgs_data ? GeoADAPTER : EclADAPTER, parseLine(lm.group()), _custom_data);
      }
    }
    return res;
  }

  private static ArrayList<double[]> parseLine(final String _line)
  {
    ArrayList<double[]> res = new ArrayList<>();
    for (StringTokenizer iter = new StringTokenizer(_line, ","); iter.hasMoreTokens(); )
    {
      res.add(parsePair(iter.nextToken().trim(), new double[2]));
    }
    return res;
  }

  private static double[] parsePair(final String _pair, double[] _buffer)
  {
    StringTokenizer iter = new StringTokenizer(_pair, " ");
    _buffer[0] = Double.parseDouble(iter.nextToken());
    _buffer[1] = Double.parseDouble(iter.nextToken());
    return _buffer;
  }

  public static final G4d.InputAdapter<ArrayList<double[]>> EclADAPTER = points -> {
    V4d[] shape = new V4d[points.size()];
    double total_len = 0.;
    double[] local_len = new double[shape.length];
    int index = 0;
    double[] p1 = null;
    for (int i = 0; i < local_len.length; i++)
    {
      double[] p2 = points.get(i);
      if (p1 != null)
      {
        total_len = (local_len[index] = local_len[index - 1] + Math.sqrt(Euclid.calcDistanceSq(p2[0], p2[1], p1[0], p1[1])));
      }
      p1 = p2;
      index++;
    }
    for (index = 0; index < local_len.length; index++)
    {
      double o = local_len[index] = local_len[index] / total_len;
      double[] p = points.get(index);

      shape[index] = new V4d(p[0], p[1], p.length > 2 ? p[2] : 0, o);
    }
    return new AnAssociation<>(shape, total_len);
  };

  public static final G4d.InputAdapter<ArrayList<double[]>> GeoADAPTER = points -> {
    V4d[] shape = new V4d[points.size()];
    double total_len = 0.;
    double[] local_len = new double[shape.length];
    int index = 0;
    double[] p1 = null;
    for (int i = 0; i < local_len.length; i++)
    {
      double[] p2 = points.get(i);
      if (p1 != null)
      {
        total_len = (local_len[index] = local_len[index - 1] + Geodetic.calcDistanceInMeters(p2[1], p2[0], p1[1], p1[0]));
      }
      p1 = p2;
      index++;
    }
    for (index = 0; index < local_len.length; index++)
    {
      double o = local_len[index] = local_len[index] / total_len;
      double[] p = points.get(index);

      shape[index] = new V4d(p[0], p[1], p.length > 2 ? p[2] : 0, o);
    }
    return new AnAssociation<>(shape, total_len);
  };

  public static double clamp(final double _value, final double _min, final double _max)
  {
    if (_max > _min)
    {
      if (_value < _min)
      {
        return _min;
      }
      if (_value > _max)
      {
        return _max;
      }
      return _value;
    }
    else // handle situation if borders offsets provided in reversed mode
    {
      if (_value < _max)
      {
        return _max;
      }
      if (_value > _min)
      {
        return _min;
      }
      return _value;

    }
  }


  public static boolean sameWithTolerance(final double _d1, final double _d2, final double _tolerance)
  {
    return Math.abs(_d2 - _d1) <= _tolerance;
  }




  /**
   * check is two tangent ranges overlap each other with a tolerance, a range provide as array of two doubles , first is min tangent, second - max
   * the main problem is that tangent is periodical function, so a tangent is the same with +/- PI/2
   *
   * @param _r1 range one
   * @param _r2 range two
   * @param _tolerance tolerance
   * @return true if overlap
   */
  public static boolean overlapTangentRangesWithTolerance(final double[] _r1, final double[] _r2, final double _tolerance)
  {
    double d1 = Math.max(_r1[0], _r2[0]);
    double d2 = Math.min(_r1[1], _r2[1]);
    boolean ok = d2 >= d1 || (d1 - d2) <= _tolerance;
    if (!ok) // could be match across +-Pi/2
    {
      ok = (_r1[1] > (Euclid.PI_2 - _tolerance) && _r2[0] < (-Euclid.PI_2 + _tolerance))  // maximum of r1 close to Pi/2 and minimum of r2 close to -p/2
        || (_r2[1] > (Euclid.PI_2 - _tolerance) && _r1[0] < (-Euclid.PI_2 + _tolerance)); // maximum of r2 close to Pi/2 and minimum of r1 close to -p/2
    }
    return ok;
  }


}
