package factorio.train.analyser.graph;

import java.util.ArrayList;

public class Node {
    public Section section;
    private ArrayList<Node> nextNodes = new ArrayList<Node>();
    //private Node[] dependsOn;
    private Track[] tracks;
    public String name;

    public Node(Section sectionNew, String nameNew){
        section = sectionNew;
        name = nameNew;
        section.addNode(this);
    }

    public void addNodeToList(Node nextNode){
        nextNodes.add(nextNode);
    }

    public ArrayList<Node> getNextNodes(){
        return nextNodes;
    }
}
