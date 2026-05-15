# BYOW Project

This project is an implementation of the CS61B BYOW game. It generates a
deterministic tile world from a seed, supports keyboard play, save/load, replay,
HUD rendering, limited vision, and collectible light sources.

## How to Run

Run the main entry point:

```text
byow.Core.Main
```

The keyboard menu supports:

```text
N - start a new game
L - load a saved game
R - replay the saved game
Q - quit
```

When starting a new game, type a numeric seed and press `S` to begin. Seeds are
limited to 18 digits so they can be safely parsed as a Java `long`.

## Controls

During gameplay:

```text
W/A/S/D - move the avatar
G       - toggle limited vision
:Q      - save and quit to the menu
M       - return to the main menu without saving
```

The goal is to collect every light source in the world. Walking onto a light
source removes it from the map. The HUD shows progress in the form:

```text
Eat all the lights: collected/total
```

## Input String Support

The autograder entry point is:

```java
public TETile[][] interactWithInputString(String input)
```

Supported examples:

```text
n123s
n123swwaadd
n123sww:q
l
lww
```

Input is case-insensitive. A string beginning with `l` loads the previously saved
input history from `save.txt`, appends the remaining input, and replays the result.
The save file stores input history rather than a serialized `TETile[][]`, so the
same seed and commands deterministically reconstruct the same game state.

## World Generation

World generation lives in:

```text
byow/Core/WorldGenerator.java
```

The generation pipeline is:

```text
1. Create a Random object from the seed.
2. Fill the world with NOTHING.
3. Generate random rectangular rooms.
4. Reject rooms that overlap or leave the usable map area.
5. Connect rooms using Kruskal's minimum spanning tree algorithm.
6. Add a few extra hallway connections for loops.
7. Add walls around floor tiles.
```

Rooms are connected with L-shaped hallways. Hallway width is randomly chosen as
1 or 2 tiles. The MST guarantees that all rooms are connected, while extra edges
make the world less tree-like.

## Game State

Runtime state lives in:

```text
byow/Core/GameState.java
```

`GameState` owns:

```text
world        - the real logical map
lightedWorld - the rendered map after lighting is applied
playerX/Y    - avatar position
history      - replayable input history
lights       - remaining collectible light sources
```

Movement updates the real world, refreshes the lighted world, and appends valid
movement commands to history. Light sources are collectible: if the avatar moves
onto a light source, that source is removed and the lighting is recomputed.

## Lighting

The visual style uses dark blue floors, gray walls, and blue light gradients.
Each light source affects nearby floor rendering based on:

```text
distance from the light source
line of sight between the light and the tile
```

Distance uses square/Chebyshev distance:

```java
max(abs(x - lightX), abs(y - lightY))
```

Line of sight uses a Bresenham-style grid line check. If a wall lies between a
light source and a tile, that tile does not receive light from that source.

## Limited Vision

Limited vision is enabled by default and can be toggled with `G`. It uses a
diamond-shaped awareness radius around the avatar:

```java
abs(x - playerX) + abs(y - playerY) <= SIGHT_RADIUS
```

Tiles outside the radius render as `NOTHING`. The full lighted map is still kept
internally, so toggling vision only changes what is displayed.

## HUD

The renderer reserves extra vertical space above the map for a HUD. The HUD
shows:

```text
left   - tile description under the mouse
center - game status or light collection progress
right  - basic controls
```

To avoid flicker, gameplay rendering draws the tile map and HUD into the same
StdDraw frame before calling `StdDraw.show()`.

## Save and Load

Saving writes the replayable input history to:

```text
save.txt
```

The generated `save.txt` file is a runtime artifact and should not be committed.
It can be deleted safely; the next save operation will recreate it.

## Project Structure

```text
byow/Core/Engine.java         - menu, input parsing, rendering loop, save/load
byow/Core/GameState.java      - player state, movement, vision, lighting, lights
byow/Core/WorldGenerator.java - deterministic world generation
byow/Core/RandomUtils.java    - random helper functions
byow/TileEngine/Tileset.java  - tile definitions and visual theme
```

## Notes

The project is designed to remain deterministic. New random behavior should come
from a seed-derived `Random` object, not from system time or unseeded randomness.
