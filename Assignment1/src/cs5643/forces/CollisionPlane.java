package cs5643.forces;

import javax.media.opengl.GL2;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import cs5643.particles.Constants;
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
	
	/**
	 * Checks if the point p is in collision with this plane.
	 * @param p
	 * @return
	 */
	public double detectCollision(Point3d p) {
		temp.set(p);
		temp.sub(pointOnPlane);
		double dot = temp.dot(normal);
		return dot;
	}
	
	/**
	 * Given the point target, add collision resolution vector to result.
	 * @param position
	 */
	public void addToMinCorrection(Vector3d result, Point3d target) {
		// temp = pointOnPlane - target (points from target to point on the plane)
		temp.set(pointOnPlane);
		temp.sub(target);
		// project temp (a) onto normal (b); luckily normal is a unit vector, so (a dot b) * b will do
		double ab = temp.dot(normal);
		temp.set(normal);
		temp.scale(ab);
		result.add(temp);
	}

	/**
	 * Implements the normal force to this plane in case it is desired.
	 * Currently unused.
	 */
	@Override
	public void applyForce() {
		for(Particle particle : ps.P) {
			if(getDistanceSq(particle.getPos()) < 0.001) {
				temp.set(particle.getForce());
				double Nf = normal.dot(temp);
				temp.scale(Nf);
				if(temp.dot(particle.getForce()) < 0) {
					temp.negate();
					particle.accumulateForce(temp.x, temp.y, temp.z);	
				}
			}
		}
		
	}

	/**
	 * Displays the normal vector to this plane as a line segment
	 * extending out from the point on the plane.
	 */
	@Override
	public void display(GL2 gl) {
		gl.glBegin(GL2.GL_LINE_STRIP);
		temp.set(normal);
		temp.scale(0.2);
		temp.add(pointOnPlane);
		gl.glVertex3d(pointOnPlane.x, pointOnPlane.y, pointOnPlane.z);
		gl.glVertex3d(temp.x, temp.y, temp.z);
		gl.glEnd();
		
	}

	@Override
	public ParticleSystem getParticleSystem() {
		return ps;
	}
	
	/**
	 * Implements elastic collisions in case they are desired.
	 * Currently unused.
	 * 
	 * @param p
	 */
	public void reflect(Particle p) {
		temp.set(p.v);
		double Nx = normal.dot(temp);
		temp.scale(Nx);
		p.v.sub(temp);
		temp.scale(Constants.ELASTICITY_R);
		p.v.sub(temp);
	}
}
