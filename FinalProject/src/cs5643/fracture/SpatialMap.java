package cs5643.fracture;

import java.util.HashMap;

import javax.vecmath.Point2d;

public class SpatialMap {
	
	private HashMap<Int2, Point2d> map;
	
	public SpatialMap() {
		map = new HashMap<Int2, Point2d>();
	}
	
	

}


class Int2 {
	
	public int x, y;
	
	public Int2(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public static final int prime1 = 73856093;
	public static final int prime2 = 19349663;
	
	public int hashCode() {
		return (x * prime1) ^ (y * prime2);
	}
	
	public boolean equals(Object other) {
		if(other instanceof Int2) {
			Int2 i = (Int2)other;
			return (x == i.x && y == i.y);
		}
		return false;
	}
}
