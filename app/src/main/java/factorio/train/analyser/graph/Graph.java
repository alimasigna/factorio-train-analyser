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


        ArrayList<Entity>[][] entries = matrix.getMatrix();
        ArrayList<ArrayList<Track>> Nodes;
        for( int x = 0; x < entries.length; x++) {
            for( int y = 0; y < entries[x].length; y++) {
                if(entries[x][y] == null) continue;
                for( int i = 0; i < entries[x][y].size(); i++) {
                    LookUp[] connections = filterLookups(entries[x][y].get(i).getConnected(), entries);
                    LookUp[] crossings = filterLookups(entries[x][y].get(i).getCrossed(), entries);
                    System.out.println("DEBUG");
                }
            }
        }
    }

    //removes all LookUps to empty or non-existent coordinates
    private static LookUp[] filterLookups(LookUp[] lookUps, ArrayList<Entity>[][] entries) {
        ArrayList<LookUp> validLookUps = new ArrayList<>();
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
                        validLookUps.add(lookUp);
                    }
                }
            }
        }
        return validLookUps.toArray(new LookUp[0]);
    }
}
