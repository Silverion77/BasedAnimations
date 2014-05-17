package cs5643.fracture;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import javax.media.opengl.GL2;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.World;
import org.dyn4j.dynamics.contact.ContactPoint;
import org.dyn4j.geometry.AABB;
import org.dyn4j.geometry.Mass;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;

public class FractureSystem {

	private Random rng;
	private World world;
	private ArrayList<ConvexPolygon> polygons;
	private ArrayList<WeldedPolygon> weldedPolygons;
	private ArrayList<Bullet> bullets;
	private ArrayList<Bullet> bulletsToRemove;

	private Object lock = new Object();

	private ArrayList<FractureMap> fractureMaps;
	public int currentMap = 0;

	public FractureSystem() {
		polygons = new ArrayList<ConvexPolygon>();
		weldedPolygons = new ArrayList<WeldedPolygon>();
		bullets = new ArrayList<Bullet>();
		bulletsToRemove = new ArrayList<Bullet>();
		world = new World();
		rng = new Random();

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
		addConvex(p);

		ArrayList<Vector2> points2 = new ArrayList<Vector2>();
		points2.add(u1);
		points2.add(u2);
		points2.add(u3);

		ConvexPolygon q = new ConvexPolygon(points2);
		addConvex(q);

		fractureMaps = new ArrayList<FractureMap>();
		fractureMaps.add(makeRandomFractureMap(10));

		// TODO: End of test stuff to remove
		addWalls();
	}

	public FractureMap makeCubesFractureMap(int numCubes) {
		ArrayList<ConvexPolygon> convs = new ArrayList<ConvexPolygon>();
		ArrayList<Vector2> pts = new ArrayList<Vector2>();

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
		return new FractureMap(convs, true);
	}

	public FractureMap makeRandomFractureMap(int numPoints) {
		ArrayList<Vector2> pts = new ArrayList<Vector2>();
		for(int i = 0; i < numPoints; i++) {
			Vector2 randVec = new Vector2(rng.nextDouble(), rng.nextDouble());
			pts.add(randVec);
		}
		return FractureMapFactory.generateVoronoi(pts);
	}

