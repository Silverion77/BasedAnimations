package cs5643.particles;

/**
 * Default constants. Add your own as necessary.
 * 
 * @author Doug James, January 2007
 * @author Eston Schweickart, February 2014
 * @author Ari Karo, March 2014
 */
public class Constants
{
	
    /** Mass of a particle. */
    public static final double PARTICLE_MASS     = 1.0;

    /** Camera rotation speed constants. */
    public static final double CAM_SIN_THETA     = Math.sin(0.1);
    public static final double CAM_COS_THETA     = Math.cos(0.1);
    
    /** rho_0, the desired initial density of the cloth in kg/m^2*/
    public static final double REST_DENSITY = 1.54;
    
    /** Number of iterations to run constraint projection */
    public static final int NUM_SOLVER_ITERATIONS = 24;
    
    public static final double DRAG_COEFF = 0.9;
    
    /** Stiffness of cloth stretch constraint. */
    public static double K_STRETCH = 0.5;
    private static double PROJ_K_STRETCH = 1-Math.pow((0.5), 1. / Constants.NUM_SOLVER_ITERATIONS);
    public static double getProjKS() { return PROJ_K_STRETCH; }
    
    public static void setKStretch(double k) {
    	K_STRETCH = k;
    	PROJ_K_STRETCH = Math.pow((1 - k), 1. / Constants.NUM_SOLVER_ITERATIONS);
    	PROJ_K_STRETCH = 1 - PROJ_K_STRETCH;
    }
    
    /** Stiffness of cloth bend constraint. */
    public static double K_BEND = 0.5;
    private static double PROJ_K_BEND = 1-Math.pow((0.5), 1. / Constants.NUM_SOLVER_ITERATIONS);
    public static double getProjKB() { return PROJ_K_BEND; }
    
    public static void setKBend(double k) {
    	K_BEND = k;
    	PROJ_K_BEND = Math.pow((1 - k), 1. / Constants.NUM_SOLVER_ITERATIONS);
    	PROJ_K_BEND = 1 - PROJ_K_BEND;
    }
    
    public static final double K_DAMPING = 0.1;
    
    public static final double K_PRESSURE = 1;
    
    public static final double BIN_SIZE = 0.2;
    
    public static final double H_THICKNESS = 0.005;
    
}
