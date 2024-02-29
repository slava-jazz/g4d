package today.geojutsu;

import today.geojutsu.match.Index2d;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class RTree<TData extends Index2d.Data> extends Index2d<TData>
{
  private final Options o;
  private final InsertBuffer insertBuffer;
  private Node root;

  public RTree(final Options _o)
  {
    o = _o;
    insertBuffer = new InsertBuffer();
  }

  @Override
  public synchronized void insert(final TData _data)
  {
    if (root == null)
    {
      root = buildLeafNode(_data);
    }
    else
    {
      if (root.recursivelyInsert(insertBuffer, _data) == InsertStatus.split)
      {
        root = buildTreeNode(insertBuffer.newNodes[0]);
        root.accommodate(insertBuffer.newNodes[1]);
      }
    }
  }

  @Override
  public Collection<TData> select(final AABB _range)
  {
    List<TData> list = new LinkedList<>();
    if (root != null)
    {
      root.rangeQuery(_range, list);
    }
    return list;
  }

  @Override
  public void iterateAll(final ElementObserver<TData> _observer)
  {
    if (root != null)
    {
      root.recursivelyIterate(_observer);
    }
  }

  @Override
  public void iterateAll(final ElementAndOverlapsObserver<TData> _observer, final double[] _dxdy_tolerance)
  {
    if (root != null)
    {
      root.recursivelyIterate(_observer, _dxdy_tolerance, new AABB());
    }
  }

  public static class Options
  {
    private final int maxChildren;

    public Options(final int _maxChildren)
    {
      maxChildren = _maxChildren;
    }
  }

  @SuppressWarnings("unchecked")
  private class InsertBuffer
  {
    private final TData[] dataBuffer = (TData[])Array.newInstance(Data.class, o.maxChildren + 1);
    private final Node[] nodeBuffer = (Node[])Array.newInstance(Node.class, o.maxChildren + 1);
    private final Node[] newNodes = (Node[])Array.newInstance(Node.class, 2);

    private final int[] seeds = new int[] {0, 0};

    private void fillDataBuffer(final Node _node, final TData _data_to_add)
    {
      System.arraycopy(_node.objects, 0, dataBuffer, 0, o.maxChildren);
      dataBuffer[o.maxChildren] = _data_to_add;
    }

    private void fillNodeBuffer(final Node _node, final Node _node_to_add)
    {
      System.arraycopy(_node.children, 0, nodeBuffer, 0, o.maxChildren);
      nodeBuffer[o.maxChildren] = _node_to_add;
    }

    private void selectDataSeeds()
    {
      double max_distance = 0.;
      for (int i = 0; i < dataBuffer.length; i++)
      {
        for (int j = i + 1; j < dataBuffer.length; j++)
        {
          double distance = dataBuffer[i].getAABB().distanceSq(dataBuffer[j].getAABB());
          if (distance > max_distance)
          {
            seeds[0] = i;
            seeds[1] = j;
            max_distance = distance;
          }
        }
      }
      newNodes[0] = buildLeafNode(dataBuffer[seeds[0]]);
      newNodes[1] = buildLeafNode(dataBuffer[seeds[1]]);
    }

    private void selectNodeSeeds()
    {
      double max_distance = 0.;
      for (int i = 0; i < nodeBuffer.length; i++)
      {
        for (int j = i + 1; j < nodeBuffer.length; j++)
        {
          double distance = nodeBuffer[i].distanceSq(nodeBuffer[j]);
          if (distance > max_distance)
          {
            seeds[0] = i;
            seeds[1] = j;
            max_distance = distance;
          }
        }
      }
      newNodes[0] = buildTreeNode(nodeBuffer[seeds[0]]);
      newNodes[1] = buildTreeNode(nodeBuffer[seeds[1]]);
    }

    private void spitData()
    {
      for (int i = 0; i < dataBuffer.length; i++)
      {
        if (i != seeds[0] && i != seeds[1])
        {
          final TData data = dataBuffer[i];
          double e0 = newNodes[0].calcEnlargement(data.getAABB());
          double e1 = newNodes[1].calcEnlargement(data.getAABB());
          if (e0 > e1)
          {
            newNodes[0].accommodate(data);
          }
          else if (e1 > e0)
          {
            newNodes[1].accommodate(data);
          }
          else
          {
            double a0 = newNodes[0].calcArea();
            double a1 = newNodes[1].calcArea();
            if (a0 > a1)
            {
              newNodes[0].accommodate(data);
            }
            else if (a1 > a0)
            {
              newNodes[1].accommodate(data);
            }
            else if (newNodes[0].occupiedQty < newNodes[1].occupiedQty)
            {
              newNodes[0].accommodate(data);
            }
            else
            {
              newNodes[1].accommodate(data);
            }
          }
        }
      }
    }

    private void spitNode()
    {
      for (int i = 0; i < nodeBuffer.length; i++)
      {
        if (i != seeds[0] && i != seeds[1])
        {
          final Node node = nodeBuffer[i];
          double e0 = newNodes[0].calcEnlargement(node);
          double e1 = newNodes[1].calcEnlargement(node);
          if (e0 > e1)
          {
            newNodes[0].accommodate(node);
          }
          else if (e1 > e0)
          {
            newNodes[1].accommodate(node);
          }
          else
          {
            double a0 = newNodes[0].calcArea();
            double a1 = newNodes[1].calcArea();
            if (a0 > a1)
            {
              newNodes[0].accommodate(node);
            }
            else if (a1 > a0)
            {
              newNodes[1].accommodate(node);
            }
            else if (newNodes[0].occupiedQty < newNodes[1].occupiedQty)
            {
              newNodes[0].accommodate(node);
            }
            else
            {
              newNodes[1].accommodate(node);
            }
          }
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private class Node extends AABB
  {
    private Node[] children;
    private TData[] objects;
    private int occupiedQty;

    private Node(final Node _node)
    {
      super(_node);
      children = (Node[])Array.newInstance(Node.class, o.maxChildren);
      children[occupiedQty++] = _node;
    }

    private Node(final TData _object)
    {
      super(_object.getAABB());
      objects = (TData[])Array.newInstance(Data.class, o.maxChildren);
      objects[occupiedQty++] = _object;
    }

    private boolean testRecursive()
    {
      boolean ok = true;
      if (isLeaf())
      {
        for (int i = 0; ok && i < occupiedQty; i++)
        {
          ok = contains(objects[i].getAABB());
          if (!ok)
          {
            System.out.println("!contains(objects[i].getAABB());");
          }
        }
      }
      else
      {
        for (int i = 0; ok && i < occupiedQty; i++)
        {
          ok = children[i].testRecursive() && contains(children[i]);
          if (!ok)
          {
            System.out.println("!contains(children[i]);");
          }
        }
      }
      return ok;
    }

    private int findMinEnlargement(final AABB _aabb)
    {
      int ret = 0;
      double min_e = children[ret].calcEnlargement(_aabb);
      for (int i = 1; i < occupiedQty; i++)
      {
        double e = children[i].calcEnlargement(_aabb);
        if (e < min_e ||
            (e == min_e && children[i].calcArea() < children[ret].calcArea()))
        {
          min_e = e;
          ret = i;
        }
      }
      return ret;
    }

    private void removeNode(int _index)
    {
      int last_index = occupiedQty - 1;
      if (_index != last_index)
      {
        children[_index] = children[last_index];
      }
      occupiedQty--;
      reset(children[0]);
      for (int i = 1; i < occupiedQty; i++)
      {
        extend(children[i]);
      }
    }

    private boolean accommodate(final Node _node)
    {
      boolean ok = occupiedQty < o.maxChildren;
      if (ok)
      {
        children[occupiedQty++] = _node;
        extend(_node);
      }
      return ok;
    }

    private boolean accommodate(final TData _object)
    {
      boolean ok = occupiedQty < o.maxChildren;
      if (ok)
      {
        objects[occupiedQty++] = _object;
        extend(_object.getAABB());
      }
      return ok;
    }

    private ObserverStatus recursivelyIterate(final ElementObserver<TData> _observer)
    {
      ObserverStatus status = null;
      if (!isLeaf())
      {
        for (int i = 0; status != ObserverStatus.stop && i < occupiedQty; i++)
        {
          status = children[i].recursivelyIterate(_observer);
        }
      }
      else
      {
        for (int i = 0; status != ObserverStatus.stop && i < occupiedQty; i++)
        {
          status = _observer.onData(objects[i]);
        }
      }
      return status;
    }

    private ObserverStatus recursivelyIterate(final ElementAndOverlapsObserver<TData> _observer, final double[] _t, final AABB _buffer)
    {
      ObserverStatus status = null;
      if (!isLeaf())
      {
        for (int i = 0; status != ObserverStatus.stop && i < occupiedQty; i++)
        {
          status = children[i].recursivelyIterate(_observer, _t, _buffer);
        }
      }
      else
      {
        for (int i = 0; status != ObserverStatus.stop && i < occupiedQty; i++)
        {
          status = _t == null ?
              _observer.onData(objects[i], select(objects[i].getAABB())) :
              _observer.onData(objects[i], select(_buffer.reset(objects[i].getAABB()).surroundBy(_t[0], _t[1])))
          ;
        }
      }
      return status;
    }

    private InsertStatus recursivelyInsert(final InsertBuffer _buffer, final TData _object)
    {
      InsertStatus status = null;
      if (!isLeaf()) // drill stack down
      {
        int select_id = findMinEnlargement(_object.getAABB());
        switch (status = children[select_id].recursivelyInsert(_buffer, _object))
        {
          case accommodated:
          {
            extend(_object.getAABB());
            break;
          }

          case split:
          {
            removeNode(select_id);
            accommodate(_buffer.newNodes[0]);
            if (accommodate(_buffer.newNodes[1]))
            {
              status = InsertStatus.accommodated;
            }
            else
            {
              _buffer.fillNodeBuffer(this, _buffer.newNodes[1]);
              _buffer.selectNodeSeeds();
              _buffer.spitNode();
            }
          }
        }

      }
      else
      {
        // so we are on bottom of the stack now
        if (accommodate(_object))
        {
          status = InsertStatus.accommodated;
        }
        else
        {
          _buffer.fillDataBuffer(this, _object);
          _buffer.selectDataSeeds();
          _buffer.spitData();
          status = InsertStatus.split;
        }
      }
      return status;
    }

    private void rangeQuery(final AABB _range, final Collection<TData> _buffer)
    {
      for (int i = 0; i < occupiedQty; i++)
      {
        if (isLeaf())
        {
          if (objects[i].isSelectable() && objects[i].getAABB().overlaps(_range))
          {
            _buffer.add(objects[i]);
          }
        }
        else
        {
          if (children[i].overlaps(_range))
          {
            children[i].rangeQuery(_range, _buffer);
          }
        }
      }
    }

    private boolean isLeaf()
    {
      return objects != null;
    }
  }

  private Node buildTreeNode(final Node _node)
  {
    return new Node(_node);
  }

  private Node buildLeafNode(final TData _data)
  {
    return new Node(_data);
  }

}

