package factorio.train.analyser.graph;

import factorio.train.analyser.jsonmodels.Entity;
import factorio.train.analyser.jsonmodels.Position;
import factorio.train.analyser.rail.RailEnd;
import factorio.train.analyser.rail.RailGeometry;

import java.util.ArrayList;

/**
 * A single rail entity from the blueprint, the piece the whole train route
 * model is built from.
 *
 * <p>A track knows its own shape: the two {@link RailEnd}s a train can enter it
 * through and the centre line it would run along. Those come from
 * {@link RailGeometry}, so whether two tracks connect or cross is a question
 * about geometry rather than a lookup in a table of neighbour positions.
 */
public class Track {
    private transient ArrayList<Node> nodes;

    private transient ArrayList<Node> frontierNodes;
    private transient boolean isEndTrack;
    private double length;
    private int direction;
    private Position position;
    private transient ArrayList<Track> goesTo;
    private transient ArrayList<Track> protectedFrom;
    private String name;
    private transient static int numOfGeneratedTracks = 0;
    private int trackId;

    private transient RailEnd[] ends;
    private transient double[][] centreLine;
    private transient int layer;

    public Track(Node parent, Entity entity) {
        frontierNodes = new ArrayList<>();
        nodes = new ArrayList<>();
        goesTo = new ArrayList<>();
        protectedFrom = new ArrayList<>();
        isEndTrack = false;
        if (parent != null) this.nodes.add(parent);
        this.direction = entity.getDirection();
        this.position = new Position(entity.getPosition().getX(), entity.getPosition().getY());
        this.name = entity.getName();
        this.length = RailGeometry.length(this.name, this.direction);
        this.layer = RailGeometry.layer(this.name);
        this.ends = RailGeometry.endsOf(this.name, this.direction, position.getX(), position.getY());
        this.centreLine = RailGeometry.centreLine(this.name, this.direction, position.getX(), position.getY());
        this.trackId = ++numOfGeneratedTracks;
    }

    public void addNode(Node node) {
        this.nodes.add(node);
    }

    public int getTrackId() {
        return trackId;
    }

    /** Arc length of the rail in tiles. */
    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    /**
     * The two ends a train can enter this track through, in blueprint
     * coordinates. The index is what {@link RailNetwork} and {@link Graph} mean
     * by "the first" and "the second" end of a track.
     */
    public RailEnd[] getEnds() {
        return ends;
    }

    /** The path a train takes across this track, as a polyline. */
    public double[][] getCentreLine() {
        return centreLine;
    }

    /**
     * Ground level or elevated. Rails only connect to rails on the same layer,
     * which is what keeps an elevated crossing separate from the ground one
     * underneath it.
     */
    public int getLayer() {
        return layer;
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

    @Override
    public String toString() {
        return name + "@" + direction + " (" + position.getX() + "," + position.getY() + ")";
    }
}
