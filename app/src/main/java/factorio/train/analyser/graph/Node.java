package factorio.train.analyser.graph;

public class Node {
    private Section section;
    private Node[] nextNodes;
    private Node[] dependsOn;
    private Track[] tracks;
}
