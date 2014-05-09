import java.awt.Dimension;
import java.awt.event.MouseEvent;

import javax.media.opengl.GL2;

import org.dyn4j.geometry.Vector2;


public class OrthoMap {

	private int width, height;
	private double ratio;
	private double left;	  
	private double right;
	private double bottom;	  
	private double top;

	public OrthoMap(int windowWidth, int windowHeight) {
		width = windowWidth;
		height = Math.max(windowHeight, 1);
		ratio = (double)width / (double)height;
		System.out.println("aspect ratio = " + ratio);
		left = 0;
		bottom = 0;
		top = Constants.HEIGHT;
		right = ratio * top;
		System.out.println("right = " + right);
	}
	
	public void apply_glOrtho(GL2 gl) {
		gl.glOrtho(left, right, bottom, top, 0, 1);
	}
	
	public void getPoint(MouseEvent e, Vector2 v) {
		Dimension size = e.getComponent().getSize();
		
		double x = right * (double)e.getX()/(double)size.width;

		double y = top * (1. - (double)e.getY()/(double)size.height);
		
		v.set(x, y);
	}
	
}
