package factorio.train.analyser.graph;

import factorio.train.analyser.jsonmodels.Entity;
import factorio.train.analyser.rail.Direction;
import factorio.train.analyser.rail.Polylines;
import factorio.train.analyser.rail.RailEnd;
import factorio.train.analyser.rail.RailGeometry;
import factorio.train.analyser.rail.SignalSpots;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The rails of a blueprint, indexed so the three questions the graph
 * generation asks can be answered geometrically.
 *
 * <ul>
 * <li><b>Which tracks continue from here?</b> Two rails connect when an end of
 * one sits on an end of the other with the opposing heading. Factorio puts
 * every rail connection point on an integer tile, so this is an exact lookup
 * rather than a search through candidate positions.</li>
 * <li><b>Which signals sit on this track, and which way do they let trains
 * run?</b> A signal's legal positions come from {@link SignalSpots}; the end it
 * guards and the direction of travel it permits follow from its own
 * direction.</li>
 * <li><b>Which tracks cross this one?</b> Decided by intersecting centre lines,
 * which is what replaced the hand-written crossing table the 1.1 version of the
 * tool needed - by far the most laborious part of that implementation.</li>
 * </ul>
 */
public class RailNetwork {

    /** The two signal types that split a rail network into blocks. */
    private static final String RAIL_SIGNAL = "rail-signal";
    private static final String RAIL_CHAIN_SIGNAL = "rail-chain-signal";

    /**
     * A signal sitting beside a track, resolved against the track's geometry.
     *
     * @param signal   the signal entity itself
     * @param endIndex which of the track's two ends the signal stands at, and
     *                 therefore where the block boundary falls
     * @param outbound whether the signal lets trains leave the track through
     *                 that end. A signal faces the trains it governs, so a
     *                 signal facing out of the track governs traffic coming in,
     *                 and one facing into it governs traffic going out
     */
    public record SignalPlacement(Entity signal, int endIndex, boolean outbound) {

        /** Chain signals are what the deadlock analysis treats as protection. */
        public boolean isChainSignal() {
            return RAIL_CHAIN_SIGNAL.equals(signal.getName());
        }
    }

    private final List<Track> tracks = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    /** Rail ends by position, for resolving connections. */
    private final Map<Long, List<Track>> tracksByEnd = new HashMap<>();

    /** Tracks by the tiles their centre line passes over, for narrowing crossing candidates. */
    private final Map<Long, List<Track>> tracksByCell = new HashMap<>();

    /** Signals by position, for resolving the spots a track offers. */
    private final Map<Long, List<Entity>> signalsByPosition = new HashMap<>();

    private final Map<Track, List<List<Track>>> connectionCache = new IdentityHashMap<>();
    private final Map<Track, List<SignalPlacement>> signalCache = new IdentityHashMap<>();
    private final Map<Track, List<Track>> crossingCache = new IdentityHashMap<>();

    /**
     * Builds the network from the entities of a blueprint.
     *
     * @param entities      every entity of the blueprint; anything that is not
     *                      a rail or a signal is ignored
     * @param withElevated  whether to include the elevated rail layer. Elevated
     *                      networks are reachable only through a
     *                      {@code rail-ramp}, whose geometry is not measured
     *                      yet, so passing true today yields an elevated layer
     *                      that is disconnected from the ground one
     */
    public RailNetwork(List<Entity> entities, boolean withElevated) {
        Set<String> skipped = new LinkedHashSet<>();
        for (Entity entity : entities) {
            String name = entity.getName();
            if (isSignal(name)) {
                signalsByPosition
                        .computeIfAbsent(key(entity.getPosition().getX(), entity.getPosition().getY()),
                                unused -> new ArrayList<>())
                        .add(entity);
                continue;
            }
            if (RailGeometry.isUnmappedRail(name)) {
                skipped.add(name);
                continue;
            }
            if (!RailGeometry.isRail(name)) {
                continue;
            }
            if (!withElevated && RailGeometry.isElevated(name)) {
                skipped.add(name);
                continue;
            }
            if (!RailGeometry.isKnownOrientation(name, entity.getDirection())) {
                skipped.add(name + " at direction " + entity.getDirection());
                continue;
            }
            tracks.add(new Track(null, entity));
        }
        for (String name : skipped) {
            warnings.add("ignored rail entity: " + name);
        }
        index();
    }

    /** Every track of the blueprint, in blueprint order. */
    public List<Track> getTracks() {
        return Collections.unmodifiableList(tracks);
    }

    /** Rails that were recognised but left out of the model. */
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    /**
     * The tracks a train can run into when it leaves the given track through
     * the given end.
     *
     * @param endIndex index into {@link Track#getEnds()}
     */
    public List<Track> connectedAt(Track track, int endIndex) {
        return connections(track).get(endIndex);
    }

    /** Which of the track's ends the given neighbour is attached to, or -1 if it is not. */
    public int endTowards(Track track, Track neighbour) {
        List<List<Track>> byEnd = connections(track);
        for (int endIndex = 0; endIndex < byEnd.size(); endIndex++) {
            if (containsSame(byEnd.get(endIndex), neighbour)) {
                return endIndex;
            }
        }
        return -1;
    }

    /** Every signal standing beside the given track. */
    public List<SignalPlacement> signalsOn(Track track) {
        return signalCache.computeIfAbsent(track, this::resolveSignals);
    }

    /** Whether any signal stands beside the given track. */
    public boolean hasSignal(Track track) {
        return !signalsOn(track).isEmpty();
    }

