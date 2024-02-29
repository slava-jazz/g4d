package today.geojutsu.match;

import today.geojutsu.G4d;

/**
 * range reference based on g4d geometry
 * @param <T> custom type of the g4d geometry
 */
public class FeatureRangeReference<T> extends RangeReference<G4d<T>>
{


  /**
   * build range reference on a g4d geometry based feature
   * @param _range the range on g4d geometry based feature which is owns the RangeReference object
   * @param _target target g4d geometry based which is referenced
   * @param _targetRange the range on target feature
   * @param _fromMinToMax direction of target range
   */
  public FeatureRangeReference(final Range _range, final G4d<T> _target, final Range _targetRange, final boolean _fromMinToMax)
  {
    super(_range, _target, _targetRange, _fromMinToMax);
  }

  /**
   * build full range reference on a g4d geometry based feature
   * @param _target target g4d geometry based which is referenced
   * @param _targetRange the range on target feature
   */
  public FeatureRangeReference(final G4d<T> _target, final Range _targetRange)
  {
    super(FULL_RANGE, _target, _targetRange, true);
  }

  /**
   * extract feature range based on source sub-range and source range to align
   * @param _ar source range to align
   * @param _sr source range ti extract
   * @return extracted feature range
   */
  FeatureRangeReference<T> extractFromSourceRange(final Range _ar, final Range _sr)
  {
    double o0 = targetRange.reProjectFromPassedRange(_sr.minOffset, range, fromMinToMax); // min offset in target range
    double o1 = targetRange.reProjectFromPassedRange(_sr.maxOffset, range, fromMinToMax); // max offset in target range
    // align on target range if possible
    double tolerance = target.absolute2parametric(MatchCoreConfig.STD.rangeToleranceInMeters);
    if (fromMinToMax)
    {
      if (Math.abs(o0 - targetRange.minOffset) < tolerance)
      {
        o0 = targetRange.minOffset;
      }
      if (Math.abs(o1 - targetRange.maxOffset) < tolerance)
      {
        o1 = targetRange.maxOffset;
      }
    }
    else
    {
      if (Math.abs(o0 - targetRange.maxOffset) < tolerance)
      {
        o0 = targetRange.maxOffset;
      }
      if (Math.abs(o1 - targetRange.minOffset) < tolerance)
      {
        o1 = targetRange.minOffset;
      }
    }
    return new FeatureRangeReference<>(_ar, target, fromMinToMax ? new Range(o0, o1) : new Range(o1, o0), fromMinToMax);
  }

  /**
   * split range on given source range offset
   * @param _o source range offset
   * @return two ranges
   */
  @SuppressWarnings("unchecked")
  FeatureRangeReference<T>[] spitAt(final double _o)
  {
    double o = targetRange.reProjectFullRangeOffset(_o, fromMinToMax); // split offset in target range
    Range[] t_rr = targetRange.split(o);
    return new FeatureRangeReference[] {
        new FeatureRangeReference<>(range, target, t_rr[fromMinToMax ? 0 : 1], fromMinToMax),
        new FeatureRangeReference<>(range, target, t_rr[fromMinToMax ? 1 : 0], fromMinToMax)};
  }

  /**
   * @return target geometry exacted according target range
   */
  public G4d<T> extractGeometry()
  {
    return target.extract(targetRange.minOffset, targetRange.maxOffset, 0);
  }

  @Override
  public String toString()
  {
    return range + " to " + target.getCustomData() + " at " + targetRange;
  }
}
