import java.util.ArrayList;

import org.dyn4j.geometry.Vector2;

public class Utils
{
	
	public static double cross2(Vector2 u, Vector2 v) {
		return (u.x * v.y) - (u.y * v.x);
	}

	public static String getPaddedNumber(int number, int length, String pad)  {
		return getPaddedString(""+number, length, pad, true);
	}

	public static String getPaddedString(String s, int length, String pad, boolean prePad) 
	{
		if(pad.length() != 1) throw new IllegalArgumentException("pad must be a single character.");
		String result = s;
		result.trim();
		if(result.length() > length) 
			throw new RuntimeException
			("input string "+s+" is already more than length="+length);

		int nPad = (length - result.length());
		for(int k=0; k<nPad; k++) {
			//System.out.println("nPad="+nPad+", result="+result+", result.length()="+result.length());
			if(prePad) 
				result = pad + result;
			else
				result = result + pad;
		}

		return result;
	}
	
	private static Vector2 r = new Vector2();
	private static Vector2 s = new Vector2();
	private static Vector2 q_p = new Vector2();
	
	public static Vector2 intersectionLineSegments(Vector2 p, Vector2 p2, Vector2 q, Vector2 q2) {
		r.set(p2).subtract(p);
		s.set(q2).subtract(q);
		q_p.set(q).subtract(p);
		double rxs = Utils.cross2(r, s);
		double t = Utils.cross2(q_p, s) / rxs;
		double u = Utils.cross2(q_p, r) / rxs;
		if(0 <= t && t <= 1 && 0 <= u && u <= 1) {
			Vector2 ret = new Vector2();
			ret.set(r).multiply(t).add(p);
			return ret;
		}
		return null;
	}
	
	private static ArrayList<Vector2> points = new ArrayList<Vector2>();
	
	public static ConvexPolygon intersect(ConvexPolygon p1, ConvexPolygon p2, long time) {
		points.clear();
		Vector2[] p1points = p1.getVerticesWorldSpace();
		Vector2[] p2points = p2.getVerticesWorldSpace();

		for(Vector2 v : p1points) {
			if(p2.pointInPolygon(v)) {
				points.add(v);
			}
		}
		for(Vector2 v : p2points) {
			if(p1.pointInPolygon(v)) {
				points.add(v);
			}
		}
		for(int i = 0; i < p1points.length; i++) {
			int next_i = (i+1) % p1points.length;
			for(int j = 0; j < p2points.length; j++) {
				int next_j = (j+1) % p2points.length;
				Vector2 inter = intersectionLineSegments(p1points[i], p1points[next_i], p2points[j], p2points[next_j]);
				if(inter != null) {
					points.add(inter);
				}
			}
		}
		System.out.println("begin result of " + points.size() + " points");
		for(Vector2 v : points) {
			System.out.println(v);
		}
		System.out.println("end result");
		try {
			return new ConvexPolygon(points);
		}
		catch(IllegalArgumentException e) {
			return null;
		}
	}
}
