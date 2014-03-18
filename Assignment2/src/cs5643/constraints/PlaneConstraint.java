package cs5643.constraints;

import javax.vecmath.Vector3d;

import cs5643.particles.Particle;
import cs5643.particles.CollisionPlane;

/**
 * A class that represents a collision constraint between one particle
 * and an infinite plane.
 * 
 * @author Chris
 *
 */
public class PlaneConstraint extends Constraint {
	
	private Particle particle;
	private CollisionPlane plane;
	
	private Vector3d temp;
	
	/**
	 * Creates a plane collision constraint with a stiffness of 1.
	 */
	public PlaneConstraint(Particle particle, CollisionPlane plane) {
		// By default, stiffness of 1.
		this(particle, plane, 1);
	}
	
	public PlaneConstraint(Particle particle, CollisionPlane plane, double k) {
		super(k);
		this.particle = particle;
		this.plane = plane;
		temp = new Vector3d();
	}

	@Override
	public double evaluate() {
		temp.set(particle.x_star);
		temp.sub(plane.getPointOnPlane());
		double dot = temp.dot(plane.getNormal());
		return dot;
	}

	@Override
	public boolean isSatisfied(double d) {
		return d >= 0;
	}

	/**
	 * Corrects the particle's position so that it is not on the wrong
	 * side of the plane.
	 */
	public void project() {
		double dot = evaluate();
		if(isSatisfied(dot)) {
			return;
		}
		// temp = pointOnPlane - target (points from target to point on the plane)
		temp.set(plane.getPointOnPlane());
		temp.sub(particle.x_star);
		// project temp (a) onto normal (b); luckily normal is a unit vector, so (a dot b) * b will do
		double ab = temp.dot(plane.getNormal());
		temp.set(plane.getNormal());
		temp.scale(ab * stiffness_k);
		particle.x_star.add(temp);
	}

	@Override
	/**
	 * I don't think this is really necessary for plane collisions.
	 */
	public Vector3d gradient(Particle p_j) {
		return null;
	}

}
