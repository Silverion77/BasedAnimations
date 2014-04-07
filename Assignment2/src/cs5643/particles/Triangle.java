package cs5643.particles;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * The face of a triangular mesh. See Mesh.java for details.
 *
 * @author Eston Schweickart, February 2014
 * @author Ari Karo, March 2014
 */
public class Triangle {

	/** The first vertex of this triangle. */
	public Vertex v0;

	/** The second vertex of this triangle. */
	public Vertex v1;

	/** The third vertex of this triangle. */
	public Vertex v2;

	/** Constructs a Triangle object from 3 vertices. */
	public Triangle(Vertex v0, Vertex v1, Vertex v2) {
		this.v0 = v0;
		this.v1 = v1;
		this.v2 = v2;
		double mass = 1./3 * area() * Constants.REST_DENSITY;
		v0.m += mass;
		v1.m += mass;
		v2.m += mass;
	}
	
	private Vector3d normal = new Vector3d();

	/** Computes the unit-length normal associated with this triangle. */
	public Vector3d getNormal() {
		Vector3d e0 = new Vector3d();
		Vector3d e1 = new Vector3d();
		e0.sub(v1.x, v0.x);
		e1.sub(v2.x, v1.x);
		normal.cross(e1, e0);
		normal.normalize();
		return normal;
	}
	
	private Vector3d predicted_n = new Vector3d();
	
	public Vector3d getPredictedNormal() {
		Vector3d e0 = new Vector3d();
		Vector3d e1 = new Vector3d();
		e0.sub(v1.x_star, v0.x_star);
		e1.sub(v2.x_star, v1.x_star);
		predicted_n.cross(e1, e0);
		predicted_n.normalize();
		return predicted_n;
	}
	
	public boolean contains(Particle p) {
		if(p instanceof Vertex) {
			Vertex v = (Vertex)p;
			return v == v0 || v == v1 || v == v2;
		}
		return false;
	}
	
	private Vector3d temp = new Vector3d();
	private Vector3d temp2 = new Vector3d();
	
	public double distanceFromPoint(Point3d p) {
		temp.sub(v0.x_star, p);
//		getPredictedNormal();
		double dist_vert = predicted_n.dot(temp);
		temp.scale(dist_vert, predicted_n);
		temp.add(p, temp);
		// temp now has the nearest point that is coplanar with the triangle
		double dist_horiz = Math.min(Utils.pointToLineDistance(p, v0.x_star, v1.x_star),
				Math.min(Utils.pointToLineDistance(p, v1.x_star, v2.x_star),
						Utils.pointToLineDistance(p, v2.x_star, v0.x_star)));
		temp.set(dist_horiz, dist_vert, 0);
		return temp.length();
	}
	
	public void addPenaltyForce(Particle p, double coeff) {
		if(contains(p)) return;
		double dist = distanceFromPoint(p.x_star);
		if(dist == 0) {
			return;
		}
		if(dist <= 10 * Constants.H_THICKNESS) {
			temp.sub(v0.x_star, p.x_star);
			double scale_factor = temp.dot(predicted_n);
			temp.sub(v1.x_star, p.x_star);
			scale_factor = Math.max(scale_factor, temp.dot(predicted_n));
			temp.sub(v2.x_star, p.x_star);
			scale_factor = Math.max(scale_factor, temp.dot(predicted_n));
			Utils.acc(p.penalty_f, coeff * scale_factor * Constants.H_THICKNESS / dist, predicted_n);
			Utils.acc(v0.penalty_f, -(coeff * scale_factor * Constants.H_THICKNESS / dist) / 3, predicted_n);
			Utils.acc(v1.penalty_f, -(coeff * scale_factor * Constants.H_THICKNESS / dist) / 3, predicted_n);
			Utils.acc(v2.penalty_f, -(coeff * scale_factor * Constants.H_THICKNESS / dist) / 3, predicted_n);
		}
	}
	
	/** Computes the area of the triangle using the vertices initial positions. */
	private double area() {
		Vector3d a = new Vector3d(v2.x0);
		Vector3d b = new Vector3d(v1.x0);
		a.sub(v0.x0);
		b.sub(v0.x0);
		double area = .5 * a.length() * b.length();
		a.normalize();
		b.normalize();
		return area * Math.sin(Math.acos(a.dot(b)));
	}
}