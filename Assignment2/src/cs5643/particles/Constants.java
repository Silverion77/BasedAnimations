package cs5643.particles;

/**
 * Default constants. Add your own as necessary.
 * 
 * @author Doug James, January 2007
 * @author Eston Schweickart, February 2014
 */
public interface Constants
{
    /** Mass of a particle. */
    public static final double PARTICLE_MASS     = 1.0;

    /** Camera rotation speed constants. */
    public static final double CAM_SIN_THETA     = Math.sin(0.1);
    public static final double CAM_COS_THETA     = Math.cos(0.1);
    
   /** Number of iterations to run constraint projection */
    public static final int NUM_SOLVER_ITERATIONS = 2;
    
    /** rho_0, the desired standing density of the fluid. */
    public static final double REST_DENSITY = 2300;
    
    /** The "smearing distance" of the wave functions. */
    public static final double KERNEL_RADIUS_H = 0.1;
    
    /** The coefficient for the viscosity filter. */
    public static final double VISCOSITY_C = 0.000001;
    
    /** In case we are implementing elastic collisions. Currently unused. */
    public static final double ELASTICITY_R = 0.1;
    
    /** The distance covered by each side of each bin. */
    public static final double BIN_STEP = KERNEL_RADIUS_H;
    
    public static final int NUM_BINS = (int)Math.ceil(1. / BIN_STEP);
    
}
