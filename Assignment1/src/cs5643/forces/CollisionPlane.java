package cs5643.forces;

import javax.media.opengl.GL2;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import cs5643.particles.Force;
import cs5643.particles.Particle;
import cs5643.particles.ParticleSystem;

public class CollisionPlane implements Force {
	
	// ax + by + cz + d = 0
	private double a, b, c, d;
	
	// Normal unit vector to this plane
	private Vector3d normal;
	
	// Temporary vector for doing calculations
	private Vector3d temp;
	
	private Point3d pointOnPlane;
	
	private ParticleSystem ps;
	
	/**
	 * Constructs a plane with normal vector <a,b,c> passing through <x_0, y_0, z_0>.
	 */
	public CollisionPlane(double a, double b, double c,
			double x_0, double y_0, double z_0, ParticleSystem p) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.ps = p;
		
		this.d = -(a * x_0 + b * y_0 + c * z_0);
		
		normal = new Vector3d(a,b,c);
		normal.normalize();
		
		pointOnPlane = new Point3d(x_0, y_0, z_0);
		
		temp = new Vector3d();
	}
	
	/**
	 * Returns the distance squared from point p to this plane, along the normal.
	 * 
	 * @param p The point.
	 * @return The distance squared from p to this plane.
	 */
	private double getDistanceSq(Point3d p) {
		double numer = a * p.x + b * p.y + c * p.z + d;
		double denom = a * a + b * b + c * c;
		return (numer * numer) / denom;
	}
	
	public boolean passedThrough(Particle p) {
		temp.set(p.getPos());
		temp.sub(pointOnPlane);
		double dot = temp.dot(normal);
		return (dot < 0);
	}

	@Override
	public void applyForce() {
		for(Particle particle : ps.P) {
			if(getDistanceSq(particle.getPos()) < 0.001) {
				// apply the normal force
			}
		}
		
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
