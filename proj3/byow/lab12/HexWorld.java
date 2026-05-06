package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Arrays;
import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    static class Position {
        int x, y;
        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    private static final long SEED = 1919810;
    private static final Random RANDOM = new Random(SEED);

    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(5);
        switch (tileNum) {
            case 0: return Tileset.GRASS;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.SAND;
            case 3: return Tileset.TREE;
            case 4: return Tileset.MOUNTAIN;
            default: return Tileset.NOTHING;
        }
    }

    /** Draws a hexagon at the given position. */
    private static void addHexagon(int length, Position pos, TETile[][] tiles) {
        TETile tile = randomTile();
        int x = pos.x;
        int y = pos.y;
        for (int i = 0; i < 2 * length; i++) {
            int blanks = length - i - 1;
            if (blanks < 0) {
                blanks = - blanks - 1;
            }
            for (int j = blanks; j < blanks + length + (length - 1 - blanks) * 2; j++) {
                tiles[x + j][y + i] = tile;
            }
        }
    }

    private static void changeNextPos(Position pos, int length, int direction) {
        switch (direction) {
            case 0:
                pos.y += 2 * length;
                return;
            case 1:
                pos.x += 2 * length - 1;
                pos.y += length;
                return;
            case 2:
                pos.x += 2 * length - 1;
                pos.y -= length;
                return;
            case 3:
                pos.y -= 2 * length;
                return;
            case 4:
                pos.x -= 2 * length - 1;
                pos.y -= length;
                return;
            case 5:
                pos.x -= 2 * length - 1;
                pos.y += length;
        }
    }

    private static void fillWithHexagons(TETile[][] tiles, int length) {
        Position startPos = new Position(4 * length - 2, 0);
        addHexagon(length, startPos, tiles);
        changeNextPos(startPos, length, 5);
        addHexagon(length, startPos, tiles);
        changeNextPos(startPos, length, 5);
        addHexagon(length, startPos, tiles);
        changeNextPos(startPos, length, 0);
        addHexagon(length, startPos, tiles);
        changeNextPos(startPos, length, 0);
        addHexagon(length, startPos, tiles);
        changeNextPos(startPos, length, 1);
        addHexagon(length, startPos, tiles);
        changeNextPos(startPos, length, 1);
        addHexagon(length, startPos, tiles);
        changeNextPos(startPos, length, 2);
        addHexagon(length, startPos, tiles);
        changeNextPos(startPos, length, 2);
        addHexagon(length, startPos, tiles);
        changeNextPos(startPos, length, 3);
        addHexagon(length, startPos, tiles);
        changeNextPos(startPos, length, 3);
        addHexagon(length, startPos, tiles);
        changeNextPos(startPos, length, 4);
        addHexagon(length, startPos, tiles);
        changeNextPos(startPos, length, 5);
        addHexagon(length, startPos, tiles);
        changeNextPos(startPos, length, 5);
        addHexagon(length, startPos, tiles);
        changeNextPos(startPos, length, 0);
        addHexagon(length, startPos, tiles);
        changeNextPos(startPos, length, 1);
        addHexagon(length, startPos, tiles);
        changeNextPos(startPos, length, 2);
        addHexagon(length, startPos, tiles);
        changeNextPos(startPos, length, 3);
        addHexagon(length, startPos, tiles);
        changeNextPos(startPos, length, 5);
        addHexagon(length, startPos, tiles);
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        int length = RANDOM.nextInt(4) + 2;
        int width = 11 * length - 6;
        int height = 10 * length;

        ter.initialize(width, height);

        TETile[][] background = new TETile[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                background[i][j] = Tileset.NOTHING;
            }
        }
        fillWithHexagons(background, length);

        ter.renderFrame(background);
    }
}
