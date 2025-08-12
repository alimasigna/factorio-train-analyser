package factorio.train.analyser.graph;

import java.util.HashMap;
import java.util.Vector;
import java.util.Arrays;

import factorio.train.analyser.jsonmodels.Entity;

public class LookUp {
    private int x;
    private int y;
    private double direction;
    private String name;

    // downward means like downward on a coordinate system, while down means its facing down
    // vice versa to upward vs up
    private static final int STRAIGHT_VERTICAL = 0;                 // |
    private static final int STRAIGHT_HORIZONTAL = 4;               // -
    private static final int STRAIGHT_DIAGONAL_DOWNWARDS = 6;       // \
    private static final int STRAIGHT_DIAGONAL_UPWARDS = 2;         // /

    //TODO: these are called "half-diagonal-rail" in game, they only connect to other half-diagonals or curved rails
    private static final int HALF_DIAGONAL_UPWARDS = 4;           // /
    private static final int HALF_DIAGONAL_DOWNWARDS = 0;         // \
    private static final int HALF_DIAGONAL_UPWARDS_FLIPPED = 2;    // /
    private static final int HALF_DIAGONAL_DOWNWARDS_FLIPPED = 6; // \

    //TODO: these are called "curved-rail-a" in the game
    private static final int CURVED_UP_LEFT = 0;                // '|
    private static final int CURVED_UP_RIGHT = 2;               // |'
    private static final int CURVED_DOWN_LEFT = 10;             // ,|
    private static final int CURVED_DOWN_RIGHT = 8;             // |,
    private static final int CURVED_RIGHT_UP = 4;               // --'
    private static final int CURVED_RIGHT_DOWN = 6;             // --,
    private static final int CURVED_LEFT_UP = 14;               // '--
    private static final int CURVED_LEFT_DOWN = 12;             // ,__

    //TODO: they are called "curved-rail-b" in the game
    private static final int CURVED_RIGHT_UPWARDS = 12;              // /
    private static final int CURVED_RIGHT_DOWNWARDS = 0;             // \
    private static final int CURVED_LEFT_UPWARDS = 4;                // /
    private static final int CURVED_LEFT_DOWNWARDS = 8;              // \

    private static final int CURVED_RIGHT_UPWARDS_FLIPPED = 10;       // /
    private static final int CURVED_RIGHT_DOWNWARDS_FLIPPED = 14;     // \
    private static final int CURVED_LEFT_UPWARDS_FLIPPED = 2;         // /
    private static final int CURVED_LEFT_DOWNWARDS_FLIPPED = 6;       // \

    // start: -
    private static final HashMap<Integer, Integer> STRAIGHT_VERTICAL_ROTATIONS = new HashMap<>() {{
        put(0, STRAIGHT_HORIZONTAL);
        put(45, STRAIGHT_DIAGONAL_DOWNWARDS);
        put(90, STRAIGHT_VERTICAL);
        put(135, STRAIGHT_DIAGONAL_UPWARDS);
        put(180, STRAIGHT_HORIZONTAL);
        put(225, STRAIGHT_DIAGONAL_DOWNWARDS);
        put(270, STRAIGHT_VERTICAL);
        put(315, STRAIGHT_DIAGONAL_UPWARDS);
    }};
    // start: / please note the rotation are derived from CURVED_ROTATIONS
    private static final HashMap<Integer, Integer> HALF_DIAGONAL_ROTATIONS = new HashMap<>() {{
        put(0, HALF_DIAGONAL_DOWNWARDS_FLIPPED);
        put(45, HALF_DIAGONAL_DOWNWARDS);
        put(90, HALF_DIAGONAL_UPWARDS_FLIPPED);
        put(135, HALF_DIAGONAL_UPWARDS);
        put(180, HALF_DIAGONAL_DOWNWARDS_FLIPPED);
        put(225, HALF_DIAGONAL_DOWNWARDS);
        put(270, HALF_DIAGONAL_UPWARDS);
    }};
    // start: --,
    private static final HashMap<Integer, Integer> CURVED_ROTATIONS = new HashMap<>() {{
        put(0, CURVED_RIGHT_DOWN);
        put(45, CURVED_RIGHT_DOWNWARDS);
        put(90, CURVED_DOWN_LEFT);
        put(135, CURVED_LEFT_UPWARDS);
        put(180, CURVED_LEFT_UP);
        put(225, CURVED_LEFT_DOWNWARDS);
        put(270, CURVED_UP_RIGHT);
        put(315, CURVED_RIGHT_UPWARDS);
    }};
    // start: --'
    private static final HashMap<Integer, Integer> CURVED_ROTATIONS_FLIPPED = new HashMap<>() {{
        put(0, CURVED_RIGHT_UP);
        put(45, CURVED_RIGHT_DOWNWARDS_FLIPPED);
        put(90, CURVED_DOWN_RIGHT);
        put(135, CURVED_LEFT_UPWARDS_FLIPPED);
        put(180, CURVED_LEFT_DOWN);
        put(225, CURVED_LEFT_DOWNWARDS_FLIPPED);
        put(270, CURVED_UP_LEFT);
        put(315, CURVED_RIGHT_UPWARDS_FLIPPED);
    }};

    public LookUp(int x, int y, int direction, String name) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.name = name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getDirection() {
        return direction;
    }

    public String getName() {
        return name;
    }

    // This method maps all the possible rails that can be CONNECTED to the given
    // entity
    // it also filters which connection cant be connected to each other
    public static LookUp[][] lookUpConnected(Track track) {
        return lookUpConnected(new Entity(-1, track.getName(), track.getPosition(), track.getDirection()));
    }

