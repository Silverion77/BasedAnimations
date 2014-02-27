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

	/** Private vector for scratch work */
	private Vector3d temp_vec = new Vector3d();
	private Point3d temp_pt = new Point3d();

	private SpaceMap space_map = new SpaceMap();

	/** Basic constructor. */
	public ParticleSystem() {
		planes.add(new CollisionPlane(1,0,0,0,0,0,this));
		planes.add(new CollisionPlane(0,1,0,0,0,0,this));
		planes.add(new CollisionPlane(0,0,1,0,0,0,this));
		planes.add(new CollisionPlane(-1,0,0,1,0,0,this));
//		planes.add(new CollisionPlane(0,-1,0,0,1,0,this));
		planes.add(new CollisionPlane(0,0,-1,0,0,1,this));
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
			p.delta_collision.set(0,0,0);
			p.delta_density.set(0,0,0);
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

		// Accumulate external forces.
		for(Force force : F) {
			force.applyForce();
		}
		for(Force plane : planes) {
			plane.applyForce();
		}

		/// TIME-STEP: (Forward Euler for now):
		for(Particle p : P) {
			// Apply external forces
			p.v.scaleAdd(dt, p.f, p.v);
			// Predict positions
			p.x_star.scaleAdd(dt, p.v, p.x);
		}

		// For each particle i, find neighbors N_i
		space_map.clear();
		space_map.addAll(P);

		for(Particle i : P) {
			space_map.getNeighbors(i);
		}

		// Position correction iterations
		for (int n = 0; n < Constants.NUM_CORRECTION_ITERATIONS; n++) {

			
			// Compute all lambda_i
			for (DensityConstraint dc : density_cs) {
				dc.compute_lambda();
			}
			
			// Compute all position corrections delta_pi
			for (Particle i : P) {
				i.density = 0;
				double scale_by = 1. / Constants.REST_DENSITY;
				i.delta_density.set(0,0,0);
				for (Particle j : i.neighbors) {
					// Surface tension correction
					double p6result = Kernel.poly6(i, j);
					i.density += p6result;
					double s_corr = Math.pow(p6result /
									Kernel.poly6(Constants.DELTA_Q2),
									Constants.TENSION_N);
					s_corr *= (-Constants.TENSION_K);
					
					double sum_lambdas = i.lambda_i + j.lambda_i + s_corr;
					Kernel.grad_spiky(temp_vec, i, j);
					temp_vec.scale(sum_lambdas);
					i.delta_density.add(temp_vec);
				}
				i.delta_density.scale(scale_by);
			}

			// Correct collisions with box boundaries (planes)
			for (Particle p : P) {
				p.delta_collision.set(0,0,0);
				for (CollisionPlane plane : planes) { {
					temp_pt.set(p.x_star);
					temp_pt.add(p.delta_density);
					if(plane.detectCollision(temp_pt) < 0)
						plane.addToMinCorrection(p.delta_collision, temp_pt);
					}
				}
			}

			for (Particle p : P) {
				p.x_star.add(p.delta_density);
				p.x_star.add(p.delta_collision);
			}
		}


		for(Particle p : P) {
			p.temp.set(p.x_star);
			p.temp.sub(p.x);
			p.temp.scale(1. / dt);
			p.v.set(p.temp);
		}
		
		// Compute vorticity at each particle
		for(Particle p : P) {
			p.updateVorticityW();
		}
		
		// Compute vorticity normal at each particle
		for(Particle p : P) {
			p.updateVorticityN();
		}
		
		// Apply vorticity confinement
		for(Particle p : P) {
			p.applyVorticity();
		}

		// Compute XSPH viscosity -- all at once so they don't affect each other
		for(Particle p : P) {
			p.updateXSPHViscosity();
		}
		
		// Apply XSPH viscosity all at once
		for(Particle p : P) {
			p.applyXSPHViscosity();
		}

		// Finalize prediction
		for(Particle p : P) {
			p.x.set(p.x_star);
		}

		time += dt;
//		System.out.println("step complete: time " + time);
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
