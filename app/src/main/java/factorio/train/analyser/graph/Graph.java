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
                    lookUpNeighbours(entries[x][y].get(i));
                    System.out.println("DEBUG");
                }
            }
        }
    }

    // This method maps all the possible rails that can be CONNECTED to the given entity
    private LookUp[] lookUpNeighbours(Entity entry) {
        LookUp[] neighbours = {};
        double xPos = entry.getPosition().getX();
        double yPos = entry.getPosition().getY();
        switch (entry.getName()) {
            case "straight-rail" :
                switch (entry.getDirection()) {
                    case 0: // |
                        neighbours = new LookUp[]{
                                new LookUp(xPos, yPos + 4,0, "straight-rail"),
                                new LookUp(xPos, yPos - 4,0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 10,0, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 10,1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 10,5, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 10,4, "curved-rail")
                        };
                        break;
                    case 2: // -
                        neighbours = new LookUp[]{
                                new LookUp(xPos + 4, yPos,2, "straight-rail"),
                                new LookUp(xPos - 4, yPos,2, "straight-rail"),
                                new LookUp(xPos + 10, yPos - 2,2, "curved-rail"),
                                new LookUp(xPos + 10, yPos + 2,3, "curved-rail"),
                                new LookUp(xPos - 10, yPos - 2,7, "curved-rail"),
                                new LookUp(xPos - 10, yPos + 2,6, "curved-rail")
                        };
                        break;
                    case 5: // \
                        neighbours = new LookUp[]{
                                new LookUp(xPos, yPos + 4,1, "straight-rail"),
                                new LookUp(xPos - 4, yPos,1, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 6,4, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 6,7, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6,0, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2,3, "curved-rail"),
                        };
                        break;
                    case 1: // \
                        neighbours = new LookUp[]{
                                new LookUp(xPos, yPos - 4,5, "straight-rail"),
                                new LookUp(xPos + 4, yPos,5, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 6,4, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2,7, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 6,0, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 6,3, "curved-rail"),
                        };
                        break;
                    case 3: // /
                        neighbours = new LookUp[]{
                                new LookUp(xPos, yPos + 4,7, "straight-rail"),
                                new LookUp(xPos + 4, yPos,7, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 6,5, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2,6, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 6,2, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6,1, "curved-rail"),
                        };
                        break;
                    case 7: // /
                        neighbours = new LookUp[]{
                                new LookUp(xPos, yPos - 4,3, "straight-rail"),
                                new LookUp(xPos - 4, yPos,3, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6,5, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 6,6, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 2,2, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 6,1, "curved-rail"),
                        };
                        break;
                }
                break;
            case "curved-rail" :
                switch (entry.getDirection()) {
                    case 0: // '|
                        neighbours = new LookUp[]{
                                new LookUp(xPos + 2, yPos + 10,0, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 6,1, "straight-rail"),
                                new LookUp(xPos, yPos + 16,5, "curved-rail")
                        };
                        break;
                    case 1: // |'
                        neighbours = new LookUp[]{
                                new LookUp(xPos - 2, yPos + 10,0, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 6,7, "straight-rail"),
                                new LookUp(xPos, yPos + 16, 4, "curved-rail")
                        };
                        break;
                    case 2: // --'
                        neighbours = new LookUp[]{
                                new LookUp(xPos - 10, yPos + 2,2, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 6,3, "straight-rail"),
                                new LookUp(xPos - 16, yPos, 7, "curved-rail")
                        };
                        break;
                    case 3: // --,
                        neighbours = new LookUp[]{
                                new LookUp(xPos - 10, yPos - 2,2, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 6,1, "straight-rail"),
                                new LookUp(xPos - 16, yPos, 6, "curved-rail")
                        };
                        break;
                    case 4: // |,
                        neighbours = new LookUp[]{
                                new LookUp(xPos - 2, yPos - 10,0, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 6,5, "straight-rail"),
                                new LookUp(xPos, yPos - 16, 1, "curved-rail")
                        };
                        break;
                    case 5: // ,|
                        neighbours = new LookUp[]{
                                new LookUp(xPos + 2, yPos - 10,0, "straight-rail"),
                                new LookUp(xPos - 6, yPos + 6,3, "straight-rail"),
                                new LookUp(xPos, yPos - 16, 0, "curved-rail")
                        };
                        break;
                    case 6: // ,--
                        neighbours = new LookUp[]{
                                new LookUp(xPos + 10, yPos - 2,2, "straight-rail"),
                                new LookUp(xPos - 6, yPos + 6,7, "straight-rail"),
                                new LookUp(xPos + 16, yPos, 3, "curved-rail")
                        };
                        break;
                    case 7: // '--
                        neighbours = new LookUp[]{
                                new LookUp(xPos + 10, yPos + 2,2, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 6,5, "straight-rail"),
                                new LookUp(xPos + 16, yPos, 2, "curved-rail")
                        };
                        break;
                }
                break;
        }
        return neighbours;
    }

    // This method maps all the possible rails that can intersect to the given entity. please note they are NOT connected
    private LookUp[] lookUpIntersects(Entity entry) {
        LookUp[] neighbours = {};
        double xPos = entry.getPosition().getX();
        double yPos = entry.getPosition().getY();
        switch (entry.getName()) {
            case "straight-rail" :
                switch (entry.getDirection()) {
                    case 0: // |
                        neighbours = new LookUp[]{
                                new LookUp(xPos, yPos,2, "straight-rail"),
                                new LookUp(xPos, yPos,5, "straight-rail"),
                                new LookUp(xPos, yPos,1, "straight-rail"),
                                new LookUp(xPos, yPos,3, "straight-rail"),
                                new LookUp(xPos, yPos,7, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 2,2, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,2, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,2, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2,2, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,2, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,2, "curved-rail"),

                                new LookUp(xPos - 6, yPos + 2,6, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,6, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,6, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2,6, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,6, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,6, "curved-rail"),

                                new LookUp(xPos - 6, yPos - 2,3, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,3, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,3, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,3, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,3, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2,3, "curved-rail"),

                                new LookUp(xPos + 6, yPos + 2,7, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,7, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,7, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,7, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,7, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2,7, "curved-rail"),

                                new LookUp(xPos - 2, yPos + 2,4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,4, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6,4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6,4, "curved-rail"),

                                new LookUp(xPos + 2, yPos - 2,0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,0, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6,0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6,0, "curved-rail"),

                                new LookUp(xPos - 2, yPos + 6,1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,1, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,1, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6,1, "curved-rail"),

                                new LookUp(xPos + 2, yPos - 6,5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,5, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,5, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6,5, "curved-rail")
                        };
                        break;
                    case 2: // -
                        neighbours = new LookUp[]{
                                new LookUp(xPos, yPos,0, "straight-rail"),
                                new LookUp(xPos, yPos,5, "straight-rail"),
                                new LookUp(xPos, yPos,1, "straight-rail"),
                                new LookUp(xPos, yPos,3, "straight-rail"),
                                new LookUp(xPos, yPos,7, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 6,1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,1, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,1, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6,1, "curved-rail"),

                                new LookUp(xPos - 2, yPos + 6,5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,5, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,5, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6,5, "curved-rail"),

                                new LookUp(xPos + 6, yPos + 2,3, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,3, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,3, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,3, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,3, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2,3, "curved-rail"),

                                new LookUp(xPos + 6, yPos + 2,7, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,7, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,7, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,7, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,7, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2,7, "curved-rail"),

                                new LookUp(xPos + 2, yPos + 6,4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,4, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,4, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6,4, "curved-rail"),

                                new LookUp(xPos + 2, yPos + 6,0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,0, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,0, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6,0, "curved-rail"),

                                new LookUp(xPos - 6, yPos + 2,6, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,6, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,6, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,6, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2,6, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,6, "curved-rail"),

                                new LookUp(xPos - 6, yPos + 2,2, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,2, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,2, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,2, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2,2, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,2, "curved-rail")

                        };
                        break;
                    case 5: // \
                        neighbours = new LookUp[]{
                                new LookUp(xPos, yPos,2, "straight-rail"),
                                new LookUp(xPos - 4, yPos,2, "straight-rail"),
                                new LookUp(xPos, yPos,7, "straight-rail"),
                                new LookUp(xPos - 4, yPos,3, "straight-rail"),
                                new LookUp(xPos, yPos,0, "straight-rail"),
                                new LookUp(xPos, yPos + 4,0, "straight-rail"),
                                new LookUp(xPos, yPos,3, "straight-rail"),
                                new LookUp(xPos, yPos + 4,7, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 6,5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6,5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,5, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6,5, "curved-rail"),

                                new LookUp(xPos - 2, yPos + 6,4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6,4, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,4, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2,4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,4, "curved-rail"),

                                new LookUp(xPos - 6, yPos + 6,1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6,1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,1, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6,1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6,1, "curved-rail"),

                                new LookUp(xPos + 2, yPos + 2,0, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,0, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 6,0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6,0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6,0, "curved-rail"),

                                new LookUp(xPos - 2, yPos - 2,3, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2,3, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,3, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,3, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2,3, "curved-rail"),

                                new LookUp(xPos - 10, yPos - 2,7, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2,7, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,7, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,7, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,7, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6,7, "curved-rail"),

                                new LookUp(xPos - 10, yPos + 2,6, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 2,6, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,6, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,6, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,6, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2,6, "curved-rail"),

                                new LookUp(xPos - 6, yPos + 6,2, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 2,2, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,2, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2,2, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,2, "curved-rail")
                        };
                        break;
                    case 1: // \
                        neighbours = new LookUp[]{
                                new LookUp(xPos, yPos,2, "straight-rail"),
                                new LookUp(xPos + 4, yPos,2, "straight-rail"),
                                new LookUp(xPos, yPos,3, "straight-rail"),
                                new LookUp(xPos + 4, yPos,7, "straight-rail"),
                                new LookUp(xPos, yPos,0, "straight-rail"),
                                new LookUp(xPos, yPos + 4,0, "straight-rail"),
                                new LookUp(xPos, yPos,7, "straight-rail"),
                                new LookUp(xPos, yPos - 4,3, "straight-rail"),

                                new LookUp(xPos + 6, yPos - 6,1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6,1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,1, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6,1, "curved-rail"),

                                new LookUp(xPos + 2, yPos - 6,0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6,0, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,0, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2,0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,0, "curved-rail"),

                                new LookUp(xPos + 6, yPos - 6,5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6,5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,5, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6,5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6,5, "curved-rail"),

                                new LookUp(xPos - 2, yPos - 2,4, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,4, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 6,4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6,4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6,4, "curved-rail"),

                                new LookUp(xPos + 2, yPos + 2,7, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2,7, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,7, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,7, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2,7, "curved-rail"),

                                new LookUp(xPos + 10, yPos + 2,3, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2,3, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,3, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,3, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,3, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6,3, "curved-rail"),

                                new LookUp(xPos + 10, yPos - 2,2, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2,2, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,2, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,2, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,2, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 2,2, "curved-rail"),

                                new LookUp(xPos - 6, yPos + 2,6, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,6, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2,6, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 6,6, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,6, "curved-rail")
                        };
                        break;
                    case 3: // /
                        neighbours = new LookUp[]{
                                new LookUp(xPos, yPos,0, "straight-rail"),
                                new LookUp(xPos, yPos + 2,0, "straight-rail"),
                                new LookUp(xPos, yPos,1, "straight-rail"),
                                new LookUp(xPos + 2, yPos,5, "straight-rail"),
                                new LookUp(xPos, yPos,2, "straight-rail"),
                                new LookUp(xPos + 2, yPos,2, "straight-rail"),
                                new LookUp(xPos, yPos,5, "straight-rail"),
                                new LookUp(xPos, yPos + 2,1, "straight-rail"),

                                new LookUp(xPos + 2, yPos + 2,4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6,4, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 6,4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6,4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,4, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,4, "curved-rail"),

                                new LookUp(xPos + 2, yPos + 6,5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6,5, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,5, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2,5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,5, "curved-rail"),

                                new LookUp(xPos + 6, yPos + 6,0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6,0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6,0, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6,0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,0, "curved-rail"),

                                new LookUp(xPos - 2, yPos + 2,1, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6,1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6,1, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 6,1, "curved-rail"),

                                new LookUp(xPos + 2, yPos - 2,6, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2,6, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,6, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,6, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 2,6, "curved-rail"),

                                new LookUp(xPos - 2, yPos + 6,2, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,2, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,2, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,2, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2,2, "curved-rail"),
                                new LookUp(xPos + 10, yPos - 2,2, "curved-rail"),

                                new LookUp(xPos + 10, yPos + 2,3, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2,3, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,3, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,3, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,3, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2,3, "curved-rail"),

                                new LookUp(xPos - 6, yPos - 2,7, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,7, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2,7, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 6,7, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,7, "curved-rail"),
                        };
                        break;
                    case 7: // /
                        neighbours = new LookUp[]{
                                new LookUp(xPos, yPos,0, "straight-rail"),
                                new LookUp(xPos, yPos - 4,0, "straight-rail"),
                                new LookUp(xPos, yPos,5, "straight-rail"),
                                new LookUp(xPos - 4, yPos,1, "straight-rail"),
                                new LookUp(xPos, yPos,2, "straight-rail"),
                                new LookUp(xPos - 4, yPos,2, "straight-rail"),
                                new LookUp(xPos, yPos,1, "straight-rail"),
                                new LookUp(xPos, yPos - 4,5, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 2,0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6,0, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 6,0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6,0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,0, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,0, "curved-rail"),

                                new LookUp(xPos - 2, yPos - 2,1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6,1, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6,1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,1, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 2,1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,1, "curved-rail"),

                                new LookUp(xPos - 2, yPos - 2,4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6,4, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 6,4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6,4, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6,4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,4, "curved-rail"),

                                new LookUp(xPos - 2, yPos + 2,5, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6,5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6,5, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 6,5, "curved-rail"),

                                new LookUp(xPos - 2, yPos + 2,2, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 2,2, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,2, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,2, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2,2, "curved-rail"),

                                new LookUp(xPos + 2, yPos - 6,6, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,6, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,6, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2,6, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 2,6, "curved-rail"),
                                new LookUp(xPos - 10, yPos + 2,6, "curved-rail"),

                                new LookUp(xPos - 10, yPos - 2,7, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2,7, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,7, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2,7, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,7, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2,7, "curved-rail"),

                                new LookUp(xPos + 6, yPos + 2,3, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2,3, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2,3, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 6,3, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2,3, "curved-rail"),
                        };
                        break;
                }
                break;
            case "curved-rail" :
                switch (entry.getDirection()) {
                    case 0: // '|
                        neighbours = new LookUp[]{
                                new LookUp(xPos + 2, yPos + 6,2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,2, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,2, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 6,2, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 6,2, "straight-rail"),

                                new LookUp(xPos + 2, yPos + 6,0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,0, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 6,0, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 2,7, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 6,7, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6,7, "straight-rail"),

                                new LookUp(xPos - 6, yPos - 6,3, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,3, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 6,3, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,3, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6,3, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 2,5, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 6,5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,5, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,5, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6,5, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 2,1, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,1, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,1, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6,1, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 10,1, "straight-rail"),

                                new LookUp(xPos - 4, yPos - 12,0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8,0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4,0, "curved-rail"),
                                new LookUp(xPos, yPos - 8,0, "curved-rail"),
                                new LookUp(xPos, yPos - 4,0, "curved-rail"),

                                new LookUp(xPos - 4, yPos + 8,2, "curved-rail"),
                                new LookUp(xPos, yPos + 8,2, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8,2, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4,2, "curved-rail"),
                                new LookUp(xPos, yPos + 4,2, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4,2, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4,2, "curved-rail"),
                                new LookUp(xPos, yPos,2, "curved-rail"),
                                new LookUp(xPos - 8, yPos,2, "curved-rail"),
                                new LookUp(xPos - 4, yPos,2, "curved-rail"),
                                new LookUp(xPos + 4, yPos,2, "curved-rail"),
                                new LookUp(xPos + 8, yPos,2, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4,2, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4,2, "curved-rail"),
                                new LookUp(xPos, yPos - 4,2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4,2, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4,2, "curved-rail"),
                                new LookUp(xPos, yPos - 8,2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8,2, "curved-rail"),

                                new LookUp(xPos - 4, yPos - 8,4, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 8,4, "curved-rail"),
                                new LookUp(xPos, yPos - 4,4, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4,4, "curved-rail"),
                                new LookUp(xPos, yPos,4, "curved-rail"),
                                new LookUp(xPos, yPos + 4,4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4,4, "curved-rail"),
                                new LookUp(xPos, yPos + 8,4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8,4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 12,4, "curved-rail"),

                                new LookUp(xPos - 12, yPos - 4,6, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4,6, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4,6, "curved-rail"),
                                new LookUp(xPos, yPos - 4,6, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4,6, "curved-rail"),
                                new LookUp(xPos - 8, yPos,6, "curved-rail"),
                                new LookUp(xPos - 4, yPos,6, "curved-rail"),
                                new LookUp(xPos, yPos,6, "curved-rail"),
                                new LookUp(xPos + 4, yPos,6, "curved-rail"),
                                new LookUp(xPos + 8, yPos,6, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4,6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4,6, "curved-rail"),
                                new LookUp(xPos, yPos + 4,6, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4,6, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4,6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8,6, "curved-rail"),
                                new LookUp(xPos, yPos + 8,6, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8,6, "curved-rail"),

                                new LookUp(xPos - 4, yPos - 8,5, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4,5, "curved-rail"),
                                new LookUp(xPos - 4, yPos,5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4,5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8,5, "curved-rail"),
                                new LookUp(xPos, yPos - 12,5, "curved-rail"),
                                new LookUp(xPos, yPos - 8,5, "curved-rail"),
                                new LookUp(xPos, yPos - 4,5, "curved-rail"),
                                new LookUp(xPos, yPos,5, "curved-rail"),
                                new LookUp(xPos, yPos + 4,5, "curved-rail"),
                                new LookUp(xPos, yPos + 8,5, "curved-rail"),
                                new LookUp(xPos, yPos + 12,5, "curved-rail"),

                                new LookUp(xPos - 8, yPos - 4,7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4,7, "curved-rail"),
                                new LookUp(xPos, yPos - 4,7, "curved-rail"),
                                new LookUp(xPos - 12, yPos - 8,7, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 8,7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8,7, "curved-rail"),
                                new LookUp(xPos - 8, yPos,7, "curved-rail"),
                                new LookUp(xPos - 4, yPos,7, "curved-rail"),
                                new LookUp(xPos, yPos,7, "curved-rail"),
                                new LookUp(xPos + 4, yPos,7, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4,7, "curved-rail"),
                                new LookUp(xPos, yPos + 4,7, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4,7, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4,7, "curved-rail"),

                                new LookUp(xPos - 8, yPos,1, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4,7, "curved-rail"),
                                new LookUp(xPos - 4, yPos,7, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4,7, "curved-rail"),
                                new LookUp(xPos, yPos - 12,7, "curved-rail"),
                                new LookUp(xPos, yPos - 8,7, "curved-rail"),
                                new LookUp(xPos, yPos - 4,7, "curved-rail"),
                                new LookUp(xPos, yPos,7, "curved-rail"),
                                new LookUp(xPos, yPos + 4,7, "curved-rail"),
                                new LookUp(xPos, yPos + 8,7, "curved-rail"),
                                new LookUp(xPos, yPos + 12,7, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8,7, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4,7, "curved-rail"),
                                new LookUp(xPos + 4, yPos,7, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4,7, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8,7, "curved-rail"),

                                new LookUp(xPos - 8, yPos - 4,3, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4,3, "curved-rail"),
                                new LookUp(xPos, yPos - 4,3, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4,3, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 8,3, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8,3, "curved-rail"),
                                new LookUp(xPos, yPos + 8,3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8,3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 8,3, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4,3, "curved-rail"),
                                new LookUp(xPos, yPos + 4,3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4,3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4,3, "curved-rail"),
                                new LookUp(xPos - 4, yPos,3, "curved-rail"),
                                new LookUp(xPos, yPos + 4,3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4,3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4,3, "curved-rail")
                        };
                        break;
                    case 1: // |'
                        neighbours = new LookUp[]{
                                new LookUp(xPos - 2, yPos + 6,2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,2, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6,2, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 6,2, "straight-rail"),

                                new LookUp(xPos + 2, yPos - 6,0, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 6,0, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 2,7, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 6,7, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 10,7, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,7, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 2,3, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,3, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 6,3, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,3, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6,3, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 2,5, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 6,5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6,5, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 6,5, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 6,1, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,1, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,1, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,1, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6,1, "straight-rail"),
                                //TODO other curved
                        };
                        break;
                    case 2: // --'
                        neighbours = new LookUp[]{
                                new LookUp(xPos - 2, yPos - 2,2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,2, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2,2, "straight-rail"),
                                new LookUp(xPos - 6, yPos + 2,2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,2, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 2,0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,0, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,0, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2,0, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 6,0, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 2,1, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,1, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,1, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,1, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2,1, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 2,5, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,5, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2,5, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 6,5, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 2,7, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,7, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,7, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2,7, "straight-rail"),

                                new LookUp(xPos - 10, yPos + 2,3, "straight-rail"),
                                new LookUp(xPos - 6, yPos + 2,3, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,3, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,3, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,3, "straight-rail"),
                                //TODO MEINSSSS!!
                        };
                        break;
                    case 3: // --,
                        neighbours = new LookUp[]{
                                new LookUp(xPos + 2, yPos - 2,2, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,2, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 2,2, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2,2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,2, "straight-rail"),

                                new LookUp(xPos + 2, yPos - 2,0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,0, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 2,0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,0, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2,0, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 6,0, "straight-rail"),

                                new LookUp(xPos - 6, yPos - 2,3, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,3, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,3, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,3, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2,3, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 2,7, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 2,7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,7, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2,7, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 6,7, "straight-rail"),

                                new LookUp(xPos - 6, yPos - 2,5, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,5, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,5, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2,5, "straight-rail"),

                                new LookUp(xPos - 10, yPos - 2,1, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 2,1, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,1, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,1, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,1, "straight-rail"),
                                //TODO other curved
                        };
                        break;
                    case 4: // |,
                        neighbours = new LookUp[]{
                                new LookUp(xPos - 2, yPos - 6,2, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6,2, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 6,2, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 6,0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,0, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6,0, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 6,3, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,3, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,3, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,3, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6,3, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 6,7, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6,7, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 6,7, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 6,1, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,1, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,1, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,1, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6,1, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 10,5, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 6,5, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,5, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,5, "straight-rail"),
                                //TODO other curved
                        };
                        break;
                    case 5: // ,|
                        neighbours = new LookUp[]{
                                new LookUp(xPos - 6, yPos + 6,2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 6,2, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6,2, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 6,0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,0, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,0, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6,0, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 2,3, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,3, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,3, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6,3, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 10,3, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 2,7, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 6,7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,7, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,7, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6,7, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 2,1, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 6,1, "straight-rail"),
                                new LookUp(xPos - 6, yPos + 6,1, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,1, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6,1, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 2,5, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 6,5, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6,5, "straight-rail"),

                                new LookUp(xPos - 4, yPos + 8,2, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 8,2, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4,2, "curved-rail"),
                                new LookUp(xPos, yPos + 4,2, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4,2, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4,2, "curved-rail"),
                                new LookUp(xPos, yPos - 8,2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8,2, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 8,2, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4,2, "curved-rail"),
                                new LookUp(xPos, yPos - 4,2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4,2, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4,2, "curved-rail"),
                                new LookUp(xPos - 4, yPos,2, "curved-rail"),
                                new LookUp(xPos, yPos,2, "curved-rail"),
                                new LookUp(xPos + 4, yPos,2, "curved-rail"),
                                new LookUp(xPos + 8, yPos,2, "curved-rail"),

                                new LookUp(xPos + 4, yPos - 8,4, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4,4, "curved-rail"),
                                new LookUp(xPos + 4, yPos,4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4,4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8,4, "curved-rail"),
                                new LookUp(xPos, yPos - 12,4, "curved-rail"),
                                new LookUp(xPos, yPos - 8,4, "curved-rail"),
                                new LookUp(xPos, yPos - 4,4, "curved-rail"),
                                new LookUp(xPos, yPos,4, "curved-rail"),
                                new LookUp(xPos, yPos + 4,4, "curved-rail"),
                                new LookUp(xPos, yPos + 8,4, "curved-rail"),
                                new LookUp(xPos, yPos + 12,4, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4,4, "curved-rail"),
                                new LookUp(xPos - 4, yPos,4, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4,4, "curved-rail"),
                                new LookUp(xPos - 8, yPos,4, "curved-rail"),

                                new LookUp(xPos - 4, yPos + 8,6, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 8,6, "curved-rail"),
                                new LookUp(xPos - 12, yPos + 8,6, "curved-rail"),
                                new LookUp(xPos, yPos + 4,6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4,6, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4,6, "curved-rail"),
                                new LookUp(xPos + 4, yPos,6, "curved-rail"),
                                new LookUp(xPos, yPos,6, "curved-rail"),
                                new LookUp(xPos - 4, yPos,6, "curved-rail"),
                                new LookUp(xPos - 8, yPos,6, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4,6, "curved-rail"),
                                new LookUp(xPos, yPos - 4,6, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4,6, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4,6, "curved-rail"),

                                new LookUp(xPos, yPos - 12,0, "curved-rail"),
                                new LookUp(xPos, yPos - 8,0, "curved-rail"),
                                new LookUp(xPos, yPos - 4,0, "curved-rail"),
                                new LookUp(xPos, yPos,0, "curved-rail"),
                                new LookUp(xPos, yPos + 4,0, "curved-rail"),
                                new LookUp(xPos, yPos + 8,0, "curved-rail"),
                                new LookUp(xPos, yPos + 12,0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8,0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4,0, "curved-rail"),
                                new LookUp(xPos - 4, yPos,0, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4,0, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8,0, "curved-rail"),

                                new LookUp(xPos - 4, yPos + 12,5, "curved-rail"),
                                new LookUp(xPos, yPos + 8,5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8,5, "curved-rail"),
                                new LookUp(xPos, yPos + 4,5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4,5, "curved-rail"),

                                new LookUp(xPos + 4, yPos + 4,7, "curved-rail"),
                                new LookUp(xPos, yPos + 4,7, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4,7, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4,7, "curved-rail"),
                                new LookUp(xPos - 12, yPos + 4,7, "curved-rail"),
                                new LookUp(xPos + 8, yPos,7, "curved-rail"),
                                new LookUp(xPos + 4, yPos,7, "curved-rail"),
                                new LookUp(xPos, yPos,7, "curved-rail"),
                                new LookUp(xPos - 4, yPos,7, "curved-rail"),
                                new LookUp(xPos - 8, yPos,7, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8,7, "curved-rail"),
                                new LookUp(xPos, yPos - 8,7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8,7, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4,7, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4,7, "curved-rail"),
                                new LookUp(xPos, yPos - 4,7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4,7, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4,7, "curved-rail"),

                                new LookUp(xPos + 8, yPos,3, "curved-rail"),
                                new LookUp(xPos + 4, yPos,3, "curved-rail"),
                                new LookUp(xPos, yPos,3, "curved-rail"),
                                new LookUp(xPos - 4, yPos,3, "curved-rail"),
                                new LookUp(xPos - 8, yPos,3, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4,3, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4,3, "curved-rail"),
                                new LookUp(xPos, yPos - 4,3, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4,3, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8,3, "curved-rail"),
                                new LookUp(xPos, yPos - 8,3, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8,3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4,3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4,3, "curved-rail"),
                                new LookUp(xPos, yPos + 4,3, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4,3, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4,3, "curved-rail"),
                                new LookUp(xPos, yPos + 8,3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8,3, "curved-rail"),

                                new LookUp(xPos, yPos,1, "curved-rail"),
                                new LookUp(xPos, yPos + 4,1, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4,1, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8,1, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 8,1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 12,1, "curved-rail"),
                                new LookUp(xPos, yPos - 8,1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8,1, "curved-rail"),
                                new LookUp(xPos, yPos - 4,1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4,1, "curved-rail"),
                        };
                        break;
                    case 6: // ,--
                        neighbours = new LookUp[]{
                                new LookUp(xPos - 6, yPos + 2,2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,2, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,2, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2,2, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 2,0, "straight-rail"),
                                new LookUp(xPos - 6, yPos + 6,0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,0, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,0, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2,0, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 2,5, "straight-rail"),
                                new LookUp(xPos - 6, yPos + 2,5, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,5, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2,5, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 6,1, "straight-rail"),
                                new LookUp(xPos - 6, yPos + 2,1, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,1, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,1, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2,1, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 2,3, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,3, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,3, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,3, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2,3, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 2,7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,7, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,7, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2,7, "straight-rail"),
                                new LookUp(xPos + 10, yPos - 2,7, "straight-rail"),
                                //TODO other curved
                        };
                        break;
                    case 7: // '--
                        neighbours = new LookUp[]{
                                new LookUp(xPos - 6, yPos - 2,2, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,2, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2,2, "straight-rail"),

                                new LookUp(xPos - 6, yPos - 2,0, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 6,0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,0, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,0, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2,0, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 2,5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2,5, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,5, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2,5, "straight-rail"),
                                new LookUp(xPos + 10, yPos + 2,5, "straight-rail"),

                                new LookUp(xPos - 6, yPos - 2,1, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,1, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,1, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,1, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2,1, "straight-rail"),

                                new LookUp(xPos + 2, yPos + 2,3, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2,3, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 6,3, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 2,3, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,3, "straight-rail"),

                                new LookUp(xPos - 6, yPos - 2,7, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2,7, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2,7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2,7, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2,7, "straight-rail"),
                                
                                new LookUp(xPos + 8, yPos + 4,6, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4,6, "curved-rail"),
                                new LookUp(xPos, yPos + 4,6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4,6, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4,6, "curved-rail"),
                                new LookUp(xPos - 12, yPos,6, "curved-rail"),
                                new LookUp(xPos - 8, yPos,6, "curved-rail"),
                                new LookUp(xPos - 4, yPos,6, "curved-rail"),
                                new LookUp(xPos, yPos,6, "curved-rail"),
                                new LookUp(xPos + 4, yPos,6, "curved-rail"),
                                new LookUp(xPos + 8, yPos,6, "curved-rail"),
                                new LookUp(xPos + 12, yPos,6, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4,6, "curved-rail"),
                                new LookUp(xPos, yPos - 4,6, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4,6, "curved-rail"),
                                new LookUp(xPos, yPos - 8,6, "curved-rail"),

                                new LookUp(xPos, yPos - 4,4, "curved-rail"),
                                new LookUp(xPos, yPos,4, "curved-rail"),
                                new LookUp(xPos, yPos + 4,4, "curved-rail"),
                                new LookUp(xPos, yPos + 8,4, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4,4, "curved-rail"),
                                new LookUp(xPos + 4, yPos,4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4,4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8,4, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4,4, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 8,4, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8,4, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4,4, "curved-rail"),
                                new LookUp(xPos - 4, yPos,4, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4,4, "curved-rail"),
                                new LookUp(xPos + 8, yPos,4, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4,4, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 8,4, "curved-rail"),

                                new LookUp(xPos, yPos - 8,1, "curved-rail"),
                                new LookUp(xPos, yPos - 4,1, "curved-rail"),
                                new LookUp(xPos, yPos,1, "curved-rail"),
                                new LookUp(xPos, yPos + 4,1, "curved-rail"),
                                new LookUp(xPos, yPos + 8,1, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4,1, "curved-rail"),
                                new LookUp(xPos + 8, yPos,1, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4,1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8,1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4,1, "curved-rail"),
                                new LookUp(xPos + 4, yPos,1, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4,1, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8,1, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 12,1, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8,1, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4,1, "curved-rail"),
                                new LookUp(xPos - 4, yPos,1, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4,1, "curved-rail"),

                                new LookUp(xPos - 8, yPos - 4,0, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 8,0, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 12,0, "curved-rail"),
                                new LookUp(xPos, yPos + 4,0, "curved-rail"),
                                new LookUp(xPos, yPos,0, "curved-rail"),
                                new LookUp(xPos, yPos - 4,0, "curved-rail"),
                                new LookUp(xPos, yPos - 8,0, "curved-rail"),
                                new LookUp(xPos - 4, yPos,0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4,0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8,0, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4,0, "curved-rail"),
                                new LookUp(xPos + 4, yPos,0, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4,0, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8,0, "curved-rail"),

                                new LookUp(xPos - 8, yPos - 4,2, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4,2, "curved-rail"),
                                new LookUp(xPos, yPos - 4,2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4,2, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4,2, "curved-rail"),
                                new LookUp(xPos + 12, yPos,2, "curved-rail"),
                                new LookUp(xPos + 8, yPos,2, "curved-rail"),
                                new LookUp(xPos + 4, yPos,2, "curved-rail"),
                                new LookUp(xPos, yPos,2, "curved-rail"),
                                new LookUp(xPos - 4, yPos,2, "curved-rail"),
                                new LookUp(xPos - 8, yPos,2, "curved-rail"),
                                new LookUp(xPos - 12, yPos,2, "curved-rail"),

                                new LookUp(xPos - 12, yPos - 4,7, "curved-rail"),
                                new LookUp(xPos - 8, yPos,7, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4,7, "curved-rail"),
                                new LookUp(xPos - 4, yPos,7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4,7, "curved-rail"),

                                new LookUp(xPos, yPos - 8,5, "curved-rail"),
                                new LookUp(xPos, yPos - 4,5, "curved-rail"),
                                new LookUp(xPos, yPos,5, "curved-rail"),
                                new LookUp(xPos, yPos + 4,5, "curved-rail"),
                                new LookUp(xPos, yPos + 8,5, "curved-rail"),
                                new LookUp(xPos - 8, yPos,5, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4 - 8,5, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4,5, "curved-rail"),
                                new LookUp(xPos + 4, yPos,5, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4,5, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8,5, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8,5, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4,5, "curved-rail"),
                                new LookUp(xPos - 4, yPos,5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4,5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8,5, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4,5, "curved-rail"),
                                new LookUp(xPos + 8, yPos,5, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4,5, "curved-rail"),

                                new LookUp(xPos, yPos,3, "curved-rail"),
                                new LookUp(xPos + 12, yPos + 4,3, "curved-rail"),
                                new LookUp(xPos + 8, yPos,3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4,3, "curved-rail"),
                                new LookUp(xPos - 4, yPos,3, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4,3, "curved-rail"),
                                new LookUp(xPos + 4, yPos,3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4,3, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4,3, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 8,3, "curved-rail"),
                        };
                        break;
                }
                break;
        }
        return neighbours;
    }
}
