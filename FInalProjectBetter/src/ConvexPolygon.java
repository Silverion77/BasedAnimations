import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.media.opengl.GL2;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.joint.MouseJoint;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;

public class ConvexPolygon extends Body {

	private Polygon polygon;
	private float[] color;

	private boolean pinned = false;

	private MouseJoint mouseJoint = null;

	public ConvexPolygon(ArrayList<Vector2> points) {
		ArrayList<Vector2> borderPoints = makeHullFromPoints(points);
		polygon = new Polygon(borderPoints.toArray(new Vector2[0]));
		setupPolygon(polygon);
	}

	public ConvexPolygon(Polygon p) {
		setupPolygon(p);
	}

	private Vector2[] worldVertices = null;
	private long timestamp;

	public Vector2[] getVerticesWorldSpace(long time) {
		if(worldVertices == null || time > timestamp) {
			worldVertices = getVerticesWorldSpace();
			timestamp = time;
		}
		return worldVertices;
	}

	public Vector2[] getVerticesWorldSpace() {
		Vector2[] local = polygon.getVertices();
		Vector2[] world = new Vector2[local.length];
		for(int i = 0; i < world.length; i++) {
			world[i] = this.getWorldPoint(local[i]);
		}
		return world;
	}
	
	public Vector2[] getVertices() {
		return polygon.getVertices();
	}

	public void setupPolygon(Polygon p) {
		color = new float[4];
		// Randomly color polygons
		this.color[0] = (float)Math.random() * 0.5f + 0.5f;
		this.color[1] = (float)Math.random() * 0.5f + 0.5f;
		this.color[2] = (float)Math.random() * 0.5f + 0.5f;
		this.color[3] = 1.0f;
		this.addFixture(polygon, 1);
		this.setMass();
		worldVertices = new Vector2[polygon.getVertices().length];
	}

	public void display(GL2 gl) {
		// save the original transform
		gl.glPushMatrix();

		// transform the coordinate system from world coordinates to local coordinates  
		gl.glTranslated(this.transform.getTranslationX(), this.transform.getTranslationY(), 0.0);
		// rotate about the z-axis
		gl.glRotated(Math.toDegrees(this.transform.getRotation()), 0.0, 0.0, 1.0);

		gl.glColor4fv(this.color, 0);

		gl.glBegin(GL2.GL_POLYGON);
		for (Vector2 v : polygon.getVertices()) {
			gl.glVertex2d(v.x, v.y);
		}
		gl.glEnd();

		gl.glColor3f(0, 0, 0);

		gl.glBegin(GL2.GL_LINE_LOOP);
		for (Vector2 v : polygon.getVertices()) {
			gl.glVertex2d(v.x, v.y);
		}
		gl.glEnd();

		// restore the old transform
		gl.glPopMatrix();
	}

	public boolean isPinned() {
		return pinned;
	}

	public void setPosition(double x, double y) {
		this.translateToOrigin();
		this.translate(x,y);
	}

	public boolean pointInPolygon(Vector2 v) {
		return polygon.contains(v);
	}

	public void pin(Vector2 v) {
		pinned = true;
		mouseJoint = new MouseJoint(this, this.getWorldCenter(), 2, 0.5, 200000);
		mouseJoint.setTarget(v);
		this.getWorld().addJoint(mouseJoint);
	}

	public void setJointTarget(Vector2 v) {
		mouseJoint.setTarget(v);
	}

	public void unpin() {
		pinned = false;
		this.getWorld().removeJoint(mouseJoint);
		mouseJoint = null;
	}

	/**
	 * Tests if p1,p2,p3 (in that order) form a counterclockwise turn.
	 * Returns positive if they are CCW, negative if they are CW, 0 if they are co-linear.
	 * @param p1
	 * @param p2
	 * @param p3
	 * @return
	 */
	public static double ccw(Vector2 p1, Vector2 p2, Vector2 p3) {
		return (p2.x - p1.x) * (p3.y - p1.y) - (p2.y - p1.y) * (p3.x - p1.x);
	}

	private static ArrayList<Vector2> upper = new ArrayList<Vector2>();
	private static ArrayList<Vector2> lower = new ArrayList<Vector2>();
	private static PointSorter sorter = new PointSorter();

	/**
	 * Implements the monotone chain algorithm for computing a convex hull from
	 * a set of points.
	 * 
	 * http://en.wikibooks.org/wiki/Algorithm_Implementation/Geometry/Convex_hull/Monotone_chain
	 * 
	 * @param pointSet
	 * @return
	 */
	public static ArrayList<Vector2> makeHullFromPoints(ArrayList<Vector2> pointSet) {
		upper.clear();
		lower.clear();
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
		if(lower.size() > 0) {
			lower.remove(lower.size() - 1);
		}
		if(upper.size() > 0) {
			upper.remove(upper.size() - 1);
		}
		lower.addAll(upper);
		return lower;
	}
}

class PointSorter implements Comparator<Vector2> {

	@Override
	public int compare(Vector2 p1, Vector2 p2) {
		if(p1.x < p2.x) {
			return -1;
		}
		else if(p1.x == p2.x && p1.y < p2.y) {
			return -1;
		}
		else if(p1.x == p2.x && p1.y == p2.y) {
			return 0;
		}
		else return 1;
	}

}
