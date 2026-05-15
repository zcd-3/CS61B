package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class WorldGenerator {
    /** generate the map from a seed */
    public static TETile[][] generateWorld(long seed) {
        final Random random = new Random(seed);
        TETile[][] world = new TETile[Engine.WIDTH][Engine.HEIGHT];
        fillWithNothing(world);

        ArrayList<Room> rooms = generateRooms(world, random);
        connectRoomsV2(world, random, rooms);
        addWalls(world);
        return world;
    }

    /** fill the tiles with nothing */
    private static void fillWithNothing(TETile[][] tiles) {
        for (TETile[] tile : tiles) {
            Arrays.fill(tile, Tileset.NOTHING);
        }
    }

    /** generate rooms */
    private static ArrayList<Room> generateRooms(TETile[][] world, Random random) {
        int maxRooms = RandomUtils.uniform(random, 20, 35);
        int attempts = RandomUtils.uniform(random, 80, 160);
        int minRoomWidth = 5;
        int maxRoomWidth = 14;
        int minRoomHeight = 4;
        int maxRoomHeight = 10;

        ArrayList<Room> rooms = new ArrayList<>();
        for (int i = 0; i < attempts; i++) {
            if (rooms.size() >= maxRooms) {
                break;
            }
            int x = RandomUtils.uniform(random, 0, Engine.WIDTH - minRoomWidth - 1);
            int y = RandomUtils.uniform(random, 0, Engine.HEIGHT - minRoomHeight - 1);
            int rWidth = RandomUtils.uniform(random, minRoomWidth, maxRoomWidth);
            int rHeight = RandomUtils.uniform(random, minRoomHeight, maxRoomHeight);
            Room room = new Room(x, y, rWidth, rHeight);
            if (isValidRoom(room, rooms)) {
                rooms.add(room);
            }
        }

        for (Room r : rooms) {
            r.fillFloor(world);
        }

        return rooms;
    }

    private static class Room {
        int x;
        int y;
        int width;
        int height;

        Room(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        int centerX() {
            return x + width / 2;
        }
        int centerY() {
            return y + height / 2;
        }

        boolean intersects(Room other) {
            return this.x <= other.x + other.width
                    && this.x + this.width >= other.x
                    && this.y <= other.y + other.height
                    && this.y + this.height >= other.y;
        }

        void fillFloor(TETile[][] world) {
            for (int i = x; i < x + width; i++) {
                for (int j = y; j < y + height; j++) {
                    world[i][j] = Tileset.FLOOR;
                }
            }
        }
    }

    /** check the validation of the new room */
    private static boolean isValidRoom(Room room, ArrayList<Room> rooms) {
        if (room.x < 1 || room.y < 1
                || room.x + room.width >= Engine.WIDTH - 1
                || room.y + room.height >= Engine.HEIGHT - 1) {
            return false;
        }

        for (Room other : rooms) {
            if (room.intersects(other)) {
                return false;
            }
        }

        return true;
    }

    /** generate the halls by simply connects the rooms' center in order */
    private static void connectRoomsV1(TETile[][] world, Random random, ArrayList<Room> rooms) {
        for (int i = 0; i < rooms.size() - 1; i++) {
            Room r1 = rooms.get(i);
            Room r2 = rooms.get(i + 1);
            connectTwoRooms(world, random, r1, r2);
        }
    }

    /** generate the halls by the MST algorithm */
    private static void connectRoomsV2(TETile[][] world, Random random, ArrayList<Room> rooms) {
        ArrayList<Edge> edges = new ArrayList<>();
        for (int i = 0; i < rooms.size(); i++) {
            for (int j = i + 1; j < rooms.size(); j++) {
                edges.add(new Edge(i, j, rooms));
            }
        }
        Collections.sort(edges);

        UnionFind uf = new UnionFind(rooms.size());
        int mstCount = 0;

        for (Edge edge : edges) {
            int r1 = edge.r1;
            int r2 = edge.r2;
            if (!uf.connected(r1, r2)) {
                uf.union(r1, r2);
                mstCount++;
                edge.selected = true;
                connectTwoRooms(world, random, rooms.get(r1), rooms.get(r2));
            }
            if (mstCount == rooms.size() - 1) {
                break;
            }
        }

        int extraEdges = RandomUtils.uniform(random, 1, 5);
        ArrayList<Edge> candidates = new ArrayList<>();
        for (Edge edge : edges) {
            if (!edge.selected) {
                candidates.add(edge);
            }
        }
        Collections.shuffle(candidates, random);
        int extraCount = Math.min(extraEdges, candidates.size());
        for (int i = 0; i < extraCount; i++) {
            Edge edge = candidates.get(i);
            connectTwoRooms(world, random, rooms.get(edge.r1), rooms.get(edge.r2));
        }
    }

    private static void connectTwoRooms(TETile[][] world, Random random, Room r1, Room r2) {
        int x1 = r1.centerX();
        int x2 = r2.centerX();
        int y1 = r1.centerY();
        int y2 = r2.centerY();
        if (RandomUtils.bernoulli(random)) {
            drawHorizontalHallway(world, random, x1, x2, y1);
            drawVerticalHallway(world, random, y1, y2, x2);
        } else {
            drawHorizontalHallway(world, random, x1, x2, y2);
            drawVerticalHallway(world, random, y1, y2, x1);
        }
    }

    /** helper function of connectTwoRooms */
    private static void drawHorizontalHallway(TETile[][] world, Random random,
                                              int x1, int x2, int y) {
        int width = RandomUtils.uniform(random, 1, 3);
        int start = Math.min(x1, x2);
        int end = Math.max(x1, x2);

        for (int x = start; x <= end; x++) {
            for (int dy = 0; dy < width; dy++) {
                world[x][y + dy] = Tileset.FLOOR;
            }
        }
    }

    /** helper function of connectTwoRooms */
    private static void drawVerticalHallway(TETile[][] world, Random random,
                                            int y1, int y2, int x) {
        int width = RandomUtils.uniform(random, 1, 3);
        int start = Math.min(y1, y2);
        int end = Math.max(y1, y2);

        for (int y = start; y <= end; y++) {
            for (int dx = 0; dx < width; dx++) {
                world[x + dx][y] = Tileset.FLOOR;
            }
        }
    }

    /** add walls at last for a new map */
    private static void addWalls(TETile[][] world) {
        for (int x = 1; x < Engine.WIDTH - 1; x++) {
            for (int y = 1; y < Engine.HEIGHT - 1; y++) {
                if (world[x][y] == Tileset.NOTHING && touchesFloor(world, x, y)) {
                    world[x][y] = Tileset.WALL;
                }
            }
        }
    }

    /** helper function of addWalls */
    private static boolean touchesFloor(TETile[][] world, int x, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (world[x + dx][y + dy] == Tileset.FLOOR) {
                    return true;
                }
            }
        }
        return false;
    }

    private static class Edge implements Comparable<Edge> {
        int r1;
        int r2;
        int weight;
        boolean selected;
        Edge(int r1, int r2, ArrayList<Room> rooms) {
            this.r1 = r1;
            this.r2 = r2;
            Room a = rooms.get(r1);
            Room b = rooms.get(r2);
            int dx = a.centerX() - b.centerX();
            int dy = a.centerY() - b.centerY();
            weight = dx * dx + dy * dy;
            selected = false;
        }

        @Override
        public int compareTo(Edge other) {
            return this.weight - other.weight;
        }
    }

    private static class UnionFind {
        int[] parent;
        UnionFind(int size) {
            parent = new int[size];
            for (int i = 0; i < size; i++) {
                parent[i] = i;
            }
        }

        int find(int x) {
            while (x != parent[x]) {
                parent[x] = parent[parent[x]];
                x = parent[x];
            }
            return x;
        }

        boolean connected(int a, int b) {
            return find(a) == find(b);
        }

        void union(int a, int b) {
            parent[find(a)] = find(b);
        }
    }
}
