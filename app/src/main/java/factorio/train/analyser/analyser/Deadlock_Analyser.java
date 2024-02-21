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

    public ArrayList<ArrayList<Section>> deadlockCheck() {
        ArrayList<ArrayList<Section>> result = DeadlockDetection(graph.getSections());
        return result;
    }

    private ArrayList<ArrayList<Section>> DeadlockDetection(ArrayList<Section> sections){

        ArrayList<ArrayList<Section>> result = new ArrayList<ArrayList<Section>>();
        for (Section section : sections) {
            
            if(section.getIsEndSection()){

                ArrayList<Node> endNodes = section.getNodes();
                for (Node endnode : endNodes) {
                    ArrayList<Section> path = new ArrayList<Section>();
                    result.add(recursion(endnode, path));
                    for (Section sectionFree : sections) {
                        sectionFree.setIsFree(true);
                    }
                }
            }
        }
        return result;
    }

    private ArrayList<Section> recursion(Node node, ArrayList<Section> pathYet){

        Section nodeSection = node.getSection();

        for (Node nodeInSection : nodeSection.getNodes()) {

            nodeSection.setIsFree(false);
            pathYet.add(nodeSection);
            ArrayList<Node> nextNodes = nodeInSection.getNextNodes();

            for (Node nextNode : nextNodes) {
                
                if(nextNode.getSection().getIsEndSection()){
                    nodeSection.setIsFree(true);
                    pathYet.remove(nodeSection);
                    continue;
                }else if(!nextNode.getSection().getIsFree()){

                    return pathYet;
                }
                else{
                    return recursion(nextNode, pathYet);
                }
            }
        }
        return pathYet;
    }
}
