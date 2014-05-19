=====================
CS 5643 Final Project
cy284 & aak82
=====================

This is a 2D dynamic fracture simulator; for full details see the report.
Spacebar pauses/unpauses the simulation.

Quick outline of all the buttons on the menu:

 - Clear: removes all objects.
 - Drag: click and drag an object to move it around.
 - Create (Convex): Left-click to place control points; right-click to make the convex hull.
 - Create (Welded): Left-click to place control points (in clockwise or counterclockwise order);
     right-click to make polygon.
 - Create {Map}: Left-click on small window to place control points; right-click to create
     Voronoi diagram of points.
 - Next/Previous map: Cycles through all loaded maps.
 - Load/Save map: Store and retrieve maps to/from disk.
 - Delete: Click a polygon to delete it.
 - Fracture: Click on an object to cause a fracture centered at the clicked point.
 - Shoot: Right-click to set origin of projectiles; left-click to fire.
 - Clean up: Remove all objects of sufficiently small mass (small particles that slow down the simulation).
 - Impact radius slider: Drag to change impact radius.

Submission videos:

 - convex-creation.avi: Creating convex polygons.
 - manual-fracture.avi: Fracturing using the manual fracture tool.
 - shooting-fracture.avi: Fracturing using projectiles.
 - welded-creation.avi: Creating welded polygons.

Any instability or jitter is probably because of dyn4j, wink wink.

Have fun!
