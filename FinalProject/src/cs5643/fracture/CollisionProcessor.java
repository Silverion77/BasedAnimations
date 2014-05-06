package cs5643.fracture;

import java.util.ArrayList;

public class CollisionProcessor {
	
	private Convex[] convexes;
	private ArrayList<ConvexPair> candidates;
	
	public CollisionProcessor(ArrayList<Convex> convexes) {
		this.convexes = (Convex[]) convexes.toArray(new Convex[0]);
		candidates = new ArrayList<ConvexPair>();
	}
	
	private void collectCandidates() {
		candidates.clear();
		for(int i = 0; i < convexes.length; i++) {
			for(int j = 0; j < convexes.length; j++) {
				if(i == j) continue;
				Convex ci = convexes[i];
				Convex cj = convexes[j];
				if(Convex.intersects(ci, cj)) {
					candidates.add(new ConvexPair(i, j));
				}
			}
		}
	}
	
	public void processCollisions() {
		collectCandidates();
	}

}