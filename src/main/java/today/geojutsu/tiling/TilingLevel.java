package today.geojutsu.tiling;

import today.geojutsu.G4d;
import today.geojutsu.Tools;
import today.geojutsu.match.RangeReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * This class has fixed amount of immutable instances, each represents one NDS level from 1 to 13
 * each instance provides metrics and spatial functionality for specific NDS level
 */
public class TilingLevel
{
  // see "getter" methods descriptions
  private final int ndsMortonId;
  private final int tileEdgeLength;
  private final int tileHalfEdgeLength;
  private final int tilesXQty;
  private final int tilesYQty;
  private final int totalTilesQty;
  private final int minNdsMortonTileId;
  private final int maxNdsMortonTileId;
  private final int highestOneBitMask;
  private final int decodeMask;

  public static final TilingLevel[] NDS_LEVELS = new TilingLevel[]
      {
          new TilingLevel(1),
          new TilingLevel(2),
          new TilingLevel(3),
          new TilingLevel(4),
          new TilingLevel(5),
          new TilingLevel(6),
          new TilingLevel(7),
          new TilingLevel(8),
          new TilingLevel(9),
          new TilingLevel(10),
          new TilingLevel(11),
          new TilingLevel(12),
          new TilingLevel(13),
          new TilingLevel(14),
          new TilingLevel(15)
      };
  
  public static final TilingLevel NDS_01 = NDS_LEVELS[0];
  public static final TilingLevel NDS_02 = NDS_LEVELS[1];
  public static final TilingLevel NDS_03 = NDS_LEVELS[2];
  public static final TilingLevel NDS_04 = NDS_LEVELS[3];
  public static final TilingLevel NDS_05 = NDS_LEVELS[4];
  public static final TilingLevel NDS_06 = NDS_LEVELS[5];
  public static final TilingLevel NDS_07 = NDS_LEVELS[6];
  public static final TilingLevel NDS_08 = NDS_LEVELS[7];
  public static final TilingLevel NDS_09 = NDS_LEVELS[8];
  public static final TilingLevel NDS_10 = NDS_LEVELS[9];
  public static final TilingLevel NDS_11 = NDS_LEVELS[10];
  public static final TilingLevel NDS_12 = NDS_LEVELS[11];
  public static final TilingLevel NDS_13 = NDS_LEVELS[12];
  public static final TilingLevel NDS_14 = NDS_LEVELS[13];

  public static final TilingLevel HERE_02 = NDS_01;
  public static final TilingLevel HERE_03 = NDS_02;
  public static final TilingLevel HERE_04 = NDS_03;
  public static final TilingLevel HERE_05 = NDS_04;
  public static final TilingLevel HERE_06 = NDS_05;
  public static final TilingLevel HERE_07 = NDS_06;
  public static final TilingLevel HERE_08 = NDS_07;
  public static final TilingLevel HERE_09 = NDS_08;
  public static final TilingLevel HERE_10 = NDS_09;
  public static final TilingLevel HERE_11 = NDS_10;
  public static final TilingLevel HERE_12 = NDS_11;
  public static final TilingLevel HERE_13 = NDS_12;
  public static final TilingLevel HERE_14 = NDS_13;
  public static final TilingLevel HERE_15 = NDS_14;
  public static final TilingLevel HERE_16 = NDS_LEVELS[14];
  public static final TilingLevel HMC = HERE_12;

  /**
   * provides immutable instance for asked NDS level
   * @param _nds_level_id level number from 1 to 13
   * @return NdsLevel instance or null if passed level_id not in right diapason
   */
  public static TilingLevel getFromNdsLevelNr(final int _nds_level_id)
  {
    return _nds_level_id > 0 && _nds_level_id < 16 ? NDS_LEVELS[_nds_level_id - 1] : null;
  }


  public static TilingLevel getFromHereLevelNr(final int _here_level_id)
  {
    return getFromNdsLevelNr(_here_level_id - 1);
  }

  /**
   * provides immutable instance of NDS level for specific NDS tile id
   * @param _tile_id valid NDS tile id
   * @return level of NDS tile or null if passed id is wrong
   */
  public static TilingLevel getFromNdsTileId(final int _tile_id)
  {
    TilingLevel l = null;
    switch (Integer.highestOneBit(_tile_id))
    {
      case 131072:
      {
        l = NDS_LEVELS[0];
        break;
      }
      case 262144:
      {
        l = NDS_LEVELS[1];
        break;
      }
      case 524288:
      {
        l = NDS_LEVELS[2];
        break;
      }
      case 1048576:
      {
        l = NDS_LEVELS[3];
        break;
      }
      case 2097152:
      {
        l = NDS_LEVELS[4];
        break;
      }
      case 4194304:
      {
        l = NDS_LEVELS[5];
        break;
      }
      case 8388608:
      {
        l = NDS_LEVELS[6];
        break;
      }
      case 16777216:
      {
        l = NDS_LEVELS[7];
        break;
      }
      case 33554432:
      {
        l = NDS_LEVELS[8];
        break;
      }
      case 67108864:
      {
        l = NDS_LEVELS[9];
        break;
      }
      case 134217728:
      {
        l = NDS_LEVELS[10];
        break;
      }
      case 268435456:
      {
        l = NDS_LEVELS[11];
        break;
      }
      case 536870912:
      {
        l = NDS_LEVELS[12];
        break;
      }
      case 1073741824:
      {
        l = NDS_LEVELS[13];
        break;
      }
      case -2147483648:
      {
        l = NDS_LEVELS[14];
        break;
      }
    }
    return l;
  }

