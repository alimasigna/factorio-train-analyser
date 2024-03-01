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
            for (int i = 0; i < nodeSection.getNodes().size(); i++) {
                Node nodeInSection = nodeSection.getNodes().get(i);
                if(inList(resultYet.chainSignalProtected, nodeInSection)){
                    resultYet.chainSignalsVisited.add(nodeInSection);
                }
                if(inList(resultYet.chainSignalsVisited, nodeInSection)){
                    continue;
                }
                else if(nodeInSection.getDependsOn().size() > 0) {
                    for(Node nodeProt : nodeInSection.getDependsOn()){
                        resultYet.chainSignalProtected.add(nodeProt);
                    }
                }
                else{
                    nodeSection.setIsFree(false);
                }
                resultYet.deadlockPath.add(nodeInSection);
                ArrayList<Node> nextNodes = nodeInSection.getNextNodes();
                for(Node nextNode : nextNodes) {
                    if(nextNode.getIsOutput()) {
                        nodeSection.setIsFree(true);
                        resultYet.deadlockPath.remove(nodeInSection);
                    }else{
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
