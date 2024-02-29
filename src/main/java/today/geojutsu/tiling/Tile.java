package today.geojutsu.tiling;

import today.geojutsu.AABB;
import today.geojutsu.V4d;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * represents specific NDS tile and provide metrical information and simply spatial information
 */
public class Tile extends AABB
{
  private final TilingLevel level;
  private final int ndsId;
  private final int shortMorton;
  private final int [] gridPosition;
  private final int [] mbr;
  private final int [] center;

  public static Tile buildFromWgs(final V4d _v, final TilingLevel _level)
  {
    return buildFromMortonCode(MortonGrid.encode(_v.xLon,_v.yLat),_level);
  }

  /**
   * build instance for given NDS tile id
   *
   * @param _tile_id NDS tile id
   * @return NdsTile object
   */
  public static Tile buildFromNdsTileId(final int _tile_id)
  {
    TilingLevel level = TilingLevel.getFromNdsTileId(_tile_id);
    return new Tile(level,_tile_id);
  }

  /**
   * build instance of tile on specific position in specific level
   *
   * @param _level NDS level where position is asked
   * @param _x_grid horizontal position of tile
   * @param _y_grid vertical position of tile
   * @return NdsTile object or null if grid position does not valid for provided level
   */
  public static Tile buildFromGridPosition(final TilingLevel _level, final int _x_grid, final int _y_grid)
  {
    int x = _x_grid%_level.getTilesXQty();
    int y = _y_grid%_level.getTilesYQty();
    if(x < 0)
    {
      x =  _level.getTilesXQty() + x;
    }
    if(y < 0)
    {
      y =  _level.getTilesYQty() + y;
    }
    return new Tile(_level,_level.getNdsTileId(x,y));
  }

  /**
   * build instance (on specific NDS level) which contains geographical position coded according morton rule
   *
   * @param _m morton code of specific position (see @Morton.encode method to get it)
   * @param _level NDS level where tile will calculated
   * @return NdsTile object
   */
  public static Tile buildFromMortonCode(final long _m, final TilingLevel _level)
  {
    int shift = (31 - _level.getNdsMortonId()) << 1;
    int level_morton = (int)(_m >>> shift);
    int id = level_morton | _level.getHighestOneBitMask();
    return buildFromNdsTileId(id);
  }

  /**
   * Create an instance of {@link Tile} from an HAD partition id.
   *
   * @param _here_id an HAD partition id.
   * @return an instance of {@link Tile}.
   */
  public static Tile buildFromHereId(final int _here_id)
  {
    int levelId = levelIdFromHereId(_here_id);
    return buildFromNdsTileId((1 << 16 + levelId) + (11 << (levelId << 1) - 1 ^ _here_id));
  }

  private static int levelIdFromHereId(int hadPartitionId)
  {
    int hadLevelId = 0;
    while (hadPartitionId > 1)
    {
      hadLevelId++;
      hadPartitionId >>= 2;
    }
    return hadLevelId - 1;
  }

  /**
   * @return NDS level of that tile
   */
  public TilingLevel getLevel()
  {
    return level;
  }

  /**
   * @return NDS tile id of that tile
   */
  public int getNdsId()
  {
    return ndsId;
  }

  /**
   * @return short version of morton code of that tile.
   * short version is always depend on NDS level and in fact represent the tile number on that level.
   */
  public int getShortMorton()
  {
    return shortMorton;
  }

  /**
   * @return two integers array: x,y - position of that tile on it's level
   */
  public int[] getGridPosition()
  {
    return gridPosition;
  }

  /**
   * @return four integers array: x_min,y_min, x_max,y_max - border of that tile in WGS, scaled to integer.
   * please use Morton.x2lon or @Morton.y2lat to convert it back to WGS
   */
  public int[] getMbrNDS()
  {
    return mbr;
  }

  /**
   * @return two integers array: x,y - center of that tile in WGS, scaled to integer.
   * please use Morton.x2lon or @Morton.y2lat to convert it back to WGS
   */
  public int[] getCenterNDS()
  {
    return center;
  }

