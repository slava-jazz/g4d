package today.geojutsu;

public abstract class G4dIter
{
  int position;
  final G4d<?> geometry;

  public G4dIter(final G4d<?> _geometry)
  {
    geometry = _geometry;
  }

  public G4dIter(final G4d<?>.MSegment _segment)
  {
    geometry = _segment.getFeature();
  }

  protected G4dIter(final int _position, final G4d<?> _geometry)
  {
    position = _position;
    geometry = _geometry;
  }

  public abstract void reset();

  public abstract boolean next();

  public abstract boolean previous();

  public abstract int getStepsRemain();

  /**
   * @return first vertex of the edge in the native digitalising order
   */
  public abstract V4d getEdgeFirstVertex();

  /**
   * @return last vertex of the edge in the native digitalising order
   */
  public abstract V4d getEdgeLastVertex();

  public double getTangent()
  {
    return geometry.edges[position].tangent;
  }

  public abstract double viewNextTangent();

  public AABB getAABB()
  {
    return geometry.edges[position];
  }

  public abstract boolean isLast();

  public abstract G4dIter clone();

  @SuppressWarnings("unchecked")
  public <T> G4d<T> getGeometry()
  {
    return (G4d<T>)geometry;
  }

  /**
   * @return absolute length of the edge
   */
  public double getLength()
  {
    return geometry.edges[position].getLength();
  }

  /**
   * linear interpolate V4d for specific offset provided as parameter
   * @param _offset paramedical offset, must be inside edge's range
   * @param _offset_tolerance tolerance to choose existing shape point
   * @return the V4d on the edge or null if passed offset is out of edge's range
   */
  public abstract V4d lerpVertex(final double _offset, final double _offset_tolerance);

  public int getPosition()
  {
    return position;
  }

  public void setPosition(final int _position)
  {
    position = _position;
  }

  @Override
  public String toString()
  {
    return "{" + position + " [" + getEdgeFirstVertex() + "; " + getEdgeLastVertex() + "], t:" + geometry.edges[position].tangent + '}';
  }

  public boolean containsOffset(final double _offset)
  {
    return geometry.shape[position].o <= _offset && geometry.shape[position + 1].o >= _offset;
  }
}
