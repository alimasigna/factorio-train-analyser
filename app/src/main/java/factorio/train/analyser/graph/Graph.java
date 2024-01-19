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
                    LookUp[] neighbours = entries[x][y].get(i).getConnected();
                    LookUp[] intersects = entries[x][y].get(i).getCrossed();
                    System.out.println("DEBUG");
                }
            }
        }
    }
}
