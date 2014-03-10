====================
CS 5643 Assignment 1
====================
Chris Yu (cy284) and Ari Karo (aak82)

Videos (in videos/ directory):

  - CANNONS.mp4: demo of shooting spheres
  - floor_roof.mp4: demo of slanting floor and removeable roof
  - gravity.mp4: demo of modifying gravity
  - make_it_rain.mp4: demo of "rain", and removeable wall
  - no_scorr_vs_scorr.mp4: comparison of simulation with no surface tension
        (on left) vs with surface tension (on right)

Summary:

We implemented the position-based fluid simulator, using the formulas given in
class and in the paper "Position-Based Fluids" by Macklin and Müller. The core
simulator follows the timestepping scheme given in the paper.

In order to locate the neighbors of a particle, we used a sparse hashmap that
partitions the entire space into cubes of side length h (the kernel radius).
Then, we can find any particle's neighbors by looking in that particle's bucket
as well as all adjacent buckets. We used a hashmap so that particles would not
necessarily be constrained to the box.

As extensions, we implemented a number of interactive elements:

  - The ability to change the location the camera is targeting using WASD (in
    addition to still being able to rotate with arrow keys).
  - The ability to add or remove the roof of the cube. This is because in some
    circumstances, particles may splash very high, and this allows the user to
    see the full extent of the splash.
  - The ability to add or remove one of the walls of the cube, allowing the water
    to flow outward.
  - The ability of toggling the floor between a level plane and a slanted plane.
    This enables us to see the behavior of water flowing down a surface.
  - A toggle that causes particles to be generated and fall into the cube from
    above, simulating rain.
  - The ability to shoot spheres that travel through the cube and collide with
    the water, causing disturbances. The spheres are fired in the direction that
    the camera is facing.
  - The ability to change the direction of gravity by clicking and dragging on
    the screen. The new gravity points in the direction the mouse was dragged,
    relative to where the camera is looking.

* In our opinion, the spheres are the most entertaining element.

Findings:

Our final constants were the following:

    Rest density = 2300
    Kernel radius (h) = 0.1
    Viscosity (c) = 1e-6
    Surface tension:
        k = 5
        delta Q = 0.2h
        n = 4
    Vorticity (epsilon) = 0.1

We also had to scale the magnitude of the spiky kernel's gradient by 0.001,
as we found that without this damping, the spiky kernel would produce massive
density corrections that caused the system to explode.

The surface tension is high, but with our parameters, we found that lower
values did not have any appreciable effect. You can observe the difference
in one of our videos (no_scorr_vs_scorr.mp4).

With these parameters, we have a fairly low-resolution simulation -- a lesser
number of particles fills a greater space. This makes it well-suited for the
interactivity that we detailed above. The particles respond in a fluid-like
way; incompressibility is maintained, and the particles exhibit 

Collaborators:
    
We did not significantly discuss the assignment with anyone outside our group.















