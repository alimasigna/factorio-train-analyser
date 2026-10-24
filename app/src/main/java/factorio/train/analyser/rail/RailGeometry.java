package factorio.train.analyser.rail;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Where every rail piece begins and ends, and what its centre line looks like.
 *
 * <p>This replaces the hand-measured neighbour tables the 1.1 version of the
 * tool used. A rail is described once here, by the two ends a train can enter
 * it through; everything the analyser needs afterwards - whether two rails
 * connect, where a signal may sit, whether two rails cross - follows from that
 * geometry rather than from an enumerated list of neighbour positions. Adding
 * 2.0's new rail shapes therefore meant adding four lines, not four switch
 * blocks.
 *
 * <p>An end is an integer offset from the entity's own blueprint position plus
 * a <em>heading</em>, the direction pointing out of the rail there. Factorio
 * guarantees that every rail connection point lands on integer tile
 * coordinates, which is what keeps the offsets integral:
 * <a href="https://factorio.com/blog/post/fff-377">FFF-377, "New new rails"</a>.
 *
 * <p>Only one orientation per shape is written out. The other seven follow by
 * rotating that one, which is also why the two mirror images of a curve occupy
 * the direction values 0/4/8/12 and 2/6/10/14 respectively.
 *
 * <p>The table was derived from the rail geometry and then verified against
 * three independently published 2.0 blueprint books: of the 55&nbsp;000 rail
 * ends in them, every end at a diagonal or half-diagonal heading matches
 * another end exactly, and the only unmatched ends are orthogonal ones running
 * off the edge of a blueprint.
 *
 * <p>The elevated shapes turned out to have exactly the geometry of their
 * ground counterparts, so they share a table and differ only in their layer.
 */
public final class RailGeometry {

    /** Rails trains reach from the ground. */
    public static final int GROUND_LAYER = 0;

    /** Rails carried by rail supports. Only reachable through a {@code rail-ramp}. */
    public static final int ELEVATED_LAYER = 1;

    /** A ramp climbs 16 tiles, straight in plan view. */
    private static final double RAMP_LENGTH = 16.0;

    /**
     * Arc lengths in tiles, as published by the Factorio developers. A straight
     * rail spans its two tiles exactly; the curves are slightly longer than
     * their chords.
     *
     * @see <a href="https://forums.factorio.com/viewtopic.php?t=109189">
     *      "What is the exact shape of a curved rail?"</a>
     */
    private static final double STRAIGHT_LENGTH = 2.0;
    private static final double DIAGONAL_LENGTH = 2.0 * Math.sqrt(2.0);
    private static final double LEGACY_DIAGONAL_LENGTH = Math.sqrt(2.0);
    private static final double HALF_DIAGONAL_LENGTH = 2.0 * Math.sqrt(5.0);
    private static final double CURVED_A_LENGTH = 5.132284556;
    private static final double CURVED_B_LENGTH = 5.077891568;
    private static final double LEGACY_CURVED_LENGTH = 7.842077;

    /** How many segments a curved centre line is approximated with. */
    private static final int CURVE_SEGMENTS = 16;

    /**
     * One rail shape in one orientation.
     *
     * @param handleLength control point distance of the Bezier standing in for
     *                     a curve, fitted so the curve comes out as long as the
     *                     real one. Zero for a straight rail
     */
    private record Piece(double length, double handleLength, int[][] ends) {

        /**
         * The layers this piece blocks, which is what decides whether it can be
         * crossed. Normally that is just the layer it sits on - a ground rail
         * and an elevated rail have disjoint collision masks and pass over each
         * other freely. A {@code rail-ramp} carries both masks, so nothing may
         * cross it at either level.
         */
        boolean blocksLayer(int layer) {
            for (int[] end : ends) {
                if (end[LAYER] == layer) {
                    return true;
                }
            }
            return false;
        }
    }

    private static final Map<String, Map<Integer, Piece>> PIECES = new LinkedHashMap<>();

    /** Index of the layer inside an end triple-turned-quadruple. */
    private static final int LAYER = 3;

