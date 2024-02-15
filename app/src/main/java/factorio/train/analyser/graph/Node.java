package factorio.train.analyser.graph;

import java.util.ArrayList;

public class Node {
    private Section section;
    private ArrayList<Node>  nextNodes;
    private ArrayList<Node>  dependsOn;
    private ArrayList<Track> tracks;

    private boolean hasBeenMerged;

    public Node () {
        tracks = new ArrayList<>();
        hasBeenMerged = false;
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
        }
    }
}
