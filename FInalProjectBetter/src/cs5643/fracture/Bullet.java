package cs5643.fracture;
import java.util.ArrayList;

import org.dyn4j.geometry.Vector2;


public class Bullet extends ConvexPolygon {

	private int kills;
	
	public Bullet(ArrayList<Vector2> points) {
		super(points);
		kills = 0;
	}
	
	public int getKills() {
		return kills;
	}
	
	public void awardKill() {
		kills++;
	}

}
