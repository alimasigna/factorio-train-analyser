package factorio.train.analyser.graph;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

public class Section {
    @Expose
    private ArrayList<Node> nodes;
    private boolean isFree;

    private static int numOfGeneratedSections = 0;
    @Expose
    private int id;

    public Section(ArrayList<Node> nodes) {
        for (Node node : nodes) {
            node.setSection(this);
        }
        this.nodes = new ArrayList<>(nodes);
        this.isFree = true;
        this.id = ++numOfGeneratedSections;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public int getId() {
        return id;
    }

    public boolean getIsFree() {
        return this.isFree;
    }

    public void setIsFree(boolean isFree) {
        this.isFree = isFree;
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
