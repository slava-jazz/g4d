package today.geojutsu.match;

import today.geojutsu.AABB;

import java.util.Collection;

public abstract class Index2d<TData extends Index2d.Data>
{
  public interface Data
  {
    boolean isSelectable();

    AABB getAABB();
  }

  public interface ElementObserver<TData extends Data>
  {
    ObserverStatus onData(TData _data);
  }

  public interface ElementAndOverlapsObserver<TData extends Data>
  {
    ObserverStatus onData(TData _data, Collection<TData> _overlapped_neighbors);
  }

  public abstract void insert(final TData _data);

  public abstract Collection<TData> select(final AABB _range);

  public abstract void iterateAll(final ElementObserver<TData> _observer);

  public abstract void iterateAll(final ElementAndOverlapsObserver<TData> _observer, final double[] _dxdy_tolerance);

  protected enum InsertStatus
  {
    accommodated,
    split
  }

  public enum ObserverStatus
  {
    next, stop
  }

}
