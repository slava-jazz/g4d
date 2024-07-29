package today.geojutsu.io.shp;

import today.geojutsu.AABB;
import today.geojutsu.AnAssociation;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;

public class Utils
{

  public static final Comparator<ShapeRecord> ORDER_BY_RECORD_NR = (_o1,_o2) ->
  {
    return _o1.recordHeader.recordNumber - _o2.recordHeader.recordNumber;
  };


  public static AnAssociation<ShapeFileHeader,ArrayList<ShapeRecord>> readShapes(final String _path) throws IOException
  {
    ArrayList<ShapeRecord> res = null;
    ByteBuffer bb = loadFile(new File(_path + ".shp"));
    ShapeFileHeader fh = ShapeFileHeader.read(bb);
    if(fh != null)
    {
      switch (fh.getType())
      {
        case Point:
        case PointZ:
        case PointM:
        {
          res = readPoints(fh.getType(),bb);
          break;
        }

        case MultiPoint:
        case MultiPointZ:
        case MultiPointM:
        {
          res = readMultiPoints(fh.getType(),bb);
          break;
        }

        case PolyLine:
        case PolyLineZ:
        case PolyLineM:
        case Polygon:
        case PolygonZ:
        case PolygonM:
        {
          res = readPolyLineAndPolygon(fh.getType(),bb);
          break;
        }

      }
    }

    try
    {
      DbfFile dbf = new DbfFile( new File(_path + ".dbf"));
      dbf.read();
      DbfFieldHeader[] dfh = dbf.getFieldHeaders();
      fh.setSemanticHeader(dfh);
      int size = Math.max(dbf.getRecords().length,res.size());
      for(int i = 0; i < size; i++)
      {
        res.get(i).setSemantic(dbf.getRecords()[i]);
      }
    }
    catch (Exception _e)
    {
      throw new RuntimeException(_e);
    }
    return new AnAssociation<>(fh,res);
  }

  public static ArrayList<ShapeRecord> readPoints(final ShapeType _type, final ByteBuffer _bb)
  {
    _bb.position(100);
    ArrayList<ShapeRecord> ret = new ArrayList<>();
    while (_bb.position() != _bb.capacity())
    {
      ShapeRecordHeader h = ShapeRecordHeader.read(_bb);
      ret.add(new ShapeRecord(h).setSingleShapeFeature(new ShapePoint [] {ShapePoint.read(_type,_bb)}));
    }
    ret.sort(ORDER_BY_RECORD_NR);
    return ret;
  }

  public static ArrayList<ShapeRecord> readMultiPoints(final ShapeType _type, final ByteBuffer _bb)
  {
    _bb.position(100);
    ArrayList<ShapeRecord> ret = new ArrayList<>();
    while (_bb.position() != _bb.capacity())
    {
      ShapeRecordHeader rh = ShapeRecordHeader.read(_bb);
      ShapeGroupHeader h = ShapeGroupHeader.read(_bb);
      ShapePoint [] p = new ShapePoint[h.getMembersQty()];
      for(int i = 0; i < p.length; i++)
      {
        p[i] = ShapePoint.read2d(_type,_bb);
      }
      if(_type.hasZ())
      {
        h.updateZRange(_bb);
        for (ShapePoint _shapePoint : p)
        {
          _shapePoint.addZ(_bb);
        }
      }
      if(_type.hasM())
      {
        h.updateMRange(_bb);
        for (ShapePoint _shapePoint : p)
        {
          _shapePoint.addM(_bb);
        }
      }
      ret.add(new ShapeRecord(rh).setGroupHeader(h).setSingleShapeFeature(p));
    }
    ret.sort(ORDER_BY_RECORD_NR);
    return ret;
  }

  public static ArrayList<ShapeRecord> readPolyLineAndPolygon(final ShapeType _type, final ByteBuffer _bb)
  {
    _bb.position(100);
    ArrayList<ShapeRecord> ret = new ArrayList<>();
    while (_bb.position() != _bb.capacity())
    {
      ShapeRecordHeader rh = ShapeRecordHeader.read(_bb);
      ShapeGroupHeader h = ShapeGroupHeader.read(_bb);
      int num_points = _bb.getInt();
      ShapePoint [][] lines = new ShapePoint[h.membersQty][];
      int start_index = _bb.getInt();
      for (int i = 1; i < h.membersQty; i++)
      {
        int next_start_index = _bb.getInt();
        int size = next_start_index - start_index;
        lines[i - 1] = new ShapePoint[size];
        start_index = next_start_index;
      }
      lines[h.membersQty - 1] = new ShapePoint[num_points - start_index];

      for (ShapePoint[] line : lines)
      {
        for (int i = 0; i < line.length; i++)
        {
          line[i] = ShapePoint.read2d(_type, _bb);
        }
      }

      if(_type.hasZ())
      {
        h.updateZRange(_bb);
        for (ShapePoint[] line : lines)
        {
          for (ShapePoint point : line)
          {
            point.addZ(_bb);
          }
        }
      }
      if(_type.hasM())
      {
        h.updateMRange(_bb);
        for (ShapePoint[] line : lines)
        {
          for (ShapePoint point : line)
          {
            point.addM(_bb);
          }
        }
      }
      ret.add(new ShapeRecord(rh).setGroupHeader(h).setMultiShapeFeature(lines));
    }
    ret.sort(ORDER_BY_RECORD_NR);
    return ret;
  }


  public static AABB readAABB(final ByteBuffer _bb)
  {
    return new AABB(_bb.getDouble(),_bb.getDouble(),_bb.getDouble(),_bb.getDouble());
  }

  public static ByteBuffer loadFile(final File _file) throws IOException
  {
    BufferedInputStream in = new BufferedInputStream(Files.newInputStream(_file.toPath()));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte [] buffer = new byte[1024];
    int read;
    while ((read = in.read(buffer,0,buffer.length)) > 0)
    {
      out.write(buffer,0,read);
    }
    in.close();
    return ByteBuffer.wrap(out.toByteArray());
  }

}