  public V4d getCenterWGS()
  {
    return new V4d(MortonGrid.x2lon(center[0]), MortonGrid.y2lat(center[1]));
  }

  /**
   * @return nearby tile from left
   */
  public Tile getWestTile()
  {
    return buildFromGridPosition(level,getLeftPosition(gridPosition[0]), gridPosition[1]);
  }

  /**
   * @return nearby tile from right
   */
  public Tile getEastTile()
  {
    return buildFromGridPosition(level,getRightPosition(gridPosition[0]), gridPosition[1]);
  }

  /**
   * @return nearby tile from top
   */
  public Tile getNordTile()
  {
    return buildFromGridPosition(level,gridPosition[0], gridPosition[1] + 1);
  }

  /**
   * @return nearby tile from bottom
   */
  public Tile getSouthTile()
  {
    return buildFromGridPosition(level,gridPosition[0], gridPosition[1] - 1);
  }

  /**
   * @return nearby tile from left-top
   */
  public Tile getNordWestTile()
  {
    return buildFromGridPosition(level,getLeftPosition(gridPosition[0]), gridPosition[1] + 1);
  }

  /**
   * @return nearby tile from right-top
   */
  public Tile getNordEastTile()
  {
    return buildFromGridPosition(level,getRightPosition(gridPosition[0]), gridPosition[1] + 1);
  }

  /**
   * @return nearby tile from left-bottom
   */
  public Tile getSouthWestTile()
  {
    return buildFromGridPosition(level,getLeftPosition(gridPosition[0]), gridPosition[1] - 1);
  }

  public Tile[] getTilesAround()
  {
    return new Tile[]
        {
            getNordTile(),
            getNordEastTile(),
            getEastTile(),
            getSouthEastTile(),
            getSouthTile(),
            getSouthWestTile(),
            getWestTile(),
            getNordWestTile()
       };
  }
  /**
   * @return nearby tile from right bottom
   */
  public Tile getSouthEastTile()
  {
    return buildFromGridPosition(level,getRightPosition(gridPosition[0]), gridPosition[1] - 1);
  }

  /**
   * check if given position in scaled to integer WGS is inside that tile
   * use Morton.lon2x and @Morton.lat2y to convert
   * @param _x position in scaled to integer WGS
   * @param _y position in scaled to integer WGS
   * @return true if provided position inside or on border
   */
  public boolean contains(final int _x, final int _y)
  {
    return mbr[0] <= _x && mbr[2] > _x && mbr[1] <= _y && mbr[3] > _y; // max NOT included
  }

  /**
   * check if given rectangle fully inside that tile.
   * coordinates of passed rectangle must be provided in in scaled to integer WGS
   * use Morton.lon2x and @Morton.lat2y to convert
   * @param _x1 upper left corner
   * @param _y1 upper left corner
   * @param _x2 bottom right corner
   * @param _y2 bottom right corner
   * @return true if rectangle fully inside that tile
   */
  public boolean contains(final int _x1, final int _y1, final int _x2, final int _y2)
  {
    return contains(_x1,_y1) && contains(_x2,_y2);
  }

  /**
   * check if passed tile fully inside that tile
   * @param _tm tile to check
   * @return true if included
   */
  public boolean contains(final Tile _tm)
  {
    return contains(_tm.mbr[0],_tm.mbr[1],_tm.mbr[2],_tm.mbr[3]);
  }

  /**
   * iterate all geometrical children on passed level
   * @param _level NDS level where children are asked. must be below level of that tile
   * @return iterable to iterate children
   */
  public Iterable<Tile> getChildren(final TilingLevel _level)
  {
    return new ChildIterable(level.getMinChild(ndsId, _level), level.getMaxChild(ndsId, _level));
  }

  /**
   * find geometrical parent of that tile on specific level
   * @param _level NDS level above level of that tile
   * @return parent tile
   */
  public Tile getParent(final TilingLevel _level)
  {
    int parent_id = level.getParent(ndsId, _level);
    return parent_id != -1 ? Tile.buildFromNdsTileId(parent_id) : null;
  }

