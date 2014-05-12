import java.util.ArrayList;

import javax.media.opengl.GL2;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.AABB;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;

public class FractureSystem {

	private World world;
	private ArrayList<ConvexPolygon> polygons;
	private ArrayList<WeldedPolygon> weldedPolygons;
	private long timesteps = 0;
	
	private ConvexPolygon p1, p2;

	private ArrayList<FractureMap> fractureMaps;
	public int currentMap = 0;

	public FractureSystem() {
		polygons = new ArrayList<ConvexPolygon>();
		weldedPolygons = new ArrayList<WeldedPolygon>();
		world = new World();

		// TODO: test stuff that we should delete

		// Make some test shapes
		Vector2 v1 = new Vector2(2,3);
		Vector2 v2 = new Vector2(6,4);
		Vector2 v3 = new Vector2(3,4.5);
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
		p1 = p;
		addConvex(p);

		ArrayList<Vector2> points2 = new ArrayList<Vector2>();
		points2.add(u1);
		points2.add(u2);
		points2.add(u3);

		ConvexPolygon q = new ConvexPolygon(points2);
		p2 = q;
		addConvex(q);

		ArrayList<ConvexPolygon> convs = new ArrayList<ConvexPolygon>();
		ArrayList<Vector2> pts = new ArrayList<Vector2>();

		int numCubes = 20;
		double cubeLength = 1.0 / numCubes;
		// Make fracture map (4x4 cubes)
		for(int x = 0; x < numCubes; x++) {
			for(int y = 0; y < numCubes; y++) {
				pts.clear();
				Vector2 downLeft = new Vector2(x * cubeLength, y * cubeLength);
				Vector2 upLeft = new Vector2(x * cubeLength, (y+1) * cubeLength);
				Vector2 downRight = new Vector2((x+1) * cubeLength, y * cubeLength);
				Vector2 upRight = new Vector2((x+1) * cubeLength, (y+1) * cubeLength);
				pts.add(downLeft);
				pts.add(downRight);
				pts.add(upLeft);
				pts.add(upRight);
				convs.add(new ConvexPolygon(pts));
			}
		}
		
		fractureMaps = new ArrayList<FractureMap>();
		fractureMaps.add(new FractureMap(convs, true));

		// TODO: End of test stuff to remove

		Rectangle bottom = new Rectangle(Constants.WIDTH, Constants.WALL_THICKNESS);
		bottom.translate(Constants.WIDTH / 2, -Constants.WALL_THICKNESS / 2);
		Body bottomWall = new Body();
		bottomWall.addFixture(bottom);
		world.addBody(bottomWall);

		Rectangle top = new Rectangle(Constants.WIDTH, Constants.WALL_THICKNESS);
		top.translate(Constants.WIDTH / 2, Constants.HEIGHT + Constants.WALL_THICKNESS / 2);
		Body topWall = new Body();
		topWall.addFixture(top);
		world.addBody(topWall);

		Rectangle left = new Rectangle(Constants.WALL_THICKNESS, Constants.HEIGHT + 2 * Constants.WALL_THICKNESS);
		left.translate(-Constants.WALL_THICKNESS / 2, Constants.HEIGHT / 2);
		Body leftWall = new Body();
		leftWall.addFixture(left);
		world.addBody(leftWall);

		Rectangle right = new Rectangle(Constants.WALL_THICKNESS, Constants.HEIGHT + 2 * Constants.WALL_THICKNESS);
		right.translate(Constants.WIDTH + Constants.WALL_THICKNESS / 2, Constants.HEIGHT / 2);
		Body rightWall = new Body();
		rightWall.addFixture(right);
		world.addBody(rightWall);
	}
	
	public void nextMap() {
		currentMap = (currentMap + 1) % fractureMaps.size();
	}
	
	public void previousMap() {
		if (currentMap == 0)
			currentMap = fractureMaps.size() - 1;
		else
			currentMap--;
	}

	public Fracturable pickBody(Vector2 point) {
		Fracturable picked = null;
		for(ConvexPolygon p : polygons) {
			if (p.contains(point)) {
				picked = p;
			}
		}
		if(picked == null) {
			for(WeldedPolygon p : weldedPolygons) {
				if(p.contains(point)) {
					picked = p;
				}
			}
		}
		return picked;
	}
	
