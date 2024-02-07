package factorio.train.analyser.graph;

import factorio.train.analyser.Matrix;
import factorio.train.analyser.jsonmodels.Entity;

import java.util.ArrayList;

public class Graph {
    private Section[] sections;

    private ArrayList<Node> nodes;
    private Matrix matrix;

    public Graph(String encodedString){
        nodes = new ArrayList<>();
        setMatrix(encodedString);
        setNodes();
    }

    private void setMatrix(String encodedString) {
        if(encodedString!=null) matrix = new Matrix(encodedString);
    }

    private void setNodes() {
        if(matrix == null) return;

        ArrayList<Track> knownTracks = new ArrayList<>();
        ArrayList<Track>[][] tracks = matrix.convertToTracks();

        for( int x = 0; x < tracks.length; x++) {          //go through all tracks, we only go through tracks. signals should be discovered this way too
            for (int y = 0; y < tracks[x].length; y++) {
                if (tracks[x][y] == null) continue;
                for (int i = 0; i < tracks[x][y].size(); i++) {
                    Track track = tracks[x][y].get(i);
                    if (knownTracks.contains(track)) continue; //we only use tracks skip everything
                    //recNode(track, null, tracks, knownTracks, false);
                    betterRec(track,null, tracks, knownTracks);
                }
            }
        }
        System.out.println("XDD");
    }

    private ArrayList<Node> betterRec(Track currentTrack, ArrayList<Track> visitedTracks, ArrayList<Track>[][] tracks, ArrayList<Track> knownTracks) {
        //store path callbyvalue
        Track previousTrack;
        ArrayList<Track> previousTracks = new ArrayList<>();
        if(visitedTracks == null) {
            previousTrack = null;
        } else {
            previousTracks.addAll(visitedTracks);
            previousTrack = previousTracks.get(previousTracks.size()-1);
        }
        previousTracks.add(currentTrack);
        knownTracks.add(currentTrack);

        ArrayList<Track> in = filterLookupsToTrack(currentTrack.getConnected()[0], tracks);
        ArrayList<Track> out = filterLookupsToTrack(currentTrack.getConnected()[1], tracks);

        ArrayList<Track> frontier = new ArrayList<>();
        ArrayList<Track> callBack = new ArrayList<>();

        //set frontier and callback
        if(previousTrack == null) {
            frontier = in;
            callBack = out;
        } else if(in.contains(previousTrack)) {
            callBack = in;
            frontier = out;
        } else if(out.contains(previousTrack)) {
            callBack = out;
            frontier = in;
        }

        ArrayList<Node> frontierNodes = new ArrayList<>();
        ArrayList<Node> callbackNodes = new ArrayList<>();

        for( Track frontTrack : frontier ){
            if(previousTracks.contains(frontTrack)) {
                continue;
            }
            if(!frontTrack.getFrontierNodes().isEmpty()) {
                frontierNodes.addAll(frontTrack.getFrontierNodes());
                continue;
            }
            frontierNodes.addAll(betterRec(frontTrack, previousTracks, tracks, knownTracks));
        }

        if(frontier.size() == 0) { //Basecase
            Node base = new Node();
            frontierNodes.add(base);
        }

        for( Node frontNode : frontierNodes ) { //add current track to incoming frontier nodes
            frontNode.addTrack(currentTrack);
            currentTrack.addNode(frontNode);
            currentTrack.getFrontierNodes().add(frontNode);
        }

        for ( Track callbackTrack : callBack ) {
            if(callbackTrack == previousTrack) continue;
            //the followgin if statements are need possible loops inside a node
            if(previousTracks.contains(callbackTrack)) {
                continue;
            }
            if(!callbackTrack.getFrontierNodes().isEmpty()) {
                callbackNodes.addAll(callbackTrack.getFrontierNodes());
                continue;
            }
                callbackNodes.addAll(betterRec(callbackTrack, previousTracks, tracks, knownTracks));
        }



        for( Node frontNode : frontierNodes ) {
            if(callBack.size()==0){ //if our callback is empty, we add the given frontierNodes
                nodes.add(frontNode);
                continue;
            }
                for( Node callbackNode : callbackNodes ) {
                    Node temp = Node.mergeNodes(frontNode, callbackNode);
                    nodes.add(temp);
                    nodes.remove(callbackNode);
                }
        }

        return frontierNodes; // gives back all possible connected nodes
    }

    //removes all LookUps to empty or non-existent coordinates
    private static Entity[] filterLookups(LookUp[] lookUps, ArrayList<Entity>[][] entries) {
        ArrayList<Entity> validLookUps = new ArrayList<>();
        int x, y;
        int maxX = entries.length;
        int maxY = entries[0].length;
        for (LookUp lookUp : lookUps) { //go through all possible lookups
            x = lookUp.getX();
            y = lookUp.getY();
            if (x >= 0 && y >= 0 //x and y must be positive
                    && x < maxX && y < maxY //must be smaller than the matrix
                    && entries[x][y] != null) { //there has to be entities
                for( int i = 0; i < entries[x][y].size(); i++) { //if all is true, iterate through the entities on x and y
                    if( entries[x][y].get(i).getDirection() == lookUp.getDirection()
                    && entries[x][y].get(i).getName().equals(lookUp.getName())) { //for a match we need same direction and name
                        validLookUps.add(entries[x][y].get(i));
                    }
                }
            }
        }
        return validLookUps.toArray(new Entity[0]);
    }

    private static ArrayList<Track> filterLookupsToTrack(LookUp[] lookUps, ArrayList<Track>[][] tracks) {
        ArrayList<Track> validLookUps = new ArrayList<>();
        int x, y;
        int maxX = tracks.length;
        int maxY = tracks[0].length;
        for (LookUp lookUp : lookUps) { //go through all possible lookups
            x = lookUp.getX();
            y = lookUp.getY();
            if (x >= 0 && y >= 0 //x and y must be positive
                    && x < maxX && y < maxY //must be smaller than the matrix
                    && tracks[x][y] != null) { //there has to be entities
                for( int i = 0; i < tracks[x][y].size(); i++) { //if all is true, iterate through the entities on x and y
                    if( tracks[x][y].get(i).getDirection() == lookUp.getDirection()
                            && tracks[x][y].get(i).getName().equals(lookUp.getName())) { //for a match we need same direction and name
                        validLookUps.add(tracks[x][y].get(i));
                    }
                }
            }
        }
        return validLookUps;
    }
}
