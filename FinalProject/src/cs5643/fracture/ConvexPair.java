package cs5643.fracture;

/** 
 * A hashable int representation of a pair of indexed rigid
 * bodies.  
 * 
 * @author Doug James, March 2007.
 */
public class ConvexPair
{
	private int i, j, hash;

	public ConvexPair(int i, int j)
	{
		if(i==j) throw new IllegalArgumentException("i==j not allowed");

		this.i = i;
		this.j = j;

		int min = Math.min(i,j);
		int max = Math.max(i,j);

		if(max >= (int)Short.MAX_VALUE) 
			throw new IllegalArgumentException
			("Can't handle body indices > Short.MAX_VALUE="+Short.MAX_VALUE+
					", but max(i,j) was "+max);

		hash = (int)Short.MAX_VALUE * min  +  max;
	}

	/** Body "i" index */
	public final int i()        { return i; }

	/** Body "j" index */
	public final int j()        { return j; }

	/** Hashcode for body pair: hash(i,j)=hash(j,i). */
	public final int hashcode() { return hash; }
}
