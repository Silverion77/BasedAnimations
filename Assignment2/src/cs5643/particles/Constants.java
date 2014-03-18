package cs5643.particles;

/**
 * Default constants. Add your own as necessary.
 * 
 * @author Doug James, January 2007
 * @author Eston Schweickart, February 2014
 * @author Ari Karo, March 2014
 */
public interface Constants
{
    /** Mass of a particle. */
    public static final double PARTICLE_MASS     = 1.0;

    /** Camera rotation speed constants. */
    public static final double CAM_SIN_THETA     = Math.sin(0.1);
    public static final double CAM_COS_THETA     = Math.cos(0.1);
    
    /** rho_0, the desired initial density of the cloth in kg/m^2*/
    public static final double REST_DENSITY = 1.54;
    
    /** Number of iterations to run constraint projection */
    public static final int NUM_SOLVER_ITERATIONS = 2;
    
    /** Stiffness of cloth stretch constraint. */
    public static final double K_STRETCH = .2;
    
    /** Stiffness of cloth bend constraint. */
    public static final double K_BEND = .2;
    
}