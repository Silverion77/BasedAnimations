package cs5643.particles;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class CollisionPlane {

	private Vector3d normal;
	private Point3d pointOnPlane;
	private Vector3d temp;
	
	/**
	 * Creates a plane with normal vector (a,b,c) that passes through
	 * point (x_0, y_0, z_0).
	 */
	public CollisionPlane(double a, double b, double c, double x_0, double y_0, double z_0) {
		normal = new Vector3d(a,b,c);
		normal.normalize();
		pointOnPlane = new Point3d(x_0, y_0, z_0);
		temp = new Vector3d();
	}
	
	public Vector3d getNormal() {
		return normal;
	}
	
	public Point3d getPointOnPlane() {
		return pointOnPlane;
	}

}
