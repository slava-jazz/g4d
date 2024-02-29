package today.geojutsu.match;

import java.io.Serializable;
import java.util.Comparator;

/**
 * a range to range base reference on parameterized custom type T
 * @param <T>  custom type which is referenced
 */
public class RangeReference<T> implements Serializable
{

  public final Range range;  // the range on feature which is owns the RangeReference object
  public final T target;     // target object which is referenced
  public Range targetRange;  // the range on target object
  public boolean fromMinToMax; // direction of target range

  /**
   * build a RangeReference object
   * @param _range the range on feature which is owns the RangeReference object
   * @param _target target object which is referenced
   * @param _targetRange the range on target object
   * @param _fromMinToMax direction of target range
   */
  public RangeReference(final Range _range, final T _target, final Range _targetRange, final boolean _fromMinToMax)
  {
    range = _range;
    target = _target;
    targetRange = _targetRange;
    fromMinToMax = _fromMinToMax;
  }

  /**
   * split target range on two at providing offset
   * @param _o offset to split
   * @return two range references connected to eac other on split offset
   */
  @SuppressWarnings("unchecked")
  public RangeReference<T>[] splitTargetRange(final double _o)
  {
    double os = targetRange.reProjectFromPassedRange(_o, FULL_RANGE, fromMinToMax);
    return fromMinToMax ?
        new RangeReference[] {
            new RangeReference<>(range, target, new Range(targetRange.minOffset, os), true),
            new RangeReference<>(range, target, new Range(os, targetRange.maxOffset), true)
        } :
        new RangeReference[] {
            new RangeReference<>(range, target, new Range(os, targetRange.maxOffset), false),
            new RangeReference<>(range, target, new Range(targetRange.minOffset, os), false)
        };
  }

  /**
   * revere direction of target range
   */
  void revert()
  {
    fromMinToMax = !fromMinToMax;
  }

  public double target2parent(final double _target_space_offset)
  {
    return range.reProjectFromPassedRange(_target_space_offset,targetRange,fromMinToMax);
  }

  public double parent2target(final double _parent_space_offset)
  {
    return targetRange.reProjectFromPassedRange(_parent_space_offset,range,fromMinToMax);
  }
  /**
   * parametric range
   */
  public static class Range implements Comparable<Range>
  {
    public final double minOffset; // minimum offset of the range
    public final double maxOffset; // maximum offset of the range

    /**
     * build parametric range
     * @param _minOffset minimum offset of the range
     * @param _maxOffset maximum offset of the range
     */
    public Range(final double _minOffset, final double _maxOffset)
    {
      minOffset = _minOffset;
      maxOffset = _maxOffset;
    }

    /**
     * check is the range valid: between 0 and 1 and has positive size
     * @return true if the rage is valid
     */
    public boolean isValid()
    {
      return minOffset <= maxOffset && minOffset >= 0. && maxOffset <= 1.;
    }

    @Override
    public int compareTo(final Range _range)
    {
      int res = Double.compare(minOffset, _range.minOffset);
      return res == 0 ? Double.compare(_range.maxOffset, maxOffset) : res;
    }

    /**
     * @return parametric size of the range
     */
    public double size()
    {
      return maxOffset - minOffset;
    }

    /**
     * calculate overlap factor of the range with other range
     * @param _r other range
     * @return overlap factor
     */
    public double calcOverlapFactor(final Range _r)
    {
      return calcCross(_r).size() / _r.size();
    }

    /**
     * check is the other range may be counted as match based overlap factor threshold
     * @param _r range to match
     * @param overlapThreshold overlap factor match threshold
     * @return true if two ranges are matched
     */
    public boolean isMatchedByOverlapFactor(final Range _r, final double overlapThreshold)
    {
      return calcOverlapFactor(_r) >= overlapThreshold;
    }

    /**
     * check if the rage includes other range. other range may touch the range
     * @param _r other range
     * @return true if included
     */
    public boolean contains(final Range _r)
    {
      return minOffset <= _r.minOffset && maxOffset >= _r.maxOffset;
    }

    public boolean containsWithTolerance(final Range _r, Double tolerance)
    {
      return minOffset - _r.minOffset < tolerance &&
          maxOffset - _r.maxOffset > -tolerance;
    }

