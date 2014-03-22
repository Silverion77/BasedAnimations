package cs5643.constraints;

import javax.vecmath.Vector3d;

import cs5643.particles.Constants;
import cs5643.particles.Mesh;
import cs5643.particles.Triangle;
import cs5643.particles.Particle;

public class VolumeConstraint extends Constraint {
	
	private Vector3d temp1;
	private Vector3d temp2;
	private Mesh mesh;
	
	private double rest_volume;
	
	public VolumeConstraint(Mesh m) {
		this(Constants.K_PRESSURE, m);
	}
	
	public VolumeConstraint(double k, Mesh m) {
		super(k);
		temp1 = new Vector3d();
		temp2 = new Vector3d();
		mesh = m;
		rest_volume = scalarProduct();
	}
	
	private double scalarProduct() {
		double volume = 0;
		for(Triangle t : mesh.triangles) {
			// (p0 x p1) . p2
			temp1.set(t.v0.x_star);
			temp2.set(t.v1.x_star);
			temp1.cross(temp1, temp2);
			temp2.set(t.v2.x_star);
			volume += temp1.dot(temp2);
		}
		return volume;
	}

	@Override
	public double evaluate() {
		double volume = scalarProduct();
		return volume - (Constants.K_PRESSURE * rest_volume);
	}

	@Override
	public boolean isSatisfied(double d) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void project() {
		double scaling = evaluate();
		double scale_denom = 0;
		computeGradients();
		// equation 8 of PBD
		for(Particle j : mesh.vertices) {
			scale_denom += (j.w() * j.volumeGradient.lengthSquared());
		}
		scaling = scaling / scale_denom;
		// equation 9 of PBD
		for(Particle i : mesh.vertices) {
			temp1.set(i.volumeGradient);
			temp1.scale(-scaling * i.w());
			i.x_star.add(temp1);
		}
		
	}
	
	private void computeGradients() {
		// zero all gradients
		for(Particle p : mesh.vertices) {
			p.volumeGradient.set(0,0,0);
		}
		// now accumulate gradients for every triangle
		for(Triangle t : mesh.triangles) {
			// add for t0
			temp1.set(t.v1.x_star);
			temp2.set(t.v2.x_star);
			temp1.cross(temp1, temp2);
			t.v0.volumeGradient.add(temp1);
			// add for t1
			temp1.set(t.v2.x_star);
			temp2.set(t.v0.x_star);
			temp1.cross(temp1, temp2);
			t.v1.volumeGradient.add(temp1);
			// add for t2
			temp1.set(t.v0.x_star);
			temp2.set(t.v1.x_star);
			temp1.cross(temp1, temp2);
			t.v2.volumeGradient.add(temp1);
		}
	}

	@Override
	public Vector3d gradient(Particle p_j) {
		// TODO Auto-generated method stub
		return null;
	}

}