	public void addWalls() {

		Rectangle bottom = new Rectangle(Constants.WIDTH, Constants.WALL_THICKNESS);
		bottom.translate(Constants.WIDTH / 2, -Constants.WALL_THICKNESS / 2);
		Body bottomWall = new Body();
		bottomWall.addFixture(bottom);
		bottomWall.setMass(Mass.Type.INFINITE);
		world.addBody(bottomWall);

		Rectangle top = new Rectangle(Constants.WIDTH, Constants.WALL_THICKNESS);
		top.translate(Constants.WIDTH / 2, Constants.HEIGHT + Constants.WALL_THICKNESS / 2);
		Body topWall = new Body();
		topWall.addFixture(top);
		topWall.setMass(Mass.Type.INFINITE);
		world.addBody(topWall);

		Rectangle left = new Rectangle(Constants.WALL_THICKNESS, Constants.HEIGHT + 2 * Constants.WALL_THICKNESS);
		left.translate(-Constants.WALL_THICKNESS / 2, Constants.HEIGHT / 2);
		Body leftWall = new Body();
		leftWall.setMass(Mass.Type.INFINITE);
		leftWall.addFixture(left);
		world.addBody(leftWall);

		Rectangle right = new Rectangle(Constants.WALL_THICKNESS, Constants.HEIGHT + 2 * Constants.WALL_THICKNESS);
		right.translate(Constants.WIDTH + Constants.WALL_THICKNESS / 2, Constants.HEIGHT / 2);
		Body rightWall = new Body();
		rightWall.addFixture(right);
		rightWall.setMass(Mass.Type.INFINITE);
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

	public FractureMap getCurrentMap() {
		return fractureMaps.get(currentMap);
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

	public void clear() {
		synchronized(lock) {
			world.removeAllBodies();
			world.removeAllJoints();
			polygons.clear();
			weldedPolygons.clear();
			bullets.clear();
			addWalls();
		}
	}

	ArrayList<Polygon> fractured = new ArrayList<Polygon>();
	ArrayList<Polygon> unfractured = new ArrayList<Polygon>();
	ArrayList<Polygon> temp = new ArrayList<Polygon>();
	ArrayList<Polygon> fuse_back = new ArrayList<Polygon>();
	ArrayList<Polygon> fractured_pieces = new ArrayList<Polygon>();

	public void fracture(Fracturable wp, Vector2 impactPoint) {

		fractured.clear();
		unfractured.clear();
		temp.clear();
		fractured_pieces.clear();
		fuse_back.clear();

		wp.polygonsWithinR(Constants.IMPACT_RADIUS, impactPoint, fractured, unfractured);

		AABB box = null;

		for(Polygon p : fractured) {
			if(box == null) {
				box = p.createAABB(wp.getTransform());
			}
			else {
				box.union(p.createAABB());
			}
		}

		Vector2 lowerLeft = new Vector2(impactPoint.x, impactPoint.y);

		if(box == null) return;
		double maxXDiff = Math.max(box.getMaxX() - impactPoint.x, impactPoint.x - box.getMinX());
		double maxYDiff = Math.max(box.getMaxY() - impactPoint.y, impactPoint.y - box.getMinY());
		double maxSide = Math.max(2 * maxXDiff, 2 * maxYDiff);

		FractureMap shifted = fractureMaps.get(currentMap).translateAndScale(lowerLeft, maxSide);

		for(Polygon p : fractured) {
			temp.addAll(shifted.fracture(p, wp.getTransform()));
		}
		for(Polygon p : temp) {
			if(Utils.distancePointToPolygon(p, impactPoint) > Constants.IMPACT_RADIUS) {
				fuse_back.add(p);
			}
			else {
				fractured_pieces.add(p);
			}
		}
		for(Polygon p : fractured_pieces) {
			ConvexPolygon conv = new ConvexPolygon(p);
			addConvex(conv);
			conv.setLinearVelocity(wp.getLinearVelocity());
			lowerLeft.set(conv.getWorldCenter()).subtract(impactPoint);
			lowerLeft.normalize();
			lowerLeft.multiply(Constants.EXPLOSION_IMPULSE * conv.getMass().getMass());
			conv.applyImpulse(lowerLeft);
		}
		if(unfractured.isEmpty() && fuse_back.isEmpty()) {
			remove(wp);
			return;
		}
		for(Polygon p : unfractured) {
			p.rotate(wp.getTransform().getRotation());
			p.translate(wp.getTransform().getTranslation());
		}
		unfractured.addAll(fuse_back);
		for (WeldedPolygon welded : WeldedPolygon.splitIslands(unfractured)) {
			if(welded.getMass().getMass() < Constants.MIN_MASS) continue;
			welded.setLinearVelocity(wp.getLinearVelocity());
			lowerLeft.set(welded.getWorldCenter()).subtract(impactPoint);
			lowerLeft.normalize();
			lowerLeft.multiply(Constants.EXPLOSION_IMPULSE * welded.getMass().getMass());
			welded.applyImpulse(lowerLeft);
			addWelded(welded);
		}
		remove(wp);
	}

	public void addFractureMap(FractureMap fm) {
		fractureMaps.add(fm);
		currentMap = fractureMaps.size() - 1;
	}

	private void addConvex(ConvexPolygon p) {
		if(p.getMass().getMass() < Constants.MIN_MASS) return;
		polygons.add(p);
		world.addBody(p);
	}

	public void add(Fracturable f) {
		synchronized(lock) {
			if(f instanceof ConvexPolygon) {
				addConvex((ConvexPolygon)f);
			}
			else if(f instanceof WeldedPolygon) {
				addWelded((WeldedPolygon)f);
			}
		}
	}

	public void remove(Fracturable f) {
		synchronized(lock) {
			if(f instanceof ConvexPolygon) {
				removeConvex((ConvexPolygon)f);
			}
			else if (f instanceof WeldedPolygon) {
				removeWelded((WeldedPolygon)f);
			}
		}
	}

	private void removeConvex(ConvexPolygon p) {
		if(p instanceof Bullet) {
			bullets.remove(p);
		}
		polygons.remove(p);
		world.removeBody(p);
	}

	private ArrayList<Vector2> bulletPoints = new ArrayList<Vector2>();

	public void shootBullet(Vector2 origin, Vector2 target) {
		if(origin.equals(target)) return;
		Vector2 targetDir = target.difference(origin);
		targetDir.normalize();
		Vector2 pointy = origin.sum(targetDir);
		// Rotate 90 degrees left
		target.set(-targetDir.y, targetDir.x).multiply(0.2);
		Vector2 leftPoint = origin.sum(target);
		target.negate();
		Vector2 rightPoint = origin.sum(target);
		bulletPoints.clear();
		bulletPoints.add(pointy);
		bulletPoints.add(leftPoint);
		bulletPoints.add(rightPoint);
		Bullet bullet = new Bullet(bulletPoints);
		bullet.setBullet(true);

		targetDir.multiply(Constants.BULLET_VELOCITY);
		bullet.setLinearVelocity(targetDir);
		bullet.setMass(Mass.Type.INFINITE);

		synchronized(lock) {
			bullets.add(bullet);
			world.addBody(bullet);
		}
	}

	private void addWelded(WeldedPolygon p) {
		weldedPolygons.add(p);
		world.addBody(p);
	}

	private void removeWelded(WeldedPolygon p) {
		weldedPolygons.remove(p);
		world.removeBody(p);
	}

	HashSet<Fracturable> fracked = new HashSet<Fracturable>();
	
	private void processBulletFractures() {
		synchronized(lock) {
			for(Bullet bullet : bullets) {
				fracked.clear();
				for(ContactPoint cpt : bullet.getContacts(false)) {
					Body other;
					Vector2 point = cpt.getPoint();
					if(cpt.getBody1().equals(bullet)) {
						other = cpt.getBody2();
					}
					else {
						other = cpt.getBody1();
					}
					if(other instanceof Fracturable && !(other instanceof Bullet)) {
						bullet.awardKill();
						Fracturable breakMe = (Fracturable)other;
						if(fracked.contains(breakMe)) continue;
						else {
							fracked.add(breakMe);
							fracture(breakMe, point);
							if(bullet.getKills() >= Constants.KILLS_LIMIT) {
								bulletsToRemove.add(bullet);
								bullet.setBullet(false);
							}
						}
					}
				}
				if((bullet.getWorldCenter().x < 0 || bullet.getWorldCenter().x > Constants.WIDTH)
						&& (bullet.getWorldCenter().y < 0 || bullet.getWorldCenter().y > Constants.HEIGHT)) {
					bulletsToRemove.add(bullet);
					bullet.setBullet(false);
				}
			}
			for(Bullet p : bulletsToRemove) {
				bullets.remove(p);
				world.removeBody(p);
			}
			bulletsToRemove.clear();
		}
	}
	
	ArrayList<Fracturable> toRemove = new ArrayList<Fracturable>();
	
	public void cleanUpSmall(double massLimit) {
		for(ConvexPolygon p : polygons) {
			if(p.getMass().getMass() < massLimit) {
				toRemove.add(p);
			}
		}
		for(WeldedPolygon p : weldedPolygons) {
			if(p.getMass().getMass() < massLimit) {
				toRemove.add(p);
			}
		}
		for(Fracturable f : toRemove) {
			remove(f);
		}
		toRemove.clear();
	}

	public void update(double dt) {
		world.updatev(dt);
		processBulletFractures();
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
		//		fractureMaps.get(currentMap).display(gl);
		synchronized(lock) {
			for(ConvexPolygon p : polygons) {
				p.display(gl);
			}
			for(WeldedPolygon p : weldedPolygons) {
				p.display(gl);
			}
			for(ConvexPolygon p : bullets) {
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
}
