package factorio.train.analyser.graph;

import factorio.train.analyser.jsonmodels.Entity;
import factorio.train.analyser.jsonmodels.Position;

import java.util.ArrayList;

public class Track {
    private transient ArrayList<Node> nodes;

    private transient ArrayList<Node> frontierNodes;
    private transient boolean isEndTrack;
    private transient int length;
    private int direction;
    private Position position;
    private transient ArrayList<Track> goesTo;
    private transient ArrayList<Track> protectedFrom;
    private String name;
    private transient static int numOfGeneratedTracks = 0;
    private int trackId;

    public Track (Node parent, Entity entity) {
        frontierNodes = new ArrayList<>();
        nodes = new ArrayList<>();
        goesTo = new ArrayList<>();
        protectedFrom = new ArrayList<>();
        isEndTrack = false;
        if (parent != null) this.nodes.add(parent);
        this.length = entity.getName().equals("straight-rail") ? 1 : 3;
        this.direction = entity.getDirection();
        this.position = new Position(entity.getPosition().getX(), entity.getPosition().getY());
        this.name = entity.getName();
        this.trackId = ++numOfGeneratedTracks;
    }

    public void addNode(Node node) {
        this.nodes.add(node);
    }

    public int getTrackId() {
        return trackId;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setProtectedFrom(Track protectedFrom) {
        this.protectedFrom.add(protectedFrom);
    }

    public ArrayList<Track> getProtectedFrom() {
        return protectedFrom;
    }

    public void setGoesTo(Track goesTo) {
        this.goesTo.add(goesTo);
    }

    public ArrayList<Track> getGoesTo() {
        return goesTo;
    }

    public void setIsEndTrack(boolean isEndTrack) {
        this.isEndTrack = isEndTrack;
    }

    public boolean getIsEndTrack() {
        return this.isEndTrack;
    }

    public void removeNode(Node node) {
        this.nodes.remove(node);
    }

    public LookUp[][] getConnected() {
        return LookUp.lookUpConnected(this);
    }

    public LookUp[] getSignals() {
        return LookUp.lookUpSignal(this);
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

    public ArrayList<Node> getFrontierNodes() {
        return frontierNodes;
    }

    public double getDistance(Track track) {
        return getDistance(this.getPosition().getX(), this.getPosition().getY(), track.getPosition().getX(), track.getPosition().getY());
    }
    public double getDistance(Entity signal) {
        return getDistance(this.getPosition().getX(), this.getPosition().getY(), signal.getPosition().getX(), signal.getPosition().getY());
    }
    private double getDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1-x2,2) + Math.pow(y1-y2,2));
    }
}
