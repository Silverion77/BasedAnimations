package cs5643.constraints;

import javax.vecmath.Vector3d;

import cs5643.particles.Constants;
import cs5643.particles.Particle;

/**
 * A constraint that tries to maintain the same distance between
 * two particles.
 * 
 * @author Chris
 *
 */
public class StretchConstraint extends Constraint {
	
	private double rest_len;
	private Particle p1, p2;
	private Vector3d temp;
	private Vector3d gradient_temp;
	
	public StretchConstraint(Particle p1, Particle p2) {
		this(p1.x.distance(p2.x), p1, p2);
	}
	
	public StretchConstraint(double l_0, Particle p1, Particle p2) {
		this(l_0, p1, p2, Constants.K_STRETCH);
	}
	
	/**
	 * Creates a new stretching constraint between two particles, with the specified
	 * rest length and stiffness.
	 * @param l_0
	 * @param p1
	 * @param p2
	 * @param k
	 */
	public StretchConstraint(double l_0, Particle p1, Particle p2, double k) {
		super(k);
		temp = new Vector3d();
		gradient_temp = new Vector3d();
		this.rest_len = l_0;
		this.p1 = p1;
		this.p2 = p2;
	}

	/**
	 * Returns the distance between the two particles in this constraint.
	 */
	@Override
	public double evaluate() {
		double dist = p1.x_star.distance(p2.x_star);
		return dist - rest_len;
	}

	/**
	 * Returns whether the distance is equal to the rest length.
	 */
	@Override
	public boolean isSatisfied(double d) {
		return (d == 0);
	}

	/**
	 * Computes the correction that will restore the rest length.
	 * Reference: equations 10 and 11 in PBD.
	 */
	@Override
	public void project() {
		double value = evaluate();
		if(isSatisfied(value)) {
			return;
		}
		if(p1.w() == 0 && p2.w() == 0) {
			return;
		}
		double w_ratio_1 = p1.w() / (p1.w() + p2.w());
		double w_ratio_2 = - p2.w() / (p1.w() + p2.w());
		computeN();
		temp.set(gradient_temp);
		temp.scale(-value * w_ratio_1 * stiffness_k);
		p1.x_star.add(temp);
		temp.set(gradient_temp);
		temp.scale(-value * w_ratio_2 * stiffness_k);
		p2.x_star.add(temp);
	}
	
	/**
	 * Sets temp to be the normal of the distance between the two.
	 */
	private void computeN() {
		gradient_temp.sub(p1.x_star, p2.x_star);
		gradient_temp.normalize();
	}

	/**
	 * Gradient of the stretching constraint. Shouldn't need to be called
	 * on its own.
	 */
	@Override
	public Vector3d gradient(Particle p_j) {
		if(p_j.equals(p1)) {
			computeN();
			return gradient_temp;
		}
		else if(p_j.equals(p2)) {
			computeN();
			gradient_temp.negate();
			return gradient_temp;
		}
		else {
			gradient_temp.set(0,0,0);
			return gradient_temp;
		}
	}

}
