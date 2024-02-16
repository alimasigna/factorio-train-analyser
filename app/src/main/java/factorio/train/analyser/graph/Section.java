package factorio.train.analyser.graph;

import java.util.ArrayList;

public class Section {
    private ArrayList<Node> nodes;
    private boolean isFree;

    public Section(ArrayList<Node> nodes) {
        for(Node node : nodes) {
            node.setSection(this);
        }
        this.nodes = new ArrayList<>(nodes);
        // this.nodes.addAll(nodes);
        this.isFree = true;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public boolean getIsFree(){
        return this.isFree;
    }

    public void setIsFree(boolean isFree){
        this.isFree = isFree;
    }
}
