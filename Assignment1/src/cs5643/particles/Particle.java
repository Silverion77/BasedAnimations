package cs5643.particles;

import java.util.ArrayList;

import javax.vecmath.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;

/** 
 * Simple particle implementation, with miscellaneous adornments. 
 * 
 * @author Doug James, January 2007
 * @author Eston Schweickart, February 2014
 */
public class Particle
{
	/** Radius of particle's sphere graphic. */
	public static final double PARTICLE_RADIUS = 0.015;

	/** Display list index. */
	private static int PARTICLE_DISPLAY_LIST = -1;

	/** Highlighted appearance if true, otherwise white. */
	private boolean highlight = false;

	/** Default mass. */
	double   m = Constants.PARTICLE_MASS;

	/** Deformed Position. */
	public Point3d  x = new Point3d();
	
	/** Predicted position in the middle of one timestep. */
	Point3d x_star = new Point3d();

	/** Undeformed/material Position. */
	Point3d  x0 = new Point3d();

	/** Velocity. */
	public Vector3d v = new Vector3d();

	/** Force accumulator. */
	Vector3d f = new Vector3d();
	
	double density = 0;
	
	Vector3d delta_density = new Vector3d();
	Vector3d delta_collision = new Vector3d();
	
	Point3d x_star_plus_delta = new Point3d();
	
	ArrayList<Particle> neighbors = new ArrayList<Particle>();
	
	/** For scratch work. */
	Vector3d temp = new Vector3d();
	
	/** Stores the viscosity adjustment for velocity. */
	Vector3d viscosity = new Vector3d();
	Vector3d vorticity = new Vector3d();
	Vector3d vort_normal = new Vector3d();
	
	/**
	 * Lagrange multiplier for solving density constraints
	 */
	double lambda_i;

	public Point3d getPos() {
		return x;
	}
	
	public Point3d getCollisionPos() {
		x_star_plus_delta.set(x_star);
		x_star_plus_delta.add(delta_density);
		return x_star_plus_delta;
	}
	
	public Vector3d getVel() {
		return v;
	}
	public Vector3d getForce() {
		return f;
	}
	
	public void accumulateForce(double x, double y, double z) {
		f.x += x;
		f.y += y;
		f.z += z;
	}
	
	/** 
	 * Constructs particle with the specified material/undeformed
	 * coordinate, x0.
	 */
	Particle(Point3d x0) 
	{
		this.x0.set(x0);
		x.set(x0);
		x_star.set(x0);
	}

	/** Draws spherical particle using a display list. */
	public void display(GL2 gl)
	{
		if(PARTICLE_DISPLAY_LIST < 0) {// MAKE DISPLAY LIST:
			int displayListIndex = gl.glGenLists(1);
			GLU glu = GLU.createGLU();
			GLUquadric quadric = glu.gluNewQuadric();
			gl.glNewList(displayListIndex, GL2.GL_COMPILE);
			glu.gluSphere(quadric, PARTICLE_RADIUS, 16, 8);
			gl.glEndList();
			glu.gluDeleteQuadric(quadric);
			glu.destroy();
			System.out.println("MADE DISPLAY LIST "+displayListIndex+" : "+gl.glIsList(displayListIndex));
			PARTICLE_DISPLAY_LIST = displayListIndex;
		}

		/// COLOR: DEFAULT CYAN; GREEN IF HIGHLIGHTED
		float[] c = {0f, 1f, 1f, 1f};//default: cyan
		if(highlight) {
			c[2] = 0;
		}

		// Hack to make things more colorful/interesting
		c[1] = (float)x.y + (float)((1. - x.y) / 3.);
		c[2] = (float)x.x + (float)((1. - x.x) / 3.);

		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, c, 0); // Color used by shader

		/// DRAW ORIGIN-CIRCLE TRANSLATED TO "p":
		gl.glPushMatrix();
		gl.glTranslated(x.x, x.y, x.z);
		gl.glCallList(PARTICLE_DISPLAY_LIST); // Draw the particle
		gl.glPopMatrix();
	}

	/** Specifies whether particle should be drawn highlighted. */
	public void setHighlight(boolean highlight) { 
		this.highlight = highlight;   
	}
	/** True if particle should be drawn highlighted. */
	public boolean getHighlight() { 
		return highlight; 
	}
	
	static double max_visc = 0;
	
	/**
	 * Updates this particle's velocity with the XSPH viscosity adjustment,
	 * using the current list of neighbors.
	 */
	public void updateXSPHViscosity() {
		viscosity.set(0,0,0);
		for(Particle j : neighbors) {
			if(this.equals(j)) continue;
			temp.set(j.v);
			temp.sub(this.v);
			double p6result = Kernel.poly6(this, j);
			temp.scale(p6result);
			viscosity.add(temp);
		}
		viscosity.scale(Constants.VISCOSITY_C);
//		double l = viscosity.length();
//		if (l > max_visc) {
//			max_visc = l;
//			System.out.println("new max visc: " + l);
//		}
	}
	
	public void applyXSPHViscosity() {
		v.add(viscosity);
	}
	
	public void updateVorticityW() {
		vorticity.set(0,0,0);
		for(Particle j : neighbors) {
			if(this.equals(j)) continue;
			vorticity.set(j.v);
			vorticity.sub(this.v);
			Kernel.grad_spiky(temp, this, j);
			vorticity.cross(vorticity, temp);
		}
	}
	
	public void updateVorticityN() {
		vort_normal.set(0,0,0);
		double scalar = 0;
		for(Particle j : neighbors) {
			if(this.equals(j)) continue;
			scalar = (1. / j.density) * j.vorticity.length();
			Kernel.grad_spiky(temp, this, j);
			temp.scale(scalar);
			vort_normal.add(temp);
		}
		vort_normal.normalize();
	}
	
	public void applyVorticity() {
		temp.set(0,0,0);
		temp.cross(vort_normal, vorticity);
		temp.normalize();
		temp.scale(Constants.VORTICITY_EPSILON * ParticleSystemBuilder.DT);
		if(Double.isNaN(temp.x) || Double.isNaN(temp.y) || Double.isNaN(temp.z)) {
			return;
		}
		if(Double.isInfinite(temp.x) || Double.isInfinite(temp.x) || Double.isInfinite(temp.x)) {
			return;
		}
		v.add(temp);
	}

}
