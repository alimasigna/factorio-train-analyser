package factorio.train.analyser.graph;

import java.util.ArrayList;

public class Section {
    private ArrayList<Node> nodes;
    private boolean isFree;
    private boolean isEndSection;

    public Section(ArrayList<Node> nodes) {
        for (Node node : nodes) {
            node.setSection(this);
        }
        this.nodes = new ArrayList<>(nodes);
        this.isFree = true;
        isEndSection = false;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public boolean getIsFree() {
        return this.isFree;
    }

    public void setIsFree(boolean isFree) {
        this.isFree = isFree;
    }

    public boolean getIsEndSection() {
        return isEndSection;
    }

    public void setIsEndSection(boolean isEndSection) {
        this.isEndSection = isEndSection;
    }

    /**
     * Splits the current section by creating a new section with the provided nodes
     * and removing them from the current section.
     *
     * @param extractedNodes The nodes to be extracted from the current section and
     *                       used to create the new section.
     * @return The new section created from the extracted nodes.
     */
    public Section splitSection(ArrayList<Node> extractedNodes) {
        Section splittedSection = new Section(extractedNodes);
        for (Node extractedNode : extractedNodes) {
            if (this.nodes.contains(extractedNode)) {
                this.nodes.remove(extractedNode);
            }
        }
        return splittedSection;
    }
}