    public static LookUp[][] lookUpConnected(Entity entry) {
        LookUp[] entrance = {};
        LookUp[] exit = {};
        int xPos = (int) entry.getPosition().getX();
        int yPos = (int) entry.getPosition().getY();

        // These hashmaps define all possible absolute offsets for the given rail
        HashMap<Integer, Vector<Integer>> absoluteOffsetEntry = new HashMap<>();
        HashMap<Integer, Vector<Integer>> absoluteOffsetExit = new HashMap<>();

        switch (entry.getName()) {
            case "straight-rail":
                // Define all possible relative offsets for the given rail
                HashMap<HashMap<Integer, Integer>, Vector<Integer>> relativeOffset = new HashMap<>() {{
                    Vector<Integer> off1 = new Vector<>(2);
                    off1.add(-4);    // x
                    off1.add(0);     // y
                    put(STRAIGHT_VERTICAL_ROTATIONS, off1);

                    Vector<Integer> off2 = new Vector<>(2);
                    off2.add(-6);
                    off2.add(0);
                    put(CURVED_ROTATIONS, off2);

                    Vector<Integer> off3 = new Vector<>(2);
                    off3.add(-6);
                    off3.add(0);
                    put(CURVED_ROTATIONS_FLIPPED, off3);
                }};

                // Calculate the actual offsets for the given actual rail
                switch (entry.getDirection()) {
                    case STRAIGHT_HORIZONTAL: // -- -> 0°
                        absoluteOffsetEntry = rotateOffsets(relativeOffset, 0);
                        absoluteOffsetExit = rotateOffsets(relativeOffset, 180);
                        break;
                    case STRAIGHT_DIAGONAL_DOWNWARDS: // \ -> 45°,225°
                        absoluteOffsetEntry = rotateOffsets(relativeOffset, 45);
                        absoluteOffsetExit = rotateOffsets(relativeOffset, 225);
                        break;
                    case STRAIGHT_VERTICAL: // | -> 90°
                        absoluteOffsetEntry = rotateOffsets(relativeOffset, 90);
                        absoluteOffsetExit = rotateOffsets(relativeOffset, 270);
                        break;
                    case STRAIGHT_DIAGONAL_UPWARDS: // / -> 135°, 315°
                        absoluteOffsetEntry = rotateOffsets(relativeOffset, 135);
                        absoluteOffsetExit = rotateOffsets(relativeOffset, 315);
                        break;
                }

                // Iterate over the Hashmap to create the LookUp objects for entrance & exit
                entrance = generateLookUps(absoluteOffsetEntry, xPos, yPos, "straight-rail");
                exit = generateLookUps(absoluteOffsetExit, xPos, yPos, "straight-rail");
                break;
            case "curved-rail-a":
                // Adjustment are needed because the rotation HashMap is adjusted for straight rails
                // These are the offsets for entrance and exit of the curved rails type a
                HashMap<HashMap<Integer, Integer>, Vector<Integer>> relativeOffsetEntrance = new HashMap<>() {{
                    Vector<Integer> off1 = new Vector<>(2);
                    off1.add(-6);
                    off1.add(0);
                    put(rotateRotationMap(STRAIGHT_VERTICAL_ROTATIONS, 180), off1); // adjustment by 180°

                    Vector<Integer> off2 = new Vector<>(2);
                    off2.add(-8);
                    off2.add(0);
                    put(rotateRotationMap(CURVED_ROTATIONS, 180), off2);            // adjustment by 180°

                    Vector<Integer> off3 = new Vector<>(2);
                    off3.add(-8);
                    off3.add(0);
                    put(rotateRotationMap(CURVED_ROTATIONS_FLIPPED, 180), off3);    // adjustment by 180°
                }};
                HashMap<HashMap<Integer, Integer>, Vector<Integer>> relativeOffsetExit = new HashMap<>() {{
                    Vector<Integer> off1 = new Vector<>(2);
                    off1.add(10);
                    off1.add(4);
                    put(rotateRotationMap(CURVED_ROTATIONS, 180), off1);           // adjustment by 180°

                    Vector<Integer> off2 = new Vector<>(2);
                    off2.add(10);
                    off2.add(4);
                    put(rotateRotationMap(HALF_DIAGONAL_ROTATIONS, 180), off2);     // adjustment by 180°

                    Vector<Integer> off3 = new Vector<>(2);
                    off3.add(12);
                    off3.add(4);
                    put(rotateRotationMap(CURVED_ROTATIONS_FLIPPED, 225), off3);    // adjustment by 225°
                }};
                HashMap<HashMap<Integer, Integer>, Vector<Integer>> relativeOffsetExitFlipped = new HashMap<>() {{
                    Vector<Integer> off1 = new Vector<>(2);
                    off1.add(10);
                    off1.add(-4);
                    put(rotateRotationMap(CURVED_ROTATIONS, 135), off1);            // adjustment by 135°

                    Vector<Integer> off2 = new Vector<>(2);
                    off2.add(10);
                    off2.add(-4);
                    put(rotateRotationMap(HALF_DIAGONAL_ROTATIONS, 135), off2);     // adjustment by 135°

                    Vector<Integer> off3 = new Vector<>(2);
                    off3.add(12);
                    off3.add(-4);
                    put(rotateRotationMap(CURVED_ROTATIONS_FLIPPED, 180), off3);    // adjustment by 180°
                }};
                //TODO: look if the rotate offsets works with thse curved rails
                switch (entry.getDirection()) {
                    case CURVED_RIGHT_DOWN:
                        absoluteOffsetEntry = rotateOffsets(relativeOffsetEntrance, 0);
                        absoluteOffsetExit = rotateOffsets(relativeOffsetExit, 180);
                        break;
                    case CURVED_RIGHT_UP:
                        absoluteOffsetEntry = rotateOffsets(relativeOffsetEntrance, 0);
                        absoluteOffsetExit = rotateOffsets(relativeOffsetExitFlipped, 180);
                        break;
                    case CURVED_DOWN_LEFT:
                        absoluteOffsetEntry = rotateOffsets(relativeOffsetEntrance, 90);
                        absoluteOffsetExit = rotateOffsets(relativeOffsetExit, 270);
                        break;
                    case CURVED_DOWN_RIGHT:
                        absoluteOffsetEntry = rotateOffsets(relativeOffsetEntrance, 90);
                        absoluteOffsetExit = rotateOffsets(relativeOffsetExitFlipped, 270);
                        break;
                    case CURVED_LEFT_DOWN:
                        absoluteOffsetEntry = rotateOffsets(relativeOffsetEntrance, 180);
                        absoluteOffsetExit = rotateOffsets(relativeOffsetExitFlipped, 0);
                        break;
                    case CURVED_LEFT_UP:
                        absoluteOffsetEntry = rotateOffsets(relativeOffsetEntrance, 180);
                        absoluteOffsetExit = rotateOffsets(relativeOffsetExit, 0);
                        break;
                    case CURVED_UP_LEFT:
                        absoluteOffsetEntry = rotateOffsets(relativeOffsetEntrance, 270);
                        absoluteOffsetExit = rotateOffsets(relativeOffsetExitFlipped, 90);
                        break;
                    case CURVED_UP_RIGHT:
                        absoluteOffsetEntry = rotateOffsets(relativeOffsetEntrance, 270);
                        absoluteOffsetExit = rotateOffsets(relativeOffsetExit, 90);
                        break;
                }
                entrance = generateLookUps(absoluteOffsetEntry, xPos, yPos, "curved-rail-a");
                exit = generateLookUps(absoluteOffsetExit, xPos, yPos, "curved-rail-a");
                break;
            case "curved-rail-b":
                switch (entry.getDirection()) {
                    case CURVED_RIGHT_DOWNWARDS:
                    case CURVED_RIGHT_DOWNWARDS_FLIPPED:
                    case CURVED_LEFT_UPWARDS:
                    case CURVED_LEFT_UPWARDS_FLIPPED:
                    case CURVED_LEFT_DOWNWARDS:
                    case CURVED_LEFT_DOWNWARDS_FLIPPED:
                    case CURVED_RIGHT_UPWARDS:
                    case CURVED_RIGHT_UPWARDS_FLIPPED:
                }
                break;
            case "halfdiagonal-rail":
        }
        return new LookUp[][]{entrance, exit};
    }

