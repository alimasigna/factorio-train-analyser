package factorio.train.analyser.graph;

import factorio.train.analyser.Matrix;
import factorio.train.analyser.jsonmodels.Entity;

import java.util.ArrayList;

public class Graph {
    private Section[] sections;

    private ArrayList<Node> nodesFIELD;
    private Matrix matrix;

    public Graph(String encodedString){
        nodesFIELD = new ArrayList<>();
        setMatrix(encodedString);
        setNodes();
    }

    private void setMatrix(String encodedString) {
        if(encodedString!=null) matrix = new Matrix(encodedString);
    }

    private void setNodes() { //TODO this method is a wip.
        if(matrix == null) return;

        ArrayList<Entity> knownEntries = new ArrayList<>();
        ArrayList<Entity>[][] entries = matrix.getMatrix();
        ArrayList<Track>[][] tracks = matrix.convertToTracks();

        for( int x = 0; x < tracks.length; x++) {
            for( int y = 0; y < entries[x].length; y++) {
                if(entries[x][y] == null) continue;
                for( int i = 0; i < entries[x][y].size(); i++) {
                    Entity entry = entries[x][y].get(i);
                    if(knownEntries.contains(entry) || !( entry.getName().equals("straight-rail") || entry.getName().equals("curved-rail") )) continue; //we only use rails skip everything
                    Track track = tracks[x][y].get(i);
                    recLeftNode(track, tracks, entries, knownEntries);
                }
            }
        }

        /*ArrayList<Node> Nodes = new ArrayList<>();
        for( int x = 0; x < entries.length; x++) {
            for( int y = 0; y < entries[x].length; y++) {
                if(entries[x][y] == null) continue;
                for( int i = 0; i < entries[x][y].size(); i++) {
                    Entity entry = entries[x][y].get(i);
                    if(knownEntries.contains(entry) || !( entry.getName().equals("straight-rail") || entry.getName().equals("curved-rail") )) continue; //we only use rails skip everything
                    recTest(Nodes, entry, null, knownEntries, entries);
                }
            }
        }*/
    }

    private void recRightNode(Track currentTrack, ArrayList<Track>[][] tracks, ArrayList<Entity>[][] entries, ArrayList<Entity> knownEntries) {
        Track[] right = filterLookupsToTrack(currentTrack.getConnected()[1], tracks);
        if(currentTrack.getNodes() == null) {
            recLeftNode(currentTrack, tracks, entries, knownEntries);
        }
        if(right.length == 0) { //if we got all left nodes, we go for the right ones
            return;
        } else {
            for(int i = 0; i<right.length; i++) {
                recLeftNode(right[i], tracks, entries, knownEntries);
            }
        }
    }
    private void recLeftNode(Track currentTrack, ArrayList<Track>[][] tracks, ArrayList<Entity>[][] entries, ArrayList<Entity> knownEntries) {
        Track[] left = filterLookupsToTrack(currentTrack.getConnected()[0], tracks);
        Track[] right = filterLookupsToTrack(currentTrack.getConnected()[1], tracks);

        ArrayList<Node> currentParentNodes = currentTrack.getNodes();

        if(left.length == 0) { //basecase if there are no connected
            Node newParentNode = new Node();
            currentTrack.addParentNode(newParentNode);
            newParentNode.addTrack(currentTrack); //if we reache basecase we add node
        } else {
            for(Track trackLeft : left) { //go thorugh all left tracks
                ArrayList<Node> parentNodesLeft = trackLeft.getNodes();
                if(parentNodesLeft.size() == 0) { //if we have no parents recursivly call this method
                    recLeftNode(trackLeft, tracks, entries, knownEntries);
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
            if(right.length>1) { // we only clone if we now that there are more than one exists
                for(int k = 1; k<right.length; k++) { // if we have 2 endings we only need 1 clone etc
                    clone.add(givenNodes.get(i).cloneNode());
                }
            }

            for(int j = 0; j<right.length; j++) {
                if(j==0){
                    connectTrackToNode(currentTrack, givenNodes.get(i));
                    connectTrackToNode(right[j], givenNodes.get(i));
                    continue;
                }
                connectTrackToNode(currentTrack, clone.get(j-1));
                connectTrackToNode(right[j], clone.get(j-1));
            }
        }

        //now we add the currentParentNodes to the overall Nodespool
        for(Node parent : currentParentNodes) {
            if(!nodesFIELD.contains(parent)) {
                nodesFIELD.add(parent);
            }
        }

        if(right.length == 0) { //if we got all left nodes, we go for the right ones
            return;
        } else {
            for(int i = 0; i<right.length; i++) { //TODO endless rec
                recLeftNode(right[i], tracks, entries, knownEntries);
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
    /*private void recTest(ArrayList<Node> Nodes, Entity entry, Node currentNode, ArrayList<Entity> knownEntries, ArrayList<Entity>[][] entries){
        if(knownEntries.contains(entry)) return; //if we already visited this track we can skip it
        knownEntries.add(entry);
        Track track;
        if(currentNode == null) {   // we currently have no node selected
            currentNode = new Node();
        }
        track = new Track(currentNode, entry.getName().equals("straight-rail") ? 1 : 3, entry.getDirection(), (int) entry.getPosition().getX(), (int) entry.getPosition().getY());
        currentNode.addTrack(track);
        if(!Nodes.contains(currentNode)) Nodes.add(currentNode);

        Entity[] connectionsL = filterLookups(entry.getConnected()[0], entries);
        Entity[] connectionsR = filterLookups(entry.getConnected()[1], entries);

        Node clone = currentNode.cloneNode(); // we always clone the current node incase there might be multiple new connections
        int counter = 0;
        for(Entity connection : connectionsL) {
            if(knownEntries.contains(connection)) {
                //TODO add nodes to the current track, if we are on an end it should stop
                continue;
            }
            if(counter > 0) { //if we have more connection we gotta make new nodes
                recTest(Nodes, connection, clone, knownEntries, entries);
            } else {
                recTest(Nodes, connection, currentNode, knownEntries, entries);
            }

            counter++;
        }
        System.out.println("WE LEFT REC");
        Entity[] crossings = filterLookups(entry.getCrossed(), entries);

    }*/

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

    private static Track[] filterLookupsToTrack(LookUp[] lookUps, ArrayList<Track>[][] tracks) {
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
        return validLookUps.toArray(new Track[0]);
    }
}
