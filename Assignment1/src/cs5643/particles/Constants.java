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
    public static final double CAM_SIN_THETA     = Math.sin(0.2);
    public static final double CAM_COS_THETA     = Math.cos(0.2);
    
   /** Number of iterations to run the inner loop (position corrections). */
    public static final int NUM_CORRECTION_ITERATIONS = 3;
    
    /** rho_0, the desired standing density of the fluid. */
    public static final double REST_DENSITY = 2500;
    
    /** The "smearing distance" of the wave functions. */
    public static final double KERNEL_RADIUS_H = 0.1;
    
    public static final double VISCOSITY_C = 0.00001;
    public static final double ELASTICITY_R = 0.1;
    
    public static final double TENSION_K = 0.1;
    public static final double TENSION_DELTA_Q = 0.2 * KERNEL_RADIUS_H;
    public static final double DELTA_Q2 = TENSION_DELTA_Q * TENSION_DELTA_Q;
    
    public static final double TENSION_N = 4;
    
    public static final double VORTICITY_EPSILON = 1;
    
    /** :( */
    public static final double SPIKY_DAMPING = 0.001;
    
    /** The distance covered by each side of each bin. */
    public static final double BIN_STEP = KERNEL_RADIUS_H;
    
    public static final int NUM_BINS = (int)Math.ceil(1. / BIN_STEP);
    
    /**
     * "Alternatively, constraint force mixing (CFM) [Smith 2006] can be 
     * used to regularize the constraint."
     */
    public static final double MAGIC_EPSILON = 0.1;

    /** h raised to the ninth power. Used in poly6 kernel. */
    public static final double H9 = Math.pow(KERNEL_RADIUS_H, 9);
    
    /** Also used in poly6 kernel. */
    public static final double H2 = KERNEL_RADIUS_H * KERNEL_RADIUS_H;
    
    /** h raised to the sixth power. Used in spiky kernel. */
    public static final double H6 = Math.pow(KERNEL_RADIUS_H, 6);
    
}
