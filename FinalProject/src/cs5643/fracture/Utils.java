package cs5643.fracture;
import java.util.ArrayList;
import java.util.Collections;

import javax.vecmath.*;

/** 
 * Catch-all utilities (feel free to add on). 
 * 
 * @author Doug James, January 2007
 */
public class Utils
{

	/**  Returns (ax*by-ay*bx).  */
	static double crossZ(Tuple2d a, Tuple2d b)  {  return (a.x*b.y - a.y*b.x);   }

	/**
	 * sum += scale*v
	 */
	public static void acc(Tuple2d sum, double scale, Tuple2d v)
	{
		sum.x += scale * v.x;
		sum.y += scale * v.y;
	}


	/**
	 * 
	 * @param pad Pre-padding char.
	 */
	public static String getPaddedNumber(int number, int length, String pad)  {
		return getPaddedString(""+number, length, pad, true);
	}

	/**
	 * @param prePad Pre-pads if true, else post pads.
	 */
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
	
	private static PointSorter sorter = new PointSorter();
	
	/**
	 * Tests if p1,p2,p3 (in that order) form a counterclockwise turn.
	 * Returns positive if they are CCW, negative if they are CW, 0 if they are co-linear.
	 * @param p1
	 * @param p2
	 * @param p3
	 * @return
	 */
	public static double ccw(Point2d p1, Point2d p2, Point2d p3) {
		return (p2.x - p1.x) * (p3.y - p1.y) - (p2.y - p1.y) * (p3.x - p1.x);
	}
	
	/**
	 * Implements the monotone chain algorithm for computing a convex hull from
	 * a set of points.
	 * 
	 * http://en.wikibooks.org/wiki/Algorithm_Implementation/Geometry/Convex_hull/Monotone_chain
	 * 
	 * @param pointSet
	 * @return
	 */
	public static Convex makeHullFromPoints(ArrayList<Point2d> pointSet) {
		ArrayList<Point2d> upper = new ArrayList<Point2d>();
		ArrayList<Point2d> lower = new ArrayList<Point2d>();
		Collections.sort(pointSet, sorter);
		for (int i = 0; i < pointSet.size(); i++) {
			while(lower.size() >= 2 && ccw(lower.get(lower.size() - 2), lower.get(lower.size() - 1), pointSet.get(i)) <= 0) {
				lower.remove(lower.size() - 1);
			}
			lower.add(pointSet.get(i));
		}
		for(int i = pointSet.size() - 1; i >= 0; i--) {
			while(upper.size() >= 2 && ccw(upper.get(upper.size() - 2), upper.get(upper.size() - 1), pointSet.get(i)) <= 0) {
				upper.remove(upper.size() - 1);
			}
			upper.add(pointSet.get(i));
		}
		lower.remove(lower.size() - 1);
		upper.remove(upper.size() - 1);
		lower.addAll(upper);
		Convex c = new Convex(lower);
		return c;
	}
	
	public static double cross2d(Vector2d u, Vector2d v) {
		return (u.x * v.y) - (u.y * v.x);
	}
	
	private static Vector2d r = new Vector2d();
	private static Vector2d s = new Vector2d();
	private static Vector2d q_p = new Vector2d();
	
	/**
	 * Determines the intersection of line segments (p1, p2) and (q1, q2) if it exists
	 */
	public static Point2d intersectionLineSegments(Point2d p, Point2d p2, Point2d q, Point2d q2) {
		r.sub(p2, p);
		s.sub(q2, q);
		q_p.sub(q, p);
		double rxs = Utils.cross2d(r, s);
		double t = Utils.cross2d(q_p, s) / rxs;
		double u = Utils.cross2d(q_p, r) / rxs;
		if(0 <= t && t <= 1 && 0 <= u && u <= 1) {
			Point2d ret = new Point2d();
			ret.scale(t, r);
			ret.add(p);
			return ret;
		}
		return null;
	}
}
