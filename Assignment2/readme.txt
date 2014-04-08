====================
CS 5643 Assignment 2
====================
Chris Yu (cy284) and Ari Karo (aak82)

Videos (in videos/ directory):

  - different_stiffness.mp4: demo of the hanging mesh with k_bend and k_stretch
							 values of .2, .5, and .8.
  - collisions.mp4: demo of two spheres bumping into one another with collisions.s
  - dragging_sphere.mp4: demo of particle dragging on a inflated sphere.
  - self_collisions.mp4: demo of self collisions with piece of cloth.
  - stiff_sliders.mp4: demo of the k-value sliders used while simulation.
					   First k_stretch is altered, followed by k_bend.
  
Summary:

We implemented the position-based dynamic simulator, using the formulas given in
class and in the paper "Position Based Dynamics" by Müller, Heidelberger, et al. The core
simulator follows the timestepping scheme given in the paper.


As extensions, we implemented both self-collisions as described in section 4.3 in the paper.
This was further expanded to allow collisions between objects.
Additionally, we added simple interactive k-value sliders that allow the user to alter the value
of k-bend and k-stretch during the simulation.

Findings:

Collaborators:
    
http://www.feenixcollection.com/images/feenixautore4.pngWe did not significantly discuss the assignment with anyone outside our group.















