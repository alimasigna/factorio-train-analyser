package factorio.train.analyser.graph;

import java.util.ArrayList;

public class Node {
    private transient Section section;
    private transient ArrayList<ArrayList<Node>> nextNodes;
    private transient ArrayList<ArrayList<Node>> protectedFrom;
    private ArrayList<Track> tracks;
    private int length;
    private boolean isOutput;
    private boolean isInput;

    private transient boolean hasBeenMerged;
    private transient boolean isEndNode;
    private transient static int numOfGeneratedNodes = 0;
    private int nodeId;
    private ArrayList<ArrayList<Integer>> nextNodesIds;
    private ArrayList<ArrayList<Integer>> protectedFromIds;

    public Node() {
        nextNodes = new ArrayList<>(2);
        protectedFrom = new ArrayList<>(2);
        nextNodesIds = new ArrayList<>(2);
        protectedFromIds = new ArrayList<>(2);
        tracks = new ArrayList<>();
        hasBeenMerged = false;
        isEndNode = false;
        isOutput = false;
        isInput = false;
        length = 0;
        this.nodeId = ++numOfGeneratedNodes;
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

    public int getNodeId() {
        return nodeId;
    }

    public int getLength() {
        return length;
    }

    public void setNextNodes(ArrayList<Node> nextNodes) {
        // every node can have up to 2 possible groups of nextNodes
        if (!this.nextNodes.contains(nextNodes)) {
            this.nextNodes.add(nextNodes);
            this.nextNodesIds.add(extractIds(nextNodes));
        }
    }

    public void setProtectedFrom(ArrayList<Node> protectedFrom) {
        // every node can have up to 2 possible groups from where it is protected from
        if (!this.protectedFrom.contains(protectedFrom)) {
            this.protectedFrom.add(protectedFrom);
            this.protectedFromIds.add(extractIds(protectedFrom));
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

    private ArrayList<Integer> extractIds(ArrayList<Node> toBeExtracted) {
        ArrayList<Integer> extractedIds = new ArrayList<>();
        for (Node node : toBeExtracted) {
            extractedIds.add(node.getNodeId());
        }
        return extractedIds;
    }
}
