package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 100;
    public static final int HEIGHT = 60;
    public static final int MENU_WIDTH = 40;
    public static final int MENU_HEIGHT = 60;
    private static final int HUD_HEIGHT = 3;
    private static final int MAX_SEED_LENGTH = 18;
    private static final File SAVE_FILE = new File("save.txt");

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        while (true) {
            showMenu();
            char key = waitForMenuKey();
            switch (key) {
                case 'q':
                    System.exit(0);
                    break;
                case 'n':
                    runGame(gameStateFromString('n' + getNewSeed() + 's', false));
                    break;
                case 'l':
                    if (hasSaveFile()) {
                        runGame(gameStateFromString("l", false));
                    }
                    break;
                case 'r':
                    if (hasSaveFile()) {
                        ter.initialize(WIDTH, HEIGHT + HUD_HEIGHT);
                        gameStateFromString("l", true);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private static char waitForMenuKey() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char key = Character.toLowerCase(StdDraw.nextKeyTyped());
                if (key == 'q' || key == 'n' || key == 'l' || key == 'r') {
                    return key;
                }
            } else {
                StdDraw.pause(10);
            }
        }
    }

    private static boolean hasSaveFile() {
        return SAVE_FILE.exists() && SAVE_FILE.isFile();
    }
    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        GameState state = gameStateFromString(input, false);
        return state.getWorld(true);
    }

    private GameState gameStateFromString(String s, boolean replay) {
        String input = s.toLowerCase();
        if (input.charAt(0) == 'l') {
            String savedInput = loadInputHistory();
            input = savedInput + input.substring(1);
        }

        int sIndex = input.indexOf('s');
        long seed = Long.parseLong(input.substring(1, sIndex));

        GameState state = new GameState(seed);
        for (int i = sIndex + 1; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == ':') {
                if (i + 1 < input.length() && input.charAt(i + 1) == 'q') {
                    saveInputHistory(state.getHistory());
                }
                break;
            } else if (isMovement(c)) {
                state.move(c);
                if (replay) {
                    renderWithHUD(state);
                    StdDraw.pause(100);
                }
            }
        }
        if (replay) {
            state.status = 2;
            renderWithHUD(state);
            StdDraw.pause(1000);
        }
        return state;
    }

    private static boolean isMovement(char c) {
        return c == 'w' || c == 'a' || c == 's' || c == 'd';
    }

    private static void saveInputHistory(String inputHistory) {
        try {
            Files.writeString(SAVE_FILE.toPath(), inputHistory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String loadInputHistory() {
        try {
            return Files.readString(SAVE_FILE.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isInMap(int x, int y) {
        return !(x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT);
    }


    private void runGame(GameState state) {
        ter.initialize(WIDTH, HEIGHT + HUD_HEIGHT);
        renderWithHUD(state);

        boolean colonTyped = false;
        int lastMouseX = -1;
        int lastMouseY = -1;

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char key = Character.toLowerCase(StdDraw.nextKeyTyped());

                if (colonTyped) {
                    if (key == 'q') {
                        saveInputHistory(state.getHistory());
                        state.status = 1;
                        renderWithHUD(state);
                        StdDraw.pause(1000);
                        return;
                    } else {
                        colonTyped = false;
                    }
                } else if (isMovement(key)) {
                    state.move(key);
                    renderWithHUD(state);

                    lastMouseX = (int) StdDraw.mouseX();
                    lastMouseY = (int) StdDraw.mouseY();
                } else if (key == ':') {
                    colonTyped = true;
                } else if (key == 'g') {
                    state.toggleVision();
                    renderWithHUD(state);
                } else if (key == 'm') {
                    return;
                }
            }

            int mouseX = (int) StdDraw.mouseX();
            int mouseY = (int) StdDraw.mouseY();

            if (mouseX != lastMouseX || mouseY != lastMouseY) {
                renderWithHUD(state);
                lastMouseX = mouseX;
                lastMouseY = mouseY;
            }

            StdDraw.pause(20);
        }
    }


    private void renderWithHUD(GameState state) {
        TETile[][] world = state.getWorld(false);
        StdDraw.clear(Color.BLACK);
        for (int x = 0; x < world.length; x++) {
            for (int y = 0; y < world[0].length; y++) {
                world[x][y].draw(x, y);
            }
        }

        int mouseX = (int) StdDraw.mouseX();
        int mouseY = (int) StdDraw.mouseY();
        String tileDescription = "";
        if (isInMap(mouseX, mouseY)) {
            tileDescription = world[mouseX][mouseY].description();
        }

        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 16));
        StdDraw.textLeft(1, HEIGHT + 1.5, "Tile: " + tileDescription);
        StdDraw.text(WIDTH / 2.0, HEIGHT + 1.5, state.getTitle());
        StdDraw.textRight(WIDTH - 1, HEIGHT + 1.5, "WASD to move | :Q to save | M back to menu");
        StdDraw.show();
    }

    private static void showMenu() {
        StdDraw.setCanvasSize(MENU_WIDTH * 16, MENU_HEIGHT * 16);
        StdDraw.setXscale(0, MENU_WIDTH);
        StdDraw.setYscale(0, MENU_HEIGHT);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();

        Font titleFont = new Font("Monaco", Font.BOLD, 44);
        StdDraw.setFont(titleFont);
        StdDraw.setPenColor(new Color(255, 220, 120));
        StdDraw.text(MENU_WIDTH / 2.0, MENU_HEIGHT * 0.72, "CS61B: BYOW");

        Font optionFont = new Font("Monaco", Font.BOLD, 24);
        StdDraw.setFont(optionFont);
        StdDraw.setPenColor(Color.WHITE);

        StdDraw.text(MENU_WIDTH / 2.0, MENU_HEIGHT / 2.0 + 2, "New Game (N)");
        StdDraw.text(MENU_WIDTH / 2.0, MENU_HEIGHT / 2.0, "Load Game (L)");
        StdDraw.text(MENU_WIDTH / 2.0, MENU_HEIGHT / 2.0 - 2, "Replay Game (R)");
        StdDraw.text(MENU_WIDTH / 2.0, MENU_HEIGHT / 2.0 - 4, "Quit (Q)");
        StdDraw.show();
    }

    private static void inputSeedMenu(String seed) {
        StdDraw.clear(Color.BLACK);

        Font titleFont = new Font("Monaco", Font.BOLD, 34);
        StdDraw.setFont(titleFont);
        StdDraw.setPenColor(new Color(255, 220, 120));
        StdDraw.text(MENU_WIDTH / 2.0, MENU_HEIGHT * 0.68, "Enter Seed");

        Font seedFont = new Font("Monaco", Font.BOLD, 28);
        StdDraw.setFont(seedFont);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(MENU_WIDTH / 2.0, MENU_HEIGHT / 2.0, seed);

        Font hintFont = new Font("Monaco", Font.PLAIN, 18);
        StdDraw.setFont(hintFont);
        StdDraw.setPenColor(Color.LIGHT_GRAY);
        StdDraw.text(MENU_WIDTH / 2.0, MENU_HEIGHT / 2.0 - 5, "Press S to start");
        StdDraw.show();
    }

    private static String getNewSeed() {
        while (StdDraw.hasNextKeyTyped()) {
            StdDraw.nextKeyTyped();
        }
        StringBuilder input = new StringBuilder();
        inputSeedMenu(input.toString());
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char key = Character.toLowerCase(StdDraw.nextKeyTyped());
                if (key == 's' && input.length() > 0) {
                    return input.toString();
                } else if (Character.isDigit(key) && input.length() < MAX_SEED_LENGTH) {
                    input.append(key);
                }
                inputSeedMenu(input.toString());
            } else {
                StdDraw.pause(10);
            }
        }
    }
}
