package factorio.train.analyser.rail;

/**
 * One of the two ends of a rail, in absolute blueprint coordinates. The heading
 * is the direction pointing out of the rail at this end, so two rails meet
 * exactly when one end sits on top of another with opposing headings.
 */
public record RailEnd(double x, double y, int heading) {

    /** Positions are integral in Factorio, so this only has to absorb float noise. */
    private static final double EPSILON = 1e-6;

    /** Whether a train can run straight from this end into the given one. */
    public boolean meets(RailEnd other) {
        return heading == Direction.opposite(other.heading) && isAt(other.x(), other.y());
    }

    /** Whether this end sits at the given position. */
    public boolean isAt(double otherX, double otherY) {
        return Math.abs(x - otherX) < EPSILON && Math.abs(y - otherY) < EPSILON;
    }
}
