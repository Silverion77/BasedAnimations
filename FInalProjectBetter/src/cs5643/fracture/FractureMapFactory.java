package cs5643.fracture;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;

public class FractureMapFactory {

	public static FractureMap generateVoronoi(List<Vector2> controlPts) {
		ArrayList<ConvexPolygon> res = new ArrayList<ConvexPolygon>();
		Vector2 ortho = new Vector2();
		Vector2 midpt = new Vector2();
		for (Vector2 pt : controlPts) {
			Polygon p = new Rectangle(1, 1);
			p.translate(.5, .5);
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

	public static Polygon vectorConvexHullIntersection(Polygon p, Vector2 midpt, Vector2 intersector, Vector2 controlPt) {
		ArrayList<Vector2> points = new ArrayList<Vector2>();
		Vector2 q = midpt.sum(intersector.product(2));
		Vector2 q2 = midpt.difference(intersector.product(2));
		boolean positive = Utils.cross2(intersector, midpt.difference(controlPt)) >= 0;
		boolean otherPos;
		Vector2 intersection;
		for (int i = 0; i < p.getVertices().length; i++) {
			otherPos = Utils.cross2(intersector, midpt.difference(p.getVertices()[i])) >= 0;
			if (positive == otherPos)
				points.add(new Vector2(p.getVertices()[i]));
			intersection = Utils.intersectionLineSegments(p.getVertices()[i], p.getVertices()[(i+1)%p.getVertices().length], q, q2);
			if (intersection != null)
				points.add(new Vector2(intersection));
		}
		return new Polygon(ConvexPolygon.makeHullFromPoints(points).toArray(new Vector2[0]));
	}

	public static FractureMap buildMap(File file) throws FileNotFoundException, BadMapException {
		Scanner s = new Scanner(file);
		int line = 0;
		ArrayList<ConvexPolygon> res = new ArrayList<ConvexPolygon>();
		ArrayList<Vector2> points = new ArrayList<Vector2>();
		while (s.hasNextLine()) {
			String next = s.nextLine();
			line++;
			next = next.trim();
			if (next.startsWith("#") || next.isEmpty()) {
				System.out.println(points.size());
				if (points.size() == 0) continue;
				if (points.size() < 3) {
					s.close();
					throw new BadMapException(line, "Convex in map must have at least 3 vertices.");
				}
				res.add(new ConvexPolygon(new Polygon(ConvexPolygon.makeHullFromPoints(points).toArray(new Vector2[0]))));
				points.clear();
				continue;
			}
			String[] tokens = next.split(" ");
			Vector2 pt = new Vector2();
			pt.x = Double.parseDouble(tokens[0]);
			pt.y = Double.parseDouble(tokens[1]);
			points.add(pt);
		}
		s.close();
		if (points.size() > 1) {
			if (points.size() < 3)
				throw new BadMapException(line, "Convex in map must have at least 3 vertices.");
			res.add(new ConvexPolygon(new Polygon(ConvexPolygon.makeHullFromPoints(points).toArray(new Vector2[0]))));
		}
		return new FractureMap(res, true);
	}

	public static void saveMap(File file, FractureMap fractureMap) throws IOException {
		if (!file.exists())
			file.createNewFile();
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		for (ConvexPolygon cp : fractureMap.getPolygons()) {
			for (Vector2 pt : cp.getVertices()) {
				bw.write(pt.x + " " + pt.y + "\n");
			}
			bw.write("##\n");
		}
		bw.close();
	}

	/** An Exception class for dealing with bad input fracture maps. */
	public static class BadMapException extends Exception {
		public BadMapException(String s) {
			super(s);
		}

		public BadMapException(int line, String s) {
			super("Line " + line + ": " + s);
		}
	}
}
