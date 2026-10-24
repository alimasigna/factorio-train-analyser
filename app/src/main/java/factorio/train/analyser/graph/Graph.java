package factorio.train.analyser.graph;

import factorio.train.analyser.Matrix;
import factorio.train.analyser.graph.RailNetwork.SignalPlacement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * The train route model of a blueprint: every section, each holding the nodes a
 * train can occupy, and the references between them that the deadlock analysis
 * walks.
 *
 * <p>Generation runs in four steps, unchanged in shape from the 1.1 version of
 * the tool. Connected rails are collected into nodes; nodes that cross each
 * other are merged into sections; the signal relations the tracks picked up
 * during parsing are lifted onto their nodes; and the nodes at the edge of the
 * blueprint are marked as the junction's inputs and outputs.
 *
 * <p>What changed for 2.0 is underneath: connections, signals and crossings
 * now come from {@link RailNetwork}, which derives them from rail geometry
 * rather than from tables of neighbour positions measured by hand in the game.
 * That is what lets the new {@code half-diagonal-rail} and the split
 * {@code curved-rail-a}/{@code curved-rail-b} work without a case of their own.
 */
public class Graph {
    private ArrayList<Section> sections;

    private transient ArrayList<Node> nodes;
    private transient RailNetwork network;
    private transient List<String> warnings = new ArrayList<>();
    private transient static int numOfGeneratedGraphs = 0;
    private int graphId;

    /**
     * Builds the model of a blueprint, both levels of it.
     *
     * @param encodedString the encoded blueprint string
     */
    public Graph(String encodedString) {
        this(encodedString, true);
    }

    /**
     * Builds the model of a blueprint.
     *
     * @param encodedString the encoded blueprint string
     * @param withElevated  whether to include the elevated rail layer. Leaving
     *                      it out analyses the ground network on its own, which
     *                      cuts the model at every {@code rail-ramp}
     */
    public Graph(String encodedString, boolean withElevated) {
        nodes = new ArrayList<>();
        sections = new ArrayList<>();
        if (encodedString != null) {
            Matrix matrix = new Matrix(encodedString);
            warnings.addAll(matrix.getWarnings());
            network = new RailNetwork(matrix.getEntities(), withElevated);
            warnings.addAll(network.getWarnings());
        }
        setSections();
        graphId = ++numOfGeneratedGraphs;
    }

    public int getGraphId() {
        return graphId;
    }

    public ArrayList<Section> getSections() {
        return sections;
    }

    /**
     * Anything about the blueprint the caller should be told: which blueprint of
     * a book was taken, whether it had to be converted from a pre-2.0 export,
     * and which rails were recognised but left out of the model.
     */
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    /**
     * Fills the sections of the graph from the rails of the blueprint.
     *
     * <p>Every rail that has not been visited yet starts a parse run, which
     * collects all nodes reachable from it without passing a signal into one
     * section. Those sections are then merged where they cross, the signal
     * relations are lifted from tracks onto nodes, and the input and output
     * flags are set.
     */
    private void setSections() {
        if (network == null) {
            return;
        }

        Set<Track> knownTracks = Collections.newSetFromMap(new IdentityHashMap<>());

        for (Track track : network.getTracks()) {
            if (knownTracks.contains(track)) {
                continue;
            }
            parseTracksToNodes(track, null, knownTracks);
            if (!nodes.isEmpty()) {
                sections.add(new Section(nodes));
            }
            nodes = new ArrayList<>(); // resetting nodes after each run
        }
        mergeSections();
        mapTrackFieldToNodeField();
        setIoFlags();
    }

    /**
     * Marks the nodes at the edge of the blueprint as the junction's inputs and
     * outputs.
     *
     * <p>A node with an open end and nothing to go to is an output: trains
     * leave the junction there and it is assumed never to be blocked. A node
     * with an open end that does lead somewhere is an input, where the analysis
     * starts trains. A node can be both.
     */
    private void setIoFlags() {
        for (Section section : sections) {
            for (Node node : section.getNodes()) {
                if (node.getIsEndNode()) {
                    if (node.getNextNodes().isEmpty()) {
                        node.setIsOutput(true);
                    } else {
                        // check if next nodes are also referencing the current node, "should" be null safe lel
                        if (node.getNextNodes().get(0).get(0).getNextNodesMerged().contains(node))
                            node.setIsOutput(true);
                        node.setIsInput(true);
                    }
                }
            }
        }
    }

