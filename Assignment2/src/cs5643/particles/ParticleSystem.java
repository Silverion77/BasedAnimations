package cs5643.particles;

import java.util.*;

import javax.vecmath.*;
import javax.media.opengl.*;

import com.jogamp.opengl.util.glsl.*;

import cs5643.constraints.*;
import cs5643.forces.*;

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
	public double time;
	
	public ArrayList<Mesh> meshes;
	
	/** List of Particle objects. */
	public ArrayList<Particle> particles;
	
	/** List of Force objects. */
	public ArrayList<Force> forces;
	
	public ArrayList<Constraint> cloth_constrs;
	
	public ArrayList<Constraint> collision_constrs;
	
	private CollisionPlane floor;
	
	private Vector3d x_cm, v_cm, angular;
	private Vector3d temp1, temp2;
	private Matrix3d r_tilde, r_tilde_T, bigI;

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
	public ParticleSystem() {
		time = 0;
		meshes = new ArrayList<Mesh>();
		particles = new ArrayList<Particle>();
		forces = new ArrayList<Force>();
		cloth_constrs = new ArrayList<Constraint>();
		collision_constrs = new ArrayList<Constraint>();
		
		floor = new CollisionPlane(0,1,0,0,0,0);
		
		x_cm = new Vector3d();
		v_cm = new Vector3d();
		angular = new Vector3d();
		temp1 = new Vector3d();
		temp2 = new Vector3d();
		r_tilde = new Matrix3d();
		r_tilde_T = new Matrix3d();
		bigI = new Matrix3d();
		
		forces.add(new Gravity());
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
		forces.add(f);
	}
	
	public synchronized void addParticle(Particle p) {
		particles.add(p);
		// TODO: get rid of this
		collision_constrs.add(new PlaneConstraint(p, floor));
	}

	/** Useful for removing temporary forces, such as user-interaction
	 * spring forces. */
	public synchronized void removeForce(Force f) {
		forces.remove(f);
	}
	
	public synchronized void addMesh(Mesh m) {
		meshes.add(m);
		for(Edge e : m.edges) {
			StretchConstraint sc = new StretchConstraint(e.v0, e.v1);
			cloth_constrs.add(sc);
		}
		for(Triangle t1 : m.triangles) {
			for(Triangle t2: m.triangles) {
				if(t1.equals(t2)) continue;
				BendConstraint bc = new BendConstraint(t1,t2);
				if(bc.init) {
					cloth_constrs.add(bc);
				}
			}
		}
	}

	/** Creates particle and adds it to the particle system. 
	 * @param p0 Undeformed/material position. 
	 * @return Reference to new Particle.
	 */
	public synchronized Particle createParticle(Point3d p0) 
	{
		Particle newP = new Particle(p0);
		addParticle(newP);
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
			p.x_star.set(p.x0);
			p.v.set(0,0,0);
			p.f.set(0,0,0);
			p.setHighlight(false);
		}
		time = 0;
	}
	
	public synchronized void clear() {
		particles.clear();
		cloth_constrs.clear();
		collision_constrs.clear();
		meshes.clear();
	}

	/**
	 * Advances the time by one increment. The integrator used here should
	 * be stable.
	 */
	public synchronized void advanceTime(double dt)
	{
		// Accumulate all external forces f_ext
		for(Particle p_i : particles) {
			p_i.f.set(0,0,0);
			p_i.x_star.set(p_i.x);
			for(Force f : forces) {
				f.applyForce(p_i);
			}
		}
		
		for(Particle p_i : particles) {
			Utils.acc(p_i.v, dt / p_i.m, p_i.f);
		}
		
		for(Mesh m : meshes) {
			// Do rigid damping
			rigidDamp(m);
		}
		
		for(Particle p_i : particles) {
			Utils.acc(p_i.x_star, dt, p_i.v);
		}
		
		// TODO: generate collision constraints
		
		for(int count = 0; count < Constants.NUM_SOLVER_ITERATIONS; count++) {
			// Project the constraints
			for(Constraint c : cloth_constrs) {
				c.project();
			}
			for(Constraint c : collision_constrs) {
				c.project();
			}
		}
		
		for(Particle p_i : particles) {
			// update velocity: v_i <- (p_i - x_i) / dt
			p_i.updateVelocity(dt);
			// finalize prediction: x_i <- p_i
			p_i.finalizePrediction();
		}
		
		// TODO: "the velocities of colliding vertices are modified according
		// to friction and restitution coefficients"
		
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

		for(Particle particle : particles) {
			particle.display(gl);
		}

		prog.useProgram(gl, false);
	}
	
	/**
	 * Performs rigid damping on all vertices of the given mesh.
	 * @param m
	 */
	private void rigidDamp(Mesh m) {
		double sum_masses = 0;
		x_cm.set(0,0,0);
		v_cm.set(0,0,0);
		for(Particle p : m.vertices) {
			Utils.acc(x_cm, p.m, p.x_star);
			Utils.acc(v_cm, p.m, p.v);
			sum_masses += p.m;
		}
		x_cm.scale(1.0 / sum_masses);
		v_cm.scale(1.0 / sum_masses);
		
		// currently, angular holds L
		angular.set(0,0,0);
		bigI.setZero();
		
		for(Particle p : m.vertices) {
			// Compute r_i
			temp1.set(p.x_star);
			temp1.sub(x_cm);
			
			// While we're at it, compute r_tilde
			r_tilde.setZero();
			r_tilde.setRow(0, 0, -temp1.z, temp1.y);
			r_tilde.setRow(1, temp1.z, 0, -temp1.x);
			r_tilde.setRow(2, -temp1.y, temp1.x, 0);
			r_tilde_T.set(r_tilde);
			r_tilde_T.transpose();
			r_tilde.mul(r_tilde_T);
			r_tilde.mul(p.m);
			bigI.add(r_tilde);
			
			// Compute m_i * v_i
			temp2.set(p.v);
			temp2.scale(p.m);
			
			// Compute r_i x (m_i * v_i)
			temp1.cross(temp1, temp2);
			
			angular.add(temp1);
		}
		// At this point, have L and I.
		try {
			bigI.invert();
		}
		catch (SingularMatrixException e) {
			System.err.println("Rigid damping aborted: cannot invert matrix I");
			return;
		}
		bigI.transform(angular);
		
		// angular now holds w, the angular momentum
		for(Particle p : particles) {
			// temp2 = r_i
			temp2.set(p.x_star);
			temp2.sub(x_cm);
			// temp1 = delta v_i = v_cm + (w x r_i) - v_i
			temp1.set(angular);
			temp1.cross(temp1, temp2);
			temp1.add(v_cm);
			temp1.sub(p.v);
			Utils.acc(p.v, Constants.K_DAMPING, temp1);
		}
	}
	
	
	
	
	

}