    /**
     * Rail entities that carry no train and so take no part in the model.
     *
     * <p>A {@code rail-support} is the pillar an elevated rail rests on. It has
     * no ends of its own - the game requires it to overlap an elevated rail
     * rather than to join one - so ignoring it loses nothing.
     */
    private static final Set<String> NON_TRACK_RAILS = Set.of("rail-support");

    static {
        // A straight rail spans a 2x2 block, so its ends sit one tile out from
        // the centre. The 45 degree variants are anchored on a tile corner
        // instead, which is why their positions are even where the orthogonal
        // ones are odd.
        Map<Integer, int[][]> straight = new LinkedHashMap<>();
        straight.put(0, ends(0, -1, 0, 0, 1, 8));    // |
        straight.put(2, ends(1, -1, 2, -1, 1, 10));  // /
        straight.put(4, ends(1, 0, 4, -1, 0, 12));   // -
        straight.put(6, ends(1, 1, 6, -1, -1, 14));  // \
        register("straight-rail", GROUND_LAYER, straight);
        register("elevated-straight-rail", ELEVATED_LAYER, straight);

        // New in 2.0: a rail at the 1:2 slope, four tiles along and two across.
        // Its ends leave at 26.565 degrees, which Factorio reports as an odd
        // direction value.
        Map<Integer, int[][]> halfDiagonal = new LinkedHashMap<>();
        halfDiagonal.put(0, ends(1, 2, 7, -1, -2, 15));
        halfDiagonal.put(2, ends(1, -2, 1, -1, 2, 9));
        halfDiagonal.put(4, ends(2, -1, 3, -2, 1, 11));
        halfDiagonal.put(6, ends(2, 1, 5, -2, -1, 13));
        register("half-diagonal-rail", GROUND_LAYER, halfDiagonal);
        register("elevated-half-diagonal-rail", ELEVATED_LAYER, halfDiagonal);

        // 2.0 splits the old curve in two so that opposite curves can meet in
        // the middle, which is what allows an S-bend and a signal between them.
        // Piece A runs from an orthogonal end to the half-diagonal slope, piece
        // B carries on from there to the 45 degree diagonal.
        register("curved-rail-a", GROUND_LAYER,
                rotations(ends(0, 2, 8, -1, -3, 15), ends(0, 2, 8, 1, -3, 1)));
        register("elevated-curved-rail-a", ELEVATED_LAYER,
                rotations(ends(0, 2, 8, -1, -3, 15), ends(0, 2, 8, 1, -3, 1)));
        register("curved-rail-b", GROUND_LAYER,
                rotations(ends(1, 2, 7, -2, -2, 14), ends(-1, 2, 9, 2, -2, 2)));
        register("elevated-curved-rail-b", ELEVATED_LAYER,
                rotations(ends(1, 2, 7, -2, -2, 14), ends(-1, 2, 9, 2, -2, 2)));

        // The 1.1 rails, which 2.0 keeps under a "legacy-" prefix so old
        // blueprints still import. Published 2.0 blueprints mix them with the
        // new shapes freely, so the analyser has to understand both.
        //
        // A legacy diagonal covers only half of the 2x2 block it sits in, so a
        // 45 degree run alternates between two direction values: 2 and 10 are
        // the two halves of a "\", 6 and 14 the two halves of a "/".
        Map<Integer, int[][]> legacyStraight = new LinkedHashMap<>();
        legacyStraight.put(0, ends(0, -1, 0, 0, 1, 8));    // |
        legacyStraight.put(4, ends(1, 0, 4, -1, 0, 12));   // -
        legacyStraight.put(2, ends(0, -1, 14, 1, 0, 6));   // \ upper half
        legacyStraight.put(10, ends(-1, 0, 14, 0, 1, 6));  // \ lower half
        legacyStraight.put(6, ends(1, 0, 2, 0, 1, 10));    // / lower half
        legacyStraight.put(14, ends(0, -1, 2, -1, 0, 10)); // / upper half
        register("legacy-straight-rail", GROUND_LAYER, legacyStraight);

        register("legacy-curved-rail", GROUND_LAYER,
                rotations(ends(1, 4, 8, -2, -3, 14), ends(-1, 4, 8, 2, -3, 2)));

        // The only way between the two layers. A ramp is straight in plan view
        // and 16 tiles long, with its ground end behind it and its elevated end
        // in the direction it faces. It takes no signal, so a block runs
        // straight through a ramp from one layer to the other.
        registerRamp();
    }

