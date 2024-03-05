<style>
.std-image {
  width: 300px;
  height: auto;
  display: block;
  margin-left: auto;
  margin-right: auto;  
  box-shadow: 2px 2px 5px;
}
</style>
# Efficient geometrical library
## Basic
This library is a bit different to other geometrical library like JTS, this library  is working with a parametric geometry. 
First of all, “geometry” in this library means sequence of points and that sequence can be interpreted as a polyline or 
a ring.  A vertex (a shape point) of that geometry, additionally to regular coordinates x, y and z has a parametric offset. 
This offset represents normalized travel distance along geometry from first vertex to that specific vertex. 
The distance normalized in relative way form 0.0 to 1.0, where 0.0 is first vertex and 1.0 is the last vertex of the geometry.

<img src="images/readme/001.png" alt="parametric geometry example" class="std-image"/>

How you compute a distance between two positions, the library (in general) does not care , but it provides geodetic length calculator for your.
```java
    double length = Geodetic.calcDistanceInMeters(v1 , v2);
```

There are two basic classes to start: a V4d and a G4d. The V4d is a trivial vertex with additional property - offset:  
````java
public class V4d implements Comparable<V4d>
{
  // all properties are public due best performance
  public final double xLon;
  public final double yLat;
  public final double zAlt;
  public final double o; // parametric offset along specific shape
````
The G4d is a geometry (polyline or ring) with of course array of vertices representing the shape,but additionally that 
shape has breakout on [monotonic segments](docs/msegment.md) and can be parametrized by customer specific payload 
associated with the geometry.
```java
/**
 * represents any non point geometry. dependently on context it can be a poly-line or interior/exterior ring of a polygon
 */
public class G4d<TCustom> extends AABB
{

  final double length;   // absolute length of the feature
  final V4d[] shape;    // geometrical shape of the feature
  final Edge[] edges;    // edges of the feature's shape
  final MSegment[] segments; // monotonic segments of the feature's shape
  final TCustom customData; // custom data
```

The G4d does not have public constructor to create an instance, instead there are two static “build” methods you may use. 
First to build instance direct from V4d array and length of the feature, second using adapter to convert your geometry type 
to V4d array and length of the feature. 
```java 
  /**
   * build g4d from prepared v4d array and custom data
   * @param _length length of the feature
   * @param _shape prepared v4d array
   * @return g4d feature
   */
  public static <T> G4d<T> build(final double _length, final V4d[] _shape, final T _custom_data)
  ...
  /**
   * build g4d from custom feature using specific adapter
   *
   * @param _adapter     adapter to convert
   * @param _custom_geom custom feature
   * @param <TGeom>      custom feature's type
   * @return g4d feature
   */
  public static <TGeom, TData> G4d<TData> build(final InputAdapter<TGeom> _adapter, final TGeom _custom_geom, final TData _custom_data)
  ...
  public interface InputAdapter<TCustomGeometry>
  {
    AnAssociation<V4d[], Double> convert(final TCustomGeometry _geometry);
  }
```
I recommend to implement an adapter for your geometry type as a singleton and use it in any place. The library provide 
adapters to convert geometry given as ArrayList<double[]> (cartesian or wgs) and two helpers `convertFromLINESTRING` 
and `convertFromMULTILINESTRING`.

If you start to implement your adapter take in account that calculation of the parametric offset for each vertex as well 
as computation of the feature length is fully on your side.

## first steps 
