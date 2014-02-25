package cs5643.particles;

import javax.vecmath.Vector3d;

/**
 * A class containing functions for computing the poly6 kernel, and
 * the gradient of the spiky kernel.
 * @author Chris
 *
 */
public class Kernel {

	/**
	 * The poly6 kernel.
	 * 
	 * "Smearing distance" depends on the constant KERNEL_RADIUS_H.
	 * 
	 * @param r The distance from the particle.
	 * @return
	 */
	public static double poly6(double r) {
		double term1 = 315. / (64. * Math.PI * Constants.H9);
		double term2 = Math.pow(Constants.H2 - (r*r), 3);
		return term1 * term2;
	}
	
	public static double poly6(Particle i, Particle j) {
		return poly6(i.x.distance(j.x));
	}
	
	/**
	 * The magnitude of the gradient of the spiky kernel.
	 * 
	 * @param r
	 * @return
	 */
	public static double grad_spiky(double r) {
		if(r == 0) {
			return 0;
		}
		double term1 = -45. / (Math.PI * Constants.H6);
		double term2 = Math.pow(Constants.KERNEL_RADIUS_H - r, 2);
		return term1 * term2;
	}
	
	/**
	 * Evaluates spiky(p1 - p2) and writes the result to result.
	 * 
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static void grad_spiky(Vector3d result, Particle p1, Particle p2) {
		result.set(p1.x);
		result.sub(p2.x);
		if(result.x == 0 && result.y == 0 && result.z == 0) {
			return;
		}
		double magn = grad_spiky(result.length());
		result.normalize();
		result.scale(magn);
	}
	
}