    /**
     * check if the range includes passed offset. the offset may touch the range
     * @param _o offset to test
     * @return true if included
     */
    public boolean contains(final double _o)
    {
      return minOffset <= _o && maxOffset >= _o;
    }

    public boolean contains(final double _o, final double _tolerance)
    {
      return (minOffset - _tolerance) <= _o && (maxOffset + _tolerance) >= _o;
    }

    public boolean containsWithoutTouch(final double _o)
    {
      return minOffset < _o && maxOffset > _o;
    }

    /**
     * check if the range overlaps other range
     * @param _r other range
     * @return true if overlaps
     */
    public boolean isOverlapped(final FeatureRangeReference.Range _r)
    {
      return Math.max(minOffset, _r.minOffset) < Math.min(maxOffset, _r.maxOffset);
    }

    /**
     * calculate new range started from 0 and having the same size as the range
     * @return same size zero based range
     */
    public Range calcAlignedTo0()
    {
      return new Range(0, size());
    }

    /**
     * split range ont two by specific offset
     * @param _o offset to split
     * @return two ranges connected on split offset
     */
    Range[] split(final double _o)
    {
      return new Range[] {new Range(minOffset, _o), new Range(_o, maxOffset)};
    }

    /**
     * check is the range touches (with tolerance) other range
     * @param _r other offset
     * @param _tolerance touch tolerance
     * @return true if touched
     */
    public boolean isTouched(final FeatureRangeReference.Range _r, final double _tolerance)
    {
      return Math.abs(minOffset - _r.maxOffset) <= _tolerance || Math.abs(maxOffset - _r.minOffset) <= _tolerance;
    }

    /**
     * calculate cross range for the range and other range
     * @param _r other range
     * @return cross range
     */
    public Range calcCross(final Range _r)
    {
      return new Range(Math.max(minOffset, _r.minOffset), Math.min(maxOffset, _r.maxOffset));
    }

    /**
     * calculate minimal union range includes the range and other range
     * @param _r other range
     * @return minimal union range
     */
    public Range calcSumma(final FeatureRangeReference.Range _r)
    {
      return new Range(Math.min(minOffset, _r.minOffset), Math.max(maxOffset, _r.maxOffset));
    }

    /**
     * check is the range [0, 1]
     * @return true if [0, 1]
     */
    public boolean isFullRange()
    {
      return minOffset == 0. && maxOffset == 1.;
    }

    /**
     * recalculate an offset in other range to corresponded offset in the range
     * @param _o other offset
     * @param _r other range
     * @param _same_direction do other range and the range having same direction
     * @return corresponded offset in that range
     */
    public double reProjectFromPassedRange(final double _o, final Range _r, boolean _same_direction)
    {
      double factor = size() / _r.size();
      double ret = _same_direction ? minOffset + (_o - _r.minOffset) * factor : maxOffset - (_o - _r.minOffset) * factor;
      return ret;
    }

    /**
     * recalculate an offset in  full range to corresponded offset in the range
     * @param _o offset in full range
     * @param _same_direction do full range and the range having same direction
     * @return corresponded offset in that range
     */
    public double reProjectFullRangeOffset(final double _o, boolean _same_direction)
    {
      return _same_direction ? minOffset + _o * size() : maxOffset - _o * size();
    }

    public boolean isOverlapped(final double _min, final double _max)
    {
      return Math.max(minOffset, _min) < Math.min(maxOffset, _max);
    }

    @Override
    public String toString()
    {
      return "[" + minOffset + ", " + maxOffset + "]";
    }
  }

  public static class RangeComparator implements Comparator<Range>
  {
    private final double tolerance;

    public RangeComparator(final double _tolerance)
    {
      tolerance = _tolerance;
    }

    @Override
    public int compare(final Range _r1, final Range _r2)
    {
      int res = PairMatcher.compareWithTolerance(_r1.minOffset, _r2.minOffset, tolerance);
      return res == 0 ? PairMatcher.compareWithTolerance(_r1.maxOffset, _r2.maxOffset, tolerance) : res;
    }
  }

  public static final Range FULL_RANGE = new Range(0, 1);

}

