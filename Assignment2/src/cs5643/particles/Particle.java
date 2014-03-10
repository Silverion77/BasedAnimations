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
	public static final double PARTICLE_RADIUS = 0.02;

	/** Display list index. */
	private static int PARTICLE_DISPLAY_LIST = -1;

	/** Highlighted appearance if true, otherwise white. */
	private boolean highlight = false;

	/** Default mass. */
//	double   m = Constants.PARTICLE_MASS;

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
		float[] c = {0f, 0.7f, 1f, 1f};//default: cyan
		c[1] = (float)x.y;
		if(highlight) {
			c[2] = 0;
		}
		
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
}
