package cs5643.fracture;

import javax.media.opengl.GL;
import javax.vecmath.Point2d;

public class Gravity implements Force {

	Point2d force = new Point2d(0, -9.8);
	
	@Override
	public void applyForce(Convex c) {
		Utils.acc(c.force, c.getMass(), force);
	}

	@Override
	public void display(GL gl) {
		// TODO Auto-generated method stub
	}
}
