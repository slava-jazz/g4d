package today.geojutsu.tiling;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MortonGridTests
{
  public static class T
  {
    public final double xLat;
    public final double yLon;
    public final double x;
    public final double y;

    public T(final double _xLat, final double _yLon, final double _x, final double _y)
    {
      xLat = _xLat;
      yLon = _yLon;
      x = _x;
      y = _y;
    }
  }

  @Test
  @DisplayName("basic transformation")  void basic()
  {
    assertEquals(MortonGrid.lon2x(0),0);
    assertEquals(MortonGrid.lat2y(0),0);
  }


}
