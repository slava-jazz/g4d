package today.geojutsu;

public class AnAssociation<TA, TB>
{
  public final TA a;
  public final TB b;

  public AnAssociation(final TA _a, final TB _b)
  {
    a = _a;
    b = _b;
  }

  @Override
  public String toString()
  {
    return "AnAssociation{" +
        "a=" + a +
        ", b=" + b +
        '}';
  }

}
