package cs5643.forces;

import javax.media.opengl.*;

import cs5643.particles.Particle;

/**
 * Particle system force.
 * 
 * @author Doug James, January 2007
 */
public interface Force  
{
    /** 
     * Applies this force to the given particle.
     */
    public void applyForce(Particle p);

    /** Display any instructive force information, e.g., direction. */
    public void display(GL2 gl);
}
