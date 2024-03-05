package factorio.train.analyser.analyser;

import java.util.ArrayList;
import java.util.HashMap;
import factorio.train.analyser.graph.Graph;
import factorio.train.analyser.graph.Section;
import factorio.train.analyser.graph.Node;
import java.util.Map.Entry;

public class Deadlock_Analyser {

    private Graph graph;

    public Deadlock_Analyser() {}

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public ArrayList<Result> deadlockCheck() {
        if(graph != null){
            ArrayList<Result> result = deadlockDetection(graph.getSections());
            return result;
        }else{
            return null;
        }
    }

    private ArrayList<Result> deadlockDetection(ArrayList<Section> sections) {

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

        if (nodeSection.getIsFree()) {  //if this Section is not free, there is already a train here and we cant continue -> Deadlock
            for (Node nodeInSection : nodeSection.getNodes()) {

                if (nodeInSection.getIsOutput() && !nodeInSection.getIsInput()) {   //if the node is an Output, ignore it
                    continue;
                }

                if (!nodeInSection.getIsOutput() || nodeInSection.getIsOutput() && nodeInSection.getIsInput()) {    //a Train can be here by default
                    nodeSection.setIsFree(false);
                }
                resultYet.addToDeadlockPath(nodeInSection);
                if (nodeInSection.getProtectedFrom().size() > 0 ) { //this Node is protected by another Node

                    for(ArrayList<Node> protectedFrom : nodeInSection.getProtectedFrom()) { //if the oredecessor is a protector, no train can be here by default, the next node must be checked
                        for (Node protector : protectedFrom) {
                            if(protector.equals(predecessor)) {
                                nodeSection.setIsFree(true);
                            }
                        }
                    }

                    for (ArrayList<Node> nextNodes : nodeInSection.getNextNodes()) { //we know this Node is protected by someone, if the next Node is not the precessor go for it, if it is the precessor only jump if we are an Input
                        for (Node nextNode : nextNodes) {
                            if ((nextNode != predecessor && !findPair(resultYet.getChainSignalsVisited(), nextNode, nodeInSection)) || nextNode.equals(predecessor) && nodeInSection.getIsInput() && !findPair(resultYet.getChainSignalsVisited(), nextNode, nodeInSection) ||!(!inList(nodeInSection.getProtectedFrom(), predecessor) && nextNode.equals(predecessor)) && !findPair(resultYet.getChainSignalsVisited(), nextNode, nodeInSection)) {
                                HashMap newPair = new HashMap<Node, Node>();
                                newPair.put(nodeInSection, nextNode);
                                resultYet.addToChainSignalsVisited(newPair);
                                recursion(nextNode, resultYet, nodeInSection);
                            }
                        }
                    }
                }
                else { //no protection, just go to the next node
                    for (ArrayList<Node> nextNodes : nodeInSection.getNextNodes()) {
                        for (Node nextNode : nextNodes) {
                            recursion(nextNode, resultYet, nodeInSection);
                        }
                    }
                }
            }
        } else {
            resultYet.setResult(true);
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

    private boolean inList(ArrayList<ArrayList<Node>> list, Node node){

        for(ArrayList<Node> nodes : list){
            for(Node nodeInList : nodes){
                if(nodeInList.equals(node)){
                    return true;
                }
            }
        }
        return false;
    }
}
