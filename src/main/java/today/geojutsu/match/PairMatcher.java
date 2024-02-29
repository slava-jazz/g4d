package today.geojutsu.match;

import today.geojutsu.*;
import java.util.TreeSet;

/**
 * the geometrical pair matcher
 */
class PairMatcher
{

  /**
   * find the start position to match and detect direction of matching for target feature,
   * take in account that source feature always matched in native digitisation order
   *
   * @param _src_seg source segment there the start position have to be found
   * @param _trg_seg target segment there the start position have to be found
   * @param _cfg tolerances required
   * @param d2_buffer temporary buffer to avoid small memory allocation and do not stress GC
   * @return MatchingContext with start position and iterators according matching direction
   */
  static <TCustom> TreeSet<MatchingContext> syncEdgesAndMatchFirstPoint(
      final G4d<TCustom>.MSegment _src_seg,
      final G4d<TCustom>.MSegment _trg_seg,
      final MatchCoreConfig _cfg,
      final double[] d2_buffer)
  {
    final DirectionMatched dm = matchDirection(_src_seg, _trg_seg, _cfg);
    final TreeSet<MatchingContext> matched_starts = new TreeSet<>(); // ordered by distance between matched points on source and target segments
    if (dm != DirectionMatched.notMatched)
    {
      final G4dIter sourceIterator = new G4DForwardIter(_src_seg); // source is going always native direction
      final G4dIter targetIterator = dm == DirectionMatched.same ?
          new G4DForwardIter(_trg_seg) :  // native direction
          new G4DBackwardIter(_trg_seg);  // reverted direction
      // do we prefer to move along target feature to find sync point
      final boolean sync_along_target = Euclid.calcDistanceSq(sourceIterator.getEdgeLastVertex(), targetIterator.getEdgeFirstVertex())
          >
          Euclid.calcDistanceSq(sourceIterator.getEdgeFirstVertex(), targetIterator.getEdgeLastVertex());
      // edges iterators along matched segments
      final G4dIter sync_iterator = sync_along_target ? targetIterator : sourceIterator;  // primary iterator to find sync position
      final G4dIter second_sync_iterator = sync_along_target ? sourceIterator : targetIterator; // secondary iterator to find sync position
      // how many iterations we have for each iterator to do not cross monotonic segment border
      final int sync_max_step = sync_along_target ? _trg_seg.getEdgesQty() : _src_seg.getEdgesQty();
      final int sync_second_max_step = sync_along_target ? _src_seg.getEdgesQty() : _trg_seg.getEdgesQty();
      final int sync_start = sync_iterator.getPosition(); // initial position of sync iterator
      boolean has_more_edges = true;
      int sync_steps_cntr = 0;
      int sync_second_step_cntr = 0;
      boolean first_iteration = true; // the match loop must be done at list one times.
      while (true)
      {
        if (!first_iteration && // exclude iterators positions check for first time
            (!has_more_edges || sync_steps_cntr >= sync_max_step))
        { // we can not move forward along primary sync feature
          if (sync_second_step_cntr >= sync_second_max_step)
          { // we can not move forward along secondary sync feature
            break; // the end
          }
          // let's move primary iterator to initial position and move along secondary feature
          sync_iterator.setPosition(sync_start);
          sync_steps_cntr = 0;
          second_sync_iterator.next();
          sync_second_step_cntr++; // count step along secondary feature
        }
        sync_steps_cntr++; //count step along primary feature
        first_iteration = false;

        // let's start matching of two current edges
        // the sync position could be two vertexes ot projection of one vertex to other edge
        int tangent_diff = Euclid.compareTangentWithTolerance(sourceIterator.getTangent(), targetIterator.getTangent(), _cfg.tangentTolerance);
        if (tangent_diff == 0) // edges having the tangents in tolerance frame
        {
          if (sourceIterator.getAABB().overlaps(targetIterator.getAABB())) // we have the edges AABB overlap
          {
            // there we probably have sync point to start matching
            V4d sample_vertex = sourceIterator.getEdgeFirstVertex();
            V4d candidate_vertex = targetIterator.getEdgeFirstVertex();

            // just simply  check first points
            int x_error = compareWithTolerance(sample_vertex.xLon - candidate_vertex.xLon, 0, _cfg.xLonTolerance);
            int y_error = compareWithTolerance(sample_vertex.yLat - candidate_vertex.yLat, 0, _cfg.yLatTolerance);

            if (x_error != 0 || y_error != 0) // start of edges not matched
            {
              // first try to project sample vertex on candidate edge
              double[] p = Euclid.calcProjectionPoint(targetIterator.getEdgeFirstVertex(), targetIterator.getEdgeLastVertex(), sample_vertex,
                  d2_buffer);
              if (p != null && compareWithTolerance(Euclid.calcDistanceSq(p, sample_vertex), 0, _cfg.distanceSquareTolerance) == 0)
              {
                // we have projection, let's count it as possible start position
                matched_starts.add(
                    new MatchingContext(
                        sourceIterator,
                        targetIterator,
                        sample_vertex.o,
                        Euclid.calcOffset(targetIterator.getEdgeFirstVertex(), targetIterator.getEdgeLastVertex(), p),
                        Euclid.calcDistanceSq(p, sample_vertex))
                );
                if ( // let's see do we have a better match then move on one edge ahead
                    compareWithTolerance(Euclid.calcDistanceSq(p, targetIterator.getEdgeLastVertex()), 0, _cfg.distanceSquareTolerance) == 0 &&
                        !targetIterator.isLast() &&
                        compareWithTolerance(sourceIterator.getTangent(), targetIterator.viewNextTangent(), _cfg.tangentTolerance) == 0)
                { // yes, we shall try next edge
                  has_more_edges = targetIterator.next();
                  continue;
                }
              }
              else if (p == null) // okay no projection for sample vertex on candidate edge, now  try project candidate vertex on sample edge
              {
                p = Euclid.calcProjectionPoint(sourceIterator.getEdgeFirstVertex(), sourceIterator.getEdgeLastVertex(), candidate_vertex, d2_buffer);
                if (p != null && compareWithTolerance(Euclid.calcDistanceSq(p, candidate_vertex), 0, _cfg.distanceSquareTolerance) == 0)
                { // good the projection exists, let's count it as possible sync position
                  matched_starts.add(
                      new MatchingContext(
                          sourceIterator,
                          targetIterator,
                          Euclid.calcOffset(sourceIterator.getEdgeFirstVertex(), sourceIterator.getEdgeLastVertex(), p),
                          candidate_vertex.o,
                          Euclid.calcDistanceSq(p, candidate_vertex))
                  );
                }
                else
                { // no projection found, or it is far away from candidate vertex, just continue matching if possible
                  has_more_edges = sync_iterator.next();
                }
              }
              else
              { // projection is far away from sample vertex
                has_more_edges = sync_iterator.next();
              }
            }
            else // cool! the start of edges matched, log the offsets and move to next edges
            {
              matched_starts.add(
                  new MatchingContext(
                      sourceIterator,
                      targetIterator,
                      sample_vertex.o,
                      candidate_vertex.o,
                      Euclid.calcDistanceSq(sample_vertex, candidate_vertex))
              );
              // check the end of the target edge, probably it would better
              V4d cs = second_sync_iterator.getEdgeFirstVertex();
              V4d ce = sync_iterator.getEdgeLastVertex();
              x_error = compareWithTolerance(cs.xLon - ce.xLon, 0, _cfg.xLonTolerance);
              y_error = compareWithTolerance(cs.yLat - ce.yLat, 0, _cfg.yLatTolerance);

              if (x_error == 0 && y_error == 0)
              {
                double d1 = Euclid.calcDistanceSq(sample_vertex, candidate_vertex);
                double d2 = Euclid.calcDistanceSq(cs, ce);
                if (
                    d2 < d1 &&
                        !sync_iterator.isLast() &&
                        compareWithTolerance(second_sync_iterator.getTangent(), sync_iterator.viewNextTangent(), _cfg.tangentTolerance) == 0)
                {
                  // the last edge vertex looks like good alternative
                  // lets check it by one more iteration
                  has_more_edges = sync_iterator.next();
                  continue;
                }
              }
            }
          }
          else // edges having the same tangent, but far away from each other
          {
            has_more_edges = sync_iterator.next();
          }
        }
        else
        {
          has_more_edges = sync_iterator.next();
        }
      }
    }
    return matched_starts;
  }

