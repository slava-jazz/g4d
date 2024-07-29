package today.geojutsu.io.shp;

import java.nio.ByteBuffer;

public enum ShapeType
{
  Null(0)
    {
      @Override
      public boolean hasZ()
      {
        return false;
      }

      @Override
      public boolean hasM()
      {
        return false;
      }

      @Override
      public boolean isPolygon()
      {
        return false;
      }

      @Override
      public boolean isPolyLine()
      {
        return false;
      }

      @Override
      public boolean isPoint()
      {
        return false;
      }

      @Override
      public boolean isMultiPoint()
      {
        return false;
      }
    },

  Point(1)
    {
      @Override
      public boolean hasZ()
      {
        return false;
      }

      @Override
      public boolean hasM()
      {
        return false;
      }

      @Override
      public boolean isPolygon()
      {
        return false;
      }

      @Override
      public boolean isPolyLine()
      {
        return false;
      }

      @Override
      public boolean isPoint()
      {
        return true;
      }

      @Override
      public boolean isMultiPoint()
      {
        return false;
      }

    },
  PolyLine(3)
    {
      @Override
      public boolean hasZ()
      {
        return false;
      }

      @Override
      public boolean hasM()
      {
        return false;
      }

      @Override
      public boolean isPolygon()
      {
        return false;
      }

      @Override
      public boolean isPolyLine()
      {
        return true;
      }

      @Override
      public boolean isPoint()
      {
        return false;
      }

      @Override
      public boolean isMultiPoint()
      {
        return false;
      }

    },
  Polygon(5)
    {
      @Override
      public boolean hasZ()
      {
        return false;
      }

      @Override
      public boolean hasM()
      {
        return false;
      }

      @Override
      public boolean isPolygon()
      {
        return true;
      }

      @Override
      public boolean isPolyLine()
      {
        return false;
      }

      @Override
      public boolean isPoint()
      {
        return false;
      }

      @Override
      public boolean isMultiPoint()
      {
        return false;
      }

    },
  MultiPoint(8)
    {
      @Override
      public boolean hasZ()
      {
        return false;
      }

      @Override
      public boolean hasM()
      {
        return false;
      }

      @Override
      public boolean isPolygon()
      {
        return false;
      }

      @Override
      public boolean isPolyLine()
      {
        return false;
      }

      @Override
      public boolean isPoint()
      {
        return false;
      }

      @Override
      public boolean isMultiPoint()
      {
        return true;
      }

    },

  PointZ(11)
    {
      @Override
      public boolean hasZ()
      {
        return true;
      }

      @Override
      public boolean hasM()
      {
        return false;
      }

      @Override
      public boolean isPolygon()
      {
        return false;
      }

      @Override
      public boolean isPolyLine()
      {
        return false;
      }

      @Override
      public boolean isPoint()
      {
        return true;
      }

      @Override
      public boolean isMultiPoint()
      {
        return false;
      }

    },
  PolyLineZ(13)
    {
      @Override
      public boolean hasZ()
      {
        return true;
      }

      @Override
      public boolean hasM()
      {
        return false;
      }

      @Override
      public boolean isPolygon()
      {
        return false;
      }

      @Override
      public boolean isPolyLine()
      {
        return true;
      }

      @Override
      public boolean isPoint()
      {
        return false;
      }

      @Override
      public boolean isMultiPoint()
      {
        return false;
      }

    },
  PolygonZ(15)
    {
      @Override
      public boolean hasZ()
      {
        return true;
      }

      @Override
      public boolean hasM()
      {
        return false;
      }

      @Override
      public boolean isPolygon()
      {
        return true;
      }

      @Override
      public boolean isPolyLine()
      {
        return false;
      }

      @Override
      public boolean isPoint()
      {
        return false;
      }

      @Override
      public boolean isMultiPoint()
      {
        return false;
      }
    },
  MultiPointZ(18)
    {
      @Override
      public boolean hasZ()
      {
        return true;
      }

      @Override
      public boolean hasM()
      {
        return false;
      }

      @Override
      public boolean isPolygon()
      {
        return false;
      }

      @Override
      public boolean isPolyLine()
      {
        return false;
      }

      @Override
      public boolean isPoint()
      {
        return false;
      }

      @Override
      public boolean isMultiPoint()
      {
        return true;
      }
    },

  PointM(21)
    {
      @Override
      public boolean hasZ()
      {
        return true;
      }

      @Override
      public boolean hasM()
      {
        return true;
      }

      @Override
      public boolean isPolygon()
      {
        return false;
      }

      @Override
      public boolean isPolyLine()
      {
        return false;
      }

      @Override
      public boolean isPoint()
      {
        return true;
      }

      @Override
      public boolean isMultiPoint()
      {
        return false;
      }
    },
  PolyLineM(23)
    {
      @Override
      public boolean hasZ()
      {
        return true;
      }

      @Override
      public boolean hasM()
      {
        return true;
      }

      @Override
      public boolean isPolygon()
      {
        return false;
      }

      @Override
      public boolean isPolyLine()
      {
        return true;
      }

      @Override
      public boolean isPoint()
      {
        return false;
      }

      @Override
      public boolean isMultiPoint()
      {
        return false;
      }

    },
  PolygonM(25)
    {
      @Override
      public boolean hasZ()
      {
        return true;
      }

      @Override
      public boolean hasM()
      {
        return true;
      }

      @Override
      public boolean isPolygon()
      {
        return true;
      }

      @Override
      public boolean isPolyLine()
      {
        return false;
      }

      @Override
      public boolean isPoint()
      {
        return false;
      }

      @Override
      public boolean isMultiPoint()
      {
        return false;
      }

    },
  MultiPointM(28)
    {
      @Override
      public boolean hasZ()
      {
        return true;
      }

      @Override
      public boolean hasM()
      {
        return true;
      }

      @Override
      public boolean isPolygon()
      {
        return false;
      }

      @Override
      public boolean isPolyLine()
      {
        return false;
      }

      @Override
      public boolean isPoint()
      {
        return false;
      }

      @Override
      public boolean isMultiPoint()
      {
        return true;
      }
    },

  MultiPatch(31)
    {
      @Override
      public boolean hasZ()
      {
        return true;
      }

      @Override
      public boolean hasM()
      {
        return true;
      }

      @Override
      public boolean isPolygon()
      {
        return false;
      }

      @Override
      public boolean isPolyLine()
      {
        return false;
      }

      @Override
      public boolean isPoint()
      {
        return false;
      }

      @Override
      public boolean isMultiPoint()
      {
        return false;
      }

    };


  public final int id;

  ShapeType(final int _id)
  {
    id = _id;
  }

  public int getId()
  {
    return id;
  }

  public static ShapeType read(final ByteBuffer _bb)
  {
    return fromInt(_bb.getInt());
  }

  public static ShapeType fromInt(final int _id)
  {
    switch (_id)
    {
      case 0: return Null;
      case 1: return Point;
      case 3: return PolyLine;
      case 5: return Polygon;
      case 8: return MultiPoint;
      case 11: return PointZ;
      case 13: return PolyLineZ;
      case 15: return PolygonZ;
      case 18: return MultiPointZ;
      case 21: return PointM;
      case 23: return PolyLineM;
      case 25: return PolygonM;
      case 28: return MultiPointM;
      case 31: return MultiPatch;
    }
    return null;
  }

  public abstract boolean hasZ();
  public abstract boolean hasM();
  public abstract boolean isPolygon();
  public abstract boolean isPolyLine();
  public abstract boolean isPoint();
  public abstract boolean isMultiPoint();

}
