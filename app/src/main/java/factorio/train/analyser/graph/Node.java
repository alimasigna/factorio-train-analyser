package factorio.train.analyser.graph;

import java.util.ArrayList;

public class Node {
    private Section section;
    private ArrayList<Node>  nextNodes;
    private ArrayList<Node>  dependsOn;
    private ArrayList<Track> tracks;

    public Node () {
        tracks = new ArrayList<>();
    }

    public Node (ArrayList<Track> tracks, ArrayList<Node> dependsOn, ArrayList<Node> nextNodes, Section section) {
        if (tracks == null) {
            this.tracks = new ArrayList<>();
        } else {
            this.tracks = new ArrayList<>(tracks);
        }

        if (dependsOn == null) {
            this.dependsOn = new ArrayList<>();
        } else {
            this.dependsOn = new ArrayList<>(dependsOn);
        }

        if (nextNodes == null) {
            this.nextNodes = new ArrayList<>();
        } else {
            this.nextNodes = new ArrayList<>(nextNodes);
        }

        this.section = section;
    }

    public void addTrack (Track track) {
        this.tracks.add(track);
    }

    public void addTrack (ArrayList<Track> tracks) {
        for(Track track : tracks) {
            addTrack(track);
            track.addNode(this);
        }
    }

    public ArrayList<Track> getTracks() {
        return tracks;
    }

    public Node cloneNode(){
        Node newNode = new Node(this.tracks, this.dependsOn, this.nextNodes, this.section);
        addClonedNodeToTrack(newNode);
        return newNode;
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
        }
    }

    private void addClonedNodeToTrack(Node newParent){
        for ( Track track: this.tracks) {
            track.addNode(newParent);
        }
    }
}
