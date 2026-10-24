package factorio.train.analyser.rail;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Checks the rail shapes the whole model is derived from. */
public class RailGeometryTest {

    private static final List<String> GROUND_RAILS = List.of(
            "straight-rail", "half-diagonal-rail", "curved-rail-a", "curved-rail-b",
            "legacy-straight-rail", "legacy-curved-rail");

    /** In the same order as the first four of {@link #GROUND_RAILS}. */
    private static final List<String> ELEVATED_RAILS = List.of(
            "elevated-straight-rail", "elevated-half-diagonal-rail",
            "elevated-curved-rail-a", "elevated-curved-rail-b");

    @Test
    public void everyRailHasTwoEndsOnTheIntegerGrid() {
        for (String name : GROUND_RAILS) {
            for (int direction = 0; direction < Direction.COUNT; direction++) {
                if (!RailGeometry.isKnownOrientation(name, direction)) {
                    continue;
                }
                RailEnd[] ends = RailGeometry.endsOf(name, direction, 0, 0);
                assertEquals(name + "@" + direction, 2, ends.length);
                for (RailEnd end : ends) {
                    // Factorio puts every rail connection point on a whole
                    // tile, which is what makes end matching exact.
                    assertEquals(name + "@" + direction, Math.rint(end.x()), end.x(), 0);
                    assertEquals(name + "@" + direction, Math.rint(end.y()), end.y(), 0);
                }
            }
        }
    }

    @Test
    public void curvedRailsCoverAllEightOrientationsAndStraightOnesFour() {
        for (int direction = 0; direction < Direction.COUNT; direction += 2) {
            assertTrue("curved-rail-a@" + direction,
                    RailGeometry.isKnownOrientation("curved-rail-a", direction));
            assertTrue("curved-rail-b@" + direction,
                    RailGeometry.isKnownOrientation("curved-rail-b", direction));
            assertTrue("legacy-curved-rail@" + direction,
                    RailGeometry.isKnownOrientation("legacy-curved-rail", direction));
        }
        // A 2.0 straight rail has one orientation per 45 degrees. A legacy
        // diagonal covers only half of its block, so it needs two values per
        // slope and ends up with six orientations in total.
        for (int direction = 0; direction < Direction.COUNT; direction += 2) {
            assertEquals("straight-rail@" + direction, direction < 8,
                    RailGeometry.isKnownOrientation("straight-rail", direction));
        }
        assertTrue(RailGeometry.isKnownOrientation("legacy-straight-rail", 10));
        assertTrue(RailGeometry.isKnownOrientation("legacy-straight-rail", 14));
    }

    @Test
    public void straightRailSpansItsTwoTiles() {
        RailEnd[] vertical = RailGeometry.endsOf("straight-rail", 0, 1, 1);
        assertEquals(new RailEnd(1, 0, 0, RailGeometry.GROUND_LAYER), vertical[0]);
        assertEquals(new RailEnd(1, 2, 8, RailGeometry.GROUND_LAYER), vertical[1]);
        assertEquals(2.0, RailGeometry.length("straight-rail", 0), 1e-9);
        assertTrue(RailGeometry.isStraight("straight-rail", 0));
    }

    @Test
    public void curvedRailARunsFromAnOrthogonalEndToAHalfDiagonalOne() {
        RailEnd[] ends = RailGeometry.endsOf("curved-rail-a", 0, 0, 0);
        assertEquals(new RailEnd(0, 2, 8, RailGeometry.GROUND_LAYER), ends[0]);
        assertEquals(new RailEnd(-1, -3, 15, RailGeometry.GROUND_LAYER), ends[1]);
        assertFalse(RailGeometry.isStraight("curved-rail-a", 0));
    }

    @Test
    public void aFullNinetyDegreeTurnIsTwoCurvedPairsAndJoinsUpEndToEnd() {
        // straight -> curved-a -> curved-b -> curved-b -> curved-a -> straight
        // is how 2.0 builds a quarter turn, and each joint has to line up.
        assertMeets(end("straight-rail", 0, 1, 3, 0), end("curved-rail-a", 0, 1, 0, 0));
        assertMeets(end("curved-rail-a", 0, 1, 0, 1), end("curved-rail-b", 0, -1, -5, 0));
        assertMeets(end("curved-rail-b", 0, -1, -5, 1), end("curved-rail-b", 6, -5, -9, 1));
        assertMeets(end("curved-rail-b", 6, -5, -9, 0), end("curved-rail-a", 6, -10, -11, 1));
        assertMeets(end("curved-rail-a", 6, -10, -11, 0), end("straight-rail", 4, -13, -11, 0));
    }

