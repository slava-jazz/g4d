package today.geojutsu.match;

import com.oj.g4d.*;
import today.geojutsu.*;

import java.util.*;

public class PolyLineBasicMatcher<TCustom extends Comparable<TCustom>>
{
  private final Index2d<SegmentOfFeature<TCustom>> i2d;
  private final MatchCoreConfig cfg;

  public PolyLineBasicMatcher(final MatchCoreConfig _cfg)
  {
    i2d = new RTree<>(new RTree.Options(_cfg.maxChildren));
    cfg = _cfg;
  }

  public static <TCustom extends Comparable<TCustom>> PolyLineBasicMatcher<TCustom> buildDefault(final Collection<G4d<TCustom>> _scope,
      final MatchCoreConfig _cfg)
  {
    PolyLineBasicMatcher<TCustom> pp = new PolyLineBasicMatcher<>(_cfg);
    for (G4d<TCustom> g : _scope)
    {
      g.addTolerance(_cfg.xLonTolerance, _cfg.yLatTolerance);
      for (G4d<TCustom>.MSegment s : g.getSegments())
      {
        pp.i2d.insert(new SegmentOfFeature<>(s));
      }
    }
    return pp;
  }

  public Collection<AnAssociation<G4d<TCustom>, Collection<FeatureRangeReference<TCustom>>>> match(final Collection<G4d<TCustom>> _scope)
  {
    double[] d2b = new double[2]; // temporary buffer two double long
    Collection<AnAssociation<G4d<TCustom>, Collection<FeatureRangeReference<TCustom>>>> res = new LinkedList<>();
    for (G4d<TCustom> f : _scope)
    {
      AnAssociation<G4d<TCustom>, Collection<FeatureRangeReference<TCustom>>> match = new AnAssociation<>(f, new LinkedList<>());
      for (List<FeatureRangeReference<TCustom>> pairs : matchFeature(f, d2b).values())
      {
        match.b.addAll(pairs);
      }
      if (!match.b.isEmpty())
      {
        res.add(match);
      }
    }
    return res;
  }

  public <T extends Comparable<T>> NavigableMap<T,PointProjectionReferences<TCustom>> findProjections(final NavigableMap<V4d,T> _scope, final double _x_lon_tolerance, final double _y_lat_tolerance)
  {
    AABB hot_spot = new AABB();
    double [] buffer = new double[2];
    TreeMap<T,PointProjectionReferences<TCustom>> res = new TreeMap<>();
    for (Map.Entry<V4d,T> e : _scope.entrySet())
    {
      PointProjectionReferences<TCustom> ppr = null;
      for(SegmentOfFeature<TCustom> seg : i2d.select(hot_spot.reset(e.getKey()).surroundBy(_x_lon_tolerance,_y_lat_tolerance)))
      {
          V4d p = seg.segment.findProjection(e.getKey(),hot_spot,buffer);
          if(p != null)
          {
            if(ppr == null)
            {
              ppr = new PointProjectionReferences<>(e.getKey());
            }
            ppr.projections.add(new AnAssociation<>(p,seg.getFeature()));
          }
      }
      if(ppr != null)
      {
        res.put(e.getValue(),ppr);
      }
    }
    return res;
  }

  private TreeMap<RangeReference.Range, List<FeatureRangeReference<TCustom>>> matchFeature
      (
          final G4d<TCustom> _feature,
          final double[] _d2b
      )
  {
    // matched coverage per feature
    TreeMap<TCustom, TreeSet<FeatureRangeReference<TCustom>>> coverage = new TreeMap<>();

    // matched ranges for the passed feature
    TreeMap<TCustom, FeatureRangeReference<TCustom>> matched_ranges = new TreeMap<>();

    // matched ranges fo a candidate
    TreeSet<FeatureRangeReference<TCustom>> candidate_distribution = new TreeSet<>(FEATURE_REFERENCE_COMPARATOR);
    TCustom candidate_feature = null; // current candidate

    for (G4d<TCustom>.MSegment segment : _feature.getSegments()) // go along feature segment by segment
    {
      Collection<SegmentOfFeature<TCustom>> candidates = orderByFeatureAndRange(i2d.select(segment)); // monotonic segments which can match or cross
      FeatureRangeReference<TCustom> over_0_match = null; // this range is used to prevent match through 0/1 offset, like [0.3, 1.0, 0.1]
      // that must be matched as two ranges: [0.3, 1.0] and [0.0, 0.1]
      for (SegmentOfFeature<TCustom> candidate : candidates) // candidate to make a pair
      {
        if (candidate.getFeature().getCustomData() != candidate_feature) // the new candidate
        {
          over_0_match = null; // initialize(clear) through 0/1 offset condition
          // last match with the old current candidate
          FeatureRangeReference<TCustom> last_match = takeLastMatchAndUpdateCoverage(coverage, candidate_distribution);
          if (last_match != null)
          {
            // update matched range
            matched_ranges.put(last_match.target.getCustomData(), last_match);
          }

          candidate_feature = candidate.getFeature().getCustomData(); // set the new candidate feature
          candidate_distribution = new TreeSet<>(FEATURE_REFERENCE_COMPARATOR); // sort by "start offset" and "widest range"
        }
        FeatureRangeReference<TCustom> last_match = matched_ranges.get(candidate.getFeature().getCustomData());
        if (isRangeMatched(last_match, segment, candidate.segment))
        {
          continue; // feature matched already in that range
        }
        AABB cross = null;
        // TODO? We seemingly need to consider z-levels by matching
        if (
            Tools.overlapTangentRangesWithTolerance(segment.getTangentRange(), candidate.segment.getTangentRange(),
                cfg.tangentTolerance)
                && (cross = segment.calcCross(candidate.getAABB(), 0, 0)) != null
                && !cross.isPoint()
        )
        {
          // the candidate to match
          for(PairMatcher.MatchingContext pre_match : PairMatcher.syncEdgesAndMatchFirstPoint(segment, candidate.segment, cfg, _d2b))
          {
            if(over_0_match != null && over_0_match.range.contains(pre_match.sample_start_offset))
            {
              // match is going through 0/1 offset. break it!
              continue;
            }
            FeatureRangeReference<TCustom> ref = PairMatcher.matchTail(pre_match, cfg, _d2b);
            if (ref != null)
            {
              if((ref.fromMinToMax && ref.targetRange.maxOffset == 1.0) || (!ref.fromMinToMax && ref.targetRange.minOffset == 0.))
              {
                over_0_match = ref; // prevent possible match through 0/1 offset
              }
              double len = ref.range.size() * _feature.getLength();
              if (len > cfg.minimalLinkLengthToBeShared)
              {
                candidate_distribution.add(ref);
              }
            }
          }
        }
      }
      takeLastMatchAndUpdateCoverage(coverage, candidate_distribution);
    }

    // let's prepare pair
    // the key is a range on source feature, the value collection of matches with other features for that range
    TreeMap<RangeReference.Range, List<FeatureRangeReference<TCustom>>> pairs = new TreeMap<>();
    for (Map.Entry<TCustom, TreeSet<FeatureRangeReference<TCustom>>> e : coverage.entrySet())
    {
      RangeReference.Range prev_range = null;
      for (FeatureRangeReference<TCustom> ref : e.getValue())
      {
        if (prev_range == null || !prev_range.contains(ref.range))
        {
          pairs.computeIfAbsent(ref.range, _r -> new LinkedList<>()).add(ref);
          prev_range = ref.range;
        }
      }
    }
    return pairs;
  }

