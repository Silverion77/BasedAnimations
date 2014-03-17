package cs5643.constraints;

import javax.vecmath.Vector3d;

import cs5643.particles.Constants;
import cs5643.particles.Particle;

/**
 * Abstract class that represents any constraint.
 * 
 * Subclasses will need to provide their own constructors that supply
 * the sets of particles that the constraints apply to. evaluate() and
 * project() both assume that the constraint has internal references to
 * these particles, so they are not given as arguments.
 * 
 * @author Chris
 *
 */
public abstract class Constraint {
	
	private double stiffness_k;
	
	/**
	 * Creates a constraint with stiffness k.
	 * @param k - stiffness
	 */
	public Constraint(double k) {
		stiffness_k = Math.pow((1 - k), 1. / Constants.NUM_SOLVER_ITERATIONS);
		stiffness_k = 1 - stiffness_k;
	}
	
	/**
	 * Computes the value of this constraint, evaluated with respect to
	 * whichever particles are referenced in this constraint.
	 * @return The value of the constraint.
	 */
	public abstract double evaluate();
	
	/**
	 * Returns whether or not the constraint is satisfied by the value d.
	 * The way this method is overridden determines if the constraint is
	 * an equality constraint or an inequality constraint.
	 * 
	 * @param d The constraint value to be checked.
	 * @return True if the constraint is satisfied, false otherwise.
	 */
	public abstract boolean isSatisfied(double d);

	/**
	 * Adjusts the positions of particles that this constraint applies
	 * to in order to satisfy the constraint.
	 * 
	 * In reality, this is the only method that should ever be called
	 * at all from outside.
	 */
	public abstract void project();
	
	/**
	 * The gradient of this constraint with respect to particle p_j.
	 * @param p_j The particle w.r.t. which the gradient is taken.
	 * @return A vector that is the gradient.
	 */
	public abstract Vector3d gradient(Particle p_j);
	
}
