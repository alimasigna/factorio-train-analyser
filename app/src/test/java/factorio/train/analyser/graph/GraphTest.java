package factorio.train.analyser.graph;

import factorio.train.analyser.Blueprints;
import factorio.train.analyser.Matrix;
import factorio.train.analyser.jsonmodels.Entity;
import factorio.train.analyser.analyser.DeadlockAnalyser;
import factorio.train.analyser.analyser.Result;
import factorio.train.analyser.rail.Polylines;
import factorio.train.analyser.rail.RailGeometry;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Builds the train route model from whole blueprints.
 *
 * <p>The {@code t-*} blueprints are small shapes with one obvious right answer.
 * The rest are real 2.0 designs, which is where the mixture of new rails,
 * legacy rails and elevated track actually shows up.
 */
public class GraphTest {

    @Test
    public void connectedStraightRailsBecomeOneNode() {
        Graph graph = new Graph(Blueprints.load("t-line"));
        assertEquals(1, graph.getSections().size());
        List<Node> nodes = nodesOf(graph);
        assertEquals(1, nodes.size());
        assertEquals(4, nodes.get(0).getTracks().size());
        assertEquals(8.0, nodes.get(0).getLength(), 1e-9);
    }

    @Test
    public void aSignalSplitsTheRailsIntoTwoSectionsAndPointsTheTrainOneWay() {
        Graph graph = new Graph(Blueprints.load("t-signal"));
        assertEquals(2, graph.getSections().size());
        List<Node> nodes = nodesOf(graph);
        assertEquals(2, nodes.size());

        Node input = nodes.stream().filter(Node::getIsInput).findFirst().orElseThrow();
        Node output = nodes.stream().filter(node -> !node.getIsInput()).findFirst().orElseThrow();
        assertTrue(output.getIsOutput());
        // The signal faces south, so trains run north: the input leads to the
        // output and not the other way round.
        assertTrue(input.getNextNodesMerged().contains(output));
        assertTrue(output.getNextNodesMerged().isEmpty());
    }

    @Test
    public void aChainSignalProtectsTheNodeBeyondIt() {
        Graph graph = new Graph(Blueprints.load("t-chain"));
        List<Node> protectedNodes = nodesOf(graph).stream()
                .filter(node -> !node.getProtectedFrom().isEmpty())
                .toList();
        assertEquals(1, protectedNodes.size());
    }

    @Test
    public void railsThatCrossEndUpInTheSameSection() {
        // A horizontal and a vertical line sharing one block: two separate
        // paths a train can take, but a train on either blocks the other.
        Graph graph = new Graph(Blueprints.load("t-cross"));
        assertEquals(1, graph.getSections().size());
        assertEquals(2, graph.getSections().get(0).getNodes().size());
    }

    @Test
    public void parallelRailsStayInSeparateSections() {
        Graph graph = new Graph(Blueprints.load("t-parallel"));
        assertEquals(2, graph.getSections().size());
        assertEquals(2, nodesOf(graph).size());
    }

    @Test
    public void aCurveBuiltFromTheNewSplitPiecesIsOneNode() {
        // straight -> curved-rail-a -> curved-rail-b -> 45 degree straight.
        Graph graph = new Graph(Blueprints.load("t-curve"));
        List<Node> nodes = nodesOf(graph);
        assertEquals(1, nodes.size());
        assertEquals(4, nodes.get(0).getTracks().size());
        assertEquals(2.0 + 5.132284556 + 5.077891568 + 2 * Math.sqrt(2),
                nodes.get(0).getLength(), 1e-6);
    }

    @Test
    public void aHalfDiagonalRailContinuesACurve() {
        Graph graph = new Graph(Blueprints.load("t-halfdiag"));
        List<Node> nodes = nodesOf(graph);
        assertEquals(1, nodes.size());
        assertEquals(3, nodes.get(0).getTracks().size());
    }

    @Test
    public void legacyRailsFromBeforeTheUpdateStillConnect() {
        // Two orthogonal legacy rails, a legacy curve and a run of legacy
        // diagonals, which alternate between two direction values.
        Graph graph = new Graph(Blueprints.load("t-legacy"));
        List<Node> nodes = nodesOf(graph);
        assertEquals(1, nodes.size());
        assertEquals(6, nodes.get(0).getTracks().size());
    }

