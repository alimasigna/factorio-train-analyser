package factorio.train.analyser.rail;

/**
 * Factorio's 16-value direction enum (defines.direction), which blueprints use
 * since the 2.0 update. Direction 0 is north and the values advance clockwise
 * in steps of 22.5 degrees, so 4 is east, 8 south and 12 west. Blueprints from
 * 1.1 used an 8-value enum, which is why every direction in an old blueprint is
 * exactly half of its 2.0 counterpart.
 *
 * <p>Rail entities only ever carry an even direction. The odd values do turn up
 * as rail <em>headings</em> though: a half-diagonal rail leaves its end at the
 * 1:2 slope, which is 26.565 degrees rather than the nominal 22.5, and Factorio
 * rounds that to the nearest direction value. {@link #tangentX} and
 * {@link #tangentY} therefore return the exact slope a rail has at a heading,
 * not the nominal angle of the direction value.
 */
public final class Direction {

    /** Number of distinct direction values. */
    public static final int COUNT = 16;

    private static final double R2 = Math.sqrt(2.0);
    private static final double R5 = Math.sqrt(5.0);

    /**
     * Exact outward rail tangents, indexed by heading. Even headings are the
     * true orthogonal and 45 degree grid directions; odd ones are the 1:2
     * half-diagonal slopes.
     */
    private static final double[][] TANGENTS = {
            { 0, -1 },            // 0  north
            { 1 / R5, -2 / R5 },  // 1  north-northeast, 1:2 slope
            { 1 / R2, -1 / R2 },  // 2  northeast
            { 2 / R5, -1 / R5 },  // 3  east-northeast, 2:1 slope
            { 1, 0 },             // 4  east
            { 2 / R5, 1 / R5 },   // 5  east-southeast
            { 1 / R2, 1 / R2 },   // 6  southeast
            { 1 / R5, 2 / R5 },   // 7  south-southeast
            { 0, 1 },             // 8  south
            { -1 / R5, 2 / R5 },  // 9  south-southwest
            { -1 / R2, 1 / R2 },  // 10 southwest
            { -2 / R5, 1 / R5 },  // 11 west-southwest
            { -1, 0 },            // 12 west
            { -2 / R5, -1 / R5 }, // 13 west-northwest
            { -1 / R2, -1 / R2 }, // 14 northwest
            { -1 / R5, -2 / R5 }, // 15 north-northwest
    };

    private Direction() {
    }

    /** The direction facing the other way. */
    public static int opposite(int direction) {
        return (direction + 8) & 15;
    }

    /** The direction a quarter turn clockwise, matching a clockwise rail rotation. */
    public static int rotate90Clockwise(int direction) {
        return (direction + 4) & 15;
    }

    /** The direction mirrored along the x axis, so north stays north and east becomes west. */
    public static int mirrorAlongX(int direction) {
        return (COUNT - direction) & 15;
    }

    /** x component of the exact unit tangent of a rail pointing out of the given heading. */
    public static double tangentX(int heading) {
        return TANGENTS[heading][0];
    }

    /** y component of the exact unit tangent of a rail pointing out of the given heading. */
    public static double tangentY(int heading) {
        return TANGENTS[heading][1];
    }
}
