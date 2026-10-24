package factorio.train.analyser.rail;

/**
 * Intersection tests on the polylines that {@link RailGeometry#centreLine}
 * produces.
 *
 * <p>Two rails cross when their centre lines meet somewhere that is not the
 * joint they share. A point that is an end of both polylines is a junction -
 * rails running into each other, or diverging from the same spot - and not a
 * crossing. Anything else is: one rail passing through the middle of another,
 * or ending part-way along it.
 */
public final class Polylines {

    /**
     * How close a meeting point has to be to an end to count as that end.
     *
     * <p>Generous enough to absorb the few hundredths of a tile by which the
     * Bezier approximation of a curve misses Factorio's real shape, so two
     * curves joined at a shared end are never mistaken for crossing there, and
     * still far below the distance at which rails actually cross.
     */
    private static final double JOINT_TOLERANCE = 0.15;

    private Polylines() {
    }

    /** Whether the two centre lines cross anywhere other than at a shared joint. */
    public static boolean cross(double[][] first, double[][] second) {
        if (!boundsOverlap(first, second)) {
            return false;
        }
        for (int i = 0; i + 1 < first.length; i++) {
            for (int j = 0; j + 1 < second.length; j++) {
                double[] meeting = segmentIntersection(
                        first[i], first[i + 1], second[j], second[j + 1]);
                if (meeting == null) {
                    continue;
                }
                boolean atEndOfFirst = isEndOf(first, meeting);
                boolean atEndOfSecond = isEndOf(second, meeting);
                if (!atEndOfFirst || !atEndOfSecond) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Axis aligned bounds of a polyline as {minX, minY, maxX, maxY}. */
    public static double[] bounds(double[][] line) {
        double minX = line[0][0];
        double minY = line[0][1];
        double maxX = minX;
        double maxY = minY;
        for (double[] point : line) {
            minX = Math.min(minX, point[0]);
            minY = Math.min(minY, point[1]);
            maxX = Math.max(maxX, point[0]);
            maxY = Math.max(maxY, point[1]);
        }
        return new double[] { minX, minY, maxX, maxY };
    }

    private static boolean boundsOverlap(double[][] first, double[][] second) {
        double[] a = bounds(first);
        double[] b = bounds(second);
        return a[0] <= b[2] + JOINT_TOLERANCE && b[0] <= a[2] + JOINT_TOLERANCE
                && a[1] <= b[3] + JOINT_TOLERANCE && b[1] <= a[3] + JOINT_TOLERANCE;
    }

    private static boolean isEndOf(double[][] line, double[] point) {
        return distance(line[0], point) <= JOINT_TOLERANCE
                || distance(line[line.length - 1], point) <= JOINT_TOLERANCE;
    }

    private static double distance(double[] a, double[] b) {
        return Math.hypot(a[0] - b[0], a[1] - b[1]);
    }

    /**
     * Where two line segments meet, or null if they do not. Segments that are
     * parallel report nothing: rails never lie on top of each other, so a
     * collinear overlap would be a malformed blueprint rather than a crossing.
     */
    private static double[] segmentIntersection(double[] a1, double[] a2, double[] b1, double[] b2) {
        double aX = a2[0] - a1[0];
        double aY = a2[1] - a1[1];
        double bX = b2[0] - b1[0];
        double bY = b2[1] - b1[1];
        double denominator = aX * bY - aY * bX;
        if (Math.abs(denominator) < 1e-12) {
            return null;
        }
        double offsetX = b1[0] - a1[0];
        double offsetY = b1[1] - a1[1];
        double alongFirst = (offsetX * bY - offsetY * bX) / denominator;
        double alongSecond = (offsetX * aY - offsetY * aX) / denominator;
        if (alongFirst < 0 || alongFirst > 1 || alongSecond < 0 || alongSecond > 1) {
            return null;
        }
        return new double[] { a1[0] + alongFirst * aX, a1[1] + alongFirst * aY };
    }
}