    // This method maps all the possible rails that can cross to the given entity.
    // please note they are NOT connected
    public static LookUp[] lookUpCrossed(Track entry) {
        return lookUpCrossed(new Entity(-1, entry.getName(), entry.getPosition(), entry.getDirection()));
    }

    public static LookUp[] lookUpCrossed(Entity entry) {
        LookUp[] neighbours = {};
        int xPos = (int) entry.getPosition().getX();
        int yPos = (int) entry.getPosition().getY();
        switch (entry.getName()) {
            case "straight-rail":
                switch (entry.getDirection()) {
                    case 0: // |
                        neighbours = new LookUp[]{
                                new LookUp(xPos, yPos, 2, "straight-rail"),
                                new LookUp(xPos, yPos, 5, "straight-rail"),
                                new LookUp(xPos, yPos, 1, "straight-rail"),
                                new LookUp(xPos, yPos, 3, "straight-rail"),
                                new LookUp(xPos, yPos, 7, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 2, 2, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 2, "curved-rail"),

                                new LookUp(xPos - 6, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 6, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2, 6, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 6, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 6, "curved-rail"),

                                new LookUp(xPos - 6, yPos - 2, 3, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 3, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 3, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2, 3, "curved-rail"),

                                new LookUp(xPos + 6, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2, 7, "curved-rail"),

                                new LookUp(xPos - 2, yPos + 2, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6, 4, "curved-rail"),

                                new LookUp(xPos + 2, yPos - 2, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 0, "curved-rail"),

                                new LookUp(xPos - 2, yPos + 6, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6, 1, "curved-rail"),

                                new LookUp(xPos + 2, yPos - 6, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6, 5, "curved-rail")
                        };
                        break;
                    case 2: // -
                        neighbours = new LookUp[]{
                                new LookUp(xPos, yPos, 0, "straight-rail"),
                                new LookUp(xPos, yPos, 5, "straight-rail"),
                                new LookUp(xPos, yPos, 1, "straight-rail"),
                                new LookUp(xPos, yPos, 3, "straight-rail"),
                                new LookUp(xPos, yPos, 7, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 6, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6, 1, "curved-rail"),

                                new LookUp(xPos - 2, yPos + 6, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6, 5, "curved-rail"),

                                new LookUp(xPos + 6, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 3, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 3, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2, 3, "curved-rail"),

                                new LookUp(xPos + 6, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2, 7, "curved-rail"),

                                new LookUp(xPos + 2, yPos + 6, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 4, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 4, "curved-rail"),

                                new LookUp(xPos + 2, yPos + 6, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 0, "curved-rail"),

                                new LookUp(xPos - 6, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 6, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2, 6, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 6, "curved-rail"),

                                new LookUp(xPos - 6, yPos + 2, 2, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 2, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "curved-rail")

                        };
                        break;
                    case 5: // \
                        neighbours = new LookUp[]{
                                new LookUp(xPos, yPos, 2, "straight-rail"),
                                new LookUp(xPos - 4, yPos, 2, "straight-rail"),
                                new LookUp(xPos, yPos, 7, "straight-rail"),
                                new LookUp(xPos - 4, yPos, 3, "straight-rail"),
                                new LookUp(xPos, yPos, 0, "straight-rail"),
                                new LookUp(xPos, yPos + 4, 0, "straight-rail"),
                                new LookUp(xPos, yPos, 3, "straight-rail"),
                                new LookUp(xPos, yPos + 4, 7, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 6, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6, 5, "curved-rail"),

                                new LookUp(xPos - 2, yPos + 6, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6, 4, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 4, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2, 4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 4, "curved-rail"),

                                new LookUp(xPos - 6, yPos + 6, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6, 1, "curved-rail"),

                                new LookUp(xPos + 2, yPos + 2, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 6, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6, 0, "curved-rail"),

                                new LookUp(xPos - 2, yPos - 2, 3, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2, 3, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2, 3, "curved-rail"),

                                new LookUp(xPos - 10, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6, 7, "curved-rail"),

                                new LookUp(xPos - 10, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 6, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2, 6, "curved-rail"),

                                new LookUp(xPos - 6, yPos + 6, 2, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 2, 2, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "curved-rail")
                        };
                        break;
                    case 1: // \
                        neighbours = new LookUp[]{
                                new LookUp(xPos, yPos, 2, "straight-rail"),
                                new LookUp(xPos + 4, yPos, 2, "straight-rail"),
                                new LookUp(xPos, yPos, 3, "straight-rail"),
                                new LookUp(xPos + 4, yPos, 7, "straight-rail"),
                                new LookUp(xPos, yPos, 0, "straight-rail"),
                                new LookUp(xPos, yPos + 4, 0, "straight-rail"),
                                new LookUp(xPos, yPos, 7, "straight-rail"),
                                new LookUp(xPos, yPos - 4, 3, "straight-rail"),

                                new LookUp(xPos + 6, yPos - 6, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6, 1, "curved-rail"),

                                new LookUp(xPos + 2, yPos - 6, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "curved-rail"),

                                new LookUp(xPos + 6, yPos - 6, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6, 5, "curved-rail"),

                                new LookUp(xPos - 2, yPos - 2, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 4, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 6, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6, 4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 4, "curved-rail"),

                                new LookUp(xPos + 2, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2, 7, "curved-rail"),

                                new LookUp(xPos + 10, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 3, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 3, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 3, "curved-rail"),

                                new LookUp(xPos + 10, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 2, 2, "curved-rail"),

                                new LookUp(xPos - 6, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 6, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2, 6, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 6, 6, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 6, "curved-rail")
                        };
                        break;
                    case 3: // /
                        neighbours = new LookUp[]{
                                new LookUp(xPos, yPos, 0, "straight-rail"),
                                new LookUp(xPos, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos, yPos, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos, 5, "straight-rail"),
                                new LookUp(xPos, yPos, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos, 2, "straight-rail"),
                                new LookUp(xPos, yPos, 5, "straight-rail"),
                                new LookUp(xPos, yPos + 2, 1, "straight-rail"),

                                new LookUp(xPos + 2, yPos + 2, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6, 4, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 6, 4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 4, "curved-rail"),

                                new LookUp(xPos + 2, yPos + 6, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 5, "curved-rail"),

                                new LookUp(xPos + 6, yPos + 6, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "curved-rail"),

                                new LookUp(xPos - 2, yPos + 2, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6, 1, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 6, 1, "curved-rail"),

                                new LookUp(xPos + 2, yPos - 2, 6, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2, 6, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 2, 6, "curved-rail"),

                                new LookUp(xPos - 2, yPos + 6, 2, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 2, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos + 10, yPos - 2, 2, "curved-rail"),

                                new LookUp(xPos + 10, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 3, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2, 3, "curved-rail"),

                                new LookUp(xPos - 6, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 6, 7, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 7, "curved-rail")
                        };
                        break;
                    case 7: // /
                        neighbours = new LookUp[]{
                                new LookUp(xPos, yPos, 0, "straight-rail"),
                                new LookUp(xPos, yPos - 4, 0, "straight-rail"),
                                new LookUp(xPos, yPos, 5, "straight-rail"),
                                new LookUp(xPos - 4, yPos, 1, "straight-rail"),
                                new LookUp(xPos, yPos, 2, "straight-rail"),
                                new LookUp(xPos - 4, yPos, 2, "straight-rail"),
                                new LookUp(xPos, yPos, 1, "straight-rail"),
                                new LookUp(xPos, yPos - 4, 5, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 2, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 0, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 6, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 0, "curved-rail"),

                                new LookUp(xPos - 2, yPos - 2, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 1, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 2, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 1, "curved-rail"),

                                new LookUp(xPos - 2, yPos - 2, 4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 4, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 6, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6, 4, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 4, "curved-rail"),

                                new LookUp(xPos - 2, yPos + 2, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6, 5, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 6, 5, "curved-rail"),

                                new LookUp(xPos - 2, yPos + 2, 2, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 2, 2, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2, 2, "curved-rail"),

                                new LookUp(xPos + 2, yPos - 6, 6, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 6, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 6, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos - 10, yPos + 2, 6, "curved-rail"),

                                new LookUp(xPos - 10, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2, 7, "curved-rail"),

                                new LookUp(xPos + 6, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 3, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2, 3, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 6, 3, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 3, "curved-rail")
                        };
                        break;
                }
                break;
            case "curved-rail":
                switch (entry.getDirection()) {
                    case 0: // '|
                        neighbours = new LookUp[]{
                                new LookUp(xPos + 2, yPos + 6, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 6, 2, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 6, 2, "straight-rail"),

                                new LookUp(xPos + 2, yPos + 6, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 6, 0, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 2, 7, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 6, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6, 7, "straight-rail"),

                                new LookUp(xPos - 6, yPos - 6, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 6, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6, 3, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 6, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6, 5, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 2, 1, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 10, 1, "straight-rail"),

                                new LookUp(xPos - 4, yPos - 12, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 0, "curved-rail"),

                                new LookUp(xPos - 4, yPos + 8, 2, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 2, "curved-rail"),

                                new LookUp(xPos - 4, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 12, 4, "curved-rail"),

                                new LookUp(xPos - 12, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 6, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 6, "curved-rail"),

                                new LookUp(xPos - 4, yPos - 8, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos, yPos - 12, 5, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 5, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 12, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 5, "curved-rail"),

                                new LookUp(xPos - 8, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 12, yPos - 8, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 8, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 7, "curved-rail"),

                                new LookUp(xPos - 8, yPos, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 12, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos, yPos, 1, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 1, "curved-rail"),
                                new LookUp(xPos, yPos + 12, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 1, "curved-rail"),

                                new LookUp(xPos - 8, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 8, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 3, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 8, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 3, "curved-rail")
                        };
                        break;
                    case 1: // |'
                        neighbours = new LookUp[]{
                                new LookUp(xPos - 2, yPos + 6, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6, 2, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 6, 2, "straight-rail"),

                                new LookUp(xPos + 2, yPos - 6, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 6, 0, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 6, 7, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 10, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 2, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 6, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6, 3, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 2, 5, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 6, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6, 5, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 6, 5, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 6, 1, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 1, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6, 1, "straight-rail"),

                                new LookUp(xPos - 4, yPos + 12, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos, 5, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 5, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 8, 5, "curved-rail"),

                                new LookUp(xPos - 8, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 7, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 7, "curved-rail"),

                                new LookUp(xPos - 8, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 3, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 3, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos + 12, yPos - 4, 3, "curved-rail"),

                                new LookUp(xPos + 4, yPos - 12, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 1, "curved-rail"),

                                new LookUp(xPos + 4, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos, yPos - 12, 4, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 12, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 4, "curved-rail"),

                                new LookUp(xPos - 8, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 2, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 8, 2, "curved-rail"),
                                new LookUp(xPos + 12, yPos - 8, 2, "curved-rail"),

                                new LookUp(xPos + 8, yPos, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 12, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos, 0, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 0, "curved-rail"),
                                new LookUp(xPos, yPos + 12, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 0, "curved-rail"),

                                new LookUp(xPos, yPos + 8, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 8, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 6, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 8, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 6, "curved-rail")
                        };
                        break;
                    case 2: // --'
                        neighbours = new LookUp[]{
                                new LookUp(xPos - 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos - 6, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 2, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 6, 0, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 1, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2, 1, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 2, 5, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 6, 5, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 7, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2, 7, "straight-rail"),

                                new LookUp(xPos - 10, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos - 6, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 3, "straight-rail"),

                                new LookUp(xPos - 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 12, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 8, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos, 6, "curved-rail"),

                                new LookUp(xPos + 4, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 4, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 4, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 4, "curved-rail"),

                                new LookUp(xPos + 4, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 12, yPos - 4, 2, "curved-rail"),

                                new LookUp(xPos - 8, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 12, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 12, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 7, "curved-rail"),

                                new LookUp(xPos - 4, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos, yPos, 1, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 1, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 12, 1, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 1, "curved-rail"),

                                new LookUp(xPos - 4, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 0, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 0, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 12, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos, 0, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 0, "curved-rail"),

                                new LookUp(xPos, yPos - 8, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos - 12, yPos, 3, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 12, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 3, "curved-rail"),

                                new LookUp(xPos - 8, yPos, 5, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 8, 5, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 5, "curved-rail")
                        };
                        break;
                    case 3: // --,
                        neighbours = new LookUp[]{
                                new LookUp(xPos + 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "straight-rail"),

                                new LookUp(xPos + 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 6, 0, "straight-rail"),

                                new LookUp(xPos - 6, yPos - 2, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2, 3, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 2, 7, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 6, 7, "straight-rail"),

                                new LookUp(xPos - 6, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 5, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2, 5, "straight-rail"),

                                new LookUp(xPos - 10, yPos - 2, 1, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 2, 1, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 1, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 1, "straight-rail"),

                                new LookUp(xPos - 8, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 12, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 12, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 6, "curved-rail"),

                                new LookUp(xPos - 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 12, 4, "curved-rail"),

                                new LookUp(xPos, yPos + 8, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos - 12, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 12, yPos, 2, "curved-rail"),

                                new LookUp(xPos - 8, yPos, 0, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 0, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 8, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 0, "curved-rail"),

                                new LookUp(xPos, yPos - 8, 5, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 5, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 12, 5, "curved-rail"),

                                new LookUp(xPos + 12, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 3, "curved-rail"),

                                new LookUp(xPos - 8, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 1, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 1, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos, yPos, 1, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 1, "curved-rail"),

                                new LookUp(xPos + 8, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 8, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 12, yPos - 4, 7, "curved-rail")
                        };
                        break;
                    case 4: // |,
                        neighbours = new LookUp[]{
                                new LookUp(xPos - 2, yPos - 6, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6, 2, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 6, 2, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 6, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6, 0, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 6, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6, 3, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 6, 7, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6, 7, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 6, 7, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 6, 1, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 1, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6, 1, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 10, 5, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 6, 5, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 5, "straight-rail"),

                                new LookUp(xPos + 4, yPos + 8, 7, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 8, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 8, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 7, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 7, "curved-rail"),

                                new LookUp(xPos - 4, yPos - 8, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos, yPos - 12, 5, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 5, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 12, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 5, "curved-rail"),

                                new LookUp(xPos + 4, yPos + 8, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 8, 3, "curved-rail"),
                                new LookUp(xPos + 12, yPos + 8, 3, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 3, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 3, "curved-rail"),

                                new LookUp(xPos - 4, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 12, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos, yPos, 1, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 1, "curved-rail"),
                                new LookUp(xPos, yPos + 12, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 1, "curved-rail"),

                                new LookUp(xPos - 4, yPos - 8, 2, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 2, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos + 12, yPos + 4, 2, "curved-rail"),

                                new LookUp(xPos + 4, yPos + 12, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 4, "curved-rail"),

                                new LookUp(xPos, yPos + 8, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 6, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 6, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 6, "curved-rail"),

                                new LookUp(xPos - 4, yPos - 12, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 0, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 8, 0, "curved-rail")
                        };
                        break;
                    case 5: // ,|
                        neighbours = new LookUp[]{
                                new LookUp(xPos - 6, yPos + 6, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 6, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6, 2, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 6, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6, 0, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 2, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 10, 3, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 6, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6, 7, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 6, 1, "straight-rail"),
                                new LookUp(xPos - 6, yPos + 6, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6, 1, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 2, 5, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 6, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6, 5, "straight-rail"),

                                new LookUp(xPos - 4, yPos + 8, 2, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 8, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 8, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 2, "curved-rail"),

                                new LookUp(xPos + 4, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos, yPos - 12, 4, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 12, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 4, "curved-rail"),

                                new LookUp(xPos - 4, yPos + 8, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 8, 6, "curved-rail"),
                                new LookUp(xPos - 12, yPos + 8, 6, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 6, "curved-rail"),

                                new LookUp(xPos, yPos - 12, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos, 0, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 0, "curved-rail"),
                                new LookUp(xPos, yPos + 12, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 0, "curved-rail"),

                                new LookUp(xPos - 4, yPos + 12, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 5, "curved-rail"),

                                new LookUp(xPos + 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos - 12, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 7, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 7, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 7, "curved-rail"),

                                new LookUp(xPos + 8, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos, yPos, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 3, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 3, "curved-rail"),

                                new LookUp(xPos, yPos, 1, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 1, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 8, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 12, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 1, "curved-rail")
                        };
                        break;
                    case 6: // ,--
                        neighbours = new LookUp[]{
                                new LookUp(xPos - 6, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2, 2, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos - 6, yPos + 6, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2, 0, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 2, 5, "straight-rail"),
                                new LookUp(xPos - 6, yPos + 2, 5, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2, 5, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 6, 1, "straight-rail"),
                                new LookUp(xPos - 6, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 1, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2, 1, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 3, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2, 3, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 7, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2, 7, "straight-rail"),
                                new LookUp(xPos + 10, yPos - 2, 7, "straight-rail"),

                                new LookUp(xPos - 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos - 12, yPos, 3, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 12, yPos, 3, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 3, "curved-rail"),

                                new LookUp(xPos - 8, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 12, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 5, "curved-rail"),

                                new LookUp(xPos - 12, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 6, "curved-rail"),

                                new LookUp(xPos, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos, 0, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 0, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 0, "curved-rail"),

                                new LookUp(xPos, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 8, 2, "curved-rail"),
                                new LookUp(xPos + 12, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 2, "curved-rail"),

                                new LookUp(xPos, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos, yPos, 1, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 8, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 1, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 1, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 8, 1, "curved-rail"),

                                new LookUp(xPos, yPos + 8, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos - 12, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 12, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 7, "curved-rail"),

                                new LookUp(xPos + 4, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 12, 4, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 4, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 4, "curved-rail")
                        };
                        break;
                    case 7: // '--
                        neighbours = new LookUp[]{
                                new LookUp(xPos - 6, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2, 2, "straight-rail"),

                                new LookUp(xPos - 6, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 6, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2, 0, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 5, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2, 5, "straight-rail"),
                                new LookUp(xPos + 10, yPos + 2, 5, "straight-rail"),

                                new LookUp(xPos - 6, yPos - 2, 1, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 1, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2, 1, "straight-rail"),

                                new LookUp(xPos + 2, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 6, 3, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 2, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 3, "straight-rail"),

                                new LookUp(xPos - 6, yPos - 2, 7, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 7, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2, 7, "straight-rail"),

                                new LookUp(xPos + 8, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 12, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 12, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 6, "curved-rail"),

                                new LookUp(xPos, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 4, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 8, 4, "curved-rail"),

                                new LookUp(xPos, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos, yPos, 1, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 1, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 1, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 12, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 1, "curved-rail"),

                                new LookUp(xPos - 8, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 12, 0, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 0, "curved-rail"),

                                new LookUp(xPos - 8, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 12, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 12, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 2, "curved-rail"),

                                new LookUp(xPos - 12, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 7, "curved-rail"),

                                new LookUp(xPos, yPos - 8, 5, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 5, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4 - 8, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 5, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 5, "curved-rail"),

                                new LookUp(xPos, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 12, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 8, 3, "curved-rail")
                        };
                        break;
                }
                break;
        }
        return neighbours;
    }

