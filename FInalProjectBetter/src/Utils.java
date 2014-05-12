import java.util.ArrayList;
import java.util.List;

import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Transform;
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
	
	public static double distancePointToPolygon(Polygon p, Vector2 point) {
		if(p.contains(point)) {
			return 0;
		}
		double dist = Double.POSITIVE_INFINITY;
		Vector2[] vertices = p.getVertices();
		for(int i = 0; i < vertices.length; i++) {
			int next = (i+1) % vertices.length;
			dist = Math.min(dist, distancePointToLine(vertices[i], vertices[next], point));
		}
		return dist;
	}
	
	public static double distancePointToLine(Vector2 v, Vector2 w, Vector2 p) {
		double l2 = v.distanceSquared(w);
		if(l2 == 0) {
			return v.distance(p);
		}
		Vector2 p_v = s.set(p).subtract(v);
		Vector2 w_v = r.set(w).subtract(v);
		double t = p_v.dot(w_v) / l2;
		if(t < 0) {
			return v.distance(p);
		}
		else if (t > 1) {
			return w.distance(p);
		}
		else {
			Vector2 diff = w_v.multiply(t);
			Vector2 proj = q_p.set(v).add(diff);
			return proj.distance(p);
		}
	}
	
	public static boolean polygonsAdjacent(Polygon p1, Polygon p2) {
		for(Vector2 v1 : p1.getVertices()) {
			for(Vector2 v2 : p2.getVertices()) {
				if(v1.equals(v2)) return true;
			}
		}
		return false;
	}
	
	private static ArrayList<Vector2> points = new ArrayList<Vector2>();
	private static ArrayList<Polygon> clippedPolys = new ArrayList<Polygon>();
	
	public static ArrayList<Polygon> intersect(List<Polygon> ps, Transform t, List<ConvexPolygon> clip) {
		clippedPolys.clear();
		for(ConvexPolygon cp : clip) {
			intersect(ps, t, cp, clippedPolys);
		}
		return clippedPolys;
	}
	
	private static void intersect(List<Polygon> ps, Transform t, ConvexPolygon clip, ArrayList<Polygon> acc) {
		for(Polygon p : ps) {
			Polygon result = intersect(p, t, clip);
			if(result != null) {
				acc.add(result);
			}
		}
	}
	
	public static Vector2[] polygonVerticesWorld(Polygon p, Transform t) {
		Vector2[] world = new Vector2[p.getVertices().length];
		for(int i = 0; i < world.length; i++) {
			Vector2 v = new Vector2(p.getVertices()[i]);
			t.transform(v);
			world[i] = v;
		}
		return world;
	}
	
	public static Polygon intersect(Polygon p, Transform t, ConvexPolygon clip) {
		points.clear();
		Vector2[] p1points = polygonVerticesWorld(p, t);
		Vector2[] p2points = clip.getVerticesWorldSpace();
		for(Vector2 v : p1points) {
			if(clip.contains(v)) {
				points.add(v);
			}
		}
		int p1num = points.size();
		if(p1num == p1points.length) {
			ArrayList<Vector2> temp = ConvexPolygon.makeHullFromPoints(points);
			return new Polygon(temp.toArray(new Vector2[0]));
		}
		for(Vector2 v : p2points) {
			if(p.contains(v, t)) {
				points.add(v);
			}
		}
		int p2num = points.size() - p1num;
		if(p2num == p2points.length) {
			ArrayList<Vector2> temp = ConvexPolygon.makeHullFromPoints(points);
			return new Polygon(temp.toArray(new Vector2[0]));
		}
		addIntersectionPoints(p1points, p2points, points);
		try {
			ArrayList<Vector2> temp = ConvexPolygon.makeHullFromPoints(points);
			return new Polygon(temp.toArray(new Vector2[0]));
		}
		catch(IllegalArgumentException e) {
			return null;
		}
	}
	
	public static ConvexPolygon intersect(ConvexPolygon p1, ConvexPolygon clip) {
		points.clear();
		Vector2[] p1points = p1.getVerticesWorldSpace();
		Vector2[] p2points = clip.getVerticesWorldSpace();

		for(Vector2 v : p1points) {
			if(clip.contains(v)) {
				points.add(v);
			}
		}
		int p1num = points.size();
		if(p1num == p1points.length) {
			return new ConvexPolygon(points);
		}
		for(Vector2 v : p2points) {
			if(p1.contains(v)) {
				points.add(v);
			}
		}
		int p2num = points.size() - p1num;
		if(p2num == p2points.length) {
			return new ConvexPolygon(points);
		}
		addIntersectionPoints(p1points, p2points, points);
		try {
			return new ConvexPolygon(points);
		}
		catch(IllegalArgumentException e) {
			return null;
		}
	}
	
	private static void addIntersectionPoints(Vector2[] p1points, Vector2[] p2points, ArrayList<Vector2> acc) {
		for(int i = 0; i < p1points.length; i++) {
			int next_i = (i+1) % p1points.length;
			for(int j = 0; j < p2points.length; j++) {
				int next_j = (j+1) % p2points.length;
				Vector2 inter = intersectionLineSegments(p1points[i], p1points[next_i], p2points[j], p2points[next_j]);
				if(inter != null) {
					acc.add(inter);
				}
			}
		}
	}
}
