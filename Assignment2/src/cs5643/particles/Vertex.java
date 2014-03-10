package cs5643.particles;

import java.util.ArrayList;

import javax.vecmath.Point3d;

/**
 * An implementation of vertices of a particle-based mesh.
 *
 * @author Eston Schweickart, February 2014
 */
public class Vertex extends Particle {

	/** A list of incident edges. See Mesh.java for details. */
	public ArrayList<Edge> edges = new ArrayList<Edge>();

	/** Default constructor.
	 * @param x0 The initial position of the Vertex.
	 */
	public Vertex(Point3d x0) {
		super(x0);
	}

}