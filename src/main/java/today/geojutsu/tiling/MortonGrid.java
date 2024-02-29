package today.geojutsu.tiling;


/**
 * morton encode/decode utility, specific for NDS world
 */
public class MortonGrid
{

  /**
   * covert scaled wgs to normal wgs
   * @param _x scaled x
   * @return longitude in grad
   */
  public static double x2lon(final int _x)
  {
    return _x/LON_SCALE;
  }

  /**
   * covert scaled wgs to normal wgs
   * @param _y scaled y
   * @return latitude in grad
   */
  public static double y2lat(final int _y)
  {
    double lat = (_y & 0x00000000ffffffffL)/LAT_SCALE;
    return lat > 90.?
          (lat > 180 ? lat -360.:
                       lat -180):
        lat;
  }

  /**
   * scale longitude to integer
   * @param _lon longitude in grad
   * @return scaled value
   */
  public static int lon2x(final double _lon)
  {
    return (int)(_lon*LON_SCALE);
  }

  /**
   * scale latitude to integer
   * @param _lat latitude in grad
   * @return scaled value
   */
  public static int lat2y(final double _lat)
  {
    return  (int)(_lat*LAT_SCALE);
  }

  /**
   * encode wgs position into NDS morton code
   * @param _x_lon longitude in grad
   * @param _y_lat latitude in grad
   * @return morton code
   */
  public static long encode(final double _x_lon, final double _y_lat)
  {
    int x = (int)(_x_lon*LON_SCALE);
    int y = (int)(_y_lat*LAT_SCALE);
    return encode(x,y);
  }

  /**
   * decode NDS morton code into wgs position in grad
   * @param _m moron code
   * @param _xlon_ylat two double array to store longitude and latitude
   */
  public static void decode(final long _m, double _xlon_ylat[])
  {
    _xlon_ylat[0] = decodeCoord(_m, LUT.MORTON_2_D_DECODE_X_256)/LON_SCALE;
    _xlon_ylat[1] = decodeCoord(_m, LUT.MORTON_2_D_DECODE_Y_256)/LAT_SCALE;
    if(_xlon_ylat[1] > 90.)
    {
      _xlon_ylat[1] -= 180.;
    }
  }

  /**
   * calculate NDS packed tile_id  on level 13 from WGS coordinates
   * @param _x_lon wgs longitude
   * @param _y_lat wgs latitude
   * @return NDS packed tile_id  on level 13
   */
  public static int getLevel13TileId(final double _x_lon, final double _y_lat)
  {
    long m = encode(_x_lon, _y_lat);
    int level_morton = (int)(m >>> 36);
    return  level_morton | 536870912;
  }

  /**
   * calculate NDS packed tile_id  on level 13 from nds coordinates
   * @param _x_lon wgs longitude
   * @param _y_lat wgs latitude
   * @return NDS packed tile_id  on level 13
   */
  public static int getLevel13TileIdFromNDS(final int _x_lon, final int _y_lat)
  {
    long m = encode(_x_lon, _y_lat);
    int level_morton = (int)(m >>> 36);
    return  level_morton | 536870912;
  }


  static long interleave(final int _x, final int _y)
  {
    return encode(_x,_y);
  }

  static void deInterleave(final long _m, int _xy[])
  {
    _xy[0] = decodeCoord(_m, LUT.MORTON_2_D_DECODE_X_256);
    _xy[1] = decodeCoord(_m, LUT.MORTON_2_D_DECODE_Y_256);
  }

  //--------------- NOT public part


  static long encode(final int _x, final int _y)
  {
    long answer = 0;
    for (int i = COORD_SIZE_IN_BYTES; i > 0; --i)
    {
      int shift = (i - 1) * 8;
      answer =
          (answer << 16 ) |
              LUT.MORTON_2_D_ENCODE_Y_256[(int)((_y >>> shift) & EIGHTBITMASK)] |
              LUT.MORTON_2_D_ENCODE_X_256[(int)((_x >>> shift) & EIGHTBITMASK)];
    }
    return answer & 0x7FFFFFFFFFFFFFFFL;
  }


