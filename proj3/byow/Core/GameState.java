package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.Random;

public class GameState {
    private final TETile[][] world;
    private final TETile[][] lightedWorld;
    private int playerX;
    private int playerY;
    private final Random random;
    private String history;
    private boolean limitedVision;
    private static final int SIGHT_RADIUS = 6;
    private static final int MIN_LIGHTS_NUM = 6;
    private static final int MAX_LIGHTS_NUM = 10;
    private static final int LIGHT_RADIUS = 5;
    private final int LIGHTS_NUM;
    private final ArrayList<Position> lights;
    public int status;
    private final String[] TITLE = {"Success!", "Saved", "Replay Ended"};

    private class Position {
        int x, y;
        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public GameState(long seed) {
        status = 3;
        random = new Random(seed + 1);
        LIGHTS_NUM = RandomUtils.uniform(random, MIN_LIGHTS_NUM, MAX_LIGHTS_NUM + 1);
        world = WorldGenerator.generateWorld(seed);
        lights = generateLights(LIGHTS_NUM);
        history = "n" + seed + "s";
        placeAvatar();
        limitedVision = true;
        lightedWorld = TETile.copyOf(world);
        refreshLightedWorld();
    }

    public String getTitle() {
        if (status == 3) {
             return "Eat all the lights: " + (LIGHTS_NUM - lights.size()) + "/" + LIGHTS_NUM;
        } else {
            return TITLE[status];
        }
    }

    public TETile[][] getWorld(boolean real) {
        if (!limitedVision || real) {
            return lightedWorld;
        }

        TETile[][] limited_world = new TETile[Engine.WIDTH][Engine.HEIGHT];
        for (int x = 0; x < Engine.WIDTH; x++) {
            for (int y = 0; y < Engine.HEIGHT; y++) {
                if (inSight(x, y)) {
                    limited_world[x][y] = lightedWorld[x][y];
                } else {
                    limited_world[x][y] = Tileset.NOTHING;
                }
            }
        }
        return limited_world;
    }

    private boolean inSight(int x, int y) {
        int dx = Math.abs(x - playerX);
        int dy = Math.abs(y - playerY);
        return dx + dy <= SIGHT_RADIUS;
    }

    public String getHistory() {
        return history;
    }

    public void move(char c) {
        int newX = playerX;
        int newY = playerY;
        switch (c) {
            case 'w':
                newY++;
                break;
            case 'd':
                newX++;
                break;
            case 's':
                newY--;
                break;
            case 'a':
                newX--;
                break;
        }
        if (Engine.isInMap(newX, newY) && (isFloor(newX, newY) || isLight(newX, newY))) {
            if (isLight(newX, newY)) {
                removeLightAt(newX, newY);
            }
            world[playerX][playerY] = Tileset.FLOOR;
            playerX = newX;
            playerY = newY;
            world[playerX][playerY] = Tileset.AVATAR;
            refreshLightedWorld();
            history += c;
        }
    }

    private void removeLightAt(int x, int y) {
        for (int i = 0; i < lights.size(); i++) {
            Position light = lights.get(i);
            if (light.x == x && light.y == y) {
                lights.remove(i);
                break;
            }
        }
        if (lights.isEmpty()) {
            status = 0;
        }
    }

    /** randomly place the avatar */
    private void placeAvatar() {
        boolean success = false;
        while (!success) {
            int x = RandomUtils.uniform(random, Engine.WIDTH);
            int y = RandomUtils.uniform(random, Engine.HEIGHT);
            if (Engine.isInMap(x, y) && isFloor(x, y) && !isLight(x, y)) {
                success = true;
                playerX = x;
                playerY = y;
                world[x][y] = Tileset.AVATAR;
            }
        }
    }

    public void toggleVision() {
        limitedVision = !limitedVision;
    }

    public boolean isFloor(int x, int y) {
        return world[x][y] == Tileset.FLOOR;
    }

    private boolean isLight(int x, int y) {
        return world[x][y] == Tileset.LIGHT_SOURCE;
    }

    private ArrayList<Position> generateLights(int num) {
        ArrayList<Position> lights = new ArrayList<>();
        while (lights.size() < num) {
            int x = RandomUtils.uniform(random, 0, Engine.WIDTH);
            int y = RandomUtils.uniform(random, 0, Engine.HEIGHT);
            if (Engine.isInMap(x, y) && isFloor(x, y)) {
                world[x][y] = Tileset.LIGHT_SOURCE;
                lights.add(new Position(x, y));
            }
        }
        return lights;
    }

    private void refreshLightedWorld() {
        for (int x = 0; x < Engine.WIDTH; x++) {
            for (int y = 0; y < Engine.HEIGHT; y++) {
                lightedWorld[x][y] = renderTileAt(x, y);
            }
        }
    }

    private TETile renderTileAt(int x, int y) {
        TETile tile = world[x][y];

        if (tile == Tileset.FLOOR) {
            int level = lightLevelAt(x, y);
            return floorTileForLightLevel(level);
        } else {
            return tile;
        }
    }

    private int lightLevelAt(int x, int y) {
        int bestLevel = 0;
        for (Position light : lights) {
            int distance = Math.max(Math.abs(x - light.x), Math.abs(y - light.y));

            if (distance > LIGHT_RADIUS) {
                continue;
            }
            if (!hasLineOfSight(light.x, light.y, x, y)) {
                continue;
            }

            int level = lightLevelForDistance(distance);
            bestLevel = Math.max(bestLevel, level);
        }
        return bestLevel;
    }

    private static int lightLevelForDistance(int dis) {
        return 5 - dis;
    }

    private static TETile floorTileForLightLevel(int level) {
        return switch (level) {
            case 0 -> Tileset.BLUE_LIGHT_0;
            case 1 -> Tileset.BLUE_LIGHT_1;
            case 2 -> Tileset.BLUE_LIGHT_2;
            case 3 -> Tileset.BLUE_LIGHT_3;
            case 4 -> Tileset.BLUE_LIGHT_4;
            default -> Tileset.FLOOR;
        };
    }

    private boolean hasLineOfSight(int x0, int  y0, int  x1, int  y1) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        int x = x0;
        int y = y0;

        while (!(x == x1 && y == y1)) {
            if (!(x == x0 && y == y0) && world[x][y] == Tileset.WALL) {
                return false;
            }

            int e2 = 2 * err;

            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }

            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }

        return true;
    }
}