    public static LookUp[] lookUpSignal(Track track) {
        return lookUpSignal(new Entity(-1, track.getName(), track.getPosition(), track.getDirection()));
    }

    public static LookUp[] lookUpSignal(Entity entry) {
        LookUp[] signals = {};
        int xPos = (int) entry.getPosition().getX();
        int yPos = (int) entry.getPosition().getY();
        switch (entry.getName()) {
            case "straight-rail":
                switch (entry.getDirection()) {
                    case 0: // |
                        signals = new LookUp[]{
                                new LookUp(xPos - 3, yPos - 1, 0, "rail-signal"),
                                new LookUp(xPos - 3, yPos + 1, 0, "rail-signal"),
                                new LookUp(xPos + 3, yPos - 1, 4, "rail-signal"),
                                new LookUp(xPos + 3, yPos + 1, 4, "rail-signal"),

                                new LookUp(xPos - 3, yPos - 1, 0, "rail-chain-signal"),
                                new LookUp(xPos - 3, yPos + 1, 0, "rail-chain-signal"),
                                new LookUp(xPos + 3, yPos - 1, 4, "rail-chain-signal"),
                                new LookUp(xPos + 3, yPos + 1, 4, "rail-chain-signal")
                        };
                        break;
                    case 2: // -
                        signals = new LookUp[]{
                                new LookUp(xPos - 1, yPos - 3, 2, "rail-signal"),
                                new LookUp(xPos + 1, yPos - 3, 2, "rail-signal"),
                                new LookUp(xPos - 1, yPos + 3, 6, "rail-signal"),
                                new LookUp(xPos + 1, yPos + 3, 6, "rail-signal"),

                                new LookUp(xPos - 1, yPos - 3, 2, "rail-chain-signal"),
                                new LookUp(xPos + 1, yPos - 3, 2, "rail-chain-signal"),
                                new LookUp(xPos - 1, yPos + 3, 6, "rail-chain-signal"),
                                new LookUp(xPos + 1, yPos + 3, 6, "rail-chain-signal")
                        };
                        break;
                    case 5: // \
                        signals = new LookUp[]{
                                new LookUp(xPos + 1, yPos - 1, 3, "rail-signal"),
                                new LookUp(xPos - 3, yPos + 3, 7, "rail-signal"),

                                new LookUp(xPos + 1, yPos - 1, 3, "rail-chain-signal"),
                                new LookUp(xPos - 3, yPos + 3, 7, "rail-chain-signal")
                        };
                        break;
                    case 1: // \
                        signals = new LookUp[]{
                                new LookUp(xPos - 1, yPos + 1, 3, "rail-signal"),
                                new LookUp(xPos + 3, yPos - 3, 7, "rail-signal"),

                                new LookUp(xPos - 1, yPos + 1, 3, "rail-chain-signal"),
                                new LookUp(xPos + 3, yPos - 3, 7, "rail-chain-signal")
                        };
                        break;
                    case 3: // /
                        signals = new LookUp[]{
                                new LookUp(xPos - 1, yPos - 1, 1, "rail-signal"),
                                new LookUp(xPos + 3, yPos + 3, 5, "rail-signal"),

                                new LookUp(xPos - 1, yPos - 1, 1, "rail-chain-signal"),
                                new LookUp(xPos + 3, yPos + 3, 5, "rail-chain-signal")
                        };
                        break;
                    case 7: // /
                        signals = new LookUp[]{
                                new LookUp(xPos - 3, yPos - 3, 1, "rail-signal"),
                                new LookUp(xPos + 1, yPos + 1, 5, "rail-signal"),

                                new LookUp(xPos - 3, yPos - 3, 1, "rail-chain-signal"),
                                new LookUp(xPos + 1, yPos + 1, 5, "rail-chain-signal")
                        };
                        break;
                }
                break;
            case "curved-rail":
                switch (entry.getDirection()) {
                    case 0: // '|
                        signals = new LookUp[]{
                                new LookUp(xPos - 1, yPos - 7, 3, "rail-signal"),
                                new LookUp(xPos - 5, yPos - 3, 7, "rail-signal"),
                                new LookUp(xPos - 1, yPos + 7, 0, "rail-signal"),
                                new LookUp(xPos + 5, yPos + 7, 4, "rail-signal"),

                                new LookUp(xPos - 1, yPos - 7, 3, "rail-chain-signal"),
                                new LookUp(xPos - 5, yPos - 3, 7, "rail-chain-signal"),
                                new LookUp(xPos - 1, yPos + 7, 0, "rail-chain-signal"),
                                new LookUp(xPos + 5, yPos + 7, 4, "rail-chain-signal")
                        };
                        break;
                    case 1: // |'
                        signals = new LookUp[]{
                                new LookUp(xPos + 1, yPos - 7, 1, "rail-signal"),
                                new LookUp(xPos + 5, yPos - 3, 5, "rail-signal"),
                                new LookUp(xPos - 5, yPos + 7, 0, "rail-signal"),
                                new LookUp(xPos + 1, yPos + 7, 4, "rail-signal"),

                                new LookUp(xPos + 1, yPos - 7, 1, "rail-chain-signal"),
                                new LookUp(xPos + 5, yPos - 3, 5, "rail-chain-signal"),
                                new LookUp(xPos - 5, yPos + 7, 0, "rail-chain-signal"),
                                new LookUp(xPos + 1, yPos + 7, 4, "rail-chain-signal")
                        };
                        break;
                    case 2: // --'
                        signals = new LookUp[]{
                                new LookUp(xPos - 7, yPos - 1, 2, "rail-signal"),
                                new LookUp(xPos - 7, yPos + 5, 6, "rail-signal"),
                                new LookUp(xPos + 3, yPos - 5, 1, "rail-signal"),
                                new LookUp(xPos + 7, yPos - 1, 5, "rail-signal"),

                                new LookUp(xPos - 7, yPos - 1, 2, "rail-chain-signal"),
                                new LookUp(xPos - 7, yPos + 5, 6, "rail-chain-signal"),
                                new LookUp(xPos + 3, yPos - 5, 1, "rail-chain-signal"),
                                new LookUp(xPos + 7, yPos - 1, 5, "rail-chain-signal")
                        };
                        break;
                    case 3: // --,
                        signals = new LookUp[]{
                                new LookUp(xPos - 7, yPos - 5, 2, "rail-signal"),
                                new LookUp(xPos - 7, yPos + 1, 6, "rail-signal"),
                                new LookUp(xPos + 7, yPos + 1, 3, "rail-signal"),
                                new LookUp(xPos + 3, yPos + 5, 7, "rail-signal"),

                                new LookUp(xPos - 7, yPos - 5, 2, "rail-chain-signal"),
                                new LookUp(xPos - 7, yPos + 1, 6, "rail-chain-signal"),
                                new LookUp(xPos + 7, yPos + 1, 3, "rail-chain-signal"),
                                new LookUp(xPos + 3, yPos + 5, 7, "rail-chain-signal")
                        };
                        break;
                    case 4: // |,
                        signals = new LookUp[]{
                                new LookUp(xPos - 5, yPos - 7, 0, "rail-signal"),
                                new LookUp(xPos + 1, yPos - 7, 4, "rail-signal"),
                                new LookUp(xPos + 5, yPos + 3, 3, "rail-signal"),
                                new LookUp(xPos + 1, yPos + 7, 7, "rail-signal"),

                                new LookUp(xPos - 5, yPos - 7, 0, "rail-chain-signal"),
                                new LookUp(xPos + 1, yPos - 7, 4, "rail-chain-signal"),
                                new LookUp(xPos + 5, yPos + 3, 3, "rail-chain-signal"),
                                new LookUp(xPos + 1, yPos + 7, 7, "rail-chain-signal")
                        };
                        break;
                    case 5: // ,|
                        signals = new LookUp[]{
                                new LookUp(xPos - 1, yPos - 7, 0, "rail-signal"),
                                new LookUp(xPos + 5, yPos - 7, 4, "rail-signal"),
                                new LookUp(xPos - 5, yPos + 3, 1, "rail-signal"),
                                new LookUp(xPos - 1, yPos + 7, 5, "rail-signal"),

                                new LookUp(xPos - 1, yPos - 7, 0, "rail-chain-signal"),
                                new LookUp(xPos + 5, yPos - 7, 4, "rail-chain-signal"),
                                new LookUp(xPos - 5, yPos + 3, 1, "rail-chain-signal"),
                                new LookUp(xPos - 1, yPos + 7, 5, "rail-chain-signal")
                        };
                        break;
                    case 6: // ,--
                        signals = new LookUp[]{
                                new LookUp(xPos + 7, yPos - 5, 2, "rail-signal"),
                                new LookUp(xPos + 7, yPos + 1, 6, "rail-signal"),
                                new LookUp(xPos - 7, yPos + 1, 1, "rail-signal"),
                                new LookUp(xPos - 3, yPos + 5, 5, "rail-signal"),

                                new LookUp(xPos + 7, yPos - 5, 2, "rail-chain-signal"),
                                new LookUp(xPos + 7, yPos + 1, 6, "rail-chain-signal"),
                                new LookUp(xPos - 7, yPos + 1, 1, "rail-chain-signal"),
                                new LookUp(xPos - 3, yPos + 5, 5, "rail-chain-signal")
                        };
                        break;
                    case 7: // '--
                        signals = new LookUp[]{
                                new LookUp(xPos + 7, yPos - 1, 2, "rail-signal"),
                                new LookUp(xPos + 7, yPos + 5, 6, "rail-signal"),
                                new LookUp(xPos - 3, yPos - 5, 3, "rail-signal"),
                                new LookUp(xPos - 7, yPos - 1, 7, "rail-signal"),

                                new LookUp(xPos + 7, yPos - 1, 2, "rail-chain-signal"),
                                new LookUp(xPos + 7, yPos + 5, 6, "rail-chain-signal"),
                                new LookUp(xPos - 3, yPos - 5, 3, "rail-chain-signal"),
                                new LookUp(xPos - 7, yPos - 1, 7, "rail-chain-signal")
                        };
                        break;
                }
                break;
        }
        return signals;
    }

