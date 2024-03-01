package factorio.train.analyser.graph;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

public class Node {
    private Section section;
    private Dictionary<ArrayList<Node>, Boolean> dependsOnDict;
    private ArrayList<ArrayList<Node>> nextNodes;
    //private ArrayList<Boolean> dependsOn;
    private ArrayList<Track> tracks;
    private int length;
    private boolean isOutput;
    private boolean isInput;

    private boolean hasBeenMerged;
    private boolean isEndNode;

    public Node() {
        nextNodes = new ArrayList<>(2);
        dependsOnDict = new Hashtable<ArrayList<Node>, Boolean>();
        tracks = new ArrayList<>();
        hasBeenMerged = false;
        isEndNode = false;
        isOutput = false;
        isInput = false;
        length = 0;
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

    public int getLength() {
        return length;
    }

    public void setNextNodes(ArrayList<Node> nextNodes, boolean dependsOn) {
        ArrayList<Node> groupedNextNodes = new ArrayList<>();
        // every node can have up to 2 possible groups of nextNodes
        for (Node nextNode : nextNodes) {
            groupedNextNodes.add(nextNode);
        }
        if (!this.nextNodes.contains(groupedNextNodes)) {
            this.nextNodes.add(groupedNextNodes);
            setDependsOnDict(groupedNextNodes, dependsOn);
        }
    }

    public Dictionary<ArrayList<Node>, Boolean> getDependsOnDict() {
        return dependsOnDict;
    }

    private void setDependsOnDict(ArrayList<Node> nextNodes, boolean dependsOn) {
        this.dependsOnDict.put(nextNodes, dependsOn);
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
