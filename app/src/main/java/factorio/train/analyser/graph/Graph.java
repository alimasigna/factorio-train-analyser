package factorio.train.analyser.graph;

import factorio.train.analyser.Matrix;
import factorio.train.analyser.jsonmodels.Entity;

import java.util.ArrayList;

public class Graph {
    private Section[] sections;
    private Matrix matrix;

    public Graph(String encodedString){
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
        ArrayList<Node> Nodes = new ArrayList<>();
        for( int x = 0; x < entries.length; x++) {
            for( int y = 0; y < entries[x].length; y++) {
                if(entries[x][y] == null) continue;
                for( int i = 0; i < entries[x][y].size(); i++) {
                    Entity entry = entries[x][y].get(i);
                    if(knownEntries.contains(entry) || !( entry.getName().equals("straight-rail") || entry.getName().equals("curved-rail") )) continue; //we only use rails skip everything
                    recTest(Nodes, entry, null, knownEntries, entries);
                }
            }
        }
    }

    private void recTest(ArrayList<Node> Nodes, Entity entry, Node currentNode, ArrayList<Entity> knownEntries, ArrayList<Entity>[][] entries){
        if(knownEntries.contains(entry)) return; //if we already visited this track we can skip it
        knownEntries.add(entry);
        Track track;
        if(currentNode == null) {   // we currently have no node selected
            currentNode = new Node();
        }
        track = new Track(currentNode, entry.getName().equals("straight-rail") ? 1 : 3, entry.getDirection(), (int) entry.getPosition().getX(), (int) entry.getPosition().getY());
        currentNode.addTrack(track);
        if(!Nodes.contains(currentNode)) Nodes.add(currentNode);

        Entity[] connections = filterLookups(entry.getConnected(), entries);

        Node clone = currentNode.cloneNode(); // we always clone the current node incase there might be multiple new connections
        int counter = 0;
        for(Entity connection : connections) {
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
}
