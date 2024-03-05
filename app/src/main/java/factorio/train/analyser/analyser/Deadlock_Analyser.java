package factorio.train.analyser.analyser;

import java.util.ArrayList;
import java.util.HashMap;

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
                    Result deadlockPath = recursion(node, resultOnePath, null);
                    result.add(deadlockPath);
                    for (Section sectionFree : sections) {
                        sectionFree.setIsFree(true);
                    }
                }
            }
        }
        return result;
    }

    private Result recursion(Node node, Result resultYet, Node predecessor) {

        Section nodeSection = node.getSection();

        if (nodeSection.getIsFree()) {
            for (Node nodeInSection : nodeSection.getNodes()) {

                if (nodeInSection.getIsOutput() && !nodeInSection.getIsInput()) {
                    continue;
                }

                if (!nodeInSection.getIsOutput() || nodeInSection.getIsOutput() && nodeInSection.getIsInput()) {
                    nodeSection.setIsFree(false);
                }

                if (nodeInSection.getNextNodes().size() > 1) {
                    for (ArrayList<Node> nextNodes : nodeInSection.getNextNodes()) {
                        for (Node nextNode : nextNodes) {
                            HashMap<ArrayList<Node>, Boolean> dependsOnDict = nextNode.getDependsOnDict();
                            for (Entry<ArrayList<Node>, Boolean> map : dependsOnDict.entrySet()) {
                                boolean dependsOn = map.getValue();
                                if (dependsOn && map.getKey().get(0).equals(nodeInSection)) {
                                    if (!findPair(resultYet.chainSignalProtected, nodeInSection, nextNode)) {
                                        HashMap<Node, Node> newProtected = new HashMap<Node, Node>();
                                        newProtected.put(nextNode, nodeInSection);
                                        resultYet.chainSignalProtected.add(newProtected);
                                    }
                                    nodeSection.setIsFree(true);
                                }
                            }
                        }
                    }
                } else if(predecessor != null){
                    HashMap<ArrayList<Node>, Boolean> dependsOnDict = predecessor.getDependsOnDict();
                    for (Entry<ArrayList<Node>, Boolean> map : dependsOnDict.entrySet()) {
                        boolean dependsOn = map.getValue();
                        if (dependsOn && map.getKey().get(0).equals(nodeInSection)) {
                            nodeSection.setIsFree(true);
                        }
                    }
                }

                /*
                 * boolean alreadyHere = false;
                 * for (ArrayList<Node> visitedNode : resultYet.chainSignalsVisited) {
                 * if (inList(visitedNode, nodeInSection)) {
                 * alreadyHere = true;
                 * break;
                 * }
                 * }
                 * if (alreadyHere) {
                 * continue;
                 * }
                 * 
                 * 
                 * 
                 * for (ArrayList<Node> protectedNode : resultYet.chainSignalProtected) {
                 * if (inList(protectedNode, nodeInSection)) {
                 * resultYet.chainSignalsVisited.add(protectedNode);
                 * nodeSection.setIsFree(true);
                 * }
                 * }
                 */
                resultYet.deadlockPath.add(nodeInSection);

                for (ArrayList<Node> nextNodes : nodeInSection.getNextNodes()) {
                    for (Node nextNode : nextNodes) {
                        if (!(findPair(resultYet.chainSignalProtected, nodeInSection, nextNode)
                                && nextNode.equals(predecessor)) && !resultYet.result) {
                            recursion(nextNode, resultYet, nodeInSection);
                        }
                    }
                }
            }
        } else {
            resultYet.result = true;
            return resultYet;
        }
        return resultYet;
    }

    private boolean findPair(ArrayList<HashMap<Node, Node>> list, Node protectedNode, Node protectorNode) {
        for (HashMap<Node, Node> pairs : list) {
            for (Entry<Node, Node> pair : pairs.entrySet()) {
                Node keyNode = pair.getKey();
                Node valueNode = pair.getValue();
                if (valueNode.equals(protectedNode) && keyNode.equals(protectorNode)) {
                    return true;
                }
            }
        }
        return false;
    }
}
