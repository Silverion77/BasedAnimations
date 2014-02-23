package cs5643.forces;

import javax.media.opengl.GL2;
import javax.vecmath.Point3d;

import cs5643.particles.Force;
import cs5643.particles.ParticleSystem;

public class CollisionPlane implements Force {
	
	// ax + by + cz + d = 0
	private int a, b, c, d;
	
	private ParticleSystem ps;
	
	public CollisionPlane(int a, int b, int c, int d, ParticleSystem p) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.ps = p;
	}
	
	/**
	 * Returns the distance squared from point p to this plane, along the normal.
	 * 
	 * @param p The point.
	 * @return The distance squared from p to this plane.
	 */
	private double getDistanceSq(Point3d p) {
		double numer = Math.abs(a * p.x + b * p.y + c * p.z + d);
		double denom = a * a + b * b + c * c;
		return (numer * numer) / denom;
	}

	@Override
	public void applyForce() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void display(GL2 gl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ParticleSystem getParticleSystem() {
		return ps;
	}
	
	

}