  /**
   * match the rest of features
   * @param _context context with feature iterators
   * @param _cfg tolerances
   * @param d2_buffer temporary buffer
   * @return found pair or null
   */
  static <TCustom> FeatureRangeReference<TCustom> matchTail(
      final MatchingContext _context,
      final MatchCoreConfig _cfg,
      final double[] d2_buffer)
  {
    FeatureRangeReference<TCustom> ref = null;
    AABB ba = new AABB();
    if (_context.matchTail(_cfg, d2_buffer, ba)) // the algorithm from historical reason implemented in context, so see below in that file
    {
      FeatureRangeReference.Range sr = new FeatureRangeReference.Range(_context.sample_start_offset, _context.sample_end_offset);
      FeatureRangeReference.Range tr = new FeatureRangeReference.Range(
          Math.min(_context.candidate_start_offset, _context.candidate_end_offset),
          Math.max(_context.candidate_start_offset, _context.candidate_end_offset)
      );
      ref = new FeatureRangeReference<>(sr, _context.ti.getGeometry(), tr, _context.candidate_start_offset < _context.candidate_end_offset);
    }
    return ref;
  }

  /**
   * detect direction of match.
   * @param _src_seg source segment
   * @param _trg_seg target segment
   * @param _cfg  tolerances
   * @return direction of target segment
   */
  static <TCustom> DirectionMatched matchDirection(
      final G4d<TCustom>.MSegment _src_seg,
      final G4d<TCustom>.MSegment _trg_seg,
      final MatchCoreConfig _cfg)
  {
    double min_dt = Double.MAX_VALUE;
    G4d<TCustom>.Edge ea = null; // edges to compare
    G4d<TCustom>.Edge eb = null;
    DirectionMatched res = DirectionMatched.notMatched;
    int src_qty = _src_seg.getEdgesQty();
    int trg_qty = _trg_seg.getEdgesQty();
    for (int i = 0; i < src_qty; i++)
    {
      G4d<TCustom>.Edge a = _src_seg.getEdge(i);
      for (int j = 0; j < trg_qty; j++)
      {
        G4d<TCustom>.Edge b = _trg_seg.getEdge(j);
        double dt = Euclid.calcDiffTangent(a.tangent, b.tangent);
        if (dt < min_dt)
        {
          if (a.overlaps(b))
          {
            // find most parallel and most near to each other edges
            min_dt = dt;
            ea = a;
            eb = b;
          }
        }
      }
    }
    if (ea != null && min_dt < _cfg.headingTolerance)
    {
      double dh = ea.calcEuclidHeading() - eb.calcEuclidHeading(); // heading diff
      res = Math.abs(dh) < Euclid.PI_2 ? DirectionMatched.same : DirectionMatched.opposite;
    }
    return res;
  }

