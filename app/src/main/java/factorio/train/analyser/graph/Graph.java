package factorio.train.analyser.graph;

import factorio.train.analyser.Matrix;
import factorio.train.analyser.jsonmodels.Entity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Graph {
    private ArrayList<Section> sections;

    private ArrayList<Node> nodes;
    private Matrix matrix;

    public Graph(String encodedString) {
        nodes = new ArrayList<>();
        sections = new ArrayList<>();
        setMatrix(encodedString);
        setSections();
    }

    private void setMatrix(String encodedString) {
        if (encodedString != null)
            matrix = new Matrix(encodedString);
    }

    /**
     * Sets the sections of the graph based on the tracks in the matrix.
     * The method first checks if the matrix is null. If it is, it returns without
     * doing anything.
     * It then converts the matrix to a 2D array of tracks and iterates over each
     * track. If a track is already known, it is skipped.
     * For each new track, it parses the track to nodes, creates a new section with
     * the nodes, and adds the section to the list of sections. It then resets the
     * nodes.
     * After checking all tracks, it merges the sections, maps the track fields to
     * the node fields, and generates the end sections.
     */
    private void setSections() {
        if (matrix == null)
            return;

        ArrayList<Track> knownTracks = new ArrayList<>();
        ArrayList<Track>[][] tracks = matrix.convertToTracks();

        for (int x = 0; x < tracks.length; x++) { // go through all tracks, we only go through tracks. signals should be
                                                  // discovered this way too
            for (int y = 0; y < tracks[x].length; y++) {
                if (tracks[x][y] == null)
                    continue;
                for (int i = 0; i < tracks[x][y].size(); i++) {
                    Track track = tracks[x][y].get(i);
                    if (knownTracks.contains(track))
                        continue; // we only use tracks skip everything
                    parseTracksToNodes(track, null, tracks, knownTracks);
                    Section section = new Section(nodes);
                    sections.add(section);
                    nodes = new ArrayList<>(); // resetting nodes after each run
                }
            }
        }
        mergeSections(tracks);
        mapTrackFieldToNodeField(tracks);
        setIoFlags();
    }

    /**
     * Sets the input and output flags for each node in all sections of the graph.
     * 
     * <p>
     * This method iterates over all sections, and for each section, it iterates
     * over all nodes.
     * If a node is an end node, it checks if the node has no next nodes and no
     * dependencies.
     * If so, it sets the node as an output node. Otherwise, it sets the node as an
     * input node.
     * </p>
     */
    private void setIoFlags() {
        for (Section section : sections) {
            for (Node node : section.getNodes()) {
                if (node.getIsEndNode()) {
                    if (node.getNextNodes().isEmpty()) {
                        node.setIsOutput(true);
                    } else {
                        //check if next nodes are also referencing the current node, "should" be null safe lel
                        if(node.getNextNodes().get(0).get(0).getNextNodesMerged().contains(node))
                            node.setIsOutput(true);
                        node.setIsInput(true);
                    }
                }
            }
        }
    }

    /**
     * Maps the fields of tracks to the corresponding fields of nodes.
     * The method iterates over each track in the 2D array of tracks. If a track is
     * an end track, it sets the isEndNode field of its nodes to true.
     * If a track has no outgoing tracks and no dependent tracks, it is skipped.
     * For each track that has outgoing or dependent tracks, it sets the next nodes
     * and dependent nodes of its nodes to the nodes of the outgoing and dependent
     * tracks, respectively.
     *
     * @param tracks The 2D array of tracks representing the graph.
     */
    private void mapTrackFieldToNodeField(ArrayList<Track>[][] tracks) {
        for (int x = 0; x < tracks.length; x++) {
            for (int y = 0; y < tracks[x].length; y++) {
                if (tracks[x][y] == null)
                    continue;
                for (int i = 0; i < tracks[x][y].size(); i++) {
                    Track track = tracks[x][y].get(i);

                    if (track.getIsEndTrack()) {
                        for (Node endNode : track.getNodes()) {
                            endNode.setIsEndNode(true);
                        }
                    }

                    if (track.getGoesTo().isEmpty() && track.getProtectedFrom().isEmpty())
                        continue;

                    ArrayList<Node> parentNodes = track.getNodes();
                    ArrayList<Track> outGoingTracks = track.getGoesTo();
                    ArrayList<Track> protectedFromTracks = track.getProtectedFrom();
                    
                    //get all nodes the track is referencing
                    ArrayList<Node> referencedNextNodes = new ArrayList<>();
                    for(Track outGoingTrack : outGoingTracks) {
                        referencedNextNodes.addAll(outGoingTrack.getNodes());
                    }
                    ArrayList<Node> referencedProtectedFromNodes = new ArrayList<>();
                    for(Track protectedFromTrack : protectedFromTracks) {
                        referencedProtectedFromNodes.addAll(protectedFromTrack.getNodes());
                    }

                    //setting the fields
                    for (Node parentNode : parentNodes) {
                        if(!referencedNextNodes.isEmpty())
                            parentNode.setNextNodes(referencedNextNodes);
                        if(!referencedProtectedFromNodes.isEmpty())
                            parentNode.setProtectedFrom(referencedProtectedFromNodes);
                    }
                }
            }
        }
    }

    /**
     * Merges sections of tracks into a single section.
     * The method iterates over each section, and for each section, it checks its
     * nodes. If a node has already been merged, it is ignored.
     * For each node that hasn't been merged, it checks the tracks in the node and
     * finds the crossing tracks. It then adds the nodes of the crossing tracks to
     * the check nodes if they are not already there.
     * After checking all nodes, it creates a new section with the check nodes and
     * adds it to the list of merged sections.
     * Finally, it replaces the original sections with the merged sections.
     *
     * @param tracks The 2D array of tracks representing the graph.
     */
    private void mergeSections(ArrayList<Track>[][] tracks) {
        ArrayList<Section> mergedSections = new ArrayList<>();

        for (Section section : sections) {
            Queue<Node> checkNodesQueue = new LinkedList<>(section.getNodes());
            ArrayList<Node> checkNodes = section.getNodes();
            if (checkNodes.get(0).getHasBeenMerged())
                continue; // if the node has been merged, we ignore it (it has been merged in the last
                          // step already

            while (!checkNodesQueue.isEmpty()) {
                Node checkNode = checkNodesQueue.poll();
                if (checkNode.getHasBeenMerged())
                    continue;
                ArrayList<Track> tracksInNodes = checkNode.getTracks();
                for (Track track : tracksInNodes) {
                    ArrayList<Track> crossingTracks = filterLookupsToTrack(track.getCrossed(), tracks);
                    for (Track crossedTrack : crossingTracks) {
                        ArrayList<Node> toBeAddedNodes = crossedTrack.getNodes();
                        for (Node addedNode : toBeAddedNodes) { // adding all the found new nodes
                            if (!checkNodes.contains(addedNode)) {
                                checkNodes.add(addedNode);
                                checkNodesQueue.add(addedNode);
                            }
                        }
                    }
                }
                checkNode.setHasBeenMerged(true); // setThisValue to be ignored in later steps
            }
            Section mergedSection = new Section(checkNodes);
            mergedSections.add(mergedSection);
        }
        sections = mergedSections;
    }

    /**
     * Connects the current track to the next tracks based on the signals of the
     * current track.
     * The method first filters the signals of the current track. If there are any
     * signals, it checks if the outgoing tracks from the signal contain the first
     * next track.
     * If they do, it sets each next track as a destination of the current track
     * and. If the signal is a rail-chain-signal, it sets the protectedFrom field of the
     * track.
     * If they don't, it sets the current track as a destination and, if the signal
     * is a rail-chain-signal, as a dependency of each next track.
     *
     * @param nextTracks   The list of next tracks to be connected.
     * @param currentTrack The current track to be connected.
     * @param tracks       The 2D array of tracks representing the graph.
     */
    private void connectTracks(ArrayList<Track> nextTracks, Track currentTrack, ArrayList<Track>[][] tracks) {
        if (!nextTracks.isEmpty()) {
            ArrayList<Entity> signals = filterSignals(currentTrack.getSignals(), matrix.getMatrix());
            for (Entity signal : signals) {
                ArrayList<Track> outGoingTracks = filterLookupsToTrack(
                        LookUp.lookUpOutgoingTracks(signal, currentTrack), tracks);
                if (outGoingTracks.contains(nextTracks.get(0))) {
                    for (Track nextTrack : nextTracks) {
                        currentTrack.setGoesTo(nextTrack);
                        //if it is a rail-chain-signal, we tell the next track that it is protected from current track
                        if (signal.getName().equals("rail-chain-signal"))
                            nextTrack.setProtectedFrom(currentTrack);
                    }
                } else {
                    for (Track nextTrack : nextTracks) {
                        nextTrack.setGoesTo(currentTrack);
                        //if it is a rail-chain-signal, we tell the current track that it is protected from next track
                        if (signal.getName().equals("rail-chain-signal"))
                            currentTrack.setProtectedFrom(nextTrack);
                    }
                }

            }
        }
    }

    /**
     * Parses tracks into nodes by traversing the graph of tracks recursively.
     * The method keeps track of visited tracks and known tracks. It determines the
     * frontier and callback tracks based on the connections of the current track.
     * It checks if the current track is an ending one, and if the frontier/callback
     * is behind a signal, it is ignored.
     * The method then performs a recursion step for the frontier and callback
     * tracks, skipping tracks that have already been visited or are not in the same
     * section.
     * Finally, it merges the callback and frontier nodes and returns the frontier
     * nodes.
     *
     * @param currentTrack  The current track being processed.
     * @param visitedTracks The list of tracks that have been visited.
     * @param tracks        The 2D array of tracks representing the graph.
     * @param knownTracks   The list of tracks that are known.
     * @return A list of nodes representing the frontier nodes after parsing the
     *         tracks.
     */
    private ArrayList<Node> parseTracksToNodes(Track currentTrack, ArrayList<Track> visitedTracks,
            ArrayList<Track>[][] tracks, ArrayList<Track> knownTracks) {

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

        // set frontier and callback
        ArrayList<Track> in = filterLookupsToTrack(currentTrack.getConnected()[0], tracks);
        ArrayList<Track> out = filterLookupsToTrack(currentTrack.getConnected()[1], tracks);
        ArrayList<Track> frontier = new ArrayList<>();
        ArrayList<Track> callBack = new ArrayList<>();
        if (previousTrack == null) {
            frontier = in;
            callBack = out;
        } else if (in.contains(previousTrack)) {
            callBack = in;
            frontier = out;
        } else if (out.contains(previousTrack)) {
            callBack = out;
            frontier = in;
        }

        // checks if our current track is an ending one
        if (callBack.isEmpty() || frontier.isEmpty()) {
            currentTrack.setIsEndTrack(true);
        }

        // checks if the frontier/callback is behind a signal, if yes we connect the
        // currentTrack with the frontier/callback tracks and then skip it.
        if (!isNextTrackInSection(currentTrack, frontier)) {
            connectTracks(frontier, currentTrack, tracks);
            frontier = new ArrayList<>();
        }
        if (!isNextTrackInSection(currentTrack, callBack)) {
            connectTracks(callBack, currentTrack, tracks);
            callBack = new ArrayList<>();
        }

        // declaration of the ArrayLists that will be filled with the nodes return from
        // the recursion
        ArrayList<Node> frontierNodes = new ArrayList<>();
        ArrayList<Node> callbackNodes = new ArrayList<>();

        // frontier recursion step
        for (Track frontTrack : frontier) {
            // skips if we have visited the track already
            if (previousTracks.contains(frontTrack)) {
                continue;
            }
            // if we have a signal and the frontTrack is therefor not in the same section we
            // skip
            if (hasSignalAttached(frontTrack) && !isInsideCurrentSection(currentTrack, frontTrack)) {
                continue;
            }
            // if our frontTrack has already been visited, just take his frontNodes and skip
            // the recursion
            if (!frontTrack.getFrontierNodes().isEmpty()) {
                frontierNodes.addAll(frontTrack.getFrontierNodes());
                continue;
            }
            // actual recursion step
            frontierNodes.addAll(parseTracksToNodes(frontTrack, previousTracks, tracks, knownTracks));
        }

        // basecase
        if (frontier.size() == 0 || frontierNodes.size() == 0) {
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
            if (previousTracks.contains(callbackTrack)) {
                continue;
            }

            // if we have a signal and the callbacktrack is therefor not in the same section
            // we skip
            if (hasSignalAttached(callbackTrack) && !isInsideCurrentSection(currentTrack, callbackTrack)) {
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
            callbackNodes.addAll(parseTracksToNodes(callbackTrack, previousTracks, tracks, knownTracks));
        }

        // merging of callback and frontier Nodes, this is needed to map subpaths
        for (Node frontNode : frontierNodes) {
            // if our callback is empty, we add the given frontierNodes
            if (callBack.size() == 0) {
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
     * Checks if the given track has any signals attached.
     * The method filters the signals of the track and checks if the resulting list
     * is not empty.
     *
     * @param track The track to check for attached signals.
     * @return true if the track has at least one signal attached, false otherwise.
     */
    private boolean hasSignalAttached(Track track) {
        ArrayList<Entity> signals = filterSignals(track.getSignals(), matrix.getMatrix());
        return !signals.isEmpty();
    }

    /**
     * Checks if the next track is inside the current section based on the signals
     * of the next track and the properties of the current and next tracks.
     * The method first filters the signals of the next track. If there are any
     * signals, it checks the properties of the current and next tracks to determine
     * if the next track is inside the current section.
     * The method considers the names and directions of the tracks, and the
     * distances between the current track, the signal, and the next track.
     *
     * @param currentTrack The current track.
     * @param nextTrack    The next track to be checked.
     * @return true if the next track is inside the current section or there are no
     *         signals, false otherwise.
     */
    private boolean isInsideCurrentSection(Track currentTrack, Track nextTrack) {
        ArrayList<Entity> signals = filterSignals(nextTrack.getSignals(), matrix.getMatrix());
        if (!signals.isEmpty()) {
            for (Entity signal : signals) {
                double offset = 0.0;

                // this part maps diagonal straight rails with each other
                if (currentTrack.getName().equals("straight-rail") && nextTrack.getName().equals("straight-rail")) {
                    // look on the x-axis if these type of directions
                    if ((currentTrack.getDirection() == 1 && nextTrack.getDirection() == 5)
                            || (currentTrack.getDirection() == 5 && nextTrack.getDirection() == 1)) {
                        return currentTrack.getPosition().getX() == nextTrack.getPosition().getX();
                    }
                    if ((currentTrack.getDirection() == 3 && nextTrack.getDirection() == 7)
                            || (currentTrack.getDirection() == 7 && nextTrack.getDirection() == 3)) {
                        return currentTrack.getPosition().getY() == nextTrack.getPosition().getY();
                    }
                }

                // this part maps a curved rail to diagonal rail
                if (currentTrack.getName().equals("curved-rail") &&
                        nextTrack.getName().equals("straight-rail") &&
                        nextTrack.getDirection() != 0 &&
                        nextTrack.getDirection() != 2) {
                    // these curved rails are always merged with the diagonal rail
                    if (currentTrack.getDirection() == 7 || currentTrack.getDirection() == 1
                            || currentTrack.getDirection() == 3 || currentTrack.getDirection() == 5) {
                        return true;
                    } else {
                        return false;
                    }
                }

                // this part maps a curved rail to a horizont or vertical straight-rail
                if (currentTrack.getName().equals("curved-rail") && nextTrack.getName().equals("straight-rail")
                        && (nextTrack.getDirection() == 0 || nextTrack.getDirection() == 2)) {
                    offset = 0.3;
                }
                // if the signal is between a curved and a straight the cal need the offset
                if (currentTrack.getDistance(signal) - offset < currentTrack.getDistance(nextTrack)) {
                    return false;
                    // this part maps horizon/vert straight-rails with each other
                } else if (currentTrack.getName().equals("straight-rail")
                        && nextTrack.getName().equals("straight-rail")) {
                    if ((currentTrack.getDirection() == 0 || currentTrack.getDirection() == 2)
                            && currentTrack.getDistance(signal) < 4.3) {
                        return false;
                    }

                }
            }
        }
        return true;
    }

    /**
     * Checks if all the next tracks are inside the current section.
     * The method first filters the signals of the current track. If there are any
     * signals,
     * it checks each next track to see if it's inside the current section. If any
     * next track is not inside the current section,
     * the method returns false. If there are no signals or all next tracks are
     * inside the current section, the method returns true.
     *
     * @param currentTrack The current track from which signals are extracted.
     * @param nextTracks   The list of next tracks to be checked.
     * @return true if all next tracks are inside the current section or there are
     *         no signals, false otherwise.
     */
    private boolean isNextTrackInSection(Track currentTrack, ArrayList<Track> nextTracks) {
        ArrayList<Entity> signals = filterSignals(currentTrack.getSignals(), matrix.getMatrix());
        if (!signals.isEmpty()) {
            for (Track nextTrack : nextTracks) {
                if (!isInsideCurrentSection(nextTrack, currentTrack)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Filters the provided lookups to find matching entities in the given 2D entity
     * array.
     * An entity matches a lookup if they have the same direction and name, and the
     * lookup's x and y coordinates
     * fall within the bounds of the entity array and point to a non-null list of
     * entities. This method is primarily used for signals but it works with all
     * entities.
     *
     * @param lookUps The array of lookups to be filtered.
     * @param entries The 2D array of entity lists to search for matching entities.
     * @return A list of entities that match the provided lookups.
     */
    private static ArrayList<Entity> filterSignals(LookUp[] lookUps, ArrayList<Entity>[][] entries) {
        ArrayList<Entity> validLookUps = new ArrayList<>();
        int x, y;
        int maxX = entries.length;
        int maxY = entries[0].length;
        for (LookUp lookUp : lookUps) { // go through all possible lookups
            x = lookUp.getX();
            y = lookUp.getY();
            if (x >= 0 && y >= 0 // x and y must be positive
                    && x < maxX && y < maxY // must be smaller than the matrix
                    && entries[x][y] != null) { // there has to be entities
                for (int i = 0; i < entries[x][y].size(); i++) { // if all is true, iterate through the entities on x
                                                                 // and y
                    if (entries[x][y].get(i).getDirection() == lookUp.getDirection()
                            && entries[x][y].get(i).getName().equals(lookUp.getName())) { // for a match we need same
                                                                                          // direction and name
                        validLookUps.add(entries[x][y].get(i));
                    }
                }
            }
        }
        return validLookUps;
    }

    /**
     * Filters the provided lookups to find matching tracks in the given 2D track
     * array.
     * A track matches a lookup if they have the same direction and name, and the
     * lookup's x and y coordinates
     * fall within the bounds of the track array and point to a non-null list of
     * tracks.
     *
     * @param lookUps The array of lookups to be filtered.
     * @param tracks  The 2D array of track lists to search for matching tracks.
     * @return A list of tracks that match the provided lookups.
     */
    private static ArrayList<Track> filterLookupsToTrack(LookUp[] lookUps, ArrayList<Track>[][] tracks) {
        ArrayList<Track> validLookUps = new ArrayList<>();
        int x, y;
        int maxX = tracks.length;
        int maxY = tracks[0].length;
        for (LookUp lookUp : lookUps) { // go through all possible lookups
            x = lookUp.getX();
            y = lookUp.getY();
            if (x >= 0 && y >= 0 // x and y must be positive
                    && x < maxX && y < maxY // must be smaller than the matrix
                    && tracks[x][y] != null) { // there has to be entities
                for (int i = 0; i < tracks[x][y].size(); i++) { // if all is true, iterate through the entities on x and
                                                                // y
                    if (tracks[x][y].get(i).getDirection() == lookUp.getDirection()
                            && tracks[x][y].get(i).getName().equals(lookUp.getName())) { // for a match we need same
                                                                                         // direction and name
                        validLookUps.add(tracks[x][y].get(i));
                    }
                }
            }
        }
        return validLookUps;
    }
}
