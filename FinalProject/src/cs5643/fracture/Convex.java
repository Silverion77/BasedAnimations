package cs5643.fracture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.media.opengl.GL2;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

public class Convex {
	
	private ArrayList<Point2d> points;
	private double mass, angularMass, angularVelocity;
	
	private boolean pinned = false;
	
	public Point2d x;
	public Point2d x_star;
	
	public Vector2d force, v;
	
	public Convex(Collection<Point2d> ps) {
		this.points = new ArrayList<Point2d>();
		this.points.addAll(ps);
		
		mass = 1;
		
		double accx = 0;
		double accy = 0;
		double area = 0;
		
		v = new Vector2d();
		force = new Vector2d();
		
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
	}
	
	public void display(GL2 gl) {

		gl.glBegin(GL2.GL_POLYGON);
		gl.glColor3f(0f, 0.2f, 0.8f);
		for(Point2d p : points) {
			gl.glVertex2d(p.x + x.x, p.y + x.y);
		}
		gl.glEnd();
	}
	
	public boolean pointInPolygon(Point2d p) {
		return pointInPolygon(p.x, p.y);
	}
	
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
			Point2d before = points.get(i);
			Point2d after = points.get(next);
			
			double beforeX = before.x + this.x.x;
			double beforeY = before.y + this.x.y;
			double afterX = after.x + this.x.x;
			double afterY = after.y + this.x.y;
			
			// Compute the intersection of the side with the horizontal line passing through (x,y)
			double slope = (afterY - beforeY) / (afterX - beforeX);
			double dy = y - beforeY;
			double dx = dy / slope;
			double intersection_x = beforeX + dx;
			if(intersection_x < x) continue;
			// See if the side crosses the edge on the correct side of the ray
			else if((beforeY >= y && afterY <= y) || (beforeY <= y && afterY >= y)) {
				numCrossings += 1;
			}
		}
		return (numCrossings % 2 == 1);
	}
	
	public int hashCode() {
		return points.hashCode();
	}

	public double getMass() {
		return mass;
	}
	
	public double getMassAngular() {
		return angularMass;
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
			double new_min = x_star.y + p.y;
			if(new_min < min_y) {
				min_y = new_min;
			}
		}
		if(min_y < 0) {
			x_star.y -= min_y;
		}
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
	
}