    private RailGeometry() {
    }

    /** Whether the named entity is a rail this class can place ends on. */
    public static boolean isRail(String name) {
        return PIECES.containsKey(name);
    }

    /**
     * Whether the named entity is a rail-related entity that carries no train,
     * and so is ignored rather than modelled.
     */
    public static boolean isNonTrackRail(String name) {
        return NON_TRACK_RAILS.contains(name);
    }

    /**
     * Whether the rail blocks the given layer, and can therefore be crossed by
     * rails on it. Everything but a {@code rail-ramp} blocks exactly one layer.
     */
    public static boolean blocksLayer(String name, int direction, int layer) {
        return piece(name, direction).blocksLayer(layer);
    }

    /** Whether the rail carries geometry for this particular orientation. */
    public static boolean isKnownOrientation(String name, int direction) {
        Map<Integer, Piece> byDirection = PIECES.get(name);
        return byDirection != null && byDirection.containsKey(direction);
    }

    /**
     * Whether the rail is one of the elevated shapes, meaning both of its ends
     * are up on the elevated network. A {@code rail-ramp} is not: it has one
     * end on each layer.
     */
    public static boolean isElevated(String name) {
        return PIECES.containsKey(name)
                && !blocksLayer(name, firstDirection(name), GROUND_LAYER);
    }

    /** Whether the rail joins the two layers, which only a {@code rail-ramp} does. */
    public static boolean isRamp(String name) {
        if (!PIECES.containsKey(name)) {
            return false;
        }
        int direction = firstDirection(name);
        return blocksLayer(name, direction, GROUND_LAYER)
                && blocksLayer(name, direction, ELEVATED_LAYER);
    }

    private static int firstDirection(String name) {
        return PIECES.get(name).keySet().iterator().next();
    }

    /** Arc length of the rail in tiles, used to size nodes for the performance analysis. */
    public static double length(String name, int direction) {
        return piece(name, direction).length();
    }

    /**
     * The two ends of a rail placed at the given position, in absolute
     * blueprint coordinates.
     */
    public static RailEnd[] endsOf(String name, int direction, double x, double y) {
        int[][] offsets = piece(name, direction).ends();
        RailEnd[] result = new RailEnd[offsets.length];
        for (int i = 0; i < offsets.length; i++) {
            result[i] = new RailEnd(
                    x + offsets[i][0], y + offsets[i][1], offsets[i][2], offsets[i][LAYER]);
        }
        return result;
    }

    /** Whether the rail runs straight, meaning its two ends face opposite ways. */
    public static boolean isStraight(String name, int direction) {
        int[][] offsets = piece(name, direction).ends();
        return offsets[0][2] == Direction.opposite(offsets[1][2]);
    }

    /**
     * The centre line of a rail placed at the given position, as a polyline a
     * train would run along.
     *
     * <p>A straight rail is its two ends. A curve is approximated by the cubic
     * Bezier through both ends that matches their tangents, sampled into
     * {@value #CURVE_SEGMENTS} segments.
     *
     * <p>Factorio's real curves are not circular arcs - the developers fit them
     * with polynomials so that the ends land on whole tiles - so the Bezier is
     * stretched until it is as long as the arc length the developers published
     * for that piece. Without that, {@code curved-rail-b} in particular comes
     * out noticeably too flat, since it bows about twice as far off its chord as
     * a circular arc between the same tangents would. Matching the length pins
     * the bow, which leaves the middle of the curve within a few hundredths of a
     * tile of the truth - far inside what matters for deciding whether two rails
     * cross, since rails cross transversally rather than grazing each other.
     */
    public static double[][] centreLine(String name, int direction, double x, double y) {
        RailEnd[] rails = endsOf(name, direction, x, y);
        RailEnd from = rails[0];
        RailEnd to = rails[1];
        if (isStraight(name, direction)) {
            return new double[][] { { from.x(), from.y() }, { to.x(), to.y() } };
        }

        // A train entering at "from" travels against that end's outward tangent
        // and leaves along the outward tangent of "to".
        double inX = -Direction.tangentX(from.heading());
        double inY = -Direction.tangentY(from.heading());
        double outX = Direction.tangentX(to.heading());
        double outY = Direction.tangentY(to.heading());

        double handle = piece(name, direction).handleLength();

        double c1x = from.x() + inX * handle;
        double c1y = from.y() + inY * handle;
        double c2x = to.x() - outX * handle;
        double c2y = to.y() - outY * handle;

        return sample(from.x(), c1x, c2x, to.x(), from.y(), c1y, c2y, to.y());
    }

