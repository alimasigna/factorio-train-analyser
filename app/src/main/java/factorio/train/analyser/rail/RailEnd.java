package factorio.train.analyser.rail;

/**
 * One of the two ends of a rail, in absolute blueprint coordinates.
 *
 * <p>The heading is the direction pointing out of the rail at this end, and the
 * layer says whether that end is at ground level or up on the elevated
 * network. Two rails meet when an end of one sits on an end of the other, on
 * the same layer, with opposing headings.
 *
 * <p>The layer belongs to the end rather than to the rail because of
 * {@code rail-ramp}: it has one end on each layer, and is the only way a train
 * gets between them.
 */
public record RailEnd(double x, double y, int heading, int layer) {

    /** Positions are integral in Factorio, so this only has to absorb float noise. */
    private static final double EPSILON = 1e-6;

    /** Whether a train can run straight from this end into the given one. */
    public boolean meets(RailEnd other) {
        return layer == other.layer()
                && heading == Direction.opposite(other.heading())
                && isAt(other.x(), other.y());
    }

    /** Whether this end sits at the given position. */
    public boolean isAt(double otherX, double otherY) {
        return Math.abs(x - otherX) < EPSILON && Math.abs(y - otherY) < EPSILON;
    }
}
