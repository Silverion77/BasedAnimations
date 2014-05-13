import org.dyn4j.geometry.Vector2;

public class Constants {

	public static double DT = 0.01;
	public static long DT_MILLIS = (long)(DT * 1000);
	public static double HEIGHT = 10;
	public static double WIDTH = 10.0 * 4.0 / 3.0;
	
	public static double FRACTURE_PAD = 0.01;
	
	public static double EXPLOSION_IMPULSE = 50;
	
	public static Vector2 ZERO = new Vector2(0,0);
	
	public static double IMPACT_RADIUS = 1;
	
	public static double CONTACT_EPSILON = 1e-23;
	
	public static double WALL_THICKNESS = 10;
	
}
