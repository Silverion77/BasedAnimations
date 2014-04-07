package cs5643.particles;
import javax.vecmath.*;

/** 
 * Catch-all utilities (feel free to add on). 
 * 
 * @author Doug James, January 2007
 */
public class Utils
{
	
	private static Vector3d collisionTemp = new Vector3d();
	private static Vector3d directionTemp = new Vector3d();
	
	public static boolean particleTriangleCollided(Particle p, Triangle t) {
		if(t.contains(p)) return false;
		// First check if there is a collision. If p was on one side of the triangle
		// previously and is now on the other side, then there was. In other words,
		// if the dot product did not change signs, there was no collision.
		Vector3d n = t.getNormal();
		collisionTemp.sub(p.x, t.v0.x);
		double dot1 = collisionTemp.dot(n);
		Vector3d pn = t.getPredictedNormal();
		collisionTemp.sub(p.x_star, t.v0.x_star);
		double dot2 = collisionTemp.dot(pn);
		if((dot1 >= 0 && dot2 >= 0) || (dot1 <= 0 && dot2 <= 0)) {
			return false;
		}
		// Else there might have been a collision, so we need to verify whether it was actually
		// in the triangle or not. We are using p.x as the point l_0 on the line.
		// Reference: http://en.wikipedia.org/wiki/Line%E2%80%93plane_intersection
		collisionTemp.sub(p.x_star, p.x);
		if(collisionTemp.x == 0 && collisionTemp.y == 0 && collisionTemp.z == 0) {
			return false;
		}
		collisionTemp.normalize();
		directionTemp.set(collisionTemp);
		double denom = collisionTemp.dot(n);
		if(denom == 0) return false;
		collisionTemp.sub(t.v0.x_star, p.x);
		double d = collisionTemp.dot(n) / denom;
		
		directionTemp.scale(d);
		collisionTemp.add(p.x, directionTemp);
		return isPointInTriangle(collisionTemp, t);
	}
	
	public static double pointToLineDistance(Point3d p, Point3d l1, Point3d l2) {
		directionTemp.sub(l2, l1);
		directionTemp.normalize();
		collisionTemp.sub(l1, p);
		double coeff = collisionTemp.dot(directionTemp);
		directionTemp.scale(coeff);
		collisionTemp.sub(directionTemp);
		return collisionTemp.length();
	}
	
	private static Vector3d u = new Vector3d();
	private static Vector3d v = new Vector3d();
	private static Vector3d w = new Vector3d();
	
	private static Vector3d temp1 = new Vector3d(), temp2 = new Vector3d();
	
	/**
	 * Returns whether the point is in the triangle, using the barycentric method
	 * described at http://blogs.msdn.com/b/rezanour/archive/2011/08/07/barycentric-coordinates-and-point-in-triangle-tests.aspx
	 * @param p
	 * @param tri
	 * @return
	 */
	public static boolean isPointInTriangle(Tuple3d p, Triangle tri) {
		// v0 = A, v1 = B, v2 = C
		u.sub(tri.v1.x, tri.v0.x);
		v.sub(tri.v2.x, tri.v0.x);
		w.sub(p, tri.v0.x);
		temp1.cross(v, w);
		temp2.cross(v, u);
		if(temp1.dot(temp2) < 0) return false;
		double magn_vw = temp1.length();
		temp1.cross(u, w);
		temp2.cross(u, v);
		if(temp1.dot(temp2) < 0) return false;
		double magn_uw = temp1.length();
		double magn_uv = temp2.length();
		double r = magn_vw / magn_uv;
		double t = magn_uw / magn_uv;
		return (r + t <= 1);
	}
	
	/**
	 * sum += scale*v
	 */
	public static void acc(Tuple3d sum, double scale, Tuple3d v)
	{
		sum.x += scale * v.x;
		sum.y += scale * v.y;
		sum.z += scale * v.z;
	}


	/**
	 * 
	 * @param pad Pre-padding char.
	 */
	public static String getPaddedNumber(int number, int length, String pad)  {
		return getPaddedString(""+number, length, pad, true);
	}

	/**
	 * @param prePad Pre-pads if true, else post pads.
	 */
	public static String getPaddedString(String s, int length, String pad, boolean prePad) 
	{
		if(pad.length() != 1) throw new IllegalArgumentException("pad must be a single character.");
		String result = s;
		result.trim();
		if(result.length() > length) 
			throw new RuntimeException ("input string "+s+" is already more than length="+length);

		int nPad = (length - result.length());
		for(int k=0; k<nPad; k++) {
			if(prePad) 
				result = pad + result;
			else
				result = result + pad;
		}

		return result;
	}
}
