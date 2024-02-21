package factorio.train.analyser.graph;

import java.util.ArrayList;

public class Node {
    private Section section;
    private ArrayList<Node>  nextNodes;
    private ArrayList<Node>  dependsOn;
    private ArrayList<Track> tracks;
    private int length;
    private boolean isOutput;
    private boolean isInput;

    private boolean hasBeenMerged;
    private boolean isEndNode;
   
    public Node () {
        nextNodes = new ArrayList<>();
        dependsOn = new ArrayList<>();
        tracks = new ArrayList<>();
        hasBeenMerged = false;
        isEndNode = false;
        isOutput = false;
        isInput = false;
        length = 0;
    }

    public void addTrack (Track track) {
        this.tracks.add(track);
        this.length += track.getLength();
    }

    public void addTrack (ArrayList<Track> tracks) {
        for(Track track : tracks) {
            addTrack(track);
            track.addNode(this);
        }
    }

    public int getLength() {
        return length;
    }

    public void setNextNodes(ArrayList<Node> nextNodes) {
        for(Node nextNode : nextNodes) {
            if(!this.nextNodes.contains(nextNode)){
                this.nextNodes.add(nextNode);
            }
        }  
    }

    public ArrayList<Node> getNextNodes() {
        return nextNodes;
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

    public void setDependsOn(ArrayList<Node> dependsOnNodes) {
        for(Node dependsOnNode : dependsOnNodes) {
            if(!this.dependsOn.contains(dependsOnNode)){
                this.dependsOn.add(dependsOnNode);
            }
        }  
    }

    public ArrayList<Node> getDependsOn() {
        return dependsOn;
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
    private void removeTracks(){
        for(Track track : tracks) {
            track.removeNode(this);
            this.length -= track.getLength();
        }
    }
}
