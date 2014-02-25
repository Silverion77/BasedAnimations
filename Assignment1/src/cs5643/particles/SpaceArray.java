package cs5643.particles;

import java.util.ArrayList;
import java.util.List;

public class SpaceArray {
	
	private List<Particle>[][][] map;
	private List<Particle> temp_list;
	
	public SpaceArray() {
		temp_list = new ArrayList<Particle>();
		map = new List[Constants.NUM_BINS][Constants.NUM_BINS][Constants.NUM_BINS];
		for(int i = 0; i < Constants.NUM_BINS; i++) {
			for(int j = 0; j < Constants.NUM_BINS; j++) {
				for(int k = 0; k < Constants.NUM_BINS; k++) {
					map[i][j][k] = new ArrayList<Particle>();
				}
			}
		}
	}
	
	public void addParticle(Particle p) {
		int x = (int)(p.x.x / Constants.BIN_STEP);
		int y = (int)(p.x.y / Constants.BIN_STEP);
		int z = (int)(p.x.z / Constants.BIN_STEP);
		map[x][y][z].add(p);
	}
	
	public void addAll(List<Particle> ps) {
		for (Particle p : ps) {
			addParticle(p);
		}
	}
	
	public void clear() {
		for(int i = 0; i < Constants.NUM_BINS; i++) {
			for(int j = 0; j < Constants.NUM_BINS; j++) {
				for(int k = 0; k < Constants.NUM_BINS; k++) {
					map[i][j][k].clear();
				}
			}
		}
	}
	
	public void getNeighbors(Particle p) {
		p.neighbors.clear();
		temp_list.clear();
		int x = (int)(p.x.x / Constants.BIN_STEP);
		int y = (int)(p.x.y / Constants.BIN_STEP);
		int z = (int)(p.x.z / Constants.BIN_STEP);
		
		for(int i = Math.max(x-1, 0); i <= Math.min(x+1, Constants.NUM_BINS-1); i++) {
			for(int j = Math.max(y-1, 0); j <= Math.min(y+1, Constants.NUM_BINS-1); j++) {
				for(int k = Math.max(z-1, 0); k <= Math.min(z+1, Constants.NUM_BINS-1); k++) {
					temp_list.addAll(map[i][j][k]);
				}
			}
		}
		for(Particle p2 : temp_list) {
			if(p.x.distanceSquared(p2.x) < Constants.H2) {
				p.neighbors.add(p2);
			}
		}
	}

}
