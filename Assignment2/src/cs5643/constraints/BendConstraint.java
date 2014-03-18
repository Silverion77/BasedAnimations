package cs5643.constraints;

import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Vector3d;

import cs5643.particles.Constants;
import cs5643.particles.Particle;
import cs5643.particles.Triangle;

/**
 * 
 * 
 * @author Ari Karo
 */
public class BendConstraint extends Constraint {
	private Particle p1;
	private Particle p2;
	private Particle p3;
	private Particle p4;
	private double phi_0;
	boolean init;
	
	private Vector3d temp1;
	private Vector3d temp2;
	private Vector3d temp3;
	
	/** Creates a BendConstraint given two triangles. This constructor assumes
	 * no invariants and will check whether the triangles are adjacent and set the
	 * corresponding particles correctly. If the triangles are valid, init is set to
	 * true, else init will be false.
	 * @param t1 - first triangle
	 * @param t2 - second tringle
	 */
	public BendConstraint(Triangle t1, Triangle t2) {
		super(Constants.K_BEND);
		temp1 = new Vector3d();
		temp2 = new Vector3d();
		temp3 = new Vector3d();
		if (checkAdjacent(t1, t2)) {
			
			calculatePhi_0();
			init = true;
		} else {
			init = false;
		}
	}
	
	private boolean checkAdjacent(Triangle t1, Triangle t2) {
		ArrayList<Particle> vertices_t1 = new ArrayList<Particle>();
		vertices_t1.add(t1.v0);
		vertices_t1.add(t1.v1);
		vertices_t1.add(t1.v2);
		ArrayList<Particle> vertices_t2 = new ArrayList<Particle>();
		vertices_t2.add(t2.v0);
		vertices_t2.add(t2.v1);
		vertices_t2.add(t2.v2);
		int count = 0;
		Iterator<Particle> i = vertices_t1.iterator();
		while (i.hasNext()) {
			Particle tmp1 = i.next();
			Iterator<Particle> j = vertices_t2.iterator();
			while (j.hasNext()) {
				Particle tmp2 = j.next();
				if (tmp1 == tmp2) {
					count++;
					if (count == 1)
						p1 = tmp1;
					if (count == 2)
						p2 = tmp2;
					i.remove();
					j.remove();
				}
			}
		}
		if (vertices_t1.size() == 1) {
			p3 = vertices_t1.get(0);
			p4 = vertices_t2.get(0);
			return true;
		}
		return false;
	}
	
	private void calculatePhi_0() {
		temp1.set(p2.x0);
		temp1.sub(p1.x0);
		temp2.set(p3.x0);
		temp2.sub(p1.x0);
		temp2.cross(temp1, temp2);
		temp2.normalize();
		temp3.set(p4.x0);
		temp3.sub(p1.x0);
		temp3.cross(temp1, temp2);
		temp3.normalize();
		phi_0 = Math.acos(temp2.dot(temp3));
	}
	
	@Override
	public double evaluate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isSatisfied(double d) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void project() {
		// TODO Auto-generated method stub

	}

	@Override
	public Vector3d gradient(Particle p_j) {
		// TODO Auto-generated method stub
		return null;
	}

}