    public static LookUp[] lookUpOutgoingTracks(Entity signal, Track track) {
        LookUp[] outgoingTracks = {};
        int xPos = (int) signal.getPosition().getX();
        int yPos = (int) signal.getPosition().getY();
        if (signal.getName().equals("rail-signal") || signal.getName().equals("rail-chain-signal")) {
            switch (signal.getDirection()) {
                case 0:
                    outgoingTracks = new LookUp[]{
                            new LookUp(xPos + 3, yPos + 3, 0, "straight-rail"),
                            new LookUp(xPos + 1, yPos + 9, 5, "curved-rail"),
                            new LookUp(xPos + 5, yPos + 9, 4, "curved-rail"),
                    };
                    break;
                case 6:
                    outgoingTracks = new LookUp[]{
                            new LookUp(xPos + 3, yPos - 3, 2, "straight-rail"),
                            new LookUp(xPos + 9, yPos - 5, 2, "curved-rail"),
                            new LookUp(xPos + 9, yPos - 1, 3, "curved-rail"),
                    };
                    break;
                case 2:
                    outgoingTracks = new LookUp[]{
                            new LookUp(xPos - 3, yPos + 3, 2, "straight-rail"),
                            new LookUp(xPos - 9, yPos + 1, 7, "curved-rail"),
                            new LookUp(xPos - 9, yPos + 5, 6, "curved-rail"),
                    };
                    break;
                case 4:
                    outgoingTracks = new LookUp[]{
                            new LookUp(xPos - 3, yPos - 3, 0, "straight-rail"),
                            new LookUp(xPos - 5, yPos - 9, 0, "curved-rail"),
                            new LookUp(xPos - 1, yPos - 9, 1, "curved-rail"),
                    };
                    break;
                case 7:
                    //for whatever reason, if the track is curved the striaght rail with dir 1 is outgoing
                    if (track.getName().equals("curved-rail")) {
                        outgoingTracks = new LookUp[]{
                                new LookUp(xPos + 7, yPos + 5, 0, "curved-rail"),
                                new LookUp(xPos + 9, yPos + 3, 7, "curved-rail"),
                                new LookUp(xPos + 3, yPos + 1, 1, "straight-rail"),
                        };
                    } else {
                        outgoingTracks = new LookUp[]{
                                new LookUp(xPos + 7, yPos + 5, 0, "curved-rail"),
                                new LookUp(xPos + 9, yPos + 3, 7, "curved-rail"),
                                new LookUp(xPos + 5, yPos - 1, 5, "straight-rail"),
                        };
                    }
                    break;
                case 5:
                    outgoingTracks = new LookUp[]{
                            new LookUp(xPos + 5, yPos - 7, 6, "curved-rail"),
                            new LookUp(xPos + 3, yPos - 9, 5, "curved-rail"),
                            new LookUp(xPos - 1, yPos - 5, 3, "straight-rail"),
                    };
                    break;
                case 1:
                    outgoingTracks = new LookUp[]{
                            new LookUp(xPos - 5, yPos + 7, 2, "curved-rail"),
                            new LookUp(xPos - 3, yPos + 9, 1, "curved-rail"),
                            new LookUp(xPos + 1, yPos + 5, 7, "straight-rail"),
                    };
                    break;
                case 3:
                    outgoingTracks = new LookUp[]{
                            new LookUp(xPos - 7, yPos - 5, 4, "curved-rail"),
                            new LookUp(xPos - 9, yPos - 3, 3, "curved-rail"),
                            new LookUp(xPos - 5, yPos + 1, 1, "straight-rail"),
                    };
                    break;
            }
        }
        return outgoingTracks;
    }

