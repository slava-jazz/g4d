package today.geojutsu.io.shp;

public class ShapeRecord
{
  public final ShapeRecordHeader recordHeader;
  protected ShapeGroupHeader groupHeader;
  protected ShapePoint [] singleShapeFeature;
  protected ShapePoint [][] multiShapeFeature;
  protected String [] semantic;

  public ShapeRecord(final ShapeRecordHeader _header)
  {
    recordHeader = _header;
  }

  public ShapeRecordHeader getRecordHeader()
  {
    return recordHeader;
  }

  public ShapeGroupHeader getGroupHeader()
  {
    return groupHeader;
  }

  public ShapePoint[] getSingleShapeFeature()
  {
    return singleShapeFeature;
  }

  public ShapePoint[][] getMultiShapeFeature()
  {
    return multiShapeFeature;
  }

  public String[] getSemantic()
  {
    return semantic;
  }

  ShapeRecord setGroupHeader(final ShapeGroupHeader _groupHeader)
  {
    groupHeader = _groupHeader;
    return this;
  }

  ShapeRecord setSingleShapeFeature(final ShapePoint[] _singleShapeFeature)
  {
    singleShapeFeature = _singleShapeFeature;
    return this;
  }

  ShapeRecord setMultiShapeFeature(final ShapePoint[][] _multiShapeFeature)
  {
    multiShapeFeature = _multiShapeFeature;
    return this;
  }

  public ShapeRecord setSemantic(final String[] _semantic)
  {
    semantic = _semantic;
    return this;
  }
}