  /**
   * Get the HAD partition id of the current {@link Tile} instance.
   *
   * @return the HAD partition id.
   */
  public int getHereId()
  {
    return 11 << (level.getNdsMortonId() << 1) - 1 ^ shortMorton;
  }

  private Tile(final TilingLevel _level, final int _id)
  {
    level = _level;
    ndsId = _id;
    shortMorton = _level.getHighestOneBitMask() ^ _id;
    gridPosition = new int[2];
    mbr = new int[4];
    center = new int[2];
    _level.calculateTileGridXY(ndsId,gridPosition);
    calcTileMbrAndCenter();
    reset(new AABB(MortonGrid.x2lon(mbr[0]), MortonGrid.y2lat(mbr[1]), MortonGrid.x2lon(mbr[2]), MortonGrid.y2lat(mbr[3])));
  }


  private void calcTileMbrAndCenter()
  {
    int shift = 31 - level.getNdsMortonId();
    mbr[0] = gridPosition[0] << shift;
    mbr[1] = gridPosition[1] << shift;
    int l = level.getTileEdgeLength();
    mbr[2] = mbr[0] + l;
    mbr[3] = (mbr[1] + l);
    center[0] = mbr[0] + l/2;
    center[1] = mbr[1] + l/2;
    mbr[1] = MortonGrid.lat2y(MortonGrid.y2lat(mbr[1]));
    mbr[3] = MortonGrid.lat2y(MortonGrid.y2lat(mbr[3]));
    center[1] = MortonGrid.lat2y(MortonGrid.y2lat(center[1]));
  }



  private static class ChildIterable implements Iterable<Tile>
  {
    private final int minCh;
    private final int maxCh;

    private ChildIterable(final int _minCh, final int _maxCh)
    {
      minCh = _minCh;
      maxCh = _maxCh;
    }

    @Override
    public Iterator<Tile> iterator()
    {
      return new Iterator<Tile>()
      {
        int ch = minCh;

        @Override
        public boolean hasNext()
        {
          return ch <= maxCh;
        }

        @Override
        public Tile next()
        {
          return ch <= maxCh ? Tile.buildFromNdsTileId(ch++) : null;
        }
      };
    }

    @Override
    public void forEach(final Consumer<? super Tile> _action)
    {
      for (int ch = minCh; ch <= maxCh; ch++)
      {
        _action.accept(Tile.buildFromNdsTileId(ch));
      }
    }

    @Override
    public Spliterator<Tile> spliterator()
    {
      return new SplitChildIterator(minCh, maxCh);
    }
  }

  private static class SplitChildIterator implements Spliterator<Tile>
  {
    private int maxCh;
    private int ch;

    private SplitChildIterator(final int _minCh, final int _maxCh)
    {
      maxCh = _maxCh;
      ch = _minCh;
    }

    @Override
    public boolean tryAdvance(final Consumer<? super Tile> _action)
    {
      boolean ok = ch <= maxCh;
      if (ok)
      {
        _action.accept(Tile.buildFromNdsTileId(ch++));
      }
      return ok;
    }

    @Override
    public Spliterator<Tile> trySplit()
    {
      SplitChildIterator res = null;
      int half = (maxCh + ch) / 2;
      if (ch < maxCh && half < maxCh)
      {
        res = new SplitChildIterator(ch, half);
        ch = half + 1;
      }
      return res;
    }

    @Override
    public long estimateSize()
    {
      return ch <= maxCh ? maxCh - ch + 1 : 0;
    }

    @Override
    public int characteristics()
    {
      return ORDERED | SIZED | IMMUTABLE | SUBSIZED;
    }

  }

  private int getLeftPosition(int positionX)
  {
    return (positionX - 1 + level.getTilesXQty()) % level.getTilesXQty();
  }

  private int getRightPosition(int positionX)
  {
    return (positionX + 1) % level.getTilesXQty();
  }
}
