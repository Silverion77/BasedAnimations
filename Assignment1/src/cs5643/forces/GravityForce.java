package cs5643.forces;

import javax.media.opengl.GL2;

import cs5643.particles.Force;
import cs5643.particles.Particle;
import cs5643.particles.ParticleSystem;
import cs5643.particles.ParticleSystemBuilder;

public class GravityForce implements Force {
	
	private ParticleSystem ps;
	
	public GravityForce(ParticleSystem p) {
		ps = p;
	}

	@Override
	public void applyForce() {
		for (Particle particle : ps.P) {
			particle.accumulateForce(0f, -9.8f, 0f);
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
