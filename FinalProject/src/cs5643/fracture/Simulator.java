package cs5643.fracture;

import java.awt.GridLayout;

import javax.media.opengl.*;

import java.awt.event.*;
import java.util.ArrayList;

import javax.media.opengl.awt.GLCanvas;
import javax.swing.*;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import com.jogamp.opengl.util.*;


public class Simulator implements GLEventListener {

	private JFrame frame = null;

	private BuilderGUI gui;
	private FrameExporter frameExporter;
	private OrthoMap orthoMap;
	private boolean drawWireframe = false;
	private boolean simulate = false;
	
	private RigidBodySystem rbs;
	
	public static final double DT = 0.016;

	public Simulator() {
		rbs = new RigidBodySystem();
		
		Point2d p1 = new Point2d(0.25, 0.75);
		Point2d p2 = new Point2d(0.75, 0.75);
		Point2d p3 = new Point2d(0.75, 0.25);
		Point2d p4 = new Point2d(0.25, 0.25);
		
		ArrayList<Point2d> list = new ArrayList<Point2d>();
		list.add(p1);
		list.add(p2);
		list.add(p3);
		list.add(p4);
		Convex c = Utils.makeHullFromPoints(list);
		rbs.add(c);
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glClearColor(1,1,1,0);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT); //  | GL.GL_DEPTH_BUFFER_BIT);

		{/// DRAW COMPUTATIONAL CELL BOUNDARY:
			gl.glBegin(GL2.GL_LINE_STRIP);
			if (simulate) {
				gl.glColor4f(0,0,0,1);
			}
			else {
				gl.glColor4f(1,0,0,1);
			}
			gl.glVertex2d(0,0);	gl.glVertex2d(1,0);	gl.glVertex2d(1,1);	gl.glVertex2d(0,1);	gl.glVertex2d(0,0);
			gl.glEnd();
		}

		if (drawWireframe)  gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_LINE);
		else                gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);

		gui.simulateAndDisplayScene(gl);/// <<<-- MAIN CALL
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub

	}

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
		frame.setSize(600,600);
		frame.setLocation(200, 0);
		frame.setVisible(true);
		animator.start();
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		// DEBUG PIPELINE (can use to provide GL error feedback... disable for speed)
		//drawable.setGL(new DebugGL(drawable.getGL()));

		GL2 gl = drawable.getGL().getGL2();
		System.err.println("INIT GL IS: " + gl.getClass().getName());

		gl.setSwapInterval(1);

		gl.glLineWidth(1);
		gl.glPointSize(1f);

		//gl.glDisable(gl.GL_DEPTH_TEST);

		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		//gl.glBlendFunc(GL.GL_SRC_ALPHA_SATURATE, GL.GL_ONE_MINUS_SRC_ALPHA);
		//gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
		gl.glEnable(GL2.GL_POINT_SMOOTH);
		gl.glHint  (GL2.GL_POINT_SMOOTH_HINT,  GL2.GL_NICEST);
		gl.glEnable(GL2.GL_LINE_SMOOTH);
		gl.glHint  (GL2.GL_LINE_SMOOTH_HINT,   GL2.GL_NICEST);
		gl.glEnable(GL2.GL_POLYGON_SMOOTH); 
		gl.glHint  (GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

		System.out.println("width="+width+", height="+height);
		height = Math.max(height, 1); // avoid height=0;

		GL2 gl = drawable.getGL().getGL2();
		gl.glViewport(0,0,width,height);	

		/// SETUP ORTHOGRAPHIC PROJECTION AND MAPPING INTO UNIT CELL:
		gl.glMatrixMode(GL2.GL_PROJECTION);	
		gl.glLoadIdentity();			
		orthoMap = new OrthoMap(width, height);//Hide grungy details in OrthoMap
		orthoMap.apply_glOrtho(gl);

		/// GET READY TO DRAW:
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	public static void main(String[] args) 
	{
		try {
			Simulator sim = new Simulator();
			sim.start();

		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("OOPS: "+e);
		}
	}


	class BuilderGUI implements MouseListener, MouseMotionListener, KeyListener
	{
		boolean simulate = false;

		/** Current build task (or null) */
		Task task;

		JFrame guiFrame;
		TaskSelector taskSelector = new TaskSelector();

		BuilderGUI() 
		{
			guiFrame = new JFrame("Tasks");
			guiFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			guiFrame.setLayout(new SpringLayout());
			guiFrame.setLayout(new GridLayout(6,1));

			/* Add new task buttons here, then add their functionality below. */
			ButtonGroup      buttonGroup  = new ButtonGroup();
			AbstractButton[] buttons      = { new JButton("Reset"),
					new JToggleButton ("Drag object", false),
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

		/** Simulate then display particle system and any builder
		 * adornments. */
		void simulateAndDisplayScene(GL2 gl)
		{
			/// TODO: make the system advance
			if(simulate) {
				rbs.advanceTime(DT);
			}
			// Draw particles, forces, etc.
			rbs.display(gl);

			if(simulate && frameExporter != null) {
				frameExporter.writeFrame(gl);
			}

			// Display task if any
			if(task != null) task.display(gl);
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
				String cmd = e.getActionCommand();
				System.out.println(cmd);

				if(cmd.equals("Reset")) {
					if(task != null) {
						task.reset();
					}
					else {
						resetToRest(); // set task=null
					}
				}
				else if(cmd.equals("Drag object")) {
					task = new DragTask();
				}
				else {
					System.out.println("UNHANDLED ActionEvent: "+e);
				}
			}


		}

		// Methods required for the implementation of MouseListener
		public void mouseEntered (MouseEvent e) { if(task!=null) task.mouseEntered(e);  }
		public void mouseExited  (MouseEvent e) { if(task!=null) task.mouseExited(e);   }
		public void mousePressed (MouseEvent e) { if(task!=null) task.mousePressed(e);  }
		public void mouseReleased(MouseEvent e) { if(task!=null) task.mouseReleased(e); }
		public void mouseClicked (MouseEvent e) { if(task!=null) task.mouseClicked(e);  }

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
		
		class PickTask extends Task {

			@Override
			void reset() {
				taskSelector.resetToRest();
			}
			
			protected Convex picked = null;
			protected Point2d clickLoc = new Point2d();
			protected Point2d orig_loc = new Point2d();
			
			public void mousePressed(MouseEvent e) {
				orthoMap.getPoint2d(e, clickLoc);
				picked = rbs.pickBody(clickLoc);
				if(picked != null) {
					orig_loc.set(picked.x);
				}
			}
		}
		
		class DragTask extends PickTask {
			
			Point2d draggedLoc = new Point2d();
			Vector2d diff = new Vector2d();
			
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				if(picked != null) {
					picked.setPinned(true);
				}
			}
			
			public void mouseDragged(MouseEvent e) {
				if(picked != null) {
					orthoMap.getPoint2d(e, draggedLoc);
					diff.sub(draggedLoc, clickLoc);
					
					picked.x.add(orig_loc, diff);
					picked.x_star.add(orig_loc, diff);
				}
			}
			
			public void mouseReleased(MouseEvent e) {
				if(picked != null) {
					picked.setPinned(false);
					picked = null;
				}
			}
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

		FrameExporter()  { 
			exportId += 1;
		}

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
					rbu.write(file);
				}

				System.out.println((timeNS/1000000)+"ms:  Wrote frame: "+filename);

			}catch(Exception e) { 
				e.printStackTrace();
				System.out.println("OOPS: "+e); 
			} 

			nFrames += 1;
		}
	}

}
