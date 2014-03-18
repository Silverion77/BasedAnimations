package cs5643.forces;

import javax.media.opengl.GL2;

import cs5643.particles.Particle;

public class Gravity implements Force {

	@Override
	public void applyForce(Particle p) {
		p.accumulateForce(0, -9.8 * p.m, 0);
	}

	@Override
	public void display(GL2 gl) {
		// nah
	}

}