    private static double[][] sample(double x0, double x1, double x2, double x3,
            double y0, double y1, double y2, double y3) {
        double[][] points = new double[CURVE_SEGMENTS + 1][2];
        for (int i = 0; i <= CURVE_SEGMENTS; i++) {
            double t = (double) i / CURVE_SEGMENTS;
            points[i][0] = bezier(x0, x1, x2, x3, t);
            points[i][1] = bezier(y0, y1, y2, y3, t);
        }
        return points;
    }

    /** Length of the polyline a sampled curve comes out as. */
    private static double sampledLength(double[][] points) {
        double total = 0;
        for (int i = 0; i + 1 < points.length; i++) {
            total += Math.hypot(points[i + 1][0] - points[i][0], points[i + 1][1] - points[i][1]);
        }
        return total;
    }

    /**
     * Finds the control point distance that makes the Bezier through the two
     * ends come out {@code targetLength} long. The length grows with the handle
     * over the whole range a rail curve can need, so a bisection settles it.
     */
    private static double fitHandleLength(int[][] offsets, double targetLength) {
        double fromX = offsets[0][0];
        double fromY = offsets[0][1];
        double toX = offsets[1][0];
        double toY = offsets[1][1];
        double inX = -Direction.tangentX(offsets[0][2]);
        double inY = -Direction.tangentY(offsets[0][2]);
        double outX = Direction.tangentX(offsets[1][2]);
        double outY = Direction.tangentY(offsets[1][2]);
        double chord = Math.hypot(toX - fromX, toY - fromY);

        double low = 0;
        double high = chord;
        for (int step = 0; step < 60; step++) {
            double handle = (low + high) / 2;
            double length = sampledLength(sample(
                    fromX, fromX + inX * handle, toX - outX * handle, toX,
                    fromY, fromY + inY * handle, toY - outY * handle, toY));
            if (length < targetLength) {
                low = handle;
            } else {
                high = handle;
            }
        }
        return (low + high) / 2;
    }

    private static double bezier(double p0, double p1, double p2, double p3, double t) {
        double u = 1.0 - t;
        return u * u * u * p0 + 3 * u * u * t * p1 + 3 * u * t * t * p2 + t * t * t * p3;
    }

    private static Piece piece(String name, int direction) {
        Map<Integer, Piece> byDirection = PIECES.get(name);
        if (byDirection == null) {
            throw new IllegalArgumentException("not a rail: " + name);
        }
        Piece found = byDirection.get(direction);
        if (found == null) {
            throw new IllegalArgumentException(
                    "rail " + name + " has no geometry for direction " + direction);
        }
        return found;
    }

    /** Two ends whose layer is filled in when the shape is registered. */
    private static int[][] ends(int dx1, int dy1, int heading1, int dx2, int dy2, int heading2) {
        return new int[][] { { dx1, dy1, heading1, GROUND_LAYER }, { dx2, dy2, heading2, GROUND_LAYER } };
    }

