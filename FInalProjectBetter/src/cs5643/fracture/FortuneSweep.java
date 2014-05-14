package cs5643.fracture;
import java.util.List;
import java.util.PriorityQueue;

import org.dyn4j.geometry.Vector2;

public class FortuneSweep {
	private static PriorityQueue<Event> queue;
	private static Node tree;
	
	public static Object generateVoronoiDiagram(List<Vector2> controlpts) {
		queue = new PriorityQueue<Event>();
		tree = null;
		// TODO initialize tree? initialize list?
		for (Vector2 pt : controlpts) {
			queue.add(new SiteEvent(pt));
		}
		
		while (!queue.isEmpty()) {
			Event e = queue.poll();
			if (e.isSiteEvent())
				processSite((SiteEvent)e);
			else
				processCircle((CircleEvent)e);
		}
		//TODO Finish all edges in binary search tree
		return null;
	}
	
	private static void processSite(SiteEvent event) {
		if (tree == null)
			tree = new Arc(event.pt);
		else {
			//Find correct position in binaryTree for the event
			Node parent = null;
			Node target = tree;
			while (!target.isLeaf()) {
				parent = target;
				if (event.pt.x >= target.hashCode()) //not hashcode!)
					target = target.rightChild();
				else
					target = target.leftChild();
			}
			Arc leaf = (Arc) target;
			if (leaf.circleEvent != null) {
				queue.remove(leaf.circleEvent);
				leaf.circleEvent = null;
			}
			//TODO delete circle events involving this arc from the queue
			Arc newArc = new Arc(event.pt);
			target = new BreakPoint(leaf, new BreakPoint(newArc, leaf), leaf, newArc);
			//TODO create new edge and point breakpoints to these
			
		}
	}
	
	private static void processCircle(CircleEvent event) {
		
	}
	
	private interface Node {
		boolean isLeaf();
		Node rightChild();
		Node leftChild();
		void setParent(Node n);
	}
	
	private static class BreakPoint implements Node {
		Node parent;
		Node right;
		Node left;
		Arc first;
		Arc second;
		
		public BreakPoint(Node left, Node right, Arc first, Arc second) {
			this.left = left;
			this.right = right;
			this.first = first;
			this.second = second;
		}
		
		public BreakPoint(Arc left, Arc right) {
			this.left = left;
			this.right = right;
			first = left;
			second = right;
		}
		
		@Override
		public boolean isLeaf() {
			return false;
		}
		@Override
		public Node rightChild() {
			return right;
		}
		@Override
		public Node leftChild() {
			return left;
		}
		@Override
		public void setParent(Node n) {
			parent = n;
		}
	}
	
	private static class Arc implements Node {
		Vector2 pt;
		CircleEvent circleEvent;
		Node parent;
		
		private Arc(Vector2 pt) {
			this.pt = pt;
			circleEvent = null;
		}
		@Override
		public boolean isLeaf() {
			return true;
		}
		@Override
		public Node rightChild() {
			return null;
		}
		@Override
		public Node leftChild() {
			return null;
		}
		@Override
		public void setParent(Node n) {
			parent = n;
		}
		
	}
	
	private static abstract class Event implements Comparable<Event>{
		Vector2 pt;
		abstract boolean isSiteEvent();
		abstract double getY();
		public int compareTo(Event event) {
			if (getY() > event.getY())
				return 1;
			else if (getY() == event.getY())
				return (pt.x > event.pt.x) ? 1 : (pt.x == event.pt.x) ? 0 : -1;  
			else return -1;
		}
	}
	
	private static class SiteEvent extends Event {
		private SiteEvent (Vector2 pt) {
			this.pt = pt;
		}
		
		boolean isSiteEvent() {
			return true;
		}
		
		double getY() {
			return pt.y;
		}
	}
	
	private static class CircleEvent extends Event {
		//TODO pointer to arc in binary tree that disappears
		boolean isSiteEvent() {
			return false;
		}
		
		double getY() {
			return -1;
		}
	}
}

