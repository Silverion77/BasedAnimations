package cs5643.particles;

import java.util.*;

import javax.vecmath.*;
import javax.media.opengl.*;

import com.jogamp.opengl.util.glsl.*;

import cs5643.forces.CollisionPlane;


/**
 * Maintains dynamic lists of Particle and Force objects, and provides
 * access to their state for numerical integration of dynamics.
 * 
 * @author Doug James, January 2007
 * @author Eston Schweickart, February 2014
 */
public class ParticleSystem //implements Serializable
{
	/** Current simulation time. */
	public double time = 0;

	/** List of Particle objects. */
	public ArrayList<Particle>   P = new ArrayList<Particle>();

	/** List of Force objects. */
	public ArrayList<Force>      F = new ArrayList<Force>();
	
	public ArrayList<CollisionPlane> planes = new ArrayList<CollisionPlane>();
	
	public ArrayList<DensityConstraint> density_cs = new ArrayList<DensityConstraint>();

	/** 
	 * true iff prog has been initialized. This cannot be done in the
	 * constructor because it requires a GL2 reference.
	 */
	private boolean init = false;

	/** Filename of vertex shader source. */
	public static final String[] VERT_SOURCE = {"vert.glsl"};

	/** Filename of fragment shader source. */
	public static final String[] FRAG_SOURCE = {"frag.glsl"};

	/** The shader program used by the particles. */
	ShaderProgram prog;


	/** Basic constructor. */
	public ParticleSystem() {
		planes.add(new CollisionPlane(0,1,0,0,0,0,this));
	}

	/** 
	 * Set up the GLSL program. This requires that the current directory (i.e. the package in which
	 * this class resides) has a vertex and fragment shader.
	 */
	public synchronized void init(GL2 gl) {
		if (init) return;

		prog = new ShaderProgram();
		ShaderCode vert_code = ShaderCode.create(gl, GL2ES2.GL_VERTEX_SHADER, 1, this.getClass(), VERT_SOURCE, false);
		ShaderCode frag_code = ShaderCode.create(gl, GL2ES2.GL_FRAGMENT_SHADER, 1, this.getClass(), FRAG_SOURCE, false);
		if (!prog.add(gl, vert_code, System.err) || !prog.add(gl, frag_code, System.err)) {
			System.err.println("WARNING: shader did not compile");
			prog.init(gl); // Initialize empty program
		} else {
			prog.link(gl, System.err);
		}

		init = true;
	}

	/** Adds a force object (until removed) */
	public synchronized void addForce(Force f) {
		F.add(f);
	}

	/** Useful for removing temporary forces, such as user-interaction
	 * spring forces. */
	public synchronized void removeForce(Force f) {
		F.remove(f);
	}

	/** Creates particle and adds it to the particle system. 
	 * @param p0 Undeformed/material position. 
	 * @return Reference to new Particle.
	 */
	public synchronized Particle createParticle(Point3d p0) 
	{
		Particle newP = new Particle(p0);
		P.add(newP);
		DensityConstraint dc = new DensityConstraint(newP);
		density_cs.add(dc);
		return newP;
	}

	/** 
	 * Helper-function that computes nearest particle to the specified
	 * (deformed) position.
	 * @return Nearest particle, or null if no particles. 
	 */
	public synchronized Particle getNearestParticle(Point3d x)
	{
		Particle minP      = null;
		double   minDistSq = Double.MAX_VALUE;
		for(Particle particle : P) {
			double distSq = x.distanceSquared(particle.x);
			if(distSq < minDistSq) {
				minDistSq = distSq;
				minP = particle;
			}
		}
		return minP;
	}

	/** Moves all particles to undeformed/materials positions, and
	 * sets all velocities to zero. Synchronized to avoid problems
	 * with simultaneous calls to advanceTime(). */
	public synchronized void reset()
	{
		for(Particle p : P)  {
			p.x.set(p.x0);
			p.v.set(0,0,0);
			p.f.set(0,0,0);
			p.setHighlight(false);
		}
		time = 0;
	}

	/**
	 * Incomplete/Debugging implementation of Forward-Euler
	 * step. WARNING: Contains buggy debugging forces.
	 */
	public synchronized void advanceTime(double dt)
	{
		/// Clear force accumulators:
		for(Particle p : P)  p.f.set(0,0,0);

		{
			// Accumulate external forces.
			for(Force force : F) {
				force.applyForce();
			}

		}

		/// TIME-STEP: (Forward Euler for now):
		for(Particle p : P) {
			// Apply external forces
			p.v.scaleAdd(dt, p.f, p.v);
			// Predict positions
			p.x_star.scaleAdd(dt, p.v, p.x);
		}
		
		// TODO: For each particle i, find neighbors N_i
		
		/* TODO: For each particle i:
		 * - Compute lambda_i
		 * - Compute delta_pi using lambda_i
		 * DONE - Resolve collisions by computing delta_pi_collision
		 * - x_star = x_star + delta_pi + delta_pi_collision
		 */
		
		// Position correction iterations
		for (int i = 0; i < Constants.NUM_CORRECTION_ITERATIONS; i++) {
			
			// Compute all lambda_i
			for (DensityConstraint dc : density_cs) {
				// TODO: this
			}
			
			
			// Correct collisions with box boundaries (planes)
			for (Particle p : P) {
				for (CollisionPlane plane : planes) {
					if(plane.detectCollision(p) < 0) {
						plane.setToMinCorrection(p.delta_collision, p.x_star);
					}
				}
				p.x_star.add(p.delta_collision);
			}
		}
		
		/* TODO: For each particle i:
		 * - v_i = (x_star - x) / delta_t
		 * - v_i += vorticity confinement
		 * - v_i += XSPH velocity
		 */
		
		// Finalize prediction
		for(Particle p : P) {
			p.x.set(p.x_star);
			for(CollisionPlane plane : planes) {
				if(plane.detectCollision(p) < 0) {
					p.setHighlight(true);
				}
				else {
					p.setHighlight(false);
				}
			}
		}

		time += dt;
	}

	/**
	 * Displays Particle and Force objects.
	 */
	public synchronized void display(GL2 gl) 
	{
		for(Force force : F) {
			force.display(gl);
		}

		if(!init) init(gl);

		prog.useProgram(gl, true);

		for(Particle particle : P) {
			particle.display(gl);
		}

		prog.useProgram(gl, false);
	}

}
