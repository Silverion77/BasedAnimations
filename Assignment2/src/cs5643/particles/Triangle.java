package cs5643.particles;

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

	/** Computes the unit-length normal associated with this triangle. */
	public Vector3d getNormal() {
		Vector3d e0 = new Vector3d();
		Vector3d e1 = new Vector3d();
		e0.sub(v1.x, v0.x);
		e1.sub(v2.x, v1.x);
		Vector3d normal = new Vector3d();
		normal.cross(e1, e0);
		normal.normalize();
		return normal;
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