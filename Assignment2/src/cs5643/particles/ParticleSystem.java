package cs5643.particles;

import java.util.*;

import javax.vecmath.*;
import javax.media.opengl.*;

import com.jogamp.opengl.util.glsl.*;

import cs5643.forces.Force;


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
	
	public ArrayList<Mesh> meshes = new ArrayList<Mesh>();

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
	public static final String[] VERT_SOURCE = {"mesh-vert.glsl"};

	/** Filename of fragment shader source. */
	public static final String[] FRAG_SOURCE = {"mesh-frag.glsl"};

	/** The shader program used by the particles. */
	ShaderProgram prog;


	/** Basic constructor. */
	public ParticleSystem() {}

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
		forces.add(f);
	}

	/** Useful for removing temporary forces, such as user-interaction
	 * spring forces. */
	public synchronized void removeForce(Force f) {
		forces.remove(f);
	}
	
	public synchronized void addMesh(Mesh m) {
		meshes.add(m);
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
			p.setHighlight(false);
		}
		time = 0;
	}

	/**
	 * Advances the time by one increment. The integrator used here should
	 * be stable.
	 */
	public synchronized void advanceTime(double dt)
	{
		// TODO: write an integrator that is stable
		time += dt;
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
		
		for(Mesh mesh : meshes) {
			mesh.display(gl);
		}

//		for(Particle particle : particles) {
//			particle.display(gl);
//		}

		prog.useProgram(gl, false);
	}

}