  static int encodeGrid(final int _x, final int _y)
  {
    int answer = 0;
    for (int i = GRID_COORD_SIZE_IN_BYTES; i > 0; --i)
    {
      int shift = (i - 1) * 8;
      answer =
          answer << 16 |
              LUT.MORTON_2_D_ENCODE_Y_256[(_y >>> shift) & EIGHTBITMASK] |
              LUT.MORTON_2_D_ENCODE_X_256[(_x >>> shift) & EIGHTBITMASK];
    }
    return answer & 0x7FFFFFFF;
  }

  static void decodeGrid(final int m, int xy[])
  {
    xy[0] = decodeGridCoord(m, LUT.MORTON_2_D_DECODE_X_256);
    xy[1] = decodeGridCoord(m, LUT.MORTON_2_D_DECODE_Y_256);
  }



  static private int decodeGridCoord(final int _m, final byte[] _lut)
  {
    int answer = 0;
    for (int i = 0; i < GRID_MORTON_SIZE_IN_BYTES; ++i)
    {
      answer |= (_lut[(_m >>> (i * 8)) & EIGHTBITMASK] << (4 * i));
    }
    return answer;
  }

  static private int decodeCoord(final long _m, final byte[] _lut)
  {
    int answer = 0;
    for (int i = 0; i < MORTON_SIZE_IN_BYTES; ++i)
    {
      answer |= (_lut[(int)((_m >>> (i * 8)) & EIGHTBITMASK)] << (4 * i));
    }
    return answer;
  }

  private static final int EIGHTBITMASK = 0x000000FF;
  private static final int GRID_COORD_SIZE_IN_BYTES = 2;
  private static final int GRID_MORTON_SIZE_IN_BYTES = 4;
  private static final int COORD_SIZE_IN_BYTES = 4;
  private static final int MORTON_SIZE_IN_BYTES = 8;

  private static final short BITS = 31;
  public static final double LON_SCALE = (0x1L<<BITS)/180.0D;
  public static final double LAT_SCALE = (0x1L<<BITS)/180.0D;


  private static class LUT
  {
    private static final int MORTON_2_D_ENCODE_X_256[] = new int[]
        {
            0, 1, 4, 5, 16, 17, 20, 21,
            64, 65, 68, 69, 80, 81, 84, 85,
            256, 257, 260, 261, 272, 273, 276, 277,
            320, 321, 324, 325, 336, 337, 340, 341,
            1024, 1025, 1028, 1029, 1040, 1041, 1044, 1045,
            1088, 1089, 1092, 1093, 1104, 1105, 1108, 1109,
            1280, 1281, 1284, 1285, 1296, 1297, 1300, 1301,
            1344, 1345, 1348, 1349, 1360, 1361, 1364, 1365,
            4096, 4097, 4100, 4101, 4112, 4113, 4116, 4117,
            4160, 4161, 4164, 4165, 4176, 4177, 4180, 4181,
            4352, 4353, 4356, 4357, 4368, 4369, 4372, 4373,
            4416, 4417, 4420, 4421, 4432, 4433, 4436, 4437,
            5120, 5121, 5124, 5125, 5136, 5137, 5140, 5141,
            5184, 5185, 5188, 5189, 5200, 5201, 5204, 5205,
            5376, 5377, 5380, 5381, 5392, 5393, 5396, 5397,
            5440, 5441, 5444, 5445, 5456, 5457, 5460, 5461,
            16384, 16385, 16388, 16389, 16400, 16401, 16404, 16405,
            16448, 16449, 16452, 16453, 16464, 16465, 16468, 16469,
            16640, 16641, 16644, 16645, 16656, 16657, 16660, 16661,
            16704, 16705, 16708, 16709, 16720, 16721, 16724, 16725,
            17408, 17409, 17412, 17413, 17424, 17425, 17428, 17429,
            17472, 17473, 17476, 17477, 17488, 17489, 17492, 17493,
            17664, 17665, 17668, 17669, 17680, 17681, 17684, 17685,
            17728, 17729, 17732, 17733, 17744, 17745, 17748, 17749,
            20480, 20481, 20484, 20485, 20496, 20497, 20500, 20501,
            20544, 20545, 20548, 20549, 20560, 20561, 20564, 20565,
            20736, 20737, 20740, 20741, 20752, 20753, 20756, 20757,
            20800, 20801, 20804, 20805, 20816, 20817, 20820, 20821,
            21504, 21505, 21508, 21509, 21520, 21521, 21524, 21525,
            21568, 21569, 21572, 21573, 21584, 21585, 21588, 21589,
            21760, 21761, 21764, 21765, 21776, 21777, 21780, 21781,
            21824, 21825, 21828, 21829, 21840, 21841, 21844, 21845
        };

