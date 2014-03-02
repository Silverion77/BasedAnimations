package cs5643.forces;

import javax.media.opengl.GL2;

import cs5643.particles.Force;
import cs5643.particles.Particle;
import cs5643.particles.ParticleSystem;

public class GravityForce implements Force {
	
	private ParticleSystem ps;
	private double x,y,z;
	
	public GravityForce(double x, double y, double z, ParticleSystem p) {
		this.setForce(x,y,z);
		ps = p;
	}
	
	public void setForce(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void applyForce() {
		for (Particle particle : ps.P) {
			particle.accumulateForce(x,y,z);
		}
	}

	@Override
	public void display(GL2 gl) {
		
	}

	@Override
	public ParticleSystem getParticleSystem() {
		return ps;
	}

}
