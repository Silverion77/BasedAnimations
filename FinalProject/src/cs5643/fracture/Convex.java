package cs5643.fracture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.media.opengl.GL2;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

/**
 * A convex polygon, represented as a centroid in world space, and a
 * set of vertices in body space.
 * 
 * The list of vertices is counter-clockwise.
 * 
 * @author Chris
 *
 */
public class Convex {
	
	private ArrayList<Point2d> points;
	private double angle;
	private double mass;
	
	private boolean pinned = false;
	
	public Point2d x;
	public Point2d x_star;
	
	public Vector2d force, v;
	public double torque, angularVelocity;
	
	private double boundingRadius;
	
	private double inertia;
	
	public Convex(Collection<Point2d> ps) {
		this.points = new ArrayList<Point2d>();
		this.points.addAll(ps);
		
		mass = 1;
		
		double accx = 0;
		double accy = 0;
		double area = 0;
		
		v = new Vector2d();
		force = new Vector2d();
		
		angle = 0;
		angularVelocity = 0;
		torque = 0;
		
		// Centroid computed as per http://en.wikipedia.org/wiki/Centroid
		for(int i = 0; i < points.size(); i++) {
			int next = i + 1;
			if (i == points.size() - 1) {
				next = 0;
			}
			double product = (points.get(i).x * points.get(next).y - points.get(next).x * points.get(i).y);
			area +=  product / 2;
			accx += (points.get(i).x + points.get(next).x) * product;
			accy += (points.get(i).y + points.get(next).y) * product;
		}
		
		accx /= (6 * area);
		accy /= (6 * area);
		
		x = new Point2d(accx, accy);
		x_star = new Point2d();
		x_star.set(x);
		
		for(Point2d p : points) {
			p.x -= accx;
			p.y -= accy;
		}
		
		temp.set(0,0);
		for(Point2d p : points) {
			boundingRadius = Math.max(boundingRadius, p.distance(temp));
		}
		
		inertia = momentOfInertia();
	}
	
	public List<Point2d> getPoints() {
		return points;
	}
	
	public double boundingRadius() {
		return boundingRadius;
	}
	
	public int nextCCW(int i) {
		return (i+1) % points.size();
	}
	
	public int nextCW(int i) {
		return (i+1) % points.size();
	}
	
	/**
	 * Computes moment of inertia of the convex by splitting into triangles
	 * and computing each of those.
	 * http://en.wikipedia.org/wiki/List_of_area_moments_of_inertia
	 * @return
	 */
	private double momentOfInertia() {
		double moi = 0;
		double totalArea = 0;
		for(int i = 0; i < points.size(); i++) {
			int next = (i+1) % points.size();
			double a = points.get(i).distance(points.get(next));
			double base = a;
			double b = points.get(next).distance(x);
			double c = x.distance(points.get(i));
			double[] sides = {a, b, c};
			a = sides[2];
			b = sides[1];
			c = sides[0];
			Arrays.sort(sides);
			// stable version of Heron's formula
			double area = Math.sqrt((a + (b+c)) * (c - (a-b)) * (c + (a-b)) * (a + (b-c))) / 4;
			totalArea += area;
			double height = 2 * area / base;
			moi += (base * height * height * height) / 12;
		}
		return moi * mass / totalArea;
	}
	
	Point2d temp = new Point2d();
	
	public void display(GL2 gl) {
		gl.glBegin(GL2.GL_POLYGON);
		gl.glColor3f(0f, 0.2f, 0.8f);
		for(Point2d p : points) {
			pointToWorldSpace(p, temp);
			gl.glVertex2d(temp.x, temp.y);
		}
		gl.glEnd();
	}
	
	public void pointToWorldSpace(Point2d input, Point2d output) {
		output.x = Math.cos(angle) * input.x - Math.sin(angle) * input.y + x_star.x;
		output.y = Math.sin(angle) * input.x + Math.cos(angle) * input.y + x_star.y;
	}
	
	public boolean pointInPolygon(Point2d p) {
		return pointInPolygon(p.x, p.y);
	}
	
	Point2d before = new Point2d();
	Point2d after = new Point2d();
	
