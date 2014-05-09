import java.util.ArrayList;

import javax.media.opengl.GL2;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;

public class FractureSystem {
	
	private World world;
	private ArrayList<ConvexPolygon> polygons;
	
	public FractureSystem() {
		polygons = new ArrayList<ConvexPolygon>();
		world = new World();
		
		Vector2 v1 = new Vector2(2,3);
		Vector2 v2 = new Vector2(6,4);
		Vector2 v3 = new Vector2(3,5);
		Vector2 v4 = new Vector2(5,1);
		Vector2 v5 = new Vector2(10,6);
		Vector2 v6 = new Vector2(9,7);
		
		Vector2 u1 = new Vector2(1,9);
		Vector2 u2 = new Vector2(2,7);
		Vector2 u3 = new Vector2(3,8);
		
		ArrayList<Vector2> points = new ArrayList<Vector2>();
		points.add(v1);
		points.add(v2);
		points.add(v3);
		points.add(v4);
		points.add(v5);
		points.add(v6);
		ConvexPolygon p = new ConvexPolygon(points);
		addConvex(p);

		ArrayList<Vector2> points2 = new ArrayList<Vector2>();
		points2.add(u1);
		points2.add(u2);
		points2.add(u3);
		
		ConvexPolygon q = new ConvexPolygon(points2);
		addConvex(q);
		
		Rectangle bottom = new Rectangle(Constants.WIDTH, 2);
		bottom.translate(Constants.WIDTH / 2, -1);
		Body bottomWall = new Body();
		bottomWall.addFixture(bottom);
		world.addBody(bottomWall);
		
		Rectangle top = new Rectangle(Constants.WIDTH, 2);
		top.translate(Constants.WIDTH / 2, Constants.HEIGHT + 1);
		Body topWall = new Body();
		topWall.addFixture(top);
		world.addBody(topWall);
		
		Rectangle left = new Rectangle(2, Constants.HEIGHT);
		left.translate(-1, Constants.HEIGHT / 2);
		Body leftWall = new Body();
		leftWall.addFixture(left);
		world.addBody(leftWall);
		
		Rectangle right = new Rectangle(2, Constants.HEIGHT);
		right.translate(Constants.WIDTH + 1, Constants.HEIGHT / 2);
		Body rightWall = new Body();
		rightWall.addFixture(right);
		world.addBody(rightWall);
	}
	
	public ConvexPolygon pickBody(Vector2 point) {
		ConvexPolygon picked = null;
		for(ConvexPolygon p : polygons) {
			if (p.contains(point)) {
				picked = p;
			}
		}
		return picked;
	}
	
	public void addConvex(ArrayList<Vector2> points) {
		addConvex(new ConvexPolygon(points));
	}
	
	public void addConvex(ConvexPolygon p) {
		polygons.add(p);
		world.addBody(p);
	}
	
	public void removeConvex(ConvexPolygon p) {
		polygons.remove(p);
		world.removeBody(p);
	}
	
	public void update(double dt) {
		world.updatev(dt);
	}
	
	public void display(GL2 gl) {
		for(ConvexPolygon p : polygons) {
			p.display(gl);
		}
	}
	
}