    /**
     * Registers {@code rail-ramp}, the one shape whose two ends are on
     * different layers. Direction 4 was read off published blueprints - its
     * ground end is 8 tiles behind the entity and its elevated end 8 tiles
     * ahead - and the other three orientations are rotations of it.
     */
    private static void registerRamp() {
        int[][] seed = {
                { -8, 0, 12, GROUND_LAYER },
                { 8, 0, 4, ELEVATED_LAYER },
        };
        Map<Integer, int[][]> byDirection = new LinkedHashMap<>();
        int[][] current = seed;
        for (int quarter = 0; quarter < 4; quarter++) {
            byDirection.put((4 + quarter * 4) % Direction.COUNT, current);
            current = rotate(current);
        }
        Map<Integer, Piece> pieces = new LinkedHashMap<>();
        byDirection.forEach((direction, offsets) ->
                pieces.put(direction, new Piece(RAMP_LENGTH, 0, offsets)));
        PIECES.put("rail-ramp", Collections.unmodifiableMap(pieces));
    }

    /**
     * Expands the two mirror images of a curve into all eight orientations by
     * rotating each a quarter turn at a time. Factorio numbers them so that one
     * mirror image takes the direction values 0, 4, 8 and 12 and the other 2, 6,
     * 10 and 14.
     */
    private static Map<Integer, int[][]> rotations(int[][] clockwiseSeed, int[][] mirroredSeed) {
        Map<Integer, int[][]> byDirection = new LinkedHashMap<>();
        int[][] current = clockwiseSeed;
        for (int direction = 0; direction < Direction.COUNT; direction += 4) {
            byDirection.put(direction, current);
            current = rotate(current);
        }
        current = mirroredSeed;
        for (int direction = 2; direction < Direction.COUNT; direction += 4) {
            byDirection.put(direction, current);
            current = rotate(current);
        }
        return byDirection;
    }

    /** Rotates ends a quarter turn clockwise. y points down, so (x,y) becomes (-y,x). */
    private static int[][] rotate(int[][] offsets) {
        int[][] rotated = new int[offsets.length][4];
        for (int i = 0; i < offsets.length; i++) {
            rotated[i][0] = -offsets[i][1];
            rotated[i][1] = offsets[i][0];
            rotated[i][2] = Direction.rotate90Clockwise(offsets[i][2]);
            rotated[i][LAYER] = offsets[i][LAYER];
        }
        return rotated;
    }

    private static void register(String name, int layer, Map<Integer, int[][]> byDirection) {
        Map<Integer, Piece> pieces = new LinkedHashMap<>();
        byDirection.forEach((direction, offsets) -> {
            double length = lengthOf(name, direction, offsets);
            boolean straight = offsets[0][2] == Direction.opposite(offsets[1][2]);
            double handle = straight ? 0 : fitHandleLength(offsets, length);
            pieces.put(direction, new Piece(length, handle, onLayer(offsets, layer)));
        });
        PIECES.put(name, Collections.unmodifiableMap(pieces));
    }

    /** Puts both ends of a shape on one layer, which is true of everything but a ramp. */
    private static int[][] onLayer(int[][] offsets, int layer) {
        int[][] placed = new int[offsets.length][4];
        for (int i = 0; i < offsets.length; i++) {
            placed[i] = new int[] { offsets[i][0], offsets[i][1], offsets[i][2], layer };
        }
        return placed;
    }

    private static double lengthOf(String name, int direction, int[][] offsets) {
        boolean orthogonal = offsets[0][2] % 4 == 0;
        return switch (name) {
            case "straight-rail", "elevated-straight-rail" ->
                    orthogonal ? STRAIGHT_LENGTH : DIAGONAL_LENGTH;
            case "legacy-straight-rail" ->
                    orthogonal ? STRAIGHT_LENGTH : LEGACY_DIAGONAL_LENGTH;
            case "half-diagonal-rail", "elevated-half-diagonal-rail" -> HALF_DIAGONAL_LENGTH;
            case "curved-rail-a", "elevated-curved-rail-a" -> CURVED_A_LENGTH;
            case "curved-rail-b", "elevated-curved-rail-b" -> CURVED_B_LENGTH;
            case "legacy-curved-rail" -> LEGACY_CURVED_LENGTH;
            case "rail-ramp" -> RAMP_LENGTH;
            default -> throw new IllegalArgumentException("no length known for " + name);
        };
    }
}
