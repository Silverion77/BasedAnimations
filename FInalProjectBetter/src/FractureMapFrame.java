import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;

import org.dyn4j.geometry.Vector2;

import com.jogamp.opengl.util.Animator;

public class FractureMapFrame implements GLEventListener {	
	private JFrame frame;
	private FractureSystem fs;
	private boolean newMap;
	
	private ArrayList<Vector2> points = new ArrayList<Vector2>();
	
	public FractureMapFrame(FractureSystem fracSystem) {
		fs = fracSystem;
		newMap = false;
		
		frame = new JFrame("CS5643 Fracture Map Creator");
		GLProfile glp = GLProfile.getDefault();
		GLCapabilities glc = new GLCapabilities(glp);
		GLCanvas canvas = new GLCanvas(glc);
		canvas.addGLEventListener(this);
		frame.add(canvas);
		
		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (newMap && e.getButton() == MouseEvent.BUTTON1) {
					Vector2 point = new Vector2();
					Dimension size = e.getComponent().getSize();
					double x = (double)e.getX()/(double)size.width;
					double y = (1. - (double)e.getY()/(double)size.height);
					point.set(x,y);
					points.add(point);
				}
				else if (newMap && e.getButton() == MouseEvent.BUTTON3) {
					createFractureMapFromPoints();
				}
			}
		});
		
		final Animator animator = new Animator(canvas);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// Run this on another thread than the AWT event queue to
				// make sure the call to Animator.stop() completes before
				// exiting
				new Thread(new Runnable() {
					public void run() {
						animator.stop();
						System.exit(0);
					}
				}).start();
			}
		});
		
		frame.pack();
		frame.setSize(250, 250);
		frame.setLocation(1200, 0);
		frame.setVisible(true);
		animator.start();
	}
	
	public void createFractureMapFromPoints() {
		if (points.size() == 0) return;
		FractureMap fm = ChrisVoronoiCalculation.generateVoronoi(points);
		points.clear();
		fs.addFractureMap(fm);
		newMap = false;
	}
	
	public void setDrawing(boolean isDrawing) {
		newMap = isDrawing;
	}

	@Override
	public void display(GLAutoDrawable glDrawable) {
		GL2 gl2 = glDrawable.getGL().getGL2();
		// clear screen
		gl2.glClearColor(1,1,1,1);
		gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
		
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor4f(0,0,0,1);

		gl2.glVertex2d(0, 0);
		gl2.glVertex2d(0, Constants.HEIGHT);
		gl2.glVertex2d(Constants.WIDTH, Constants.HEIGHT);
		gl2.glVertex2d(Constants.WIDTH, 0);
		
		gl2.glEnd();

		gl2.glBegin(GL2.GL_POINTS);
		gl2.glColor4f(1,0,0,1);
		for(Vector2 v : points) {
			gl2.glVertex2d(v.x, v.y);
		}
		gl2.glEnd();
		
		if (!newMap) {
			fs.getCurrentMap().display(gl2);
		}
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
		// nothing doing
	}

	@Override
	public void init(GLAutoDrawable glDrawable) {
		GL2 gl2 = glDrawable.getGL().getGL2();

		gl2.glClearColor(1, 1, 1, 1);
		gl2.setSwapInterval(0);

		// Other stuff from before
		gl2.glLineWidth(3);
		gl2.glPointSize(12f);
		gl2.glEnable(GL2.GL_BLEND);
		gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl2.glEnable(GL2.GL_POINT_SMOOTH);
		gl2.glHint  (GL2.GL_POINT_SMOOTH_HINT,  GL2.GL_NICEST);
		gl2.glEnable(GL2.GL_LINE_SMOOTH);
		gl2.glHint  (GL2.GL_LINE_SMOOTH_HINT,   GL2.GL_NICEST);
		gl2.glEnable(GL2.GL_POLYGON_SMOOTH); 
		gl2.glHint  (GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
	}

	@Override
	public void reshape(GLAutoDrawable glDrawable, int x, int y, int width, int height) {
		System.out.println("width = " + width + ", height = " + height);
		GL2 gl = glDrawable.getGL().getGL2();
		gl.glViewport(0,0,width,height);

		// Set 2D view
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();

		gl.glOrtho(0, 1, 0, 1, 0, 1);

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

}
