package byow.TileEngine;

import java.awt.Color;

/**
 * Contains constant tile objects, to avoid having to remake the same tiles in different parts of
 * the code.
 *
 * You are free to (and encouraged to) create and add your own tiles to this file. This file will
 * be turned in with the rest of your code.
 *
 * Ex:
 *      world[x][y] = Tileset.FLOOR;
 *
 * The style checker may crash when you try to style check this file due to use of unicode
 * characters. This is OK.
 */

public class Tileset {
    public static final TETile AVATAR = new TETile('@', Color.white, Color.black, "you");
    public static final TETile WALL = new TETile(' ',
            new Color(95, 95, 95), new Color(95, 95, 95), "wall");
    public static final TETile FLOOR = new TETile('.',
            new Color(95, 115, 190), new Color(8, 12, 45), "floor");
    public static final TETile NOTHING = new TETile(' ', Color.black, Color.black, "nothing");
    public static final TETile GRASS = new TETile('"', Color.green, Color.black, "grass");
    public static final TETile WATER = new TETile('≈', Color.blue, Color.black, "water");
    public static final TETile FLOWER = new TETile('❀', Color.magenta, Color.pink, "flower");
    public static final TETile LOCKED_DOOR = new TETile('█', Color.orange, Color.black,
            "locked door");
    public static final TETile UNLOCKED_DOOR = new TETile('▢', Color.orange, Color.black,
            "unlocked door");
    public static final TETile SAND = new TETile('▒', Color.yellow, Color.black, "sand");
    public static final TETile MOUNTAIN = new TETile('▲', Color.gray, Color.black, "mountain");
    public static final TETile TREE = new TETile('♠', Color.green, Color.black, "tree");
    public static final TETile BLUE_LIGHT_0 = new TETile('·',
            new Color(110, 140, 255), new Color(10, 20, 65), "dim blue light");

    public static final TETile BLUE_LIGHT_1 = new TETile('·',
            new Color(130, 165, 255), new Color(18, 35, 110), "soft blue light");

    public static final TETile BLUE_LIGHT_2 = new TETile('·',
            new Color(155, 190, 255), new Color(28, 60, 165), "blue light");

    public static final TETile BLUE_LIGHT_3 = new TETile('·',
            new Color(185, 215, 255), new Color(45, 95, 220), "bright blue light");

    public static final TETile BLUE_LIGHT_4 = new TETile('·',
            new Color(230, 245, 255), new Color(75, 135, 255), "glowing blue light");
    public static final TETile LIGHT_SOURCE = new TETile('●',
            new Color(245, 250, 255), new Color(55, 115, 245), "light source");
}


