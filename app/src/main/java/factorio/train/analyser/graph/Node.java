package factorio.train.analyser.graph;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

public class Node {
    private Section section;
    @Expose
    private ArrayList<ArrayList<Node>> nextNodes;
    @Expose
    private ArrayList<ArrayList<Node>> protectedFrom;
    @Expose
    private ArrayList<Track> tracks;
    @Expose
    private int length;
    @Expose
    private boolean isOutput;
    @Expose
    private boolean isInput;

    private boolean hasBeenMerged;
    @Expose
    private boolean isEndNode;
    private static int numOfGeneratedNodes = 0;
    @Expose
    private int id;

    public Node() {
        nextNodes = new ArrayList<>(2);
        protectedFrom = new ArrayList<>(2);
        tracks = new ArrayList<>();
        hasBeenMerged = false;
        isEndNode = false;
        isOutput = false;
        isInput = false;
        length = 0;
        this.id = ++numOfGeneratedNodes;
    }

    public void addTrack(Track track) {
        this.tracks.add(track);
        this.length += track.getLength();
    }

    public void addTrack(ArrayList<Track> tracks) {
        for (Track track : tracks) {
            addTrack(track);
            track.addNode(this);
        }
    }

    public int getId() {
        return id;
    }

    public int getLength() {
        return length;
    }

    public void setNextNodes(ArrayList<Node> nextNodes) {
        // every node can have up to 2 possible groups of nextNodes
        if (!this.nextNodes.contains(nextNodes)) {
            this.nextNodes.add(nextNodes);
        }
    }

    public void setProtectedFrom(ArrayList<Node> protectedFrom) {
        // every node can have up to 2 possible groups from where it is protected from
        if (!this.protectedFrom.contains(protectedFrom)) {
            this.protectedFrom.add(protectedFrom);
        }
    }

    public ArrayList<ArrayList<Node>> getProtectedFrom() {
        return protectedFrom;
    }

    public ArrayList<ArrayList<Node>> getNextNodes() {
        return nextNodes;
    }

    public ArrayList<Node> getNextNodesMerged() {
        ArrayList<Node> mergedNextNodes = new ArrayList<>();
        for (ArrayList<Node> groupOfNextNodes : nextNodes) {
            for (Node nextNode : groupOfNextNodes) {
                if (!mergedNextNodes.contains(nextNode)) {
                    mergedNextNodes.add(nextNode);
                }
            }
        }
        return mergedNextNodes;
    }

    public boolean getIsOutput() {
        return isOutput;
    }

    public void setIsOutput(boolean isOutput) {
        this.isOutput = isOutput;
    }

    public boolean getIsInput() {
        return isInput;
    }

    public void setIsInput(boolean isInput) {
        this.isInput = isInput;
    }

    public void setIsEndNode(boolean isEndNode) {
        this.isEndNode = isEndNode;
    }

    public boolean getIsEndNode() {
        return this.isEndNode;
    }

    public boolean getHasBeenMerged() {
        return hasBeenMerged;
    }

    public void setHasBeenMerged(boolean hasBeenMerged) {
        this.hasBeenMerged = hasBeenMerged;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public ArrayList<Track> getTracks() {
        return tracks;
    }

    public Section getSection() {
        return section;
    }

    public static Node mergeNodes(Node a, Node b) {
        Node merged = new Node();
        merged.addTrack(a.tracks);
        merged.addTrack(b.tracks);
        a.removeTracks();
        b.removeTracks();
        return merged;
    }

    private void removeTracks() {
        for (Track track : tracks) {
            track.removeNode(this);
            this.length -= track.getLength();
        }
    }
}
