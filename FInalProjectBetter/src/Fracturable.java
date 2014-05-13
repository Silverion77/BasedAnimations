import java.util.ArrayList;
import java.util.List;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.joint.MouseJoint;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;


public abstract class Fracturable extends Body {

	private boolean pinned = false;
	private MouseJoint mouseJoint = null;


	public boolean isPinned() {
		return pinned;
	}
	
	public void pin(Vector2 v) {
		pinned = true;
		mouseJoint = new MouseJoint(this, this.getWorldCenter(), 2, 0.5, 200000);
		mouseJoint.setTarget(v);
		this.getWorld().addJoint(mouseJoint);
	}

	public void setJointTarget(Vector2 v) {
		mouseJoint.setTarget(v);
	}

	public void unpin() {
		pinned = false;
		this.getWorld().removeJoint(mouseJoint);
		mouseJoint = null;
	}
	
	public abstract void polygonsWithinR(double r, Vector2 point, ArrayList<Polygon> within, ArrayList<Polygon> outside);		
}