  static class MatchingContext implements Comparable<MatchingContext>
  {
    final double sample_start_offset;
    double sample_end_offset = Double.NaN;
    final double candidate_start_offset;
    double candidate_end_offset = Double.NaN;
    final G4dIter si;  // source iterator
    final G4dIter ti;  // target iterator
    final double startDistanceSqrt; // distance square between start points on source and target features

    public MatchingContext(final G4dIter _si, final G4dIter _ci, final double _sample_start_offset,
                           final double _candidate_start_offset, final double _dsq)
    {
      sample_start_offset = _sample_start_offset;
      candidate_start_offset = _candidate_start_offset;
      si = _si.clone();
      ti = _ci.clone();
      startDistanceSqrt = _dsq;
    }

    @Override
    public int compareTo(final MatchingContext _o)
    {
      int res =  Double.compare(startDistanceSqrt,_o.startDistanceSqrt);
      if(res == 0)
      {
        res = Double.compare(sample_start_offset,_o.sample_start_offset);
      }
      return res;
    }

    boolean moveAlongSample()
    {
      return si.next();
    }

    boolean moveAlongTarget()
    {
      return ti.next();
    }

    boolean moveBackAlongSample()
    {
      return si.previous();
    }

    boolean moveBackAlongTarget()
    {
      return ti.previous();
    }

    boolean isMatched()
    {
      return !Double.isNaN(sample_start_offset) && !Double.isNaN(sample_end_offset);
    }

    double getCoverage()
    {
      return sample_end_offset - sample_start_offset;
    }

    boolean endOfSample()
    {
      return si.isLast();
    }

    boolean endOfTarget()
    {
      return ti.isLast();
    }

    boolean endOfPair()
    {
      return endOfTarget() || endOfSample();
    }

    public G4dIter getSi()
    {
      return si;
    }

    public G4dIter getTi()
    {
      return ti;
    }

    private enum LastMoveAlongFeature
    {LMA_SOURCE, LMA_TARGET}

