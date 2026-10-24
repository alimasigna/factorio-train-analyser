package factorio.train.analyser.rail;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Checks when two centre lines count as crossing. */
public class PolylinesTest {

    @Test
    public void railsThatPassThroughEachOtherCross() {
        double[][] horizontal = RailGeometry.centreLine("straight-rail", 4, 1, 1);
        double[][] vertical = RailGeometry.centreLine("straight-rail", 0, 1, 1);
        assertTrue(Polylines.cross(horizontal, vertical));
    }

    @Test
    public void twoHalfDiagonalsInTheSameBlockCross() {
        // The signature of a crossover in a 2.0 double track.
        double[][] down = RailGeometry.centreLine("half-diagonal-rail", 6, 9, 5);
        double[][] up = RailGeometry.centreLine("half-diagonal-rail", 4, 9, 5);
        assertTrue(Polylines.cross(down, up));
    }

    @Test
    public void parallelRailsTwoTilesApartDoNotCross() {
        double[][] first = RailGeometry.centreLine("straight-rail", 4, 1, 1);
        double[][] second = RailGeometry.centreLine("straight-rail", 4, 1, 3);
        assertFalse(Polylines.cross(first, second));
    }

    @Test
    public void railsJoinedEndToEndDoNotCross() {
        double[][] first = RailGeometry.centreLine("straight-rail", 0, 1, 1);
        double[][] second = RailGeometry.centreLine("straight-rail", 0, 1, 3);
        assertFalse(Polylines.cross(first, second));
    }

    @Test
    public void aCurveJoinedToItsStraightDoesNotCrossIt() {
        // The Bezier approximation must not wobble across the rail it meets.
        double[][] curve = RailGeometry.centreLine("curved-rail-a", 0, 1, 0);
        double[][] straight = RailGeometry.centreLine("straight-rail", 0, 1, 3);
        assertFalse(Polylines.cross(curve, straight));
    }

    @Test
    public void thePairOfCurvesMakingAQuarterTurnDoNotCross() {
        double[][] first = RailGeometry.centreLine("curved-rail-b", 0, -1, -5);
        double[][] second = RailGeometry.centreLine("curved-rail-b", 6, -5, -9);
        assertFalse(Polylines.cross(first, second));
    }
}
