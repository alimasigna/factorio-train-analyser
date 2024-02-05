package factorio.train.analyser.graph;

import factorio.train.analyser.jsonmodels.Entity;
import factorio.train.analyser.jsonmodels.Position;

import java.util.ArrayList;

public class Track {
    private ArrayList<Node> nodes;
    private int length;
    private int direction;

    private Position position;

    private String name;

    public Track (Node parent, int length, int direction, int xCoordinate, int yCoordinate) {
        nodes = new ArrayList<>();
        if (parent != null) this.nodes.add(parent);
        this.length = length;
        this.direction = direction;
        this.position = new Position(xCoordinate, yCoordinate);
    }

    public Track (Node parent, Entity entity) {
        nodes = new ArrayList<>();
        if (parent != null) this.nodes.add(parent);
        this.length = entity.getName().equals("straight-rail") ? 1 : 3;
        this.direction = entity.getDirection();
        this.position = new Position(entity.getPosition().getX(), entity.getPosition().getY());
        this.name = entity.getName();
    }

    public void addNode(Node node) {
        this.nodes.add(node);
    }

    public void removeNode(Node node) {
        this.nodes.remove(node);
    }

    public LookUp[][] getConnected() {
        return LookUp.lookUpConnected(this);
    }

    public LookUp[] getCrossed() {
        return LookUp.lookUpCrossed(this);
    }

    public String getName() {
        return name;
    }

    public Position getPosition() {
        return position;
    }

    public int getDirection() {
        return direction;
    }

    public ArrayList<Node> getNodes() {
        return this.nodes;
    }
}
