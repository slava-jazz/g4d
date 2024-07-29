package today.geojutsu.io.shp;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;


public abstract class ShapeFileReader
{
  protected File file;
  protected ByteBuffer bb;


  public ShapeFileReader(final File _file) throws IOException
  {
    file = _file;
    bb = ShapeFileReader.loadFile(_file);
  }


  public abstract void read() throws Exception;

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
