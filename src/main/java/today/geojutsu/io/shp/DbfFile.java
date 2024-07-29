

package today.geojutsu.io.shp;


import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;


// http://de.wikipedia.org/wiki/DBASE
// http://www.clicketyclick.dk/databases/xbase/format/
// http://www.dbase.com/KnowledgeBase/int/db7_file_fmt.htm
// http://ulisse.elettra.trieste.it/services/doc/dbase/DBFstruct.htm#T1
// http://www.dbf2002.com/dbf-file-format.html
// http://www.oocities.org/geoff_wass/dBASE/GaryWhite/dBASE/FAQ/qformt.htm
// http://www.digitalpreservation.gov/formats/fdd/fdd000325.shtml

public class DbfFile extends ShapeFileReader
{
  private byte type;
  private int dateYY;
  private int dateMM;
  private int dateDD;
  private int numberOfRecords;
  private int headerSize;
  private int recordSize;
  private DbfFieldHeader[] fieldHeaders;
  private String[][] records;


  public DbfFile(File file) throws IOException
  {
    super( file);
  }

  @Override
  public void read() throws Exception
  {
    bb.order(ByteOrder.LITTLE_ENDIAN);
    type = bb.get(0);
    dateYY = bb.get(1) + 1900;
    dateMM = bb.get(2);
    dateDD = bb.get(3);
    numberOfRecords = bb.getInt(4);
    headerSize = bb.getShort(8);
    recordSize = bb.getShort(10);

    int position = 32; // start of fields
    bb.position(position);

    int num_fields = (headerSize - position - 1) / DbfFieldHeader.HEADER_SIZE;
    fieldHeaders = new DbfFieldHeader[num_fields];

    for (int i = 0; i < fieldHeaders.length; i++)
    {
      fieldHeaders[i] = new DbfFieldHeader(bb, i);
      position += DbfFieldHeader.HEADER_SIZE;
      bb.position(position);
    }
    bb.position(headerSize);

    records = new String[numberOfRecords][num_fields];
    byte[] buffer = new byte[recordSize];
    for (int i = 0; i < numberOfRecords; i++)
    {
      bb.get(buffer);
      try
      {
        String record_as_string = new String(buffer, StandardCharsets.UTF_8);
        int from = 1;
        int to = 1;
        for (int j = 0; j < fieldHeaders.length; j++)
        {
          to += fieldHeaders[j].getLength();
          records[i][j] = record_as_string.substring(from, to).trim();
          from = to;
        }
      }
      catch (StringIndexOutOfBoundsException e)
      {
        e.printStackTrace();
      }
    }
  }

  public String[][] getRecords()
  {
    return records;
  }

  public DbfFieldHeader[] getFieldHeaders()
  {
    return fieldHeaders;
  }

  public byte getType()
  {
    return type;
  }

  public int getNumberOfRecords()
  {
    return numberOfRecords;
  }

  public int getHeaderSize()
  {
    return headerSize;
  }

  public int getRecordSize()
  {
    return recordSize;
  }

  public String getDate()
  {
    return String.format("%d.%d.%d", dateYY, dateMM, dateDD);
  }

}
