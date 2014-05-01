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
	
	
}
