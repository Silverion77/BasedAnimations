package cs5643.fracture;

import java.util.Comparator;

import javax.vecmath.Point2d;

public class PointSorter implements Comparator<Point2d> {

	@Override
	public int compare(Point2d p1, Point2d p2) {
		if(p1.x < p2.x) {
			return -1;
		}
		else if(p1.x == p2.x && p1.y < p2.y) {
			return -1;
		}
		else if(p1.x == p2.x && p1.y == p2.y) {
			return 0;
		}
		else return 1;
	}

}
