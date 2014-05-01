package cs5643.fracture;

import java.util.*;
import javax.vecmath.*;
import javax.media.opengl.*;


/**
 * Maintains a dynamic list of RigidBody objects, and provides access
 * to their state for numerical integration of dynamics and collision
 * processing.
 * 
 * @author Doug James, March 2007.
 */
public class RigidBodySystem 
{
	/** Current simulation time. */
	double time = 0;

	/** List of RigidBody objects. */
	ArrayList<Convex> convexes = new ArrayList<Convex>();

	/** List of Force objects. */
	ArrayList<Force> forces = new ArrayList<Force>();

	CollisionProcessor collisionProcessor = null;
	boolean processCollisions = true;

	//ArrayList<Constraint> C = new ArrayList<Constraint>();

	/** Basic constructor. */
	public RigidBodySystem() {
		addForce(new Gravity());
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

	/** Fragile reference to rigid bodies.  */
	Collection<Convex> getConvexes() { 
		return convexes;
	}

	/** Number of rigid bodies. */
	public int getNConvexes() { return convexes.size(); }

	/** Picks body based on some criteria, or null if none picked.  */
	public Convex pickBody(Point2d p)
	{
		Convex pick = null;
		for(Convex body : convexes) {
			if(body.pointInPolygon(p)) {
				pick = body;
			}
		}
		return pick;
	}

	/** Adds the RigidBody to the system, and invalidates the existing
	 * CollisionProcessor. */
	public synchronized void add(Convex rb) 
	{
		convexes.add(rb);
		/// INVALIDATE CollisionProcessor (needs to be rebuilt at timestep):
		collisionProcessor = null;
	} 


	/** Removes the RigidBody from the system, and invalidates the
	 * existing CollisionProcessor. */
	public synchronized void remove(Convex rb) 
	{
		convexes.remove(rb);

		/// INVALIDATE CollisionProcessor (needs to be rebuilt at timestep):
		collisionProcessor = null;
	} 

	/** Moves all rigidbodys to undeformed/materials positions, and
	 * sets all velocities to zero. Synchronized to avoid problems
	 * with simultaneous calls to advanceTime(). */
	public synchronized void reset()
	{
		System.out.println("resetting");
		convexes.clear();
		time = 0;
	}

	/**
	 * Incomplete/Debugging integrator implementation. 
	 * 
	 * TODO: Modify this function to implement the integrator based on
	 * the velocity-level complementarity constraint solver.
	 */
	public synchronized void advanceTime(double dt)
	{
		{
			for (Convex c : convexes) {
				c.force.set(0,0);
				for(Force force : forces) {
					force.applyForce(c);
				}
			}

			for(Convex c : convexes) {
				Utils.acc(c.v, dt / c.getMass(), c.force);
				// TODO: angular stuff
			}
			
			for(Convex c : convexes) {
				Utils.acc(c.x_star, dt, c.v);
			}
			
			// TODO: improve dumb collision handling here
			for(Convex c : convexes) {
				c.putOnFloor();
			}

			for(Convex c : convexes) {
				c.updateVelocity(dt);
				c.finalizePrediction();
			}
		}

		if(collisionProcessor == null) {
			collisionProcessor = new CollisionProcessor(convexes);
		}
		if(processCollisions) collisionProcessor.processCollisions();

		// TODO: advance the time

		time += dt;
	}

	/** Enables/disables collision processing. */
	public void setProcessCollisions(boolean enable)
	{
		processCollisions = enable;
	}
	/** Returns true if collision processing is enabled, and false
	 * otherwise. */
	public boolean getProcessCollisions()
	{
		return processCollisions;
	}


	/**
	 * Displays RigidBody and Force objects.
	 */
	public synchronized void display(GL2 gl) 
	{
		for(Convex body : convexes) {
			body.display(gl);
		}

		for(Force force : forces) {
			force.display(gl);
		}
	}

}
