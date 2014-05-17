package cs5643.fracture;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JToggleButton;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;

import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.geometry.decompose.Bayazit;

import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.GLReadBufferUtil;

import cs5643.fracture.FractureMapFactory.BadMapException;

public class SimulatorMain implements GLEventListener {

	private long nextUpdateTime = 0;

	private OrthoMap orthoMap;

	private JFrame frame;
	private FrameExporter frameExporter;
	private BuilderGUI gui;
	private boolean simulate;

	private FractureSystem fractureSystem = new FractureSystem();
	private FractureMapFrame fracFrame;

	private boolean shooting = false;
	private Vector2 shootingPoint = new Vector2(0, Constants.HEIGHT);

	public void start()
	{
		if(frame != null) return;

		gui = new BuilderGUI();

		frame = new JFrame("CS5643 Fracture Simulator");
		GLProfile glp = GLProfile.getDefault();
		GLCapabilities glc = new GLCapabilities(glp);
		GLCanvas canvas = new GLCanvas(glc);
		canvas.addGLEventListener(this);
		frame.add(canvas);

		canvas.addMouseListener(gui);
		canvas.addMouseMotionListener(gui);
		canvas.addKeyListener(gui);

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
		frame.setSize(816,639);
		frame.setLocation(200, 0);
		frame.setVisible(true);
		animator.start();

		fracFrame = new FractureMapFrame(fractureSystem);
	}

	private ArrayList<Vector2> points = new ArrayList<Vector2>();

	public void createConvexFromPoints() {
		if(points.size() < 3) {
			points.clear();
			return;
		}
		try {
			ConvexPolygon p = new ConvexPolygon(points);
			fractureSystem.add(p);
			points.clear();
		}
		catch(IllegalArgumentException e) {
			System.err.println("Invalid polygon.");
			points.clear();
		}
	}
	private ArrayList<Vector2> temp = new ArrayList<Vector2>();