  public static TilingLevel getFromHerePartitionId(final int _tile_id)
  {
    return getFromNdsTileId(Tile.buildFromHereId(_tile_id).getNdsId());
  }

  /**
   * @return level number from 1 to 13
   */
  public int getNdsMortonId()
  {
    return ndsMortonId;
  }

  /**
   * @return length of tile's edge on that level.
   * the length provided in gard scaled to integer
   * use Morton.LON_SCALE factor to get convert back
   */
  public int getTileEdgeLength()
  {
    return tileEdgeLength;
  }

  /**
   * @return half of length of tile's edge on that level.
   * the length provided in gard scaled to integer
   * use Morton.LON_SCALE factor to get convert back
   */
  public int getTileHalfEdgeLength()
  {
    return tileHalfEdgeLength;
  }

  /**
   * @return minimal tile id on that level
   * any id between min and max belongs to that level
   */
  public int getMinNdsMortonTileId()
  {
    return minNdsMortonTileId;
  }

  /**
   * @return maximal tile id on that level
   * any id between min and max belongs to that level
   */
  public int getMaxNdsMortonTileId()
  {
    return maxNdsMortonTileId;
  }

  /**
   * @return mask with raised bit "stop" bit specific for that level
   */
  public int getHighestOneBitMask()
  {
    return highestOneBitMask;
  }

  /**
   * @return amount of tiles in horizontal direction for that tile
   */
  public int getTilesXQty()
  {
    return tilesXQty;
  }

  /**
   * @return amount of tiles in vertical direction for that tile
   */
  public int getTilesYQty()
  {
    return tilesYQty;
  }

  /**
   * @return amount of tiles on that level
   */
  public int getTotalTilesQty()
  {
    return totalTilesQty;
  }

  /**
   * calculate tile id on specific position on that level
   * this method purposed to use together with getTileGridXY
   * to obtain neighborhoods of specific tile.
   *
   * @param _grid_x horizontal tile position
   * @param _grid_y vertical tile position
   * @return calculated tile id
   */
  public int getNdsTileId(final int _grid_x, final int _grid_y)
  {
    return MortonGrid.encodeGrid(_grid_x, _grid_y) | highestOneBitMask;
  }

  public Tile createTile(final int _grid_x, final int _grid_y)
  {
    return Tile.buildFromNdsTileId(getNdsTileId(_grid_x,_grid_y));
  }

  /**
   * calculated tile position on that level into provided buffer.
   * the buffer must be two (or more) elements long
   *
   * @param _tile tile on that level
   * @param _xy_reserved_buffer two integer buffer to store position
   */
  public void calculateTileGridXY(final Tile _tile, final int _xy_reserved_buffer[])
  {
    calculateTileGridXY(_tile.getNdsId(), _xy_reserved_buffer);
  }

  void calculateTileGridXY(final int _tile_id, final int _xy_reserved_buffer[])
  {
    MortonGrid.decodeGrid(decodeMask & _tile_id, _xy_reserved_buffer);
  }


  /**
   * calculate first child tile id on provided level.
   * child means a tile on some level below that level and
   * geometrically under provided tile id
   *
   * any child tile id is in between min child and max child ids
   *
   * @param _tile_id parent tile id on that level
   * @param _children_level some level below that level where located children tiles
   * @return first child tile id or Integer.MAX_VALUE if level provided not below that level
   */
  public int getMinChild(final int _tile_id, final TilingLevel _children_level)
  {
    int ret = Integer.MAX_VALUE;
    if(_children_level.ndsMortonId > ndsMortonId)
    {
      int morton = _tile_id & decodeMask;
      ret = (morton << (_children_level.ndsMortonId - ndsMortonId) * 2) | _children_level.getHighestOneBitMask();
    }
    return ret;
  }

  /**
   * calculate last child tile id on provided level.
   * child means a tile on some level below that level and
   * geometrically under provided tile id
   *
   * any child tile id is in between min child and max child ids
   *
   * @param _tile_id parent tile id on that level
   * @param _children_level some level below that level where located children tiles
   * @return first child tile id or 0 if level provided not below that level
   */
  public int getMaxChild(final int _tile_id, final TilingLevel _children_level)
  {
    int ret = 0;
    if(_children_level.ndsMortonId > ndsMortonId)
    {
      int total = 1 << (_children_level.ndsMortonId - ndsMortonId) * 2;
      ret = getMinChild(_tile_id,_children_level) + total - 1;
    }
    return ret;
  }

