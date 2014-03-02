package cs5643.forces;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import cs5643.particles.Force;
import cs5643.particles.Particle;
import cs5643.particles.ParticleSystem;

public class CollisionSphere implements Force {

	/** Radius of sphere graphic. */
	public static final double SPHERE_RADIUS = .2;

	/** Display list index. */
	private static int SPHERE_DISPLAY_LIST = -1;

	/** Center Position. */
	Point3d center = new Point3d();

	/** Velocity. */
	public Vector3d v = new Vector3d();

	// Temporary vector for doing calculations
	private Vector3d temp = new Vector3d();
	private Vector3d normal = new Vector3d();

	private ParticleSystem ps;

	public CollisionSphere(Point3d center, Vector3d v, ParticleSystem p) {
		this.center.set(center);
		this.v = v;
		ps = p;
	}

	public CollisionSphere(double x, double y, double z, Vector3d v, ParticleSystem p) {
		this.center.setX(x);
		this.center.setY(y);
		this.center.setZ(z);
		this.v = v;
		ps = p;
	}

	public Point3d getPos() {
		return center;
	}

	/**
	 * Checks if the point p is in collision with this sphere.
	 * @param p
	 * @return
	 */
	public boolean detectCollision(Point3d p) {
		temp.set(p);
		temp.sub(center);
		return temp.length() < SPHERE_RADIUS;
	}

	/**
	 * Given the point target, add collision resolution vector to result.
	 * @param position
	 */
	public void addToMinCorrection(Vector3d result, Point3d target) {
		temp.set(0,0,0);
		normal.set(target);
		normal.sub(center);
		double diff = SPHERE_RADIUS - normal.length();
		if (diff > 0) {
			normal.normalize();
			normal.scale(diff);
			result.add(normal);
		}
	}

	@Override
	public void applyForce() {
		for(Particle particle : ps.P) {
			normal.set(particle.getPos());
			normal.sub(center);
			temp.set(particle.getForce());
			double Nf = normal.dot(temp);
			temp.scale(Nf);
			if (temp.dot(particle.getForce()) < 0) {
				temp.negate();
				particle.accumulateForce(temp.x, temp.y, temp.z);
			}
		}
	}
	
	public void updatePos(double dt) {
		temp.set(v);
		temp.scale(dt);
		center.add(temp);
	}

	@Override
	public ParticleSystem getParticleSystem() {
		return ps;
	}

	/** Draws spherical particle using a display list. */
	public void display(GL2 gl)
	{
		if(SPHERE_DISPLAY_LIST < 0) {// MAKE DISPLAY LIST:
			int displayListIndex = gl.glGenLists(1);
			GLU glu = GLU.createGLU();
			GLUquadric quadric = glu.gluNewQuadric();
			gl.glNewList(displayListIndex, GL2.GL_COMPILE);
			glu.gluSphere(quadric, SPHERE_RADIUS, 16, 8);
			gl.glEndList();
			glu.gluDeleteQuadric(quadric);
			glu.destroy();
			System.out.println("MADE DISPLAY LIST "+displayListIndex+" : "+gl.glIsList(displayListIndex));
			SPHERE_DISPLAY_LIST = displayListIndex;
		}

		/// COLOR: DEFAULT CYAN; GREEN IF HIGHLIGHTED
		float[] c = {1f, 0.2f, 0.2f, 1f};//default: cyan

		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, c, 0); // Color used by shader

		/// DRAW ORIGIN-CIRCLE TRANSLATED TO "p":
		gl.glPushMatrix();
		gl.glTranslated(center.x, center.y, center.z);
		gl.glCallList(SPHERE_DISPLAY_LIST); // Draw the particle
		gl.glPopMatrix();
	}

}
