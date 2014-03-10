package cs5643.particles;

import java.util.*;

import javax.vecmath.*;
import javax.media.opengl.*;

import com.jogamp.opengl.util.glsl.*;

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
	public ArrayList<Particle> particles = new ArrayList<Particle>();
	
	/** List of Force objects. */
	public ArrayList<Force> forces = new ArrayList<Force>();

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

	/** Private vector for scratch work */
	private Vector3d temp_vec = new Vector3d();
	private Point3d temp_pt = new Point3d();

	/** Basic constructor. */
	public ParticleSystem() {
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

	/** Creates particle and adds it to the particle system. 
	 * @param p0 Undeformed/material position. 
	 * @return Reference to new Particle.
	 */
	public synchronized Particle createParticle(Point3d p0) 
	{
		Particle newP = new Particle(p0);
		particles.add(newP);
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
		for(Particle particle : particles) {
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
		for(Particle p : particles)  {
			p.x.set(p.x0);
			p.v.set(0,0,0);
			p.f.set(0,0,0);
			p.delta_collision.set(0,0,0);
			p.delta_density.set(0,0,0);
			p.setHighlight(false);
		}
		time = 0;
	}

	/**
	 * Implementation of one time step as described in "Position-Based Fluids"
	 */
	public synchronized void advanceTime(double dt)
	{
		// TODO: everything here
	}

	/**
	 * Displays Particle and Force objects.
	 */
	public synchronized void display(GL2 gl) 
	{
		for(Force force : forces) {
			force.display(gl);
		}

		if(!init) init(gl);

		prog.useProgram(gl, true);

		for(Particle particle : particles) {
			particle.display(gl);
		}

		prog.useProgram(gl, false);
	}

}
