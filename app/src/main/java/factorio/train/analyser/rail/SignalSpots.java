package factorio.train.analyser.rail;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Every position a rail signal may legally occupy, relative to the rail it
 * belongs to, together with the single direction it is legal at.
 *
 * <p>This is measurement rather than derivation: the placement rules are engine
 * behaviour that the rail geometry alone does not predict, so the table was
 * probed against Factorio 2.1.12 by the Factorio Blueprint Editor project and
 * is reproduced here. 152 placements across 38 rail orientations - four per
 * rail, five on a {@code curved-rail-b}, and only two on a legacy rail at a
 * diagonal.
 *
 * <p>Offsets are half-tile values, because a signal occupies a tile centre
 * while a rail is anchored on a tile corner. {@code rail-signal} and
 * {@code rail-chain-signal} agree on every one of the 38 orientations, so one
 * table serves both.
 *
 * <p>A signal's own direction is the way it faces, which is <em>opposite</em>
 * to the travel direction it governs: a signal facing south guards northbound
 * traffic and sits on the right-hand side of a northbound train.
 * {@link RailNetwork} relies on that to work out which way a signal lets trains
 * run.
 *
 * <p>The four elevated rail types are deliberately absent. Placing one needs a
 * rail support, so the probe could not sweep signals around them; an absent
 * rail gets no signals rather than the wrong ones.
 *
 * @see <a href="https://github.com/FactoryGameFan/factorio-blueprint-editor">
 *      FactoryGameFan/factorio-blueprint-editor,
 *      packages/editor/src/core/railSignalSpots.ts</a>
 */
public final class SignalSpots {

    /**
     * A legal signal placement: an offset from the rail's position and the one
     * direction the signal is legal at there.
     */
    public record Spot(double dx, double dy, int signalDirection) {
    }

    private static final Map<String, Map<Integer, List<Spot>>> SPOTS = build();

    private SignalSpots() {
    }

    /**
     * The legal signal placements for a rail orientation, or an empty list when
     * none are known for it.
     */
    public static List<Spot> of(String railName, int railDirection) {
        Map<Integer, List<Spot>> byDirection = SPOTS.get(railName);
        if (byDirection == null) {
            return List.of();
        }
        return byDirection.getOrDefault(railDirection, List.of());
    }

    private static Map<String, Map<Integer, List<Spot>>> build() {
        Map<String, Map<Integer, List<Spot>>> all = new LinkedHashMap<>();
        all.put("curved-rail-a", spotsOfCurvedRailA());
        all.put("curved-rail-b", spotsOfCurvedRailB());
        all.put("half-diagonal-rail", spotsOfHalfDiagonalRail());
        all.put("legacy-curved-rail", spotsOfLegacyCurvedRail());
        all.put("legacy-straight-rail", spotsOfLegacyStraightRail());
        all.put("straight-rail", spotsOfStraightRail());        return Collections.unmodifiableMap(all);
    }

    private static Map<Integer, List<Spot>> spotsOfCurvedRailA() {
        Map<Integer, List<Spot>> byDirection = new LinkedHashMap<>();
        byDirection.put(0, List.of(new Spot(-1.5, -1.5, 15), new Spot(0.5, -2.5, 7), new Spot(1.5, 1.5, 8), new Spot(-1.5, 1.5, 0)));
        byDirection.put(2, List.of(new Spot(-0.5, -2.5, 1), new Spot(1.5, -1.5, 9), new Spot(1.5, 1.5, 8), new Spot(-1.5, 1.5, 0)));
        byDirection.put(4, List.of(new Spot(-1.5, -1.5, 4), new Spot(1.5, -1.5, 3), new Spot(2.5, 0.5, 11), new Spot(-1.5, 1.5, 12)));
        byDirection.put(6, List.of(new Spot(-1.5, -1.5, 4), new Spot(2.5, -0.5, 5), new Spot(1.5, 1.5, 13), new Spot(-1.5, 1.5, 12)));
        byDirection.put(8, List.of(new Spot(-1.5, -1.5, 0), new Spot(1.5, -1.5, 8), new Spot(1.5, 1.5, 7), new Spot(-0.5, 2.5, 15)));
        byDirection.put(10, List.of(new Spot(-1.5, -1.5, 0), new Spot(1.5, -1.5, 8), new Spot(0.5, 2.5, 9), new Spot(-1.5, 1.5, 1)));
        byDirection.put(12, List.of(new Spot(-2.5, -0.5, 3), new Spot(1.5, -1.5, 4), new Spot(1.5, 1.5, 12), new Spot(-1.5, 1.5, 11)));
        byDirection.put(14, List.of(new Spot(-1.5, -1.5, 5), new Spot(1.5, -1.5, 4), new Spot(1.5, 1.5, 12), new Spot(-2.5, 0.5, 13)));
        return byDirection;
    }