	/**
	 * Determines if the point (x,y) is in the polygon via ray casting.
	 * http://en.wikipedia.org/wiki/Point_in_polygon
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean pointInPolygon(double x, double y) {
		int numCrossings = 0;
		for(int i = 0; i < points.size(); i++) {
			int next = (i + 1) % points.size();
			pointToWorldSpace(points.get(i), before);
			pointToWorldSpace(points.get(next), after);
			
			double beforeX = before.x;
			double beforeY = before.y;
			double afterX = after.x;
			double afterY = after.y;
			
			// Compute the intersection of the side with the horizontal line passing through (x,y)
			double slope = (afterY - beforeY) / (afterX - beforeX);
			double dy = y - beforeY;
			double dx = dy / slope;
			double intersection_x = beforeX + dx;
			if(intersection_x < x) {
				continue;
			}
			else if(beforeY == y) {
				continue;
			}
			else if(afterY == y) {
				continue;
			}
			// See if the side crosses the edge on the correct side of the ray
			else if((beforeY > y && afterY < y) || (beforeY < y && afterY > y)) {
				numCrossings++;
			}
		}
		return (numCrossings % 2 == 1);
	}
	
	Vector2d tempvec = new Vector2d();
	
	public void applyForceAtPoint(Vector2d f, Point2d r) {
		tempvec.sub(r, x);
		double cross = Utils.cross2d(tempvec, f);
		torque += cross;
		force.add(f);
	}

	public double getMass() {
		return mass;
	}
	
	public double getInertia() {
		return inertia;
	}
	
	public Vector2d getVelocity() {
		return v;
	}
	
	public double getAngularVelocity() {
		return angularVelocity;
	}

	public void applyWrenchW(Vector2d f, double tau) {
		// TODO Figure out if we need this
	}
	
	public boolean isPinned() {
		return pinned;
	}
	
	public void setPinned(boolean b) {
		pinned = b;
	}
	
	public void putOnFloor() {
		double min_y = x_star.y;
		for (Point2d p : points) {
			pointToWorldSpace(p, temp);
			double new_min = temp.y;
			if(new_min < min_y) {
				min_y = new_min;
			}
		}
		if(min_y < 0) {
			x_star.y -= min_y;
		}
	}
	
	public void clearForces() {
		force.set(0,0);
		torque = 0;
	}
	
	public void applyAccelerations(double dt) {
		Utils.acc(v, dt / getMass(), force);
		angularVelocity += dt * torque / inertia;
	}
	
	public void applyVelocities(double dt) {
		Utils.acc(x_star, dt, v);
		angle += dt * angularVelocity;
	}
	
	public void dampVelocities() {
		angularVelocity *= Constants.ANGULAR_DAMP;
		v.scale(Constants.LINEAR_DAMP);
	}
	
	public void updateVelocity(double dt) {
		if(!pinned) {
			v.set(x_star);
			v.sub(x);
			v.scale(1.0 / dt);
		}
		else {
			v.set(0,0);
		}
		
	}
	
	public void finalizePrediction() {
		if(!pinned) {
			x.set(x_star);
		}
		else {
			x_star.set(x);
		}
	}
	
	private static ArrayList<Point2d> pointsc1 = new ArrayList<Point2d>();
	private static ArrayList<Point2d> pointsc2 = new ArrayList<Point2d>();
	private static ArrayList<Point2d> intersections = new ArrayList<Point2d>();
	
	private static ArrayList<Point2d> intersections(Convex c1, Convex c2) {
		intersections.clear();
		pointsc1.clear();
		for(Point2d p : c1.points) {
			Point2d p2 = new Point2d();
			c1.pointToWorldSpace(p, p2);
			pointsc1.add(p2);
		}
		pointsc2.clear();
		for(Point2d p : c2.points) {
			Point2d p2 = new Point2d();
			c2.pointToWorldSpace(p, p2);
			pointsc2.add(p2);
		}

		for(Point2d p1 : pointsc1) {
			if(c2.pointInPolygon(p1)) {
				intersections.add(p1);
			}
		}
		for(Point2d p2 : pointsc2) {
			if(c1.pointInPolygon(p2)) {
				intersections.add(p2);
			}
		}
		return intersections;
	}
	
	private static void findCrossings(Convex c1, Convex c2, ArrayList<Point2d> acc) {
		for(int i = 0; i < pointsc1.size(); i++) {
			Point2d p1 = pointsc1.get(i);
			Point2d p2 = pointsc1.get((i+1) % pointsc1.size());
			for(int j = 0; j < pointsc1.size(); j++) {
				Point2d q1 = pointsc2.get(j);
				Point2d q2 = pointsc2.get((j+1) % pointsc2.size());
				Point2d inter = Utils.intersectionLineSegments(p1, p2, q1, q2);
				if(inter != null) acc.add(inter);
			}
		}
	}
	
	public static boolean intersects(Convex c1, Convex c2) {
		return !intersections(c1, c2).isEmpty();
	}
	
	public static Convex intersection(Convex c1, Convex c2) {
		ArrayList<Point2d> ps = intersections(c1,c2);
		findCrossings(c1, c2, ps);
		return Utils.makeHullFromPoints(ps);
	}
}