    private static final int MORTON_2_D_ENCODE_Y_256[] = new int[]
        {
            0, 2, 8, 10, 32, 34, 40, 42,
            128, 130, 136, 138, 160, 162, 168, 170,
            512, 514, 520, 522, 544, 546, 552, 554,
            640, 642, 648, 650, 672, 674, 680, 682,
            2048, 2050, 2056, 2058, 2080, 2082, 2088, 2090,
            2176, 2178, 2184, 2186, 2208, 2210, 2216, 2218,
            2560, 2562, 2568, 2570, 2592, 2594, 2600, 2602,
            2688, 2690, 2696, 2698, 2720, 2722, 2728, 2730,
            8192, 8194, 8200, 8202, 8224, 8226, 8232, 8234,
            8320, 8322, 8328, 8330, 8352, 8354, 8360, 8362,
            8704, 8706, 8712, 8714, 8736, 8738, 8744, 8746,
            8832, 8834, 8840, 8842, 8864, 8866, 8872, 8874,
            10240, 10242, 10248, 10250, 10272, 10274, 10280, 10282,
            10368, 10370, 10376, 10378, 10400, 10402, 10408, 10410,
            10752, 10754, 10760, 10762, 10784, 10786, 10792, 10794,
            10880, 10882, 10888, 10890, 10912, 10914, 10920, 10922,
            32768, 32770, 32776, 32778, 32800, 32802, 32808, 32810,
            32896, 32898, 32904, 32906, 32928, 32930, 32936, 32938,
            33280, 33282, 33288, 33290, 33312, 33314, 33320, 33322,
            33408, 33410, 33416, 33418, 33440, 33442, 33448, 33450,
            34816, 34818, 34824, 34826, 34848, 34850, 34856, 34858,
            34944, 34946, 34952, 34954, 34976, 34978, 34984, 34986,
            35328, 35330, 35336, 35338, 35360, 35362, 35368, 35370,
            35456, 35458, 35464, 35466, 35488, 35490, 35496, 35498,
            40960, 40962, 40968, 40970, 40992, 40994, 41000, 41002,
            41088, 41090, 41096, 41098, 41120, 41122, 41128, 41130,
            41472, 41474, 41480, 41482, 41504, 41506, 41512, 41514,
            41600, 41602, 41608, 41610, 41632, 41634, 41640, 41642,
            43008, 43010, 43016, 43018, 43040, 43042, 43048, 43050,
            43136, 43138, 43144, 43146, 43168, 43170, 43176, 43178,
            43520, 43522, 43528, 43530, 43552, 43554, 43560, 43562,
            43648, 43650, 43656, 43658, 43680, 43682, 43688, 43690
        };

