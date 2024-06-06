package today.geojutsu;

import java.util.ArrayList;

/**
 * Visvalingam-Whyatt shape simplification
 * see https://martinfleischmann.net/line-simplification-algorithms/
 */
public class VWSimplifier
{

  /**
   * @param _feature feature to simplify
   * @param _tolerance minimal area of a vertex base triangle
   * @return simplified geometry
   */
  public static <T> G4d<T> apply(final G4d<T> _feature, double _tolerance)
  {
    G4d<T> result = _feature;
    if (_feature.shape.length > 2)
    {
      int initial_size = _feature.shape.length - 2;
      ArrayList<Triangle> triangles = new ArrayList<>(initial_size);
      int index_to_remove = -1; // index of the triangle with minimal area, less than tolerance passed
      for (int i = 0; i < initial_size; i++)
      {
        Triangle t = new Triangle(i, i + 1, i + 2, _feature.shape);
        triangles.add(t);
        if (t.area <= _tolerance && (index_to_remove == -1 || triangles.get(index_to_remove).area > t.area))
        {
          index_to_remove = i;
        }
      }

      //todo: optimize me
      while (index_to_remove != -1 && !triangles.isEmpty()) // remove vertexes if theirs triangle's area less than tolerance passed
      {
        // re-calculate triangle
        if (index_to_remove > 0)
        {
          triangles.get(index_to_remove - 1).nextIndex = triangles.get(index_to_remove).nextIndex;
          triangles.get(index_to_remove - 1).reCalcArea(_feature.shape);
        }
        if (index_to_remove < triangles.size() - 1)
        {
          triangles.get(index_to_remove + 1).prevIndex = triangles.get(index_to_remove).prevIndex;
          triangles.get(index_to_remove + 1).reCalcArea(_feature.shape);
        }
        triangles.remove(index_to_remove);

        // look do we have more vertexes to remove
        index_to_remove = -1;
        for (int i = 0; i < triangles.size(); i++)
        {
          Triangle t = triangles.get(i);
          if (t.area <= _tolerance && (index_to_remove == -1 || triangles.get(index_to_remove).area > t.area))
          {
            index_to_remove = i;
          }
        }
      }

      if (triangles.size() < initial_size) // the shape has been simplified, need to re-create feature
      {
        final V4d[] g = new V4d[triangles.size() + 2];
        g[0] = _feature.shape[0];
        int i = 1;
        for (Triangle t : triangles)
        {
          g[i++] = _feature.shape[t.currentIndex];
        }
        g[g.length - 1] = _feature.shape[_feature.shape.length - 1];
        result = G4d.build(_feature.length, g, _feature.getCustomData());
      }
    }
    return result;
  }

  private static class Triangle
  {
    private int prevIndex;
    private final int currentIndex;
    private int nextIndex;
    private double area;

    private Triangle(final int _prevIndex, final int _currentIndex, final int _nextIndex, final V4d[] _g)
    {
      prevIndex = _prevIndex;
      currentIndex = _currentIndex;
      nextIndex = _nextIndex;
      area = Euclid.area2d(_g[_prevIndex], _g[currentIndex], _g[_nextIndex]);
    }

    private void reCalcArea(final V4d[] _g)
    {
      area = Euclid.area2d(_g[prevIndex], _g[currentIndex], _g[nextIndex]);
    }
  }

}
