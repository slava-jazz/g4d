package today.geojutsu.match;

import today.geojutsu.AnAssociation;
import today.geojutsu.G4d;
import today.geojutsu.V4d;

import java.util.LinkedList;
import java.util.List;

public class PointProjectionReferences<TCustom>
{
  final V4d thePoint;
  final List<AnAssociation<V4d, G4d<TCustom>>> projections  = new LinkedList<>();

  public PointProjectionReferences(final V4d _thePoint)
  {
    thePoint = _thePoint;
  }

  public V4d getThePoint()
  {
    return thePoint;
  }

  public List<AnAssociation<V4d, G4d<TCustom>>> getProjections()
  {
    return projections;
  }
}
