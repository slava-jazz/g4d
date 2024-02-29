package today.geojutsu;

public class G4DForwardIter extends G4dIter
{
  public G4DForwardIter(final G4d<?> _geometry)
  {
    super(_geometry);
  }

  public G4DForwardIter(final G4d<?>.MSegment _segment)
  {
    super(_segment);
    position = _segment.firstEdge.firstIndex;
  }

  private G4DForwardIter(final int _position, final G4d<?> _geometry)
  {
    super(_position, _geometry);
  }

  public void reset()
  {
    position = 0;
  }

  public boolean next()
  {
    boolean ret = !isLast();
    if (ret)
    {
      position++;
    }
    return ret;
  }

  @Override
  public boolean previous()
  {
    boolean ret = position != 0;
    if (ret)
    {
      position--;
    }
    return ret;
  }

  @Override
  public int getStepsRemain()
  {
    return geometry.edges.length - position - 1;
  }

  @Override
  public double viewNextTangent()
  {
    return isLast() ? Double.NaN : geometry.edges[position + 1].tangent;
  }

  @Override
  public G4dIter clone()
  {
    return new G4DForwardIter(position, geometry);
  }

  /**
   * @return first vertex of the edge in the native digitalising order
   */
  public V4d getEdgeFirstVertex()
  {
    return geometry.edges[position].getFirstVertex();
  }

  /**
   * @return last vertex of the edge in the native digitalising order
   */
  public V4d getEdgeLastVertex()
  {
    return geometry.edges[position].getLastVertex();
  }

  public boolean isLast()
  {
    return geometry.edges[position].isLast();
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
