package today.geojutsu;

public class G4DBackwardIter extends G4dIter
{
  public G4DBackwardIter(final G4d<?> _geometry)
  {
    super(_geometry);
  }

  public G4DBackwardIter(final G4d<?>.MSegment _segment)
  {
    super(_segment);
    position = _segment.lastEdge.firstIndex;
  }

  private G4DBackwardIter(final int _position, final G4d<?> _geometry)
  {
    super(_position, _geometry);
  }

  public void reset()
  {
    position = geometry.edges.length - 1;
  }

  public boolean next()
  {
    boolean ret = !isLast();
    if (ret)
    {
      position--;
    }
    return ret;
  }

  @Override
  public boolean previous()
  {
    boolean ret = position < (geometry.edges.length - 1);
    if (ret)
    {
      position++;
    }
    return ret;
  }

  @Override
  public int getStepsRemain()
  {
    return position;
  }

  @Override
  public double viewNextTangent()
  {
    return isLast() ? Double.NaN : geometry.edges[position - 1].tangent;
  }

  @Override
  public G4dIter clone()
  {
    return new G4DBackwardIter(position, geometry);
  }

  /**
   * @return first vertex of the edge in the native digitalising order
   */
  public V4d getEdgeFirstVertex()
  {
    return geometry.edges[position].getLastVertex();
  }

  /**
   * @return last vertex of the edge in the native digitalising order
   */
  public V4d getEdgeLastVertex()
  {
    return geometry.edges[position].getFirstVertex();
  }

  public boolean isLast()
  {
    return position == 0;
  }

  /**
   * linear interpolate V4d for specific offset provided as parameter
   * @param _offset paramedical offset, must be inside edge's range
   * @param _offset_tolerance tolerance to choose existing shape point
   * @return the V4d on the edge or null if passed offset is out of edge's range
   */
  public V4d lerpVertex(final double _offset, final double _offset_tolerance)
  {
    return geometry.edges[position].lerpVertex(_offset, _offset_tolerance);
  }

}
