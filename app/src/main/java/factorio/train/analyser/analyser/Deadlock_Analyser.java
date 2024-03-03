package factorio.train.analyser.analyser;

import java.util.ArrayList;
import factorio.train.analyser.graph.Graph;
import factorio.train.analyser.graph.Section;
import factorio.train.analyser.graph.Node;
import java.util.Map.Entry;

public class Deadlock_Analyser {

    private Graph graph;

    public Deadlock_Analyser(Graph graph) {
        this.graph = graph;
    }

    public ArrayList<Result> deadlockCheck() {
        ArrayList<Result> result = DeadlockDetection(graph.getSections());
        return result;
    }

    private ArrayList<Result> DeadlockDetection(ArrayList<Section> sections) {

        ArrayList<Result> result = new ArrayList<Result>();
        for (Section section : sections) {
            for (Node node : section.getNodes()) {
                if (node.getIsInput()) {
                    Result resultOnePath = new Result();
                    Result deadlockPath = recursion(node, resultOnePath);
                    result.add(deadlockPath);
                    for (Section sectionFree : sections) {
                        sectionFree.setIsFree(true);
                    }
                }
            }
        }
        return result;
    }

    private Result recursion(Node node, Result resultYet) {

        Section nodeSection = node.getSection();

        if (nodeSection.getIsFree()) {
            for (Node nodeInSection : nodeSection.getNodes()) {

                if (nodeInSection.getIsOutput() && !nodeInSection.getIsInput()) {
                    continue;
                }
                boolean alreadyHere = false;
                for (ArrayList<Node> visitedNode : resultYet.chainSignalsVisited) {
                    if (inList(visitedNode, nodeInSection)) {
                        alreadyHere = true;
                    }
                }
                if (alreadyHere) {
                    continue;
                }

                if (!nodeInSection.getIsOutput() || nodeInSection.getIsOutput() && nodeInSection.getIsInput()) {
                    nodeSection.setIsFree(false);
                }

                for (ArrayList<Node> protectedNode : resultYet.chainSignalProtected) {
                    if (inList(protectedNode, nodeInSection)) {
                        resultYet.chainSignalsVisited.add(protectedNode);
                        nodeSection.setIsFree(true);
                    }
                }

                resultYet.deadlockPath.add(nodeInSection);

                for (Entry<ArrayList<Node>, Boolean> map : nodeInSection.getDependsOnDict().entrySet()) {
                    boolean dependsOn = map.getValue();
                    if (dependsOn) {
                        ArrayList<Node> nextNodes = map.getKey();
                        resultYet.chainSignalProtected.add(nextNodes);
                    }
                }

                for (ArrayList<Node> nextNodes : nodeInSection.getNextNodes()) {
                    for (Node nextNode : nextNodes) {
                        recursion(nextNode, resultYet);
                    }
                }
            }
        } else {
            resultYet.result = "deadlock found";
            return resultYet;
        }
        return resultYet;
    }

    private boolean inList(ArrayList<Node> list, Node node) {
        for (Node nodeInList : list) {
            if (nodeInList.equals(node)) {
                return true;
            }
        }
        return false;
    }
}
