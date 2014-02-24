package cs5643.particles;

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
	Point3d  x = new Point3d();
	
	/** Predicted position in the middle of one timestep. */
	Point3d x_star = new Point3d();

	/** Undeformed/material Position. */
	Point3d  x0 = new Point3d();

	/** Velocity. */
	Vector3d v = new Vector3d();

	/** Force accumulator. */
	Vector3d f = new Vector3d();
	
	Vector3d delta_density = new Vector3d();
	
	Vector3d delta_collision = new Vector3d();
	
	/**
	 * Lagrange multiplier for solving density constraints
	 */
	double lambda_i;

	public Point3d getPos() {
		return x;
	}
	
	public Vector3d getVel() {
		return v;
	}
	
	public void accumulateForce(float x, float y, float z) {
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
		c[1] = (float)x.y;

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