    @Test
    public void aHalfDiagonalRailJoinsTheSameEndACurveDoes() {
        // An S-bend extends a curve with half-diagonal rails, so both have to
        // present the same end at the same place.
        assertMeets(end("curved-rail-a", 0, 1, 0, 1), end("half-diagonal-rail", 0, -1, -5, 0));
        assertEquals(2 * Math.sqrt(5), RailGeometry.length("half-diagonal-rail", 0), 1e-9);
    }

    @Test
    public void legacyDiagonalsAlternateBetweenTwoDirectionValues()
    {
        // A 1.1 diagonal run steps through two orientations, because each piece
        // covers only half of the block it sits in.
        assertMeets(end("legacy-straight-rail", 2, -3, -3, 0),
                end("legacy-straight-rail", 10, -3, -5, 1));
        assertMeets(end("legacy-straight-rail", 10, -3, -5, 0),
                end("legacy-straight-rail", 2, -5, -5, 1));
        assertEquals(Math.sqrt(2), RailGeometry.length("legacy-straight-rail", 2), 1e-9);
    }

    @Test
    public void rotatingAnOrientationFourTimesComesBackToIt() {
        for (String name : GROUND_RAILS) {
            for (int direction = 0; direction < Direction.COUNT; direction++) {
                if (!RailGeometry.isKnownOrientation(name, direction)) {
                    continue;
                }
                int turned = direction;
                for (int quarter = 0; quarter < 4; quarter++) {
                    turned = Direction.rotate90Clockwise(turned);
                }
                assertEquals(direction, turned);
            }
        }
    }

    @Test
    public void theApproximatedCentreLineMatchesThePublishedArcLength() {
        // The Bezier standing in for a curve is stretched until it is as long as
        // the arc length Factorio's developers published, so this pins down that
        // fit as well as the lengths of the straight pieces.
        assertSampledLength("straight-rail", 4, 2.0);
        assertSampledLength("straight-rail", 2, 2 * Math.sqrt(2));
        assertSampledLength("half-diagonal-rail", 0, 2 * Math.sqrt(5));
        assertSampledLength("legacy-straight-rail", 2, Math.sqrt(2));

        // Every orientation of a curve, so the handle fit is in range for all
        // of them and not just the one it was derived from.
        for (int direction = 0; direction < Direction.COUNT; direction += 2) {
            assertSampledLength("curved-rail-a", direction, 5.132284556);
            assertSampledLength("curved-rail-b", direction, 5.077891568);
            assertSampledLength("legacy-curved-rail", direction, 7.842077);
        }
    }

    @Test
    public void elevatedRailsHaveTheSameShapeAsTheirGroundCounterparts() {
        for (int i = 0; i < ELEVATED_RAILS.size(); i++) {
            String elevated = ELEVATED_RAILS.get(i);
            String ground = GROUND_RAILS.get(i);
            assertTrue(elevated, RailGeometry.isElevated(elevated));
            assertFalse(ground, RailGeometry.isElevated(ground));
            for (int direction = 0; direction < Direction.COUNT; direction++) {
                assertEquals(elevated,
                        RailGeometry.isKnownOrientation(ground, direction),
                        RailGeometry.isKnownOrientation(elevated, direction));
                if (!RailGeometry.isKnownOrientation(ground, direction)) {
                    continue;
                }
                RailEnd[] onGround = RailGeometry.endsOf(ground, direction, 0, 0);
                RailEnd[] up = RailGeometry.endsOf(elevated, direction, 0, 0);
                for (int end = 0; end < onGround.length; end++) {
                    assertEquals(elevated + "@" + direction, onGround[end].x(), up[end].x(), 0);
                    assertEquals(elevated + "@" + direction, onGround[end].y(), up[end].y(), 0);
                    assertEquals(elevated + "@" + direction,
                            onGround[end].heading(), up[end].heading());
                }
            }
        }
    }