  /**
   * calculate geometrical parent tile id on provided level above that level
   * @param _tile_id a tile id on that level
   * @param _parent_level level, above that level, where parent is located
   * @return parent tile id or 0 if provided level not above that level
   */
  public int getParent(final int _tile_id, final TilingLevel _parent_level)
  {
    int ret = 0;
    if(_parent_level.getNdsMortonId() < ndsMortonId)
    {
      int morton = getHighestOneBitMask() ^ _tile_id;
      int shift = (ndsMortonId - _parent_level.getNdsMortonId()) << 1;
      int parent_morton = morton >>> shift;
      ret = parent_morton | _parent_level.getHighestOneBitMask();
    }
    return ret;
  }

  public Splitter getSplitter()
  {
    return new Splitter(this);
  }

  private TilingLevel(final int _ndsMortonId)
  {
    ndsMortonId = _ndsMortonId;
    tileEdgeLength = 1 << (31 - _ndsMortonId);
    tileHalfEdgeLength = tileEdgeLength / 2;
    highestOneBitMask = 1 << (16 + _ndsMortonId);
    minNdsMortonTileId = highestOneBitMask;
    maxNdsMortonTileId = (highestOneBitMask - 1) | highestOneBitMask;
    decodeMask = ~highestOneBitMask;
    tilesYQty = 1 << _ndsMortonId;
    tilesXQty = 1 << (_ndsMortonId + 1);
    totalTilesQty = 1 << ((_ndsMortonId << 1) + 1);
  }

  static public class Splitter
  {
    private final double[] buffer = new double[6];
    private final TreeSet<TiledRange> parts = new TreeSet<>(RANGE_COMPARATOR);
    private final TilingLevel level;


    private static final Comparator<TiledRange> RANGE_COMPARATOR = new Comparator<TiledRange>()
    {
      @Override
      public int compare(final TiledRange _o1, final TiledRange _o2)
      {
        return _o1.range.compareTo(_o2.range);
      }
    };

    public static class TiledRange
    {
      private final Tile tile;
      private final RangeReference.Range range;

      public TiledRange(final Tile _tile, final RangeReference.Range _range)
      {
        tile = _tile;
        range = _range;
      }

      public Tile getTile()
      {
        return tile;
      }

      public RangeReference.Range getRange()
      {
        return range;
      }

      @Override
      public String toString()
      {
        return tile.getNdsId() + " "  + range.toString();
      }
    }

    private void addPart(final Tile _tile, final RangeReference.Range _range)
    {
      parts.add(new TiledRange(_tile, _range));
    }

    private double getFocus(final double _tolerance)
    {
      double f = Double.NaN;
      double o1 = 0;
      for (TiledRange tr : parts)
      {
        if (isNotTheSame(o1, tr.range.minOffset,_tolerance))
        {
          f = 0.5 * (o1 + tr.range.minOffset);
          break;
        }
        o1 = tr.range.maxOffset;
      }
      if (Double.isNaN(f) && isNotTheSame(o1, 1,_tolerance))
      {
        f = 0.5 * (o1 + 1);
      }
      return f;
    }

    private boolean isNotTheSame(final double _d1, final double _d2, final double _tolerance)
    {
      return Math.abs(_d1 - _d2) > _tolerance;
    }

    private Splitter(final TilingLevel _level)
    {
      level = _level;
    }

    public List<TiledRange> apply(final G4d<?> _wgs_shape)
    {
      parts.clear();
      double tolerance = _wgs_shape.absolute2parametric(0.03); // use 3 cm tolerance
      double focus = getFocus(tolerance);
      while (!Double.isNaN(focus))
      {
        Tile tile = Tile.buildFromWgs(_wgs_shape.calculatePoint(focus, 0), level);
        tile.findPartsInScope(_wgs_shape, buffer).forEach(r -> addPart(tile, r));
        focus = getFocus(tolerance);
      }
      List<TiledRange> res = new ArrayList<>(parts.size());
      double o = 0;
      for(TiledRange tr : parts) // close tolerance gaps
      {
        res.add(new TiledRange(tr.tile, new RangeReference.Range(o,tr.range.maxOffset)));
        o = tr.range.maxOffset;
      }
      return res;
    }

    private void debugPrint(final G4d<?> _wgs_shape)
    {
      Tools.GeoJsonComposer cm = new Tools.GeoJsonComposer();
      double o = 0;
      for(TiledRange tr : parts)
      {
        double d = tr.range.minOffset - o;
        double l = _wgs_shape.parametric2absolute(d);
        o = tr.range.maxOffset;
        System.out.println("d=" + d + " l=" + l);
        cm.add(tr.getTile(),new Tools.GeoJsonOptions().setName(tr.toString()));
        cm.add(_wgs_shape.calculatePoint(tr.range.minOffset,0),new Tools.GeoJsonOptions().setName("" + tr.range.minOffset));
        cm.add(_wgs_shape.calculatePoint(tr.range.maxOffset,0),new Tools.GeoJsonOptions().setName("" + tr.range.maxOffset));
      }
      cm.add(_wgs_shape,new Tools.GeoJsonOptions().setName(_wgs_shape.getCustomData().toString()));

      System.out.println();
      System.out.println(cm);
      System.out.println();
    }
  }
}
