package cs5643.constraints;

import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Vector3d;

import cs5643.particles.Constants;
import cs5643.particles.Particle;
import cs5643.particles.ParticleSystemBuilder;
import cs5643.particles.Triangle;
import cs5643.particles.Utils;

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
	public boolean init;
	
	// Workspace vectors
	private static Vector3d temp = new Vector3d();
	private static Vector3d n1 = new Vector3d();
	private static Vector3d n2 = new Vector3d();
	private static Vector3d p2v = new Vector3d();
	private static Vector3d p3v = new Vector3d();
	private static Vector3d p4v = new Vector3d();
	private static Vector3d q1 = new Vector3d();
	private static Vector3d q2 = new Vector3d();
	private static Vector3d q3 = new Vector3d();
	private static Vector3d q4 = new Vector3d();
	
	/** Creates a BendConstraint given two triangles. This constructor assumes
	 * no invariants and will check whether the triangles are adjacent and set the
	 * corresponding particles correctly. If the triangles are valid, init is set to
	 * true, else init will be false.
	 * @param t1 - first triangle
	 * @param t2 - second tringle
	 */
	public BendConstraint(Triangle t1, Triangle t2) {
		super(Constants.K_BEND);
		if (checkAdjacent(t1, t2)) {
			phi_0 = calculatePhi_0();
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
						p2 = tmp1;
					i.remove();
					j.remove();
					break;
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
	
	private double calculatePhi_0() {
		temp.sub(p2.x, p1.x);	//temp = p2 - p1
		n1.sub(p3.x, p1.x);
		n1.cross(temp, n1);
		n1.normalize();
		n2.sub(p4.x, p1.x);
		n2.cross(temp, n2);
		n2.normalize();
		return Math.acos(clamp(n1.dot(n2)));
	}
	
	@Override
	public double evaluate() {
		temp.sub(p2.x_star, p1.x_star);
		n1.sub(p3.x_star, p1.x_star);
		n1.cross(temp, n1);
		n1.normalize();
		n2.sub(p4.x_star, p1.x_star);
		n2.cross(temp, n2);
		n2.normalize();
		return Math.acos(n1.dot(n2)) - phi_0;
	}

	@Override
	public boolean isSatisfied(double d) {
		return (d == 0);
	}

	@Override
	public void project() {
		p2v.set(p2.x_star);
		p3v.set(p3.x_star);
		p4v.set(p4.x_star);
		
		// Calculate n1 and n2
		n1.cross(p2v, p3v);
		n1.normalize();
		n2.cross(p2v, p4v);
		n2.normalize();
		double d = clamp(n1.dot(n2));
		
		// Calculate q3
		q3.cross(p2v, n2);
		temp.cross(n1, p2v);
		Utils.acc(q3, d, temp);
		temp.cross(p2v, p3v);
		q3.scale(1 / temp.length());
		
		// Calculate q4
		q4.cross(p2v, n1);
		temp.cross(n2, p2v);
		Utils.acc(q4, d, temp);
		temp.cross(p2v, p4v);
		q4.scale(1 / temp.length());
		
		// Calculate q2
		q2.cross(p3v, n2);
		temp.cross(n1, p3v);
		Utils.acc(q2, d, temp);
		temp.cross(p2v, p3v);
		q2.scale(-1 / temp.length());
		q1.cross(p4v, n1);
		temp.cross(n2, p4v);
		Utils.acc(q1, d, temp);
		temp.cross(p2v, p4v);
		q1.scale(-1 / temp.length());
		q2.add(q1);
		
		// Calculate q1
		q1.negate(q2);
		Utils.acc(q1, -1, q3);
		Utils.acc(q1, -1, q4);
		
		// Corrections
		double num = -Math.sqrt(1 - Math.pow(d, 2)) * (Math.acos(d) - phi_0);
		double denom = p1.w() * q1.lengthSquared() + p2.w() * q2.lengthSquared()
						+ p3.w() * q3.lengthSquared() + p4.w() * q4.lengthSquared();
//		System.out.println("correction: " + num + " / " + denom);
		if(denom == 0) {
			return;
		}
		if(Double.isNaN(num) || Double.isNaN(denom)) {
//			ParticleSystemBuilder.stopEverything();
//			System.out.println(1/0);
			return;
		}
		q1.scale(stiffness_k * p1.w() * num / denom);
		q2.scale(stiffness_k * p2.w() * num / denom);
		q3.scale(stiffness_k * p3.w() * num / denom);
		q4.scale(stiffness_k * p4.w() * num / denom);
//		System.out.println("q's: " + q1 + ", " + q2 + ", " + q3 + ", " + q4);
		p1.x_star.add(q1);
		p2.x_star.add(q2);
		p3.x_star.add(q3);
		p4.x_star.add(q4);
	}
	
	private double clamp (double d) {
		if (d > 1)
			d = 1;
		if (d < -1)
			d = -1;
		return d;
	}
	
	@Override
	public Vector3d gradient(Particle p_j) {
		// TODO Auto-generated method stub
		return null;
	}

}
