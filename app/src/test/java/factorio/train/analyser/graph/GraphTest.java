package factorio.train.analyser.graph;

import factorio.train.analyser.Blueprints;
import factorio.train.analyser.Matrix;
import factorio.train.analyser.jsonmodels.Entity;
import factorio.train.analyser.rail.RailGeometry;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
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
            assertEquals(name, countGroundRails(blueprint), inNodes.size());
        }
    }

    @Test
    public void elevatedRailsAreReportedRatherThanSilentlyDropped() {
        // "demi-tour" runs part of its track overhead, so the ground model is
        // incomplete and has to say so.
        Graph graph = new Graph(Blueprints.load("demi-tour"));
        assertFalse(graph.getWarnings().isEmpty());
        assertTrue(graph.getWarnings().toString(),
                graph.getWarnings().stream().anyMatch(warning -> warning.contains("elevated")));
        assertTrue(graph.getWarnings().toString(),
                graph.getWarnings().stream().anyMatch(warning -> warning.contains("rail-ramp")));

        // A purely ground-level blueprint has nothing to report.
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
        assertEquals(countGroundRails(blueprint), inNodes.size());
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

    private static List<Node> nodesOf(Graph graph) {
        List<Node> nodes = new ArrayList<>();
        graph.getSections().forEach(section -> nodes.addAll(section.getNodes()));
        return nodes;
    }

    private static int countGroundRails(String blueprint) {
        int count = 0;
        for (Entity entity : new Matrix(blueprint).getEntities()) {
            if (RailGeometry.isRail(entity.getName())
                    && !RailGeometry.isElevated(entity.getName())) {
                count++;
            }
        }
        return count;
    }
}
