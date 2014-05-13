import java.util.ArrayList;

import javax.media.opengl.GL2;

import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Mass;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;

public class FractureMap {

	private ConvexPolygon[] polygons;
	private ArrayList<Polygon> temp_polys;
	private World mapWorld;

	public FractureMap(ArrayList<ConvexPolygon> ps, boolean baseMap) {
		polygons = ps.toArray(new ConvexPolygon[0]);
		temp_polys = new ArrayList<Polygon>();
		mapWorld = new World();

		double min_x = Double.POSITIVE_INFINITY;
		double max_x = Double.NEGATIVE_INFINITY;
		double min_y = Double.POSITIVE_INFINITY;
		double max_y = Double.NEGATIVE_INFINITY;

		for(ConvexPolygon p : polygons) {
			mapWorld.addBody(p);
			p.setMass(Mass.Type.INFINITE);
			for(Vector2 v : p.getVerticesWorldSpace()) {
				max_x = Math.max(v.x, max_x);
				max_y = Math.max(v.y, max_y);
				min_x = Math.min(v.x, min_x);
				min_y = Math.min(v.y, min_y);
			}
		}
		if(baseMap) {
			if(Math.abs((max_x - min_x) - 1) > 0.0001) {
				throw new IllegalArgumentException("width of fracture map must be 1");
			}
			if(Math.abs((max_y - min_y) - 1) > 0.0001) {
				throw new IllegalArgumentException("height of fracture map must be 1");
			}
		}
	}

	private ArrayList<Vector2> temp_vecs = new ArrayList<Vector2>();

	/**
	 * Returns a new FractureMap that is the original map scaled to have side length
	 * scale, and translated so that its center is at the vector translate.
	 * @param translate
	 * @param scale
	 * @return
	 */
	public FractureMap translateAndScale(Vector2 translate, double scale) {
		temp_vecs.clear();
		ArrayList<ConvexPolygon> copies = new ArrayList<ConvexPolygon>();
		for(ConvexPolygon p : polygons) {
			for(Vector2 v : p.getVerticesWorldSpace()) {
				Vector2 copy_vec = v.copy();
				copy_vec.multiply(scale + 2 * Constants.FRACTURE_PAD);
				copy_vec.add(translate);
				copy_vec.subtract(scale / 2 + Constants.FRACTURE_PAD, scale / 2 + Constants.FRACTURE_PAD);
				temp_vecs.add(copy_vec);
			}
			ConvexPolygon copy = new ConvexPolygon(temp_vecs);
			copies.add(copy);
			temp_vecs.clear();
		}
		FractureMap other = new FractureMap(copies, false);
		return other;
	}
	
	public ArrayList<Polygon> fracture(Polygon p, Transform t) {
		temp_polys.clear();
		for(ConvexPolygon mapPoly : polygons) {
			Polygon pol = Utils.intersect(p, t, mapPoly);
			if(pol != null) {
				temp_polys.add(pol);
			}
		}
		return temp_polys;
	}

	public ArrayList<Polygon> fracture(ConvexPolygon p) {
		return fracture(p.getPolygon(), p.getTransform());
	}
	
	public void printPoints() {
		System.out.println("Fracture map");
		for(ConvexPolygon mapPoly : polygons) {
			for(Vector2 v : mapPoly.getVerticesWorldSpace()) {
				System.out.println(v);
			}
		}
	}
	
	public void display(GL2 gl) {
		for(ConvexPolygon p : polygons) {
			p.display(gl);
		}
	}

}
