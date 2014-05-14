import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.media.opengl.GL2;

import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;


public class WeldedPolygon extends Fracturable {
	
	private ArrayList<Polygon> pieces;
	private float[] color;
	
	public WeldedPolygon() {
		pieces = new ArrayList<Polygon>();
		color = new float[4];
		color[0] = (float)Math.random() * 0.5f + 0.5f;
		color[1] = (float)Math.random() * 0.5f + 0.5f;
		color[2] = (float)Math.random() * 0.5f + 0.5f;
		color[3] = 1.0f;
	}
	
	public WeldedPolygon(Collection<Polygon> lst) {
		this();
		for(Polygon p : lst) {
			addPiece(p);
		}
	}
	
	public void addPiece(Polygon p) {
		pieces.add(p);
		this.addFixture(p, 1);
	}
	
	private static HashSet<Polygon> findConnected(HashSet<Polygon> verts) {
		HashSet<Polygon> frontier = new HashSet<Polygon>();
		HashSet<Polygon> toAdd = new HashSet<Polygon>();
		HashSet<Polygon> explored = new HashSet<Polygon>();
		
		Polygon first = verts.iterator().next();
		
		frontier.add(first);
		while(!frontier.isEmpty()) {
			for(Polygon p : frontier) {
				if(explored.contains(p)) continue;
				explored.add(p);
				for(Polygon q : verts) {
					if(!frontier.contains(q) && !explored.contains(q) && Utils.polygonsAdjacent(p, q)) {
						toAdd.add(q);
					}
				}
			}
			frontier.clear();
			for(Polygon p : toAdd) {
				frontier.add(p);
			}
			toAdd.clear();
		}
		return explored;
	}
	
	public static ArrayList<WeldedPolygon> splitIslands(ArrayList<Polygon> verts) {
		HashSet<Polygon> allVerts = new HashSet<Polygon>(verts);
		ArrayList<WeldedPolygon> components = new ArrayList<WeldedPolygon>();
		while(!allVerts.isEmpty()) {
			HashSet<Polygon> component = findConnected(allVerts);
			WeldedPolygon wp = new WeldedPolygon(component);
			wp.setMass();
			components.add(wp);
			allVerts.removeAll(component);
		}
		return components;
	}
	
	public void display(GL2 gl) {
		gl.glPushMatrix();

		// transform the coordinate system from world coordinates to local coordinates  
		gl.glTranslated(this.transform.getTranslationX(), this.transform.getTranslationY(), 0.0);
		// rotate about the z-axis
		gl.glRotated(Math.toDegrees(this.transform.getRotation()), 0.0, 0.0, 1.0);

		gl.glColor4fv(this.color, 0);
		for(Polygon p : pieces) {
			gl.glColor3f(color[0], color[1], color[2]);
			
			gl.glBegin(GL2.GL_POLYGON);
			for (Vector2 v : p.getVertices()) {
				gl.glVertex2d(v.x, v.y);
			}
			gl.glEnd();

			gl.glColor3f(0, 0, 0);

			gl.glBegin(GL2.GL_LINE_LOOP);
			for (Vector2 v : p.getVertices()) {
				gl.glVertex2d(v.x, v.y);
			}
			gl.glEnd();
		}

		// restore the old transform
		gl.glPopMatrix();
	}
	
	@Override
	public void polygonsWithinR(double r, Vector2 point, ArrayList<Polygon> within, ArrayList<Polygon> outside) {
		Vector2 local = this.getLocalPoint(point);
		for(Polygon p : pieces) {
			if(Utils.distancePointToPolygon(p, local) <= r) {
				within.add(p);
			}
			else {
				outside.add(p);
			}
		}
	}
}
