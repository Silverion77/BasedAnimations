package cs5643.particles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Hashes particles into buckets divided up by their location
 * in space. Each bucket represents a cubic section of the space.
 * 
 * @author Chris
 *
 */
public class SpaceMap {
	
	private HashMap<IntTriple, List<Particle>> map;
	private List<Particle> tempList;
	private IntTriple triple;
	
	public SpaceMap() {
		map = new HashMap<IntTriple, List<Particle>>();
		triple = new IntTriple(0,0,0);
		tempList = new ArrayList<Particle>();
	}
	
	public void addParticle(Particle p) {
		triple.floorAll(p.x.x, p.x.y, p.x.z);
		if(map.containsKey(triple)) {
			List<Particle> lst = map.get(triple);
			lst.add(p);
		}
		else {
			List<Particle> lst = new ArrayList<Particle>();
			map.put(triple, lst);
			lst.add(p);
		}
	}
	
	public void addAll(List<Particle> ps) {
		for (Particle p : ps) {
			addParticle(p);
		}
	}
	
	/**
	 * Sets p's list of neighbors according to this space map.
	 * @param p
	 * @return
	 */
	public void getNeighbors(Particle p) {
		p.neighbors.clear();
		tempList.clear();
		triple.floorAll(p.x.x, p.x.y, p.x.z);
		int x = triple.x;
		int y = triple.y;
		int z = triple.z;
		for(int i = x-1; i <= x+1; i++) {
			for(int j = y-1; j <= y+1; j++) {
				for(int k = z-1; k <= z+1; k++) {
					triple.setAll(i,j,k);
					List<Particle> lst = map.get(triple);
					if(lst != null) {
						for (Particle q : lst) {
							if(q.x.distanceSquared(p.x) < Constants.H2) {
								p.neighbors.add(q);
							}
							if(p.neighbors.size() > 20) {
								break;
							}
						}
					}
				}
			}
		}
	}
	
	public void clear() {
		for(List<Particle> lst : map.values()) {
			lst.clear();
		}
	}

}

/**
 * This class exists because Java doesn't have tuples. = =
 * @author Chris
 *
 */
class IntTriple {
	
	int x,y,z;
	
	public IntTriple(int x, int y, int z) {
		setAll(x,y,z);
	}
	
	public void setAll(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void floorAll(double x, double y, double z) {
		this.x = (int)x;
		this.y = (int)y;
		this.z = (int)z;
	}
	
	public boolean equals(Object other) {
		if(other instanceof IntTriple) {
			IntTriple i = (IntTriple)other;
			return (this.x == i.x && this.y == i.y && this.z == i.z);
		}
		return false;
	}
	
	public int hashCode() {
		return 17*x + 29*y + 7*z;
	}
	
}
