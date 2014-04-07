package cs5643.particles;

import java.util.LinkedList;

public class CollisionList {
	
	private LinkedList<Particle> ps;
	private LinkedList<Triangle> ts;
	
	private Collision tempC;
	
	public CollisionList() {
		ps = new LinkedList<Particle>();
		ts = new LinkedList<Triangle>();
		tempC = new Collision();
	}
	
	public void add(Particle p, Triangle t) {
		ps.addFirst(p);
		ts.addFirst(t);
	}
	
	public void clear() {
		ps.clear();
		ts.clear();
	}
	
	public boolean isEmpty() {
		return ps.isEmpty();
	}
	
	public Collision nextCollision() {
		Particle p = ps.pollFirst();
		Triangle t = ts.pollFirst();
		if(p == null) return null;
		tempC.set(p, t);
		return tempC;
	}

}

class Collision {
	Particle particle;
	Triangle triangle;
	
	public Collision() {
		set(null, null);
	}
	
	public Collision(Particle p, Triangle t) {
		set(p, t);
	}
	
	public void set(Particle p, Triangle t) {
		particle = p;
		triangle = t;
	}
}