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
        setNodes();
    }

    private void setMatrix(String encodedString) {
        if (encodedString != null)
            matrix = new Matrix(encodedString);
    }

    private void setNodes() {
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
                    // TODO check later if its okay
                    Section section = new Section(nodes);
                    sections.add(section);
                    nodes = new ArrayList<>(); // resetting nodes after each run
                }
            }
        }
        mergeSections(tracks);
        mapDirectionsToNodes(tracks);
        System.out.println("XDD");
    }

    private void mapDirectionsToNodes(ArrayList<Track>[][] tracks) {
        for (int x = 0; x < tracks.length; x++) { 
            for (int y = 0; y < tracks[x].length; y++) {
                if (tracks[x][y] == null)
                    continue;
                for (int i = 0; i < tracks[x][y].size(); i++) {
                    Track track = tracks[x][y].get(i);
                    if(track.goesTo.isEmpty() && track.dependsOn.isEmpty())
                        continue;
                    ArrayList<Node> parentNodes = track.getNodes();
                    ArrayList<Track> outGoingTracks = track.goesTo;
                    ArrayList<Track> outDependendTracks = track.dependsOn;
                    for(Node parentNode : parentNodes) {
                        for(Track outGoingTrack : outGoingTracks) {
                            parentNode.setNextNodes(outGoingTrack.getNodes());
                        }
                        for(Track outDependendTrack : outDependendTracks) {
                            parentNode.setDependsOn(outDependendTrack.getNodes());
                        }
                    }
                }
            }
        }
    }

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

    private void connectTracks(ArrayList<Track> nextTracks, Track currentTrack, ArrayList<Track>[][] tracks) {
        if (!nextTracks.isEmpty()) {
            ArrayList<Entity> signals = filterSignals(currentTrack.getSignals(), matrix.getMatrix());
            for (Entity signal : signals) {
                ArrayList<Track> outGoingTracks = filterLookupsToTrack(LookUp.lookUpOutgoingTracks(signal), tracks);
                if (outGoingTracks.contains(nextTracks.get(0))) {
                    for (Track nextTrack : nextTracks) {
                        currentTrack.goesTo.add(nextTrack);
                        if (signal.getName().equals("rail-chain-signal"))
                            currentTrack.dependsOn.add(nextTrack);
                    }
                } else {
                    for (Track nextTrack : nextTracks) {
                        nextTrack.goesTo.add(currentTrack);
                        if (signal.getName().equals("rail-chain-signal"))
                            nextTrack.dependsOn.add(currentTrack);
                    }
                }

            }
        }
    }

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

        ArrayList<Track> in = filterLookupsToTrack(currentTrack.getConnected()[0], tracks);
        ArrayList<Track> out = filterLookupsToTrack(currentTrack.getConnected()[1], tracks);

        ArrayList<Track> frontier = new ArrayList<>();
        ArrayList<Track> callBack = new ArrayList<>();

        // set frontier and callback
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

        // checks if the frontier/callback is behind a signal, if yes we ignore it
        if (!isNextTrackInSection(currentTrack, frontier)) {
            connectTracks(frontier, currentTrack, tracks);
            frontier = new ArrayList<>();
        }
        if (!isNextTrackInSection(currentTrack, callBack)) {
            connectTracks(callBack, currentTrack, tracks);
            callBack = new ArrayList<>();
        }

        ArrayList<Node> frontierNodes = new ArrayList<>();
        ArrayList<Node> callbackNodes = new ArrayList<>();

        // frontier rec step
        for (Track frontTrack : frontier) {
            if (previousTracks.contains(frontTrack)) {
                continue;
            }
            // if the signal is closer than the track, we stop here
            if (hasSignalAttached(frontTrack) && !isInsideCurrentSection(currentTrack, frontTrack)) {
                continue;
            }
            if (!frontTrack.getFrontierNodes().isEmpty()) {
                frontierNodes.addAll(frontTrack.getFrontierNodes());
                continue;
            }
            frontierNodes.addAll(parseTracksToNodes(frontTrack, previousTracks, tracks, knownTracks));
        }
        if (frontier.size() == 0 || frontierNodes.size() == 0) { // Basecase
            Node base = new Node();
            frontierNodes.add(base);
        }
        for (Node frontNode : frontierNodes) { // add current track to incoming frontier nodes
            frontNode.addTrack(currentTrack);
            currentTrack.addNode(frontNode);
            currentTrack.getFrontierNodes().add(frontNode);
        }
        // callback rec step
        ArrayList<Track> temp = new ArrayList<>(callBack);
        for (Track callbackTrack : temp) {
            if (callbackTrack == previousTrack)
                continue;
            // the followgin if statements are need possible loops inside a node
            if (previousTracks.contains(callbackTrack)) {
                continue;
            }
            // if the signal is closer than the track, we stop here
            if (hasSignalAttached(callbackTrack) && !isInsideCurrentSection(currentTrack, callbackTrack)) {
                callBack.remove(callbackTrack);
                continue;
            }
            if (!callbackTrack.getFrontierNodes().isEmpty()) {
                callbackNodes.addAll(callbackTrack.getFrontierNodes());
                continue;
            }
            callbackNodes.addAll(parseTracksToNodes(callbackTrack, previousTracks, tracks, knownTracks));
        }
        // merging of callback and frontier Nodes
        for (Node frontNode : frontierNodes) {
            if (callBack.size() == 0) { // if our callback is empty, we add the given frontierNodes
                nodes.add(frontNode);
                continue;
            }
            for (Node callbackNode : callbackNodes) {
                Node base = Node.mergeNodes(frontNode, callbackNode);
                nodes.add(base);
                nodes.remove(callbackNode);
            }
        }

        return frontierNodes; // gives back all possible connected nodes
    }

    private boolean hasSignalAttached(Track track) {
        ArrayList<Entity> signals = filterSignals(track.getSignals(), matrix.getMatrix());
        return !signals.isEmpty();
    }

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

    // removes all LookUps to empty or non-existent coordinates
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