    @Test
    public void elevatedAndGroundRailsDoNotBlockEachOther() {
        // Disjoint collision masks in the game: a ground rail builds freely
        // under an elevated one, which is the whole point of elevated track.
        for (String name : GROUND_RAILS) {
            assertTrue(name, RailGeometry.blocksLayer(name, 0, RailGeometry.GROUND_LAYER));
            assertFalse(name, RailGeometry.blocksLayer(name, 0, RailGeometry.ELEVATED_LAYER));
        }
        for (String name : ELEVATED_RAILS) {
            assertFalse(name, RailGeometry.blocksLayer(name, 0, RailGeometry.GROUND_LAYER));
            assertTrue(name, RailGeometry.blocksLayer(name, 0, RailGeometry.ELEVATED_LAYER));
        }
        // A ramp carries both masks, so nothing may cross it at either level.
        assertTrue(RailGeometry.blocksLayer("rail-ramp", 4, RailGeometry.GROUND_LAYER));
        assertTrue(RailGeometry.blocksLayer("rail-ramp", 4, RailGeometry.ELEVATED_LAYER));
    }

    @Test
    public void aRampJoinsAGroundRailToAnElevatedOne() {
        assertTrue(RailGeometry.isRamp("rail-ramp"));
        assertFalse(RailGeometry.isElevated("rail-ramp"));
        assertEquals(16.0, RailGeometry.length("rail-ramp", 4), 1e-9);
        assertTrue(RailGeometry.isStraight("rail-ramp", 4));

        // Direction 4 climbs eastwards: the ground end is 8 tiles west of the
        // entity and the elevated end 8 tiles east.
        RailEnd[] ramp = RailGeometry.endsOf("rail-ramp", 4, 0, 0);
        assertEquals(new RailEnd(-8, 0, 12, RailGeometry.GROUND_LAYER), ramp[0]);
        assertEquals(new RailEnd(8, 0, 4, RailGeometry.ELEVATED_LAYER), ramp[1]);

        // A ground rail joins the bottom and an elevated rail the top.
        assertMeets(ramp[0], end("straight-rail", 4, -9, 0, 0));
        assertMeets(ramp[1], end("elevated-straight-rail", 4, 9, 0, 1));

        // The layers do not leak: a ground rail cannot attach to the top of a
        // ramp, nor an elevated rail to the bottom.
        assertFalse(ramp[1].meets(end("straight-rail", 4, 9, 0, 1)));
        assertFalse(ramp[0].meets(end("elevated-straight-rail", 4, -9, 0, 0)));
    }

    @Test
    public void allFourRampOrientationsPointTheirElevatedEndForwards() {
        // 0 north, 4 east, 8 south, 12 west.
        assertEquals(new RailEnd(0, -8, 0, RailGeometry.ELEVATED_LAYER),
                RailGeometry.endsOf("rail-ramp", 0, 0, 0)[1]);
        assertEquals(new RailEnd(0, 8, 8, RailGeometry.ELEVATED_LAYER),
                RailGeometry.endsOf("rail-ramp", 8, 0, 0)[1]);
        assertEquals(new RailEnd(-8, 0, 12, RailGeometry.ELEVATED_LAYER),
                RailGeometry.endsOf("rail-ramp", 12, 0, 0)[1]);
        for (int direction : new int[] { 0, 4, 8, 12 }) {
            RailEnd[] ramp = RailGeometry.endsOf("rail-ramp", direction, 0, 0);
            assertEquals(RailGeometry.GROUND_LAYER, ramp[0].layer());
            assertEquals(RailGeometry.ELEVATED_LAYER, ramp[1].layer());
        }
    }

    @Test
    public void aRailSupportCarriesNoTrain() {
        assertTrue(RailGeometry.isNonTrackRail("rail-support"));
        assertFalse(RailGeometry.isRail("rail-support"));
    }

    private static void assertSampledLength(String name, int direction, double expected) {
        double[][] line = RailGeometry.centreLine(name, direction, 0, 0);
        double sampled = 0;
        for (int i = 0; i + 1 < line.length; i++) {
            sampled += Math.hypot(line[i + 1][0] - line[i][0], line[i + 1][1] - line[i][1]);
        }
        assertEquals(name + "@" + direction, expected, sampled, 1e-6);
    }

    private static RailEnd end(String name, int direction, double x, double y, int index) {
        return RailGeometry.endsOf(name, direction, x, y)[index];
    }

    private static void assertMeets(RailEnd first, RailEnd second) {
        assertTrue(first + " should meet " + second, first.meets(second));
    }
}