    private static final byte MORTON_2_D_DECODE_X_256[] = new byte[]
        {
            0, 1, 0, 1, 2, 3, 2, 3, 0, 1, 0, 1, 2, 3, 2, 3,
            4, 5, 4, 5, 6, 7, 6, 7, 4, 5, 4, 5, 6, 7, 6, 7,
            0, 1, 0, 1, 2, 3, 2, 3, 0, 1, 0, 1, 2, 3, 2, 3,
            4, 5, 4, 5, 6, 7, 6, 7, 4, 5, 4, 5, 6, 7, 6, 7,
            8, 9, 8, 9, 10, 11, 10, 11, 8, 9, 8, 9, 10, 11, 10, 11,
            12, 13, 12, 13, 14, 15, 14, 15, 12, 13, 12, 13, 14, 15, 14, 15,
            8, 9, 8, 9, 10, 11, 10, 11, 8, 9, 8, 9, 10, 11, 10, 11,
            12, 13, 12, 13, 14, 15, 14, 15, 12, 13, 12, 13, 14, 15, 14, 15,
            0, 1, 0, 1, 2, 3, 2, 3, 0, 1, 0, 1, 2, 3, 2, 3,
            4, 5, 4, 5, 6, 7, 6, 7, 4, 5, 4, 5, 6, 7, 6, 7,
            0, 1, 0, 1, 2, 3, 2, 3, 0, 1, 0, 1, 2, 3, 2, 3,
            4, 5, 4, 5, 6, 7, 6, 7, 4, 5, 4, 5, 6, 7, 6, 7,
            8, 9, 8, 9, 10, 11, 10, 11, 8, 9, 8, 9, 10, 11, 10, 11,
            12, 13, 12, 13, 14, 15, 14, 15, 12, 13, 12, 13, 14, 15, 14, 15,
            8, 9, 8, 9, 10, 11, 10, 11, 8, 9, 8, 9, 10, 11, 10, 11,
            12, 13, 12, 13, 14, 15, 14, 15, 12, 13, 12, 13, 14, 15, 14, 15
        };

    private static final byte MORTON_2_D_DECODE_Y_256[] = new byte[]
        {
            0, 0, 1, 1, 0, 0, 1, 1, 2, 2, 3, 3, 2, 2, 3, 3,
            0, 0, 1, 1, 0, 0, 1, 1, 2, 2, 3, 3, 2, 2, 3, 3,
            4, 4, 5, 5, 4, 4, 5, 5, 6, 6, 7, 7, 6, 6, 7, 7,
            4, 4, 5, 5, 4, 4, 5, 5, 6, 6, 7, 7, 6, 6, 7, 7,
            0, 0, 1, 1, 0, 0, 1, 1, 2, 2, 3, 3, 2, 2, 3, 3,
            0, 0, 1, 1, 0, 0, 1, 1, 2, 2, 3, 3, 2, 2, 3, 3,
            4, 4, 5, 5, 4, 4, 5, 5, 6, 6, 7, 7, 6, 6, 7, 7,
            4, 4, 5, 5, 4, 4, 5, 5, 6, 6, 7, 7, 6, 6, 7, 7,
            8, 8, 9, 9, 8, 8, 9, 9, 10, 10, 11, 11, 10, 10, 11, 11,
            8, 8, 9, 9, 8, 8, 9, 9, 10, 10, 11, 11, 10, 10, 11, 11,
            12, 12, 13, 13, 12, 12, 13, 13, 14, 14, 15, 15, 14, 14, 15, 15,
            12, 12, 13, 13, 12, 12, 13, 13, 14, 14, 15, 15, 14, 14, 15, 15,
            8, 8, 9, 9, 8, 8, 9, 9, 10, 10, 11, 11, 10, 10, 11, 11,
            8, 8, 9, 9, 8, 8, 9, 9, 10, 10, 11, 11, 10, 10, 11, 11,
            12, 12, 13, 13, 12, 12, 13, 13, 14, 14, 15, 15, 14, 14, 15, 15,
            12, 12, 13, 13, 12, 12, 13, 13, 14, 14, 15, 15, 14, 14, 15, 15
        };

  }
}