    /**
     * Lifts the relations the tracks picked up during parsing onto the nodes
     * they belong to, so that a node knows which nodes it leads into and which
     * ones protect it through a chain signal. Tracks with an open end mark
     * their nodes as end nodes, which {@link #setIoFlags()} needs.
     */
    private void mapTrackFieldToNodeField() {
        for (Track track : network.getTracks()) {
            if (track.getIsEndTrack()) {
                for (Node endNode : track.getNodes()) {
                    endNode.setIsEndNode(true);
                }
            }

            if (track.getGoesTo().isEmpty() && track.getProtectedFrom().isEmpty())
                continue;

            ArrayList<Node> parentNodes = track.getNodes();

            // get all nodes the track is referencing
            ArrayList<Node> referencedNextNodes = new ArrayList<>();
            for (Track outGoingTrack : track.getGoesTo()) {
                referencedNextNodes.addAll(outGoingTrack.getNodes());
            }
            ArrayList<Node> referencedProtectedFromNodes = new ArrayList<>();
            for (Track protectedFromTrack : track.getProtectedFrom()) {
                referencedProtectedFromNodes.addAll(protectedFromTrack.getNodes());
            }

            // setting the fields
            for (Node parentNode : parentNodes) {
                if (!referencedNextNodes.isEmpty())
                    parentNode.setNextNodes(referencedNextNodes);
                if (!referencedProtectedFromNodes.isEmpty())
                    parentNode.setProtectedFrom(referencedProtectedFromNodes);
            }
        }
    }

    /**
     * Merges the sections that overlap in the game into one.
     *
     * <p>A parse run only follows rails that are connected end to end, so two
     * rails that cross each other end up in separate sections even though a
     * train on one of them blocks the other. This walks out from every section
     * over the crossings of its tracks and collects everything it reaches into a
     * single section.
     */
    private void mergeSections() {
        ArrayList<Section> mergedSections = new ArrayList<>();

        for (Section section : sections) {
            Queue<Node> checkNodesQueue = new LinkedList<>(section.getNodes());
            ArrayList<Node> checkNodes = section.getNodes();
            if (checkNodes.isEmpty() || checkNodes.get(0).getHasBeenMerged())
                continue; // already pulled into a section by an earlier round

            while (!checkNodesQueue.isEmpty()) {
                Node checkNode = checkNodesQueue.poll();
                if (checkNode.getHasBeenMerged())
                    continue;
                for (Track track : checkNode.getTracks()) {
                    for (Track crossedTrack : network.crossing(track)) {
                        for (Node addedNode : crossedTrack.getNodes()) { // adding all the found new nodes
                            if (!checkNodes.contains(addedNode)) {
                                checkNodes.add(addedNode);
                                checkNodesQueue.add(addedNode);
                            }
                        }
                    }
                }
                checkNode.setHasBeenMerged(true); // so later rounds skip it
            }
            mergedSections.add(new Section(checkNodes));
        }
        sections = mergedSections;
    }

    /**
     * Records which way a signal between two tracks lets trains run.
     *
     * <p>Only the signals standing at the end where {@code nextTracks} attach
     * say anything about this pair, so the others are skipped. A signal letting
     * trains out of {@code currentTrack} means the current track leads into the
     * next ones; a signal letting them in means the opposite. A chain signal
     * additionally protects whatever lies beyond it, which is what the deadlock
     * analysis follows.
     *
     * @param nextTracks   the tracks on the far side of the signal
     * @param currentTrack the track the signal stands on
     */
    private void connectTracks(List<Track> nextTracks, Track currentTrack) {
        if (nextTracks.isEmpty()) {
            return;
        }
        for (SignalPlacement placement : network.signalsOn(currentTrack)) {
            List<Track> beyondSignal = network.connectedAt(currentTrack, placement.endIndex());
            if (!sharesAny(beyondSignal, nextTracks)) {
                continue; // this signal guards the other end of the track
            }
            for (Track nextTrack : nextTracks) {
                if (placement.outbound()) {
                    currentTrack.setGoesTo(nextTrack);
                    if (placement.isChainSignal())
                        nextTrack.setProtectedFrom(currentTrack);
                } else {
                    nextTrack.setGoesTo(currentTrack);
                    if (placement.isChainSignal())
                        currentTrack.setProtectedFrom(nextTrack);
                }
            }
        }
    }

