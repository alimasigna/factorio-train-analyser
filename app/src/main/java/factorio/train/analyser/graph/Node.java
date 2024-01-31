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

    public ArrayList<Track> getTracks() {
        return tracks;
    }

    public Node cloneNode(){
        Node newNode = new Node(this.tracks, this.dependsOn, this.nextNodes, this.section);
        addClonedNodeToTrack(newNode);
        return newNode;
    }

    private void addClonedNodeToTrack(Node newParent){
        for ( Track track: this.tracks) {
            track.addParentNode(newParent);
        }
    }
}