    private static Map<Integer, List<Spot>> spotsOfCurvedRailB() {
        Map<Integer, List<Spot>> byDirection = new LinkedHashMap<>();
        byDirection.put(0, List.of(new Spot(-2.5, -0.5, 14), new Spot(-0.5, -2.5, 6), new Spot(0.5, -0.5, 7), new Spot(1.5, 0.5, 7), new Spot(-0.5, 1.5, 15)));
        byDirection.put(2, List.of(new Spot(-0.5, -0.5, 1), new Spot(0.5, -2.5, 2), new Spot(2.5, -0.5, 10), new Spot(0.5, 1.5, 9), new Spot(-1.5, 0.5, 1)));
        byDirection.put(4, List.of(new Spot(-1.5, -0.5, 3), new Spot(0.5, -2.5, 2), new Spot(2.5, -0.5, 10), new Spot(0.5, 0.5, 11), new Spot(-0.5, 1.5, 11)));
        byDirection.put(6, List.of(new Spot(-0.5, -1.5, 5), new Spot(0.5, -0.5, 5), new Spot(2.5, 0.5, 6), new Spot(0.5, 2.5, 14), new Spot(-1.5, 0.5, 13)));
        byDirection.put(8, List.of(new Spot(-1.5, -0.5, 15), new Spot(0.5, -1.5, 7), new Spot(2.5, 0.5, 6), new Spot(0.5, 2.5, 14), new Spot(-0.5, 0.5, 15)));
        byDirection.put(10, List.of(new Spot(-0.5, -1.5, 1), new Spot(1.5, -0.5, 9), new Spot(0.5, 0.5, 9), new Spot(-0.5, 2.5, 10), new Spot(-2.5, 0.5, 2)));
        byDirection.put(12, List.of(new Spot(-0.5, -0.5, 3), new Spot(0.5, -1.5, 3), new Spot(1.5, 0.5, 11), new Spot(-0.5, 2.5, 10), new Spot(-2.5, 0.5, 2)));
        byDirection.put(14, List.of(new Spot(-2.5, -0.5, 14), new Spot(-0.5, -2.5, 6), new Spot(1.5, -0.5, 5), new Spot(0.5, 1.5, 13), new Spot(-0.5, 0.5, 13)));
        return byDirection;
    }

    private static Map<Integer, List<Spot>> spotsOfHalfDiagonalRail() {
        Map<Integer, List<Spot>> byDirection = new LinkedHashMap<>();
        byDirection.put(0, List.of(new Spot(-1.5, -0.5, 15), new Spot(0.5, -1.5, 7), new Spot(1.5, 0.5, 7), new Spot(-0.5, 1.5, 15)));
        byDirection.put(2, List.of(new Spot(-0.5, -1.5, 1), new Spot(1.5, -0.5, 9), new Spot(0.5, 1.5, 9), new Spot(-1.5, 0.5, 1)));
        byDirection.put(4, List.of(new Spot(-1.5, -0.5, 3), new Spot(0.5, -1.5, 3), new Spot(1.5, 0.5, 11), new Spot(-0.5, 1.5, 11)));
        byDirection.put(6, List.of(new Spot(-0.5, -1.5, 5), new Spot(1.5, -0.5, 5), new Spot(0.5, 1.5, 13), new Spot(-1.5, 0.5, 13)));
        return byDirection;
    }