  /**
   * take last match between source and target feature + update match coverage on new target provided
   * @param _coverage found matches between source feature and others
   * @param _targets  last matches on specific target
   * @return latest match on specific target in source feature direction.
   */
  private FeatureRangeReference<TCustom> takeLastMatchAndUpdateCoverage(
      final TreeMap<TCustom, TreeSet<FeatureRangeReference<TCustom>>> _coverage,
      final TreeSet<FeatureRangeReference<TCustom>> _targets
  )
  {
    FeatureRangeReference<TCustom> ref = null;
    if (!_targets.isEmpty())
    {
      TreeSet<FeatureRangeReference<TCustom>> feature_coverage = null;
      RangeReference.Range best_matched_range = null;
      for (FeatureRangeReference<TCustom> r : _targets)
      {
        if (feature_coverage == null)
        {
          feature_coverage = _coverage.computeIfAbsent(r.target.getCustomData(), _r -> new TreeSet<>(FEATURE_REFERENCE_COMPARATOR));
        }
        if (best_matched_range == null || !best_matched_range.contains(r.range)) // there are several not crossed matches possible
        {
          feature_coverage.add(r);
          best_matched_range = r.range;
          ref = r;
        }
      }
    }
    return ref;
  }

  /**
   * order list of candidates first by feature, second by range
   * @param _candidates  list of candidates
   * @return ordered list
   */
  private TreeSet<SegmentOfFeature<TCustom>> orderByFeatureAndRange(final Collection<SegmentOfFeature<TCustom>> _candidates)
  {
    TreeSet<SegmentOfFeature<TCustom>> ret = new TreeSet<>(SEGMENT_OF_FEATURE_COMPARATOR);
    ret.addAll(_candidates);
    return ret;
  }

  /**
   * check is the range of the feature already matched before
   *
   * @param _last_match  last matched pair
   * @param _source source segment
   * @param _target target segment
   * @return true if source segment or target segment  has overlap with previous match
   */
  private static boolean isRangeMatched(final FeatureRangeReference<?> _last_match, G4d<?>.MSegment _source, G4d<?>.MSegment _target)
  {
    boolean matched = _last_match != null;
    if (matched) // could be matched...
    {
      boolean source_matched = _last_match.range.contains(_source.getFirstEdge().getFirstVertex().o);

      boolean target_matched = _last_match.fromMinToMax ?
          _last_match.targetRange.contains(_target.getFirstEdge().getFirstVertex().o)
          :
          _last_match.targetRange.contains(_target.getLastEdge().getLastVertex().o);

      matched = source_matched && target_matched;
    }
    return matched;
  }

  private final Comparator<FeatureRangeReference<TCustom>> FEATURE_REFERENCE_COMPARATOR = new Comparator<FeatureRangeReference<TCustom>>()
  {
    @Override
    public int compare(final FeatureRangeReference<TCustom> _o1, final FeatureRangeReference<TCustom> _o2)
    {
      return _o1.range.compareTo(_o2.range);
    }
  };

  private final Comparator<SegmentOfFeature<TCustom>> SEGMENT_OF_FEATURE_COMPARATOR = new Comparator<SegmentOfFeature<TCustom>>()
  {
    @Override
    public int compare(final SegmentOfFeature<TCustom> _o1, final SegmentOfFeature<TCustom> _o2)
    {
      int res = _o1.segment.getFeature().getCustomData().compareTo(_o2.segment.getFeature().getCustomData());
      if (res == 0)
      {
        res = Double.compare(_o1.segment.getFirstEdge().getFirstVertex().o, _o2.segment.getFirstEdge().getFirstVertex().o);
      }
      return res;
    }
  };

}
