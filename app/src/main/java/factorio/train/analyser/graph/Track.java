package factorio.train.analyser.graph;

import java.util.ArrayList;

public class Track {
    private ArrayList<Node> nodes;
    private int length;
    private int direction;
    private int xCoordinate;
    private int yCoordinate;

    public Track (Node parent, int length, int direction, int xCoordinate, int yCoordinate) {
        nodes = new ArrayList<>();
        this.nodes.add(parent);
        this.length = length;
        this.direction = direction;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }

    public void addParentNode(Node node) {
        nodes.add(node);
    }

    public ArrayList<Node> getNodes() {
        return this.nodes;
    }
}