    // Rotates always clockwise; we read the corresponding direction from the offset map with the given degrees and calc the new offsets
    private static HashMap<Integer, Vector<Integer>> rotateOffsets(HashMap<HashMap<Integer, Integer>, Vector<Integer>> offset, int degrees) {
        degrees = degrees % 360;
        HashMap<Integer, Vector<Integer>> rotatedOffsets = new HashMap<>();

        for (HashMap<Integer, Integer> key : offset.keySet()) {
            Integer rotatedDirection = key.get(degrees);
            if (rotatedDirection != null) {
                rotatedOffsets.put(rotatedDirection, rotateVector(offset.get(key), degrees));
            }
        }

        return rotatedOffsets;
    }

    private static HashMap<Integer, Integer> rotateRotationMap(HashMap<Integer, Integer> rotationMap, int degrees) {
        HashMap<Integer, Integer> rotatedDirectionMap = new HashMap<>();
        for (Integer key : rotationMap.keySet()) {
            int newKey = (key + degrees) % 360;
            rotatedDirectionMap.put(newKey, rotationMap.get(key));
        }
        return rotatedDirectionMap;
    }

    private static Vector<Integer> rotateVector(Vector<Integer> vector, int degrees) {
        degrees = degrees % 360;
        int x = vector.get(0);
        int y = vector.get(1);

        switch (degrees) {
            case 0:
                return vector;
            case 45:
                return new Vector<>(Arrays.asList(-x, -x));
            case 90:
                return new Vector<>(Arrays.asList(y, -x));
            case 135:
                return new Vector<>(Arrays.asList(x, -x));
            case 180:
                return new Vector<>(Arrays.asList(-x, -y));
            case 225:
                return new Vector<>(Arrays.asList(x, x));
            case 270:
                return new Vector<>(Arrays.asList(-y, x));
            case 315:
                return new Vector<>(Arrays.asList(-x, x));
            default:
                throw new IllegalArgumentException("Degrees must be 0, 45, 90, 135, 180, 225, 270, or 315");
        }
    }

    private static LookUp[] generateLookUps (HashMap<Integer, Vector<Integer>> offset, int xPos, int yPos, String trackType) {
        LookUp[] result = new LookUp[offset.size()];
        int counter = 0;
        for (int key : offset.keySet()) {
            int x = offset.get(key).get(0);
            int y = offset.get(key).get(1);
            result[counter] = new LookUp(xPos + x, yPos + y, key, trackType);
            counter++;
        }
        return result;
    }
}
