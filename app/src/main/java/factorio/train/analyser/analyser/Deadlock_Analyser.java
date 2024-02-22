package factorio.train.analyser.analyser;

import java.util.ArrayList;
import factorio.train.analyser.graph.Graph;
import factorio.train.analyser.graph.Section;
import factorio.train.analyser.graph.Node;

public class Deadlock_Analyser {

    private Graph graph;

    public Deadlock_Analyser(Graph graph) {
        this.graph = graph;
    }

    public ArrayList<ArrayList<Node>> deadlockCheck() {
        ArrayList<ArrayList<Node>> result = DeadlockDetection(graph.getSections());
        return result;
    }

    private ArrayList<ArrayList<Node>> DeadlockDetection(ArrayList<Section> sections) {

        ArrayList<ArrayList<Node>> result = new ArrayList<ArrayList<Node>>();
        for (Section section : sections) {
            for (Node node : section.getNodes()) {
                if (node.getIsInput()) {
                    ArrayList<Node> path = new ArrayList<Node>();
                    ArrayList<Node> deadlockPath = recursion(node, path);
                    result.add(deadlockPath);
                    for (Section sectionFree : sections) {
                        sectionFree.setIsFree(true);
                    }
                }
            }
        }
        return result;
    }

    private ArrayList<Node> recursion(Node node, ArrayList<Node> pathYet) {

        Section nodeSection = node.getSection();

        if (nodeSection.getIsFree()) {
            for (int i = 0; i < nodeSection.getNodes().size(); i++) {
                Node nodeInSection = nodeSection.getNodes().get(i);
                nodeSection.setIsFree(false);
                pathYet.add(nodeInSection);
                ArrayList<Node> nextNodes = nodeInSection.getNextNodes();
                for(Node nextNode : nextNodes) {
                    if(nextNode.getIsOutput()) {
                        nodeSection.setIsFree(true);
                        pathYet.remove(nodeInSection);
                    }else{
                        recursion(nextNode, pathYet);
                    }
                }
            }
        } else {
            return pathYet;
        }
        return pathYet;
    }
}
