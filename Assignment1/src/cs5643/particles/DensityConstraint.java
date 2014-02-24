package cs5643.particles;

import java.util.List;

import javax.vecmath.Vector3d;

/**
 * A constraint associated with each particle that keeps the density roughly
 * uniform. Contains methods for evaluating the constraint and its gradient
 * (with respect to any other particle k), as well as for computing the value
 * lambda for this constraint's particle i.
 * 
 * @author Chris
 *
 */
public class DensityConstraint {

	private Particle i;
	private Vector3d grad_result;
	private Vector3d temp;
	
	public DensityConstraint(Particle p) {
		this.i = p;
		temp = new Vector3d();
		grad_result = new Vector3d();
	}
	
	/**
	 * Evaluates the constraint function.
	 * @param ps
	 * @return
	 */
	public double evaluate(List<Particle> ps) {
		return density(ps) / Constants.REST_DENSITY - 1;
	}
	
	private double density(List<Particle> ps) {
		double density = 0;
		for(Particle j : ps) {
			density += Kernel.poly6(i, j);
		}
		return density;
	}
	
	/**
	 * Returns the gradient of the constraint function, as a vector,
	 * with respect to particle k.
	 * 
	 * @param ps A list of neighboring particles (distance < h).
	 * @param k The particle to take the gradient with respect to.
	 * @return
	 */
	public Vector3d gradient(List<Particle> ps, Particle k) {
		grad_result.set(0,0,0);
		if(i.equals(k)) {
			for(Particle j : ps) {
				Kernel.grad_spiky(temp, i, j);
				grad_result.add(temp);
			}
		}
		else {
			Kernel.grad_spiky(grad_result, i, k);
			grad_result.negate();
		}
		grad_result.scale(1. / Constants.REST_DENSITY);
		return grad_result;
	}
	
	/**
	 * For particle i associated with this constraint, computes
	 * the Lagrange multiplier lambda_i (later used for computing
	 * position corrections for density).
	 * 
	 * @param ps A list of neighboring particles (distance < h).
	 * @return
	 */
	public double compute_lambda(List<Particle> ps) {
		double numer = this.evaluate(ps);
		double denom = Constants.MAGIC_EPSILON;
		for (Particle k : ps) {
			double grad_magn = Kernel.grad_spiky(i.x.distance(k.x));
			denom += grad_magn * grad_magn;
		}
		double l_i = -(numer/denom);
		i.lambda_i = l_i;
		return l_i;
	}
	
}