	public void createTriangulationFromPoints() {
		if(points.size() < 3) {
			points.clear();
			return;
		}
		List<Convex> convexes;
		try {
			convexes = new Bayazit().decompose(points.toArray(new Vector2[0]));
		}
		catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			points.clear();
			return;
		}
		WeldedPolygon wp = new WeldedPolygon();
		for(Convex c : convexes) {
			temp.clear();
			Polygon p = (Polygon)c;
			for(Vector2 v : p.getVertices()) {
				temp.add(v);
			}
			Polygon poly = new Polygon(temp.toArray(new Vector2[0]));
			wp.addPiece(poly);
		}
		wp.setMass();
		fractureSystem.add(wp);
		points.clear();
	}

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

		fractureSystem.display(gl2);

		gl2.glBegin(GL2.GL_POINTS);
		gl2.glColor4f(0,1,0,1);
		for(Vector2 v : points) {
			gl2.glVertex2d(v.x, v.y);
		}
		gl2.glEnd();

		if(shooting) {
			gl2.glColor4f(0,0,1,1);
			gl2.glBegin(GL2.GL_LINES);
			gl2.glVertex2d(shootingPoint.x - 0.2, shootingPoint.y - 0.2);
			gl2.glVertex2d(shootingPoint.x + 0.2, shootingPoint.y + 0.2);
			gl2.glEnd();
			gl2.glBegin(GL2.GL_LINES);
			gl2.glVertex2d(shootingPoint.x - 0.2, shootingPoint.y + 0.2);
			gl2.glVertex2d(shootingPoint.x + 0.2, shootingPoint.y - 0.2);
			gl2.glEnd();
		}

		long time = System.currentTimeMillis();

		if(simulate && time >= nextUpdateTime) {
			if(frameExporter != null) {
				frameExporter.writeFrame(gl2);
			}
			this.update();
			nextUpdateTime = time + Constants.DT_MILLIS;
		}
	}

	public void update() {
		fractureSystem.update(Constants.DT);
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
		// nothing doing
	}

	@Override
	public void reshape(GLAutoDrawable glDrawable, int x, int y, int width, int height) {
		System.out.println("width = " + width + ", height = " + height);
		GL2 gl = glDrawable.getGL().getGL2();
		gl.glViewport(0,0,width,height);

		// Set 2D view
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();

		orthoMap = new OrthoMap(width, height);
		orthoMap.apply_glOrtho(gl);

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	public SimulatorMain() {
		fractureSystem = new FractureSystem();
	}

	public static void main(String[] args) {
		try {
			SimulatorMain sim = new SimulatorMain();
			sim.start();

		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("OOPS: "+e);
		}
	}

	class BuilderGUI implements MouseListener, MouseMotionListener, KeyListener
	{

		/** Current build task (or null) */
		Task task;

		JFrame guiFrame;
		TaskSelector taskSelector = new TaskSelector();

		public BuilderGUI() 
		{
			guiFrame = new JFrame("Tasks");
			guiFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			guiFrame.setLayout(new SpringLayout());
			guiFrame.setLayout(new GridLayout(13,1));

			/* Add new task buttons here, then add their functionality below. */
			ButtonGroup      buttonGroup  = new ButtonGroup();
			AbstractButton[] buttons      = { new JButton("Clear"),
					new JToggleButton ("Drag", false),
					new JToggleButton ("Create (Convex)", false),
					new JToggleButton ("Create (Welded)", false),
					new JToggleButton ("Create (Map)", false),
					new JButton("Next Map"),
					new JButton("Previous Map"),
					new JButton("Load Map"),
					new JButton("Save Map"),
					new JToggleButton ("Delete", false),
					new JToggleButton ("Fracture", false),
					new JToggleButton ("Shoot", false),
					new JButton ("Clean Up"),
			};

			for(int i=0; i<buttons.length; i++) {
				buttonGroup.add(buttons[i]);
				guiFrame.add(buttons[i]);
				buttons[i].addActionListener(taskSelector);
			}

			guiFrame.setSize(200,200);
			guiFrame.pack();
			guiFrame.setVisible(true);

			task = null; // Set default task here
		}

		void writeFrame(GL2 gl)
		{
			if(simulate && frameExporter != null) {
				frameExporter.writeFrame(gl);
			}
		}

		/**
		 * ActionListener implementation to manage Task selection
		 * using (radio) buttons.
		 */
		class TaskSelector implements ActionListener
		{
			/** 
			 * Resets ParticleSystem to undeformed/material state,
			 * disables the simulation, and removes the active Task.
			 */
			void resetToRest() {
				// TODO reset system
				simulate = false;
				task = null;
			}

			/** Creates new Task objects to handle specified button action.
			 *  Switch to a new task, or perform custom button actions here.
			 */
			public void actionPerformed(ActionEvent e)
			{
				fracFrame.setDrawing(false);
				String cmd = e.getActionCommand();
				System.out.println(cmd);

				if(cmd.equals("Clear")) {
					if(task != null) {
						task.reset();
						task = null;
					}
					fractureSystem.clear();
				}
				else {
					shooting = false;
					if(cmd.equals("Drag")) {
						task = new DragTask();
					}
					else if(cmd.equals("Create (Convex)")) {
						task = new CreateConvexTask();
					}
					else if(cmd.equals("Create (Welded)")) {
						task = new CreateWeldedTask();
					}
					else if(cmd.equals("Create (Map)")) {
						fracFrame.setDrawing(true);
						task = null;
					}
					else if(cmd.equals("Next Map")) {
						fractureSystem.nextMap();
					}
					else if(cmd.equals("Previous Map")) {
						fractureSystem.previousMap();
					}
					else if(cmd.equals("Load Map")) {
						loadMapFromFile();
					}
					else if(cmd.equals("Save Map")) {
						saveMapToFile();
					}
					else if(cmd.equals("Delete")) {
						task = new DeleteTask();
					}
					else if(cmd.equals("Fracture")) {
						task = new FractureTask();
					}
					else if(cmd.equals("Shoot")) {
						shooting = true;
						task = new ShootTask();
					}
					else if(cmd.equals("Clean Up")) {
						fractureSystem.cleanUpSmall(Constants.CLEAN_UP_LIMIT);
					}
					else {
						System.out.println("UNHANDLED ActionEvent: "+e);
					}
				}
			}
		}

		private Vector2 temp = new Vector2();

		// Methods required for the implementation of MouseListener
		public void mouseEntered (MouseEvent e) { if(task!=null) task.mouseEntered(e);  }
		public void mouseExited  (MouseEvent e) { if(task!=null) task.mouseExited(e);   }
		public void mousePressed (MouseEvent e) { if(task!=null) task.mousePressed(e);  }
		public void mouseReleased(MouseEvent e) { if(task!=null) task.mouseReleased(e); }
		public void mouseClicked (MouseEvent e) {
			if(task != null){
				task.mouseClicked(e);
			}
		}

		// Methods required for the implementation of MouseMotionListener
		public void mouseDragged (MouseEvent e) { if(task!=null) task.mouseDragged(e);  }
		public void mouseMoved   (MouseEvent e) { if(task!=null) task.mouseMoved(e);    }

		// Methods required for the implementation of KeyListener
		public void keyTyped(KeyEvent e) { } // NOP
		public void keyPressed(KeyEvent e) { dispatchKey(e); }
		public void keyReleased(KeyEvent e) { } // NOP

		/**
		 * Handles keyboard events, e.g., spacebar toggles
		 * simulation/pausing, and escape resets the current Task.
		 */
		public void dispatchKey(KeyEvent e)
		{
			switch(e.getKeyCode()) {
			case KeyEvent.VK_SPACE:
				simulate = !simulate;
				if(simulate) {
					System.out.println("Starting simulation...");
				}
				else {
					System.out.println("Simulation paused.");
				}
				break;
			case KeyEvent.VK_E:
				if(frameExporter != null) {
					frameExporter.flushFrames();
				}
				frameExporter = ((frameExporter==null) ? (new FrameExporter(true)) : null);
				System.out.println("'e' : frameExporter = "+frameExporter);
				break;

			default:
			}
		}

		/** 
		 * "Task" command base-class extended to support
		 * building/interaction via mouse interface.  All objects
		 * extending Task are implemented here as inner classes for
		 * simplicity.
		 *
		 * Add tasks as necessary for different interaction modes.
		 */
		abstract class Task implements MouseListener, MouseMotionListener
		{
			/** Displays any task-specific OpengGL information,
			 * e.g., highlights, etc. */
			public void display(GL2 gl) {}

			// Methods required for the implementation of MouseListener
			public void mouseEntered (MouseEvent e) {}
			public void mouseExited  (MouseEvent e) {}
			public void mousePressed (MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mouseClicked (MouseEvent e) {}

			// Methods required for the implementation of MouseMotionListener
			public void mouseDragged (MouseEvent e) {}
			public void mouseMoved   (MouseEvent e) {}

			/** Override to specify reset behavior during "escape" button
			 * events, etc. */
			abstract void reset();
		}

		class ShootTask extends Task {

			public void reset() {
				taskSelector.resetToRest();
			}

			public void mousePressed(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON3) {
					orthoMap.getPoint(e, shootingPoint);
				}
				else if(e.getButton() == MouseEvent.BUTTON1) {
					orthoMap.getPoint(e, temp);
					fractureSystem.shootBullet(shootingPoint, temp);
				}
			}
		}

		class CreateConvexTask extends Task {

			public void reset() {
				taskSelector.resetToRest();
			}

			public void mousePressed(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1) {
					Vector2 point = new Vector2();
					orthoMap.getPoint(e, point);
					points.add(point);
				}
				else if(e.getButton() == MouseEvent.BUTTON3) {
					createConvexFromPoints();
				}
			}
		}

		class CreateWeldedTask extends Task {

			public void reset() {
				taskSelector.resetToRest();
			}

			public void mousePressed(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1) {
					Vector2 point = new Vector2();
					orthoMap.getPoint(e, point);
					points.add(point);
				}
				else if(e.getButton() == MouseEvent.BUTTON3) {
					createTriangulationFromPoints();
				}
			}

		}

		abstract class PickTask extends Task {
			protected Fracturable picked = null;
			protected Vector2 clicked = new Vector2();
			protected Vector2 origLoc = new Vector2();
			public void mousePressed(MouseEvent e) {
				orthoMap.getPoint(e, clicked);
				picked = fractureSystem.pickBody(clicked);
				if(picked != null) {
					origLoc.set(picked.getWorldCenter());
				}
			}

			public void reset() {
				taskSelector.resetToRest();
			}
		}

		class FractureTask extends PickTask {
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				if(picked != null) {
					fractureSystem.fracture(picked, clicked);
					picked = null;
				}
			}
		}

		class DragTask extends PickTask {
			protected Vector2 dragged = new Vector2();
			protected Vector2 diff = new Vector2();

			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				if(picked != null) {
					picked.pin(clicked);
				}
			}

			public void mouseDragged(MouseEvent e) {
				if(picked != null) {
					orthoMap.getPoint(e, dragged);
					diff.set(dragged).subtract(clicked);
					dragged.set(origLoc).add(diff);
					picked.setJointTarget(dragged);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if(picked != null) {
					picked.unpin();
					picked = null;
				}
			}
		}

		class DeleteTask extends PickTask {
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				if(picked != null) {
					fractureSystem.remove(picked);
					picked = null;
				}
			}
		}
	}

	private void loadMapFromFile() {
		JFileChooser fc = new JFileChooser("./maps");
		int choice = fc.showOpenDialog(frame);
		if (choice != JFileChooser.APPROVE_OPTION) return;
		String fileName = fc.getSelectedFile().getAbsolutePath();

		java.io.File file = new java.io.File(fileName);
		if (!file.exists()) {
			System.err.println("Error: Tried to load a map from a non-existant file.");
			return;
		}

		try {
			FractureMap fm = FractureMapFactory.buildMap(new File(fileName));
			fractureSystem.addFractureMap(fm);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (BadMapException e) {
			e.printStackTrace();
		}
	}

	private void saveMapToFile() {
		JFileChooser fc = new JFileChooser("./maps");
		int choice = fc.showOpenDialog(frame);
		if (choice != JFileChooser.APPROVE_OPTION) return;
		String fileName = fc.getSelectedFile().getAbsolutePath();

		java.io.File file = new java.io.File(fileName);
		try {
			FractureMapFactory.saveMap(file, fractureSystem.getCurrentMap());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/// Used by the FrameExporter class
	private static int exportId = -1;

	/**
	 * A class that either writes the current position of all particles to a text file,
	 * or outputs a png of the current window. Toggle the image boolean to switch modes.
	 *
	 * Text file specification:
	 * The file's first line is an integer N denoting the number of particles in the system.
	 * N lines follow, each with 3 floating point numbers describing the points'
	 * x, y, and z coordinates.
	 *
	 * WARNING: the directory "./frames/" must exist for this class to work properly.
	 */
	private class FrameExporter
	{
		public boolean image = false;
		private int nFrames  = 0;
		ArrayList<GLReadBufferUtil> buffers = new ArrayList<GLReadBufferUtil>();

		FrameExporter(boolean image) {
			this.image = image;
			exportId += 1;
		}

		void writeFrame(GL2 gl)
		{ 
			long   timeNS   = -System.nanoTime();
			String number   = Utils.getPaddedNumber(nFrames, 5, "0");
			String filename = "frames/export"+exportId+"-"+number+
					(image ? ".png" : ".txt");/// Bug: DIRECTORY MUST EXIST!

			try{ 
				java.io.File   file     = new java.io.File(filename);
				if(file.exists()) System.out.println("WARNING: OVERWRITING PREVIOUS FILE: "+filename);

				if (image) {
					GLReadBufferUtil rbu = new GLReadBufferUtil(false, false);
					rbu.readPixels(gl, false);
					buffers.add(rbu);
					//					rbu.write(file);
				}

				System.out.println((timeNS/1000000)+"ms:  Wrote frame: "+filename);

			}catch(Exception e) { 
				e.printStackTrace();
				System.out.println("OOPS: "+e); 
			} 

			nFrames += 1;
		}

		void flushFrames() {
			Thread t = new Thread(new Runnable() {
				public void run() {
					for(int i = 0; i < buffers.size(); i++) {
						GLReadBufferUtil rbu = buffers.get(i);
						String number   = Utils.getPaddedNumber(i, 5, "0");
						String filename = "frames/export"+exportId+"-"+number+
								(image ? ".png" : ".txt");/// Bug: DIRECTORY MUST EXIST!
						java.io.File file = new java.io.File(filename);
						System.out.println("Wrote frame " + filename);
						rbu.write(file);
					}
					buffers.clear();
				}
			});
			t.start();
		}
	}

}


