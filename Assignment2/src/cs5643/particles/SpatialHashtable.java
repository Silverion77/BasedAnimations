package cs5643.particles;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Implements spatial hashing as per "Optimized Spatial Hashing for
 * Collision Detection of Deformable Objects" by Teschner et al.
 * 
 * @author Chris
 *
 */
public class SpatialHashtable {
	
	private HashMap<Int3, TimestampedBucket> map;
	private Int3 triple;
	private CollisionList colls;
	
	public SpatialHashtable() {
		map = new HashMap<Int3, TimestampedBucket>();
		triple = new Int3(0,0,0);
		colls = new CollisionList();
	}
	
	public CollisionList getCollisions() {
		return colls;
	}
	
	public void clearCollisions() {
		colls.clear();
	}
	
	public void add(Particle p, int timestamp) {
		triple.set(p.x_star);
		if(map.containsKey(triple)) {
			TimestampedBucket bucket = map.get(triple);
			bucket.add(p, timestamp);
		}
		else {
			TimestampedBucket bucket = new TimestampedBucket();
			map.put(new Int3(triple), bucket);
			bucket.add(p, timestamp);
		}
	}
	
	private Vector3d temp = new Vector3d();
	
	public void findCollisions(Triangle t, int timestamp) {
		int min_x = (int)Math.floor(Math.min(t.v2.x_star.x, Math.min(t.v0.x_star.x, t.v1.x_star.x)) / Constants.BIN_SIZE);
		int max_x = (int)Math.floor(Math.max(t.v2.x_star.x, Math.max(t.v0.x_star.x, t.v1.x_star.x)) / Constants.BIN_SIZE);
		int min_y = (int)Math.floor(Math.min(t.v2.x_star.y, Math.min(t.v0.x_star.y, t.v1.x_star.y)) / Constants.BIN_SIZE);
		int max_y = (int)Math.floor(Math.max(t.v2.x_star.y, Math.max(t.v0.x_star.y, t.v1.x_star.y)) / Constants.BIN_SIZE);
		int min_z = (int)Math.floor(Math.min(t.v2.x_star.z, Math.min(t.v0.x_star.z, t.v1.x_star.z)) / Constants.BIN_SIZE);
		int max_z = (int)Math.floor(Math.max(t.v2.x_star.z, Math.max(t.v0.x_star.z, t.v1.x_star.z)) / Constants.BIN_SIZE);
		for(int x = min_x; x <= max_x; x++) {
			for(int y = min_y; y <= max_y; y++) {
				for(int z = min_z; z <= max_z; z++) {
					triple.set(x,y,z);
					if(!map.containsKey(triple)) continue;
					TimestampedBucket bucket = map.get(triple);
					bucket.verifyTimestamp(timestamp);
					for(Particle p : bucket.particles) {
						if(Utils.particleTriangleCollided(p, t)) {
							colls.add(p, t);
							t.addPenaltyForce(p, 10);
						}
						else {
							t.addPenaltyForce(p, -10);
						}
					}
				}
			}
		}
	}
	
	public void findCollisions(Mesh m, int timestamp) {
		for(Triangle t : m.triangles) {
			findCollisions(t, timestamp);
		}
	}

}

/**
 * Three integers all bundled up into one happy tuple
 * @author Chris
 *
 */
class Int3 {

	public static final int prime1 = 73856093;
	public static final int prime2 = 19349663;
	public static final int prime3 = 83492791;
	
	int x, y, z;
	public Int3(int x, int y, int z) {
		set(x,y,z);
	}
	
	public Int3(Int3 other) {
		this(other.x, other.y, other.z);
	}
	
	public void set(Point3d p) {
		x = (int)Math.floor(p.x / Constants.BIN_SIZE);
		y = (int)Math.floor(p.y / Constants.BIN_SIZE);
		z = (int)Math.floor(p.z / Constants.BIN_SIZE);
	}
	
	public void set(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public int hashCode() {
		return (x * prime1) ^ (y * prime2) ^ (z * prime3);
	}
	
	public boolean equals(Object other) {
		if(other instanceof Int3) {
			Int3 i = (Int3)other;
			return (x == i.x && y == i.y && z == i.z);
		}
		return false;
	}
}

class TimestampedBucket {
	int timestamp;
	List<Particle> particles;
	
	public TimestampedBucket() {
		timestamp = -1;
		particles = new ArrayList<Particle>();
	}
	
	public void add(Particle p, int newTimestamp) {
		verifyTimestamp(newTimestamp);
		particles.add(p);
	}
	
	public void verifyTimestamp(int t) {
		if(timestamp != t) {
			particles.clear();
			timestamp = t;
		}
	}
}