    private static Map<Integer, List<Spot>> spotsOfLegacyCurvedRail() {
        Map<Integer, List<Spot>> byDirection = new LinkedHashMap<>();
        byDirection.put(0, List.of(new Spot(-2.5, -1.5, 14), new Spot(-0.5, -3.5, 6), new Spot(2.5, 3.5, 8), new Spot(-0.5, 3.5, 0)));
        byDirection.put(2, List.of(new Spot(0.5, -3.5, 2), new Spot(2.5, -1.5, 10), new Spot(0.5, 3.5, 8), new Spot(-2.5, 3.5, 0)));
        byDirection.put(4, List.of(new Spot(-3.5, -0.5, 4), new Spot(1.5, -2.5, 2), new Spot(3.5, -0.5, 10), new Spot(-3.5, 2.5, 12)));
        byDirection.put(6, List.of(new Spot(-3.5, -2.5, 4), new Spot(3.5, 0.5, 6), new Spot(1.5, 2.5, 14), new Spot(-3.5, 0.5, 12)));
        byDirection.put(8, List.of(new Spot(-2.5, -3.5, 0), new Spot(0.5, -3.5, 8), new Spot(2.5, 1.5, 6), new Spot(0.5, 3.5, 14)));
        byDirection.put(10, List.of(new Spot(-0.5, -3.5, 0), new Spot(2.5, -3.5, 8), new Spot(-0.5, 3.5, 10), new Spot(-2.5, 1.5, 2)));
        byDirection.put(12, List.of(new Spot(3.5, -2.5, 4), new Spot(3.5, 0.5, 12), new Spot(-1.5, 2.5, 10), new Spot(-3.5, 0.5, 2)));
        byDirection.put(14, List.of(new Spot(-3.5, -0.5, 14), new Spot(-1.5, -2.5, 6), new Spot(3.5, -0.5, 4), new Spot(3.5, 2.5, 12)));
        return byDirection;
    }

    private static Map<Integer, List<Spot>> spotsOfLegacyStraightRail() {
        Map<Integer, List<Spot>> byDirection = new LinkedHashMap<>();
        byDirection.put(0, List.of(new Spot(-1.5, -0.5, 0), new Spot(1.5, -0.5, 8), new Spot(1.5, 0.5, 8), new Spot(-1.5, 0.5, 0)));
        byDirection.put(2, List.of(new Spot(1.5, -1.5, 6), new Spot(-0.5, 0.5, 14)));
        byDirection.put(4, List.of(new Spot(-0.5, -1.5, 4), new Spot(0.5, -1.5, 4), new Spot(0.5, 1.5, 12), new Spot(-0.5, 1.5, 12)));
        byDirection.put(6, List.of(new Spot(-0.5, -0.5, 2), new Spot(1.5, 1.5, 10)));
        byDirection.put(10, List.of(new Spot(0.5, -0.5, 6), new Spot(-1.5, 1.5, 14)));
        byDirection.put(14, List.of(new Spot(-1.5, -1.5, 2), new Spot(0.5, 0.5, 10)));
        return byDirection;
    }

    private static Map<Integer, List<Spot>> spotsOfStraightRail() {
        Map<Integer, List<Spot>> byDirection = new LinkedHashMap<>();
        byDirection.put(0, List.of(new Spot(-1.5, -0.5, 0), new Spot(1.5, -0.5, 8), new Spot(1.5, 0.5, 8), new Spot(-1.5, 0.5, 0)));
        byDirection.put(2, List.of(new Spot(-1.5, -0.5, 2), new Spot(-0.5, -1.5, 2), new Spot(1.5, 0.5, 10), new Spot(0.5, 1.5, 10)));
        byDirection.put(4, List.of(new Spot(-0.5, -1.5, 4), new Spot(0.5, -1.5, 4), new Spot(0.5, 1.5, 12), new Spot(-0.5, 1.5, 12)));
        byDirection.put(6, List.of(new Spot(0.5, -1.5, 6), new Spot(1.5, -0.5, 6), new Spot(-0.5, 1.5, 14), new Spot(-1.5, 0.5, 14)));
        return byDirection;
    }
}