    @Test
    public void everyRailOfARealBlueprintEndsUpInANode() {
        for (String name : List.of("reducteur", "demi-tour", "voie-2x2")) {
            String blueprint = Blueprints.load(name);
            Graph graph = new Graph(blueprint);

            Set<Track> inNodes = Collections.newSetFromMap(new IdentityHashMap<>());
            for (Node node : nodesOf(graph)) {
                inNodes.addAll(node.getTracks());
            }
            assertEquals(name, countTracks(blueprint), inNodes.size());
        }
    }

    @Test
    public void aBlueprintWithOverheadTrackIsModelledWholeByDefault() {
        // "demi-tour" runs part of its track overhead. Nothing is skipped and
        // so there is nothing to warn about.
        String blueprint = Blueprints.load("demi-tour");
        Graph graph = new Graph(blueprint);
        assertTrue(graph.getWarnings().toString(), graph.getWarnings().isEmpty());

        Set<Track> inNodes = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Node node : nodesOf(graph)) {
            inNodes.addAll(node.getTracks());
        }
        assertEquals(countTracks(blueprint), inNodes.size());
        assertTrue(inNodes.stream().anyMatch(track -> track.getName().startsWith("elevated-")));
        assertTrue(inNodes.stream().anyMatch(Track::isRamp));
    }

    @Test
    public void theGroundNetworkCanStillBeAnalysedOnItsOwn() {
        // Opting out cuts the model at every ramp, and says which rails it left.
        Graph graph = new Graph(Blueprints.load("demi-tour"), false);
        assertFalse(graph.getWarnings().isEmpty());
        assertTrue(graph.getWarnings().toString(),
                graph.getWarnings().stream().anyMatch(warning -> warning.contains("elevated")));
        assertTrue(graph.getWarnings().toString(),
                graph.getWarnings().stream().anyMatch(warning -> warning.contains("rail-ramp")));
        for (Node node : nodesOf(graph)) {
            for (Track track : node.getTracks()) {
                assertFalse(track.getName(), track.getName().startsWith("elevated-"));
            }
        }

        // A purely ground-level blueprint has nothing to report either way.
        assertTrue(new Graph(Blueprints.load("t-curve")).getWarnings().isEmpty());
    }

    @Test
    public void aCrossoverInADoubleTrackMergesItsTwoCurvesIntoOneSection() {
        // The two curves of a crossover meet at a pair of half-diagonal rails
        // sharing a position, which is how 2.0 draws an X between two lanes.
        Graph graph = new Graph(Blueprints.load("voie-2x2"));
        long crossings = graph.getSections().stream()
                .filter(section -> section.getNodes().size() > 1)
                .count();
        assertTrue("expected crossovers to merge sections, found " + crossings, crossings > 0);
    }

    @Test
    public void elevatedTrackIsReachedThroughARampAndNowhereElse() {
        Graph graph = new Graph(Blueprints.load("elevated-deadlock"));

        // A ramp carries no signal, so a block runs straight through it from
        // one level to the other - which is the only way the two levels meet.
        for (Section section : graph.getSections()) {
            if (levelsIn(section).size() > 1) {
                assertTrue("a section spans both levels without a ramp",
                        hasRamp(section));
            }
        }
        assertTrue(graph.getSections().stream().anyMatch(GraphTest::hasRamp));
    }

    @Test
    public void aFlyoverDoesNotBlockTheTrackBeneathIt() {
        // This blueprint runs ground track under the elevated loop. The two do
        // overlap looking straight down, and must still not share a block:
        // their collision masks in the game are disjoint.
        String blueprint = Blueprints.load("elevated-overlap-deadlock");
        Graph graph = new Graph(blueprint);
        RailNetwork network = new RailNetwork(new Matrix(blueprint).getEntities(), true);

        int overlappingInPlanView = 0;
        for (Track ground : network.getTracks()) {
            for (Track elevated : network.getTracks()) {
                if (ground.isRamp() || elevated.isRamp()
                        || isElevated(ground) || !isElevated(elevated)) {
                    continue;
                }
                if (Polylines.cross(ground.getCentreLine(), elevated.getCentreLine())) {
                    overlappingInPlanView++;
                }
            }
        }
        assertTrue("expected the blueprint to run track under the flyover",
                overlappingInPlanView > 0);

        for (Track track : network.getTracks()) {
            for (Track crossing : network.crossing(track)) {
                if (track.isRamp() || crossing.isRamp()) {
                    continue;
                }
                assertEquals("a flyover was treated as blocking the track beneath it",
                        isElevated(track), isElevated(crossing));
            }
        }
        for (Section section : graph.getSections()) {
            if (levelsIn(section).size() > 1) {
                assertTrue("a section spans both levels without a ramp", hasRamp(section));
            }
        }
    }

    @Test
    public void trackLaidUnderAFlyoverDoesNotChangeItsVerdict() {
        // Same elevated loop in both blueprints; the second one adds a ground
        // network underneath. The answer has to be the same.
        Graph alone = new Graph(Blueprints.load("elevated-deadlock"));
        Graph withTrackBeneath = new Graph(Blueprints.load("elevated-overlap-deadlock"));

        assertEquals(inputsOf(alone), inputsOf(withTrackBeneath));
        assertEquals(deadlocksOf(alone), deadlocksOf(withTrackBeneath));
        assertTrue("the elevated loop is expected to deadlock", deadlocksOf(alone) > 0);

        // The ground network really is there, it just does not interfere.
        assertTrue(withTrackBeneath.getSections().size() > alone.getSections().size());
    }

    @Test
    public void aBlueprintExportedBeforeTheUpdateIsConvertedAndStillAnalysed() {
        // The junction this project was originally developed against, still a
        // 1.1 export: old rail names and the old eight-value direction enum.
        String blueprint = Blueprints.load("legacy-1-1-junction");
        Graph graph = new Graph(blueprint);

        assertTrue(graph.getWarnings().toString(),
                graph.getWarnings().stream().anyMatch(warning -> warning.contains("predates")));
        assertFalse(graph.getSections().isEmpty());

        Set<Track> inNodes = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Node node : nodesOf(graph)) {
            inNodes.addAll(node.getTracks());
        }
        assertEquals(countTracks(blueprint), inNodes.size());
        // Every rail of a 1.1 export is a legacy shape once converted.
        assertTrue(inNodes.stream().allMatch(track -> track.getName().startsWith("legacy-")));
        assertTrue(nodesOf(graph).stream().anyMatch(Node::getIsInput));
        assertTrue(nodesOf(graph).stream().anyMatch(Node::getIsOutput));
    }

    @Test
    public void aBlueprintStringThatCannotBeReadYieldsAnEmptyModel() {
        assertTrue(new Graph("not a blueprint").getSections().isEmpty());
        assertTrue(new Graph(null).getSections().isEmpty());
    }

    private static boolean isElevated(Track track) {
        return track.blocksLayer(RailGeometry.ELEVATED_LAYER)
                && !track.blocksLayer(RailGeometry.GROUND_LAYER);
    }

    /** Which of the two levels the tracks of a section sit on. */
    private static Set<Integer> levelsIn(Section section) {
        Set<Integer> levels = new HashSet<>();
        for (Node node : section.getNodes()) {
            for (Track track : node.getTracks()) {
                if (track.isRamp()) {
                    levels.add(RailGeometry.GROUND_LAYER);
                    levels.add(RailGeometry.ELEVATED_LAYER);
                } else {
                    levels.add(isElevated(track)
                            ? RailGeometry.ELEVATED_LAYER : RailGeometry.GROUND_LAYER);
                }
            }
        }
        return levels;
    }

    private static boolean hasRamp(Section section) {
        return section.getNodes().stream()
                .flatMap(node -> node.getTracks().stream())
                .anyMatch(Track::isRamp);
    }

    private static long inputsOf(Graph graph) {
        return nodesOf(graph).stream().filter(Node::getIsInput).count();
    }

    private static long deadlocksOf(Graph graph) {
        List<Result> results = new DeadlockAnalyser(graph).deadlockCheck();
        return results.stream().filter(Result::getIsDeadlock).count();
    }

    private static List<Node> nodesOf(Graph graph) {
        List<Node> nodes = new ArrayList<>();
        graph.getSections().forEach(section -> nodes.addAll(section.getNodes()));
        return nodes;
    }

    /** Every entity of the blueprint a train can actually run along. */
    private static int countTracks(String blueprint) {
        int count = 0;
        for (Entity entity : new Matrix(blueprint).getEntities()) {
            if (RailGeometry.isRail(entity.getName())) {
                count++;
            }
        }
        return count;
    }
}