    /**
     * match the rest of features
     * @param _cfg tolerances
     * @param d2_buffer temporary buffer
     * @return found pair or null
     */
    boolean matchTail(final MatchCoreConfig _cfg, final double[] d2_buffer, final AABB ba)
    {
      boolean done = false;
      boolean stop_matching = false;
      boolean first_time = true;
      double prev_sample_offset = Double.NaN; // previous matched sample offset
      double prev_candidate_offset = Double.NaN; // previous matched candidate offset
      while (!stop_matching && (first_time || !endOfPair()))
      {
        first_time = false;
        boolean in_sync = false;
        boolean start_sync = false;
        double min_dx = Double.MAX_VALUE;
        double min_dy = Double.MAX_VALUE;
        LastMoveAlongFeature lmf = null;
        do
        {
          V4d sample_end = si.getEdgeLastVertex();
          V4d candidate_end = ti.getEdgeLastVertex();
          double dx = Math.abs(sample_end.xLon - candidate_end.xLon);
          double dy = Math.abs(sample_end.yLat - candidate_end.yLat);
          if (start_sync || (dx < _cfg.xLonTolerance && dy < _cfg.yLatTolerance))
          {
            if (min_dx >= dx && min_dy >= dy)
            {
              min_dx = dx;
              min_dy = dy;
              in_sync = ti.isLast();
            }
            else
            {
              moveBackAlongTarget();
              in_sync = true;
            }
            start_sync = true;
          }
          if (!in_sync)
          {
            if (ti.isLast())
            {
              if (!start_sync)
              {
                AABB.IntersectionTest it = ba.reset(sample_end, _cfg.xLonTolerance, _cfg.yLatTolerance)
                    .calcIntersectStatus(ti.getEdgeFirstVertex(), ti.getEdgeLastVertex(), null);
                if (it != AABB.IntersectionTest.outside)
                {
                  if (si.isLast())
                  {
                    in_sync = true;
                    break;
                  }
                  lmf = LastMoveAlongFeature.LMA_SOURCE;
                  moveAlongSample();
                  continue;
                }
                else if (ba.reset(candidate_end, _cfg.xLonTolerance, _cfg.yLatTolerance)
                    .calcIntersectStatus(si.getEdgeFirstVertex(), si.getEdgeLastVertex(), null) != AABB.IntersectionTest.outside)
                {
                  in_sync = true;
                }
                else if (lmf != null)
                {
                  if (lmf == LastMoveAlongFeature.LMA_SOURCE)
                  {
                    moveBackAlongSample();
                  }
                  else if (lmf == LastMoveAlongFeature.LMA_TARGET)
                  {
                    moveBackAlongTarget();
                  }
                  in_sync = true;
                }
              }
              break;
            }
            if (!start_sync)
            {
              AABB.IntersectionTest it = ba.reset(sample_end, _cfg.xLonTolerance, _cfg.yLatTolerance)
                  .calcIntersectStatus(ti.getEdgeFirstVertex(), ti.getEdgeLastVertex(), null);
              if (it != AABB.IntersectionTest.outside)
              {
                if (si.isLast())
                {
                  in_sync = true;
                  break;
                }
                lmf = LastMoveAlongFeature.LMA_SOURCE;
                moveAlongSample();
              }
              else if (ba.reset(candidate_end, _cfg.xLonTolerance, _cfg.yLatTolerance)
                  .calcIntersectStatus(si.getEdgeFirstVertex(), si.getEdgeLastVertex(), null) != AABB.IntersectionTest.outside)
              {
                lmf = LastMoveAlongFeature.LMA_TARGET;
                moveAlongTarget();
              }
              else //if(lmf != null)
              {
                if (lmf == LastMoveAlongFeature.LMA_SOURCE)
                {
                  moveBackAlongSample();
                  in_sync = true;
                }
                else if (lmf == LastMoveAlongFeature.LMA_TARGET)
                {
                  moveBackAlongTarget();
                  in_sync = true;
                }
                break;
              }
            }
            else
            {
              moveAlongTarget();
            }
          }
        }
        while (!in_sync);

        if (in_sync)
        {
          V4d sample_vertex = si.getEdgeLastVertex();
          V4d candidate_vertex = ti.getEdgeLastVertex();
          int x_error = compareWithTolerance(sample_vertex.xLon - candidate_vertex.xLon, 0, _cfg.xLonTolerance);
          int y_error = compareWithTolerance(sample_vertex.yLat - candidate_vertex.yLat, 0, _cfg.yLatTolerance);
          if (x_error != 0 || y_error != 0) // end of edges not matched
          {
            // first try to project sample vertex on candidate edge
            double[] p = Euclid.calcProjectionPoint(ti.getEdgeFirstVertex(), ti.getEdgeLastVertex(), sample_vertex, d2_buffer);
            if (p != null && compareWithTolerance(Euclid.calcDistanceSq(p, sample_vertex), 0, _cfg.distanceSquareTolerance) == 0)
            {
              prev_candidate_offset = candidate_end_offset;
              prev_sample_offset = sample_end_offset;
              candidate_end_offset = Euclid.calcOffset(ti.getEdgeFirstVertex(), ti.getEdgeLastVertex(), p);
              sample_end_offset = sample_vertex.o;
            }
            else if (p == null) // okay no projection for sample vertex on candidate edge, now  try candidate vertex on sample edge
            {
              p = Euclid.calcProjectionPoint(si.getEdgeFirstVertex(), si.getEdgeLastVertex(), candidate_vertex, d2_buffer);
              if (p != null && compareWithTolerance(Euclid.calcDistanceSq(p, candidate_vertex), 0, _cfg.distanceSquareTolerance) == 0)
              {
                prev_candidate_offset = candidate_end_offset;
                prev_sample_offset = sample_end_offset;
                sample_end_offset = Euclid.calcOffset(si.getEdgeFirstVertex(), si.getEdgeLastVertex(), p);
                candidate_end_offset = candidate_vertex.o;
              }
              else if (!si.isLast() || !ti.isLast()) // last chance
              {
                if (!si.isLast())
                {
                  si.next();
                  sample_vertex = si.getEdgeLastVertex();
                }
                else if (!ti.isLast())
                {
                  ti.next();
                  candidate_vertex = ti.getEdgeLastVertex();
                }
                x_error = compareWithTolerance(sample_vertex.xLon - candidate_vertex.xLon, 0, _cfg.xLonTolerance);
                y_error = compareWithTolerance(sample_vertex.yLat - candidate_vertex.yLat, 0, _cfg.yLatTolerance);
                if (x_error == 0 && y_error == 0)
                {
                  prev_candidate_offset = candidate_end_offset;
                  prev_sample_offset = sample_end_offset;
                  sample_end_offset = sample_vertex.o;
                  candidate_end_offset = candidate_vertex.o;
                }
              }
            }
            stop_matching = true; // break the loops
          }
          else // end of edges matched, log the offsets and move to next edges
          {
            prev_candidate_offset = candidate_end_offset;
            prev_sample_offset = sample_end_offset;
            sample_end_offset = sample_vertex.o;
            candidate_end_offset = candidate_vertex.o;
            first_time = moveAlongTarget() || moveAlongSample(); // lets start sync again
          }
        }
        else
        {
          // features not matched
          //sample_end_offset = candidate_end_offset = Double.NaN;
          stop_matching = true;
        }
      }
      if (!Double.isNaN(sample_start_offset) && !Double.isNaN(sample_end_offset) &&
          Math.abs(candidate_end_offset - candidate_start_offset) > ti.getGeometry().absolute2parametric(_cfg.rangeToleranceInMeters) &&
          Math.abs(sample_end_offset - sample_start_offset) > si.getGeometry().absolute2parametric(_cfg.rangeToleranceInMeters)
      )
      {
        done = true;

        // let's check if we have alternative matches for the same point in tolerance border
        // "alternative" means that the same point (on candidate or on sample) has two matches because of tolerance
        // we will take the closet match in that case
        if (!Double.isNaN(prev_candidate_offset) && !Double.isNaN(prev_sample_offset))
        {
          if (prev_candidate_offset == candidate_end_offset) // alternative match for candidate
          {
            V4d cp = ti.getGeometry().calculatePoint(prev_candidate_offset, 0); // candidate position
            V4d s1 = si.getGeometry().calculatePoint(prev_sample_offset, 0); // sample previous match
            V4d s2 = si.getGeometry().calculatePoint(sample_end_offset, 0); // sample last match
            if (Euclid.calcDistanceSq(cp, s1) < Euclid.calcDistanceSq(cp, s2)) // previous match was closer to candidate point
            {
              sample_end_offset = prev_sample_offset;
            }
          }
          else if (prev_sample_offset == sample_end_offset) // alternative match for sample
          {
            V4d sp = si.getGeometry().calculatePoint(prev_sample_offset, 0); // sample position
            V4d c1 = ti.getGeometry().calculatePoint(prev_candidate_offset, 0); // candidate previous match
            V4d c2 = ti.getGeometry().calculatePoint(candidate_end_offset, 0); // candidate last match
            if (Euclid.calcDistanceSq(sp, c1) < Euclid.calcDistanceSq(sp, c2)) // previous match was closer to candidate point
            {
              candidate_end_offset = prev_candidate_offset;
            }
          }
        }
      }
      return done;
    }

  }

  private enum DirectionMatched
  {
    notMatched,
    same,
    opposite
  }

  static int compareWithTolerance(final double _d1, final double _d2, final double _t)
  {
    final double d = _d1 - _d2;
    return _t >= Math.abs(d) ? 0 : (d > 0 ? 1 : -1);
  }

}
