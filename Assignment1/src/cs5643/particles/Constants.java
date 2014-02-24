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
    
   /**
    * Number of iterations to run the inner loop (position corrections).
    */
    public static final int NUM_CORRECTION_ITERATIONS = 1;

}
