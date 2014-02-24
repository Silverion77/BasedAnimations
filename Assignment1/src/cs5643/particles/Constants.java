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
    public static final int NUM_CORRECTION_ITERATIONS = 1;
    
    /** rho_0, the desired standing density of the fluid. */
    public static final double REST_DENSITY = 1;
    
    /**
     * The number of bins we are dividing the space into along each of the 3 axes,
     * for the 3-dimensional list structure.
     */
    public static final int NUM_BINS = 10;
    
    /** The distance covered by each side of each bin. */
    public static final double BIN_STEP = 1.0 / NUM_BINS;
    
    /** The "smearing distance" of the wave functions. */
    public static final double KERNEL_RADIUS_H = 0.1;
    
    /**
     * "Alternatively, constraint force mixing (CFM) [Smith 2006] can be 
     * used to regularize the constraint."
     */
    public static final double MAGIC_EPSILON = 0.001;

    /** h raised to the ninth power. Used in poly6 kernel. */
    public static final double H9 = Math.pow(KERNEL_RADIUS_H, 9);
    
    /** Also used in poly6 kernel. */
    public static final double H2 = KERNEL_RADIUS_H * KERNEL_RADIUS_H;
    
    /** h raised to the sixth power. Used in spiky kernel. */
    public static final double H6 = Math.pow(KERNEL_RADIUS_H, 6);
    
}
