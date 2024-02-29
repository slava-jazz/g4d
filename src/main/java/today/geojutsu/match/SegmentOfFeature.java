package today.geojutsu.match;

import today.geojutsu.AABB;
import today.geojutsu.G4d;

public class SegmentOfFeature<TCustom> implements Index2d.Data
{
  final G4d<TCustom>.MSegment segment;

  public SegmentOfFeature(final G4d<TCustom>.MSegment _segment)
  {
    segment = _segment;
  }

  public G4d<TCustom> getFeature()
  {
    return segment.getFeature();
  }

  @Override
  public boolean isSelectable()
  {
    return true;
  }

  @Override
  public AABB getAABB()
  {
    return segment;
  }

  @Override
  public String toString()
  {
    return "SegmentOfFeature{" +
        "segment=" + segment +
        '}';
  }
}