	ArrayList<Polygon> fractured = new ArrayList<Polygon>();
	ArrayList<Polygon> unfractured = new ArrayList<Polygon>();
	
	public void fracture(Fracturable wp, Vector2 impactPoint) {
		
	}

	public void fractureConvex(ConvexPolygon cp, Vector2 impactPoint) {
		AABB box = cp.createAABB();
		Vector2 lowerLeft = new Vector2(box.getMinX(), box.getMinY());
		double maxSide = Math.max(box.getHeight(), box.getWidth());
		FractureMap shifted = fractureMaps.get(currentMap).translateAndScale(lowerLeft, maxSide);
		ArrayList<Polygon> pieces = shifted.fracture(cp);
		removeConvex(cp);
		ArrayList<Polygon> uncut = new ArrayList<Polygon>();
		for(Polygon piece : pieces) {
			if(Utils.distancePointToPolygon(piece, impactPoint) > Constants.IMPACT_RADIUS) {
				uncut.add(piece);
			}
			else {
				ConvexPolygon conv = new ConvexPolygon(piece);
				// TODO: fusing according to the cells
				addConvex(conv);
				conv.setLinearVelocity(cp.getLinearVelocity());
				lowerLeft.set(conv.getWorldCenter()).subtract(impactPoint);
				lowerLeft.normalize();
				lowerLeft.multiply(Constants.EXPLOSION_IMPULSE * conv.getMass().getMass());
				conv.applyImpulse(lowerLeft);
			}
		}
		for(WeldedPolygon wp : WeldedPolygon.splitIslands(uncut)) {
			addWelded(wp);
		}
	}

	public void addConvex(ArrayList<Vector2> points) {
		addConvex(new ConvexPolygon(points));
	}

	public void addConvex(ConvexPolygon p) {
		polygons.add(p);
		world.addBody(p);
	}

	public void remove(Fracturable f) {
		if(f instanceof ConvexPolygon) {
			removeConvex((ConvexPolygon)f);
		}
		else if (f instanceof WeldedPolygon) {
			removeWelded((WeldedPolygon)f);
		}
	}

	public void removeConvex(ConvexPolygon p) {
		polygons.remove(p);
		world.removeBody(p);
	}

	public void addWelded(WeldedPolygon p) {
		p.setMass();
		weldedPolygons.add(p);
		world.addBody(p);
	}

	public void removeWelded(WeldedPolygon p) {
		weldedPolygons.remove(p);
		world.removeBody(p);
	}

	public void update(double dt) {
		timesteps++;
		world.updatev(dt);
	}

	//	private Vector2 displayPoint = new Vector2();

	public static Vector2[] display = new Vector2[0];
	public static float[][] colors = new float[0][3];

	public static void copyToDisplay(Vector2[] vs, float[][] cs) {
		Vector2[] display2 = new Vector2[vs.length + display.length];
		float[][] colors2 = new float[vs.length + display.length][3];
		for(int i = 0; i < display.length; i++) {
			display2[i] = display[i];
			colors2[i][0] = colors[i][0];
			colors2[i][1] = colors[i][1];
			colors2[i][2] = colors[i][2];
		}
		for(int i = 0; i < vs.length; i++) {
			display2[display.length + i] = vs[i].copy();
			colors2[display.length + i][0] = cs[i][0];
			colors2[display.length + i][1] = cs[i][1];
			colors2[display.length + i][2] = cs[i][2];
		}
		display = display2;
		colors = colors2;
	}

	public static void clearDisplay() {
		display = new Vector2[0];
	}

	public void display(GL2 gl) {
		fractureMaps.get(currentMap).display(gl);
		for(ConvexPolygon p : polygons) {
			p.display(gl);
		}
		for(WeldedPolygon p : weldedPolygons) {
			p.display(gl);
		}
		if(display != null) {
			for(int i = 0; i < display.length; i++) {
				gl.glColor3f(colors[i][0], colors[i][1], colors[i][2]);
				gl.glBegin(GL2.GL_POINTS);
				gl.glVertex2d(display[i].x, display[i].y);
				gl.glEnd();
			}
		}
	}

}
