package cs5643.fracture;
import org.dyn4j.geometry.Vector2;

public class Constants {

	public static double DT = 0.01;
	public static long DT_MILLIS = (long)(DT * 1000);
	public static double HEIGHT = 10;
	public static double WIDTH = 10.0 * 4.0 / 3.0;
	
	public static double FRACTURE_PAD = 0.01;
	
	public static double EXPLOSION_IMPULSE = 10;
	
	public static Vector2 ZERO = new Vector2(0,0);
	
	public static double IMPACT_RADIUS = 0.2;
	
	public static double CONTACT_EPSILON = 1e-23;
	
	public static double WALL_THICKNESS = 10;
	
	public static double MIN_MASS = 1e-6;
	public static double CLEAN_UP_LIMIT = 1e-2;
	
	public static double BULLET_VELOCITY = 100;
	
	public static int KILLS_LIMIT = 4;
	
	public static double MIN_VELOCITY = 40;
	public static double MIN_VEL_SQ = Math.pow(MIN_VELOCITY, 2);
	
}
