package cs5643.fracture;

import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

public class NormalForce implements Force {

	private RigidBodySystem rbs;
	public NormalForce(RigidBodySystem r) {
		rbs = r;
	}
	
	private Point2d tempPt = new Point2d();
	private ArrayList<Double> contactX = new ArrayList<Double>();
	private ArrayList<Double> contactY = new ArrayList<Double>();
	private Vector2d normal = new Vector2d();
	
	@Override
	public void applyForce() {
		for(Convex c : rbs.convexes) {
			normal.set(0, Constants.GRAVITY_FORCE);
			contactX.clear();
			contactY.clear();
			for(Point2d p : c.getPoints()) {
				c.pointToWorldSpace(p, tempPt);
				if(tempPt.y <= Constants.CONTACT_EPSILON) {
					contactX.add(tempPt.x);
					contactY.add(tempPt.y);
				}
			}
			int numContacts = contactX.size();
			normal.scale(1.0 / numContacts);
			for(int i = 0; i < numContacts; i++) {
				tempPt.set(contactX.get(i), contactY.get(i));
				c.applyForceAtPoint(normal, tempPt);
			}
			if(numContacts == 1 && c.getAngularVelocity() == 0) {
				double randAngular = Constants.rdm.nextDouble() - 0.5;
				c.torque += randAngular;
			}
			else if (numContacts == 2 && c.torque <= 1) {
				c.angularVelocity = 0;
			}
		}
	}

	@Override
	public void display(GL gl) {
		// TODO Auto-generated method stub
		
	}

}
