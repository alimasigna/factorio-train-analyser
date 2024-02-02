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
                    recLeftNode(track, null, tracks, knownTracks, false);
                }
            }
        }
        System.out.println("XDD");
    }

    private void recLeftNode(Track currentTrack, Track previousTrack, ArrayList<Track>[][] tracks, ArrayList<Track> knownTracks, boolean calledFromForwardRec) {
        knownTracks.add(currentTrack);
        //we gotta split the recursion
        ArrayList<Track> in = filterLookupsToTrack(currentTrack.getConnected()[0], tracks);
        ArrayList<Track> out = filterLookupsToTrack(currentTrack.getConnected()[1], tracks);

        //this is default if its null
        ArrayList<Track> frontier = new ArrayList<>();
        ArrayList<Track> callBack = new ArrayList<>();

        if(previousTrack != null){  //check if we have been called from re
            if(calledFromForwardRec) { //check if called from rightsided rec
                if(in.contains(previousTrack)) { //frontier is where we come from
                    frontier = in;
                    callBack = out;
                } else {
                    frontier = out;
                    callBack = in;
                }
            } else { //check if called from leftsided rec
                if(in.contains(previousTrack)) {
                    frontier = out;
                    callBack = in;
                } else {
                    frontier = in;
                    callBack = out;
                }
            }
        } else {
            frontier = in;
            callBack = out;
        }

        ArrayList<Node> currentParentNodes = currentTrack.getNodes();

        if(frontier.size() == 0) { //basecase if there are no connected
            Node newParentNode = new Node();
            currentTrack.addParentNode(newParentNode);
            newParentNode.addTrack(currentTrack); //if we reache basecase we add node
        } else {
            for(Track trackLeft : frontier) { //go thorugh all frontier tracks
                ArrayList<Node> parentNodesLeft = trackLeft.getNodes();
                if(parentNodesLeft.size() == 0) { //if we have no parents recursivly call this method
                    recLeftNode(trackLeft, currentTrack, tracks, knownTracks, false);
                } //we go down to basecas  there might be loops after we reach basecase we should have a parent
                // allLeftParentNodes.addAll(parentNodesLeft); //we add all found nodes on the left
            }
        }

        //now we gotta add the nodes we have received to our right neighbours
        ArrayList<Node> givenNodes = new ArrayList<>(); //we use giveNodes that have been previously given by rec
        givenNodes.addAll(currentParentNodes);
        for( int i = 0; i<givenNodes.size(); i++) {
            //we generate clones if we have more than 1 exit
            ArrayList <Node> clone = new ArrayList<>();
            if(callBack.size()>1) { // we only clone if we now that there are more than one exists
                for(int k = 1; k<callBack.size(); k++) { // if we have 2 endings we only need 1 clone etc
                    clone.add(givenNodes.get(i).cloneNode());
                }
            }

            for(int j = 0; j<callBack.size(); j++) {
                if(j==0){
                    connectTrackToNode(currentTrack, givenNodes.get(i));
                    connectTrackToNode(callBack.get(j), givenNodes.get(i));
                    continue;
                }
                connectTrackToNode(currentTrack, clone.get(j-1));
                connectTrackToNode(callBack.get(j), clone.get(j-1));
            }
        }

        //now we add the currentParentNodes to the overall Nodespool
        for(Node parent : currentParentNodes) {
            if(!nodes.contains(parent)) {
                nodes.add(parent);
            }
        }

        if(callBack.size() == 0) { //if we got all left nodes, we go for the right ones
            return;
        } else {
            for(int i = 0; i<callBack.size(); i++) {
                recLeftNode(callBack.get(i), currentTrack, tracks, knownTracks, true);
            }
        }

    }

    private void connectTrackToNode(Track track, Node node) {
        if(!node.getTracks().contains(track)) {
            node.addTrack(track);
        }
        if(!track.getNodes().contains(node)) {
            track.addParentNode(node);
        }
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
