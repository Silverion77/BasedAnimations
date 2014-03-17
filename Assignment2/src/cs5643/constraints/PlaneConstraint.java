package cs5643.constraints;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import cs5643.particles.Particle;
import cs5643.particles.ParticleSystem;

/**
 * A class that represents a collision constraint between one particle
 * and an infinite plane.
 * 
 * @author Chris
 *
 */
public class PlaneConstraint extends Constraint {
	
	private double a,b,c,d;
	private Particle particle;
	
	// TODO: not sure how often we will be creating plane constraints, might not want new
	private Vector3d normal;
	private Point3d pointOnPlane;
	private Vector3d temp;
	
	/**
	 * Creates a plane collision constraint with a stiffness of 1.
	 */
	public PlaneConstraint(double a, double b, double c, double x_0, double y_0, double z_0, Particle p) {
		// By default, stiffness of 1.
		this(1, a, b, c, x_0, y_0, z_0, p);
	}
	
	public PlaneConstraint(double k, double a, double b, double c, double x_0, double y_0, double z_0, Particle p) {
		super(k);
		this.a = a;
		this.b = b;
		this.c = c;
		this.particle = p;
		this.d = -(a * x_0 + b * y_0 + c * z_0);
		normal = new Vector3d(a,b,c);
		normal.normalize();
		pointOnPlane = new Point3d(x_0, y_0, z_0);
		temp = new Vector3d();
	}

	@Override
	public double evaluate() {
		temp.set(particle.x);
		temp.sub(pointOnPlane);
		double dot = temp.dot(normal);
		return dot;
	}

	@Override
	public boolean isSatisfied(double d) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void project() {
		// TODO Auto-generated method stub

	}

	@Override
	public Vector3d gradient(Particle p_j) {
		// TODO Auto-generated method stub
		return null;
	}

}
