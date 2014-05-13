import java.util.ArrayList;
import java.util.List;

import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;

public class ChrisVoronoiCalculation {
	
	public static FractureMap generateVoronoi(List<Vector2> controlPts) {
		ArrayList<ConvexPolygon> res = new ArrayList<ConvexPolygon>();
		Vector2 ortho = new Vector2();
		Vector2 midpt = new Vector2();
		for (Vector2 pt : controlPts) {
			Polygon p = new Rectangle(1, 1);
			for (Vector2 otherpt : controlPts) {
				if (pt.equals(otherpt))
					continue;
				//Find bisector between pts + midpt
				ortho.set(otherpt).subtract(pt);
				midpt.set(pt).add(ortho.x / 2, ortho.y / 2);
				ortho.set(-ortho.y, ortho.x);
				//apply Bisector to map
				ortho.normalize();
				p = vectorConvexHullIntersection(p, midpt, ortho, pt);
			}
			res.add(new ConvexPolygon(p));
		}
		return new FractureMap(res, true);
	}
	
	public static Polygon vectorConvexHullIntersection(Polygon p, Vector2 pt, Vector2 intersector, Vector2 controlPt) {
		ArrayList<Vector2> points = new ArrayList<Vector2>();
		Vector2 q = pt.add(intersector.product(2));
		Vector2 q2 = pt.difference(intersector.product(2));
		boolean positive = Utils.cross2(intersector, controlPt.difference(pt)) >= 0;
		boolean otherPos;
		Vector2 intersection;
		for (int i = 0; i < p.getVertices().length; i++) {
			otherPos = Utils.cross2(intersector, controlPt.difference(p.getVertices()[i])) >= 0;
			if (positive == otherPos)
				points.add(new Vector2(p.getVertices()[i]));
			intersection = Utils.intersectionLineSegments(p.getVertices()[i], p.getVertices()[(i+1)%p.getVertices().length], q, q2);
			if (intersection != null)
				points.add(new Vector2(intersection));
		}
		return new Polygon(ConvexPolygon.makeHullFromPoints(points).toArray(new Vector2[0]));
	}
}