    /**
     * Parses tracks into nodes by traversing the connected rails recursively.
     *
     * <p>The two ends of the current track are split into a frontier set and a
     * callback set, the callback being the one the recursion arrived from. The
     * frontier is recursed into first and returns the nodes the current track
     * joins; the callback is recursed into afterwards and its nodes are
     * multiplied with the frontier's, which is what keeps sub-paths through a
     * junction apart. A track whose frontier is empty - an open end, or an end
     * closed off by a signal - is the base case and starts a fresh node.
     *
     * @param currentTrack  the track being processed
     * @param visitedTracks the path of tracks that led here, null at the start
     * @param knownTracks   every track any parse run has already visited
     * @return the frontier nodes the caller joins onto
     */
    private ArrayList<Node> parseTracksToNodes(Track currentTrack, ArrayList<Track> visitedTracks,
            Set<Track> knownTracks) {

        // store path callbyvalue
        Track previousTrack;
        ArrayList<Track> previousTracks = new ArrayList<>();
        if (visitedTracks == null) {
            previousTrack = null;
        } else {
            previousTracks.addAll(visitedTracks);
            previousTrack = previousTracks.get(previousTracks.size() - 1);
        }
        previousTracks.add(currentTrack);
        knownTracks.add(currentTrack);

        // set frontier and callback. which end is which depends on where the
        // recursion came from; on the first call the split is arbitrary.
        List<Track> in = network.connectedAt(currentTrack, 0);
        List<Track> out = network.connectedAt(currentTrack, 1);
        List<Track> frontier = new ArrayList<>();
        List<Track> callBack = new ArrayList<>();
        if (previousTrack == null) {
            frontier = new ArrayList<>(in);
            callBack = new ArrayList<>(out);
        } else if (contains(in, previousTrack)) {
            callBack = new ArrayList<>(in);
            frontier = new ArrayList<>(out);
        } else if (contains(out, previousTrack)) {
            callBack = new ArrayList<>(out);
            frontier = new ArrayList<>(in);
        }

        // checks if our current track is an ending one
        if (callBack.isEmpty() || frontier.isEmpty()) {
            currentTrack.setIsEndTrack(true);
        }

        // a signal on this track closes off the end it stands at, so the tracks
        // beyond it belong to another section. Record the relation, then stop
        // following them.
        if (isSeparatedFromAny(currentTrack, frontier)) {
            connectTracks(frontier, currentTrack);
            frontier = new ArrayList<>();
        }
        if (isSeparatedFromAny(currentTrack, callBack)) {
            connectTracks(callBack, currentTrack);
            callBack = new ArrayList<>();
        }

        // declaration of the ArrayLists that will be filled with the nodes return from
        // the recursion
        ArrayList<Node> frontierNodes = new ArrayList<>();
        ArrayList<Node> callbackNodes = new ArrayList<>();

        // frontier recursion step
        for (Track frontTrack : frontier) {
            // skips if we have visited the track already
            if (contains(previousTracks, frontTrack)) {
                continue;
            }
            // a signal on the next track can put it in another section
            if (network.separatedBySignalOn(frontTrack, currentTrack)) {
                continue;
            }
            // if our frontTrack has already been visited, just take his frontNodes and skip
            // the recursion
            if (!frontTrack.getFrontierNodes().isEmpty()) {
                frontierNodes.addAll(frontTrack.getFrontierNodes());
                continue;
            }
            // actual recursion step
            frontierNodes.addAll(parseTracksToNodes(frontTrack, previousTracks, knownTracks));
        }

        // basecase
        if (frontier.isEmpty() || frontierNodes.isEmpty()) {
            Node base = new Node();
            frontierNodes.add(base);
        }

        // add current track to frontier nodes
        for (Node frontNode : frontierNodes) {
            frontNode.addTrack(currentTrack);
            currentTrack.addNode(frontNode);
            currentTrack.getFrontierNodes().add(frontNode);
        }

        // callback recursion step
        ArrayList<Track> temp = new ArrayList<>(callBack);
        for (Track callbackTrack : temp) {
            // skips if we have been called by the callbackTrack
            if (callbackTrack == previousTrack)
                continue;

            // skips if we have visited the track already
            if (contains(previousTracks, callbackTrack)) {
                continue;
            }

            // a signal on the next track can put it in another section
            if (network.separatedBySignalOn(callbackTrack, currentTrack)) {
                callBack.remove(callbackTrack);
                continue;
            }

            // if our callbackTrack has already been visited, just take his frontNodes and
            // skip the recursion
            if (!callbackTrack.getFrontierNodes().isEmpty()) {
                callbackNodes.addAll(callbackTrack.getFrontierNodes());
                continue;
            }
            // actual recursion step
            callbackNodes.addAll(parseTracksToNodes(callbackTrack, previousTracks, knownTracks));
        }

        // merging of callback and frontier Nodes, this is needed to map subpaths
        for (Node frontNode : frontierNodes) {
            // if our callback is empty, we add the given frontierNodes
            if (callBack.isEmpty()) {
                nodes.add(frontNode);
                continue;
            }

            for (Node callbackNode : callbackNodes) {
                Node base = Node.mergeNodes(frontNode, callbackNode);
                nodes.add(base);
                nodes.remove(callbackNode);
            }
        }

        // gives back all connected nodes
        return frontierNodes;
    }

    /**
     * Whether a signal on {@code currentTrack} cuts it off from any of the
     * given neighbours. One is enough: the whole set sits at the same end of the
     * track, so a signal there separates all of them.
     */
    private boolean isSeparatedFromAny(Track currentTrack, List<Track> nextTracks) {
        for (Track nextTrack : nextTracks) {
            if (network.separatedBySignalOn(currentTrack, nextTrack)) {
                return true;
            }
        }
        return false;
    }

    private static boolean sharesAny(List<Track> first, List<Track> second) {
        for (Track track : second) {
            if (contains(first, track)) {
                return true;
            }
        }
        return false;
    }

    private static boolean contains(List<Track> tracks, Track wanted) {
        for (Track track : tracks) {
            if (track == wanted) {
                return true;
            }
        }
        return false;
    }
}