    /**
     * Whether a signal on {@code owner} separates it from {@code other}, which
     * is to say the two tracks belong to different blocks.
     *
     * <p>This is the one question the 1.1 implementation could only answer with
     * distance thresholds and a list of special cases for curves meeting
     * diagonals. With the geometry known it is exact: a signal stands at one
     * particular end of its track, and the only track it separates the owner
     * from is the one attached to that end.
     */
    public boolean separatedBySignalOn(Track owner, Track other) {
        for (SignalPlacement placement : signalsOn(owner)) {
            if (containsSame(connectedAt(owner, placement.endIndex()), other)) {
                return true;
            }
        }
        return false;
    }

    /** The tracks whose centre line crosses the given track's. */
    public List<Track> crossing(Track track) {
        return crossingCache.computeIfAbsent(track, this::resolveCrossings);
    }

    private List<List<Track>> connections(Track track) {
        return connectionCache.computeIfAbsent(track, this::resolveConnections);
    }

    private List<List<Track>> resolveConnections(Track track) {
        List<List<Track>> byEnd = new ArrayList<>(track.getEnds().length);
        for (RailEnd end : track.getEnds()) {
            List<Track> neighbours = new ArrayList<>();
            for (Track candidate : tracksByEnd.getOrDefault(key(end.x(), end.y()), List.of())) {
                if (candidate == track || candidate.getLayer() != track.getLayer()) {
                    continue;
                }
                for (RailEnd candidateEnd : candidate.getEnds()) {
                    if (end.meets(candidateEnd)) {
                        neighbours.add(candidate);
                        break;
                    }
                }
            }
            byEnd.add(neighbours);
        }
        return byEnd;
    }

    private List<SignalPlacement> resolveSignals(Track track) {
        List<SignalPlacement> placements = new ArrayList<>();
        for (SignalSpots.Spot spot : SignalSpots.of(track.getName(), track.getDirection())) {
            double x = track.getPosition().getX() + spot.dx();
            double y = track.getPosition().getY() + spot.dy();
            for (Entity signal : signalsByPosition.getOrDefault(key(x, y), List.of())) {
                if (signal.getDirection() != spot.signalDirection()) {
                    continue;
                }
                int endIndex = guardedEnd(track, spot, x, y);
                if (endIndex < 0) {
                    continue;
                }
                int heading = track.getEnds()[endIndex].heading();
                // The signal faces the trains it governs. Facing away from the
                // track means it stops traffic coming in; facing into the track
                // means it stops traffic going out through that end.
                boolean outbound = spot.signalDirection() == Direction.opposite(heading);
                placements.add(new SignalPlacement(signal, endIndex, outbound));
            }
        }
        return placements;
    }

    /**
     * Which end of the track a signal placed at the given spot stands at.
     *
     * <p>A signal's direction only ever lies along one of the two ends of the
     * rail it belongs to, which identifies the end outright on every curve. On
     * a straight rail both ends lie on the same axis, so the nearer one wins.
     */
    private int guardedEnd(Track track, SignalSpots.Spot spot, double signalX, double signalY) {
        RailEnd[] ends = track.getEnds();
        int best = -1;
        double bestDistance = Double.MAX_VALUE;
        for (int endIndex = 0; endIndex < ends.length; endIndex++) {
            int heading = ends[endIndex].heading();
            if (spot.signalDirection() != heading
                    && spot.signalDirection() != Direction.opposite(heading)) {
                continue;
            }
            double distance = Math.hypot(signalX - ends[endIndex].x(), signalY - ends[endIndex].y());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = endIndex;
            }
        }
        return best;
    }

    private List<Track> resolveCrossings(Track track) {
        Set<Track> candidates = Collections.newSetFromMap(new IdentityHashMap<>());
        for (long cell : cellsOf(track)) {
            candidates.addAll(tracksByCell.getOrDefault(cell, List.of()));
        }
        List<Track> crossing = new ArrayList<>();
        for (Track candidate : candidates) {
            if (candidate == track || candidate.getLayer() != track.getLayer()) {
                continue;
            }
            if (Polylines.cross(track.getCentreLine(), candidate.getCentreLine())) {
                crossing.add(candidate);
            }
        }
        return crossing;
    }

    private void index() {
        for (Track track : tracks) {
            for (RailEnd end : track.getEnds()) {
                tracksByEnd.computeIfAbsent(key(end.x(), end.y()), unused -> new ArrayList<>())
                        .add(track);
            }
            for (long cell : cellsOf(track)) {
                tracksByCell.computeIfAbsent(cell, unused -> new ArrayList<>()).add(track);
            }
        }
    }

    /** The tiles the track's centre line passes over, used to bucket crossing candidates. */
    private static List<Long> cellsOf(Track track) {
        double[] bounds = Polylines.bounds(track.getCentreLine());
        List<Long> cells = new ArrayList<>();
        for (int x = (int) Math.floor(bounds[0]); x <= (int) Math.floor(bounds[2]); x++) {
            for (int y = (int) Math.floor(bounds[1]); y <= (int) Math.floor(bounds[3]); y++) {
                cells.add(cellKey(x, y));
            }
        }
        return cells;
    }

    private static boolean containsSame(List<Track> tracks, Track wanted) {
        for (Track track : tracks) {
            if (track == wanted) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSignal(String name) {
        return RAIL_SIGNAL.equals(name) || RAIL_CHAIN_SIGNAL.equals(name);
    }

    /** Positions fall on half tiles, so doubling them makes an exact integer key. */
    private static long key(double x, double y) {
        return (Math.round(x * 2) << 32) ^ (Math.round(y * 2) & 0xffffffffL);
    }

    private static long cellKey(int x, int y) {
        return ((long) x << 32) ^ (y & 0xffffffffL);
    }
}
