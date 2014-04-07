package cs5643.constraints;

import javax.vecmath.Vector3d;

import cs5643.particles.Constants;
import cs5643.particles.Particle;
import cs5643.particles.Triangle;
import cs5643.particles.Vertex;

public class VertexTriangleConstraint extends Constraint {

	private Particle v;
	private Triangle t;
	
	public VertexTriangleConstraint(Particle v, Triangle t) {
		super(1);
		this.v = v;
		this.t = t;
	}
	
	private Vector3d temp1 = new Vector3d();
	private Vector3d temp2 = new Vector3d();
	private Vector3d temp3 = new Vector3d();
	private Vector3d temp4 = new Vector3d();
	
	@Override
	public double evaluate() {
		// If the dot product is positive, the particle came from below wrt the normal
		if(v.v.dot(t.getNormal()) > 0) {
			temp3.sub(t.v2.x, t.v0.x);
			temp2.sub(t.v1.x, t.v0.x);
			temp1.cross(temp3, temp2);
			temp1.normalize();
			temp4.sub(v.x, t.v0.x);
			return temp4.dot(temp1) - Constants.H_THICKNESS;
		}
		else {
			temp2.sub(t.v1.x, t.v0.x);
			temp3.sub(t.v2.x, t.v0.x);
			temp1.cross(temp2, temp3);
			temp4.sub(v.x, t.v0.x);
			return temp4.dot(temp1) - Constants.H_THICKNESS;
		}
	}

	@Override
	public boolean isSatisfied(double d) {
		return d >= 0;
	}

	private static Vector3d p1 = new Vector3d();
	private static Vector3d p2 = new Vector3d();
	private static Vector3d p3 = new Vector3d();
	private static Vector3d q = new Vector3d();
	private static Vector3d temp_n = new Vector3d();
	
	private static Vector3d n = new Vector3d();
	private static Vector3d del_q = new Vector3d();
	private static Vector3d del_p1 = new Vector3d();
	private static Vector3d del_p2 = new Vector3d();
	private static Vector3d del_p3 = new Vector3d();
	
	private static Vector3d cross1 = new Vector3d();
	private static Vector3d cross2 = new Vector3d();
	
	@Override
	public void project() {
		
		double val = evaluate();
		if(isSatisfied(val)) return;
		
		// get all vectors with respect to p1
		p2.sub(t.v1.x_star, t.v0.x_star);
		p3.sub(t.v2.x_star, t.v0.x_star);
		q.sub(v.x_star, t.v0.x_star);
		n.cross(p2, p3);
		double magn_p2p3 = n.length();
		n.scale(1.0 / magn_p2p3);
		
		double nq = n.dot(q);
		
		// gradient wrt q is just this
		del_q.scale(-1, n);
		
		// compute for p2
		cross1.cross(p3, q);
		cross2.cross(n, p3);
		cross2.scale(nq);
		cross1.add(cross2);
		cross1.scale(-1 / magn_p2p3);
		del_p2.set(cross1);
		
		// compute for p3
		cross1.cross(p2, q);
		cross2.cross(n, p2);
		cross2.scale(nq);
		cross1.add(cross2);
		cross1.scale(1 / magn_p2p3);
		del_p3.set(cross1);
		
		// compute for p1
		del_p1.set(0,0,0);
		del_p1.sub(del_q);
		del_p1.sub(del_p2);
		del_p1.sub(del_p3);
		
		double denom = 0;
		denom += t.v0.w() * del_p1.lengthSquared();
		denom += t.v1.w() * del_p2.lengthSquared();
		denom += t.v2.w() * del_p3.lengthSquared();
		denom += v.w() * del_q.lengthSquared();
		
		if(denom == 0) return;
		double s = val / denom;
		
		p1.scale(-s * t.v0.w() * stiffness_k, del_p1);
		p2.scale(-s * t.v1.w() * stiffness_k, del_p2);
		p3.scale(-s * t.v2.w() * stiffness_k, del_p3);
		q.scale(-s * v.w() * stiffness_k, del_q);
		
		t.v0.x_star.add(p1);
		t.v1.x_star.add(p2);
		t.v2.x_star.add(p3);
		v.x_star.add(q);
		
		v.vel_damp = 0.1;
		t.v0.vel_damp = 0.1;
		t.v1.vel_damp = 0.1;
		t.v2.vel_damp = 0.1;
	}

	@Override
	public Vector3d gradient(Particle p_j) {
		return null;
	}

}
