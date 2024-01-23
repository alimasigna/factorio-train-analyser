/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package factorio.train.analyser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import factorio.train.analyser.Decoder.Decoder;
import factorio.train.analyser.graph.Graph;
import factorio.train.analyser.graph.Node;
import factorio.train.analyser.graph.Section;

public class App {

    private static final String blueprint = "0eNqV19tugkAQBuB3mWtInF1gWV6laRoPG7uJogE0NYZ37yLGtOk0/HPpgc+Bmd+BO20Ol3DuYjtQc6e4PbU9NW936uO+XR+m94bbOVBDcQhHyqhdH6dX3ToeaMwotrvwRQ2P7xmFdohDDPPxjxe3j/Zy3IQufeF15PbSXcMufwAZnU99OubUTj+UHJPRjZqcTaJ3sQvb+bNqzP6IBhN5JZNWIO2L7Iek7T+Hf9CcZ9T/Np1gFqjJVjRZMEvs1PNiFqtlsYKrLMUqS8F0WtMt11nDphNNqU6vNcvlOqepA1EvolKhzFrUApWCOTJzNFejZODBebaaRaZQMrJSKhUrKpVSKUXFqS5uDcxArazLiXV5peIlxay0/RIbZsDdMLVkQorfV8lIpFEmJVW2uB2MdspZnCyDT/lzJXAJnDE+9cy4iqfA4yi8GHKHo9pcsBhY4/XMYm0WzwnecMvKmxUIxXODm3BsFCcPZ0jRJThB+FRaOD94fCx+X6Vokjo+4l6x6viIi6XQLhYjLpaCtUz6w07PMY8nnebHg1FG19D188WruXDeOFfb2tpqHL8Bv6NbtA==";

    public String decode() {
        Matrix matrix = new Matrix(blueprint);
        Graph graph = new Graph(blueprint);

        Decoder decoder = new Decoder();
        String decodedString = decoder.decode(blueprint);
        return (decodedString + "\n" + matrix);
    }

    public static void main(String[] args) {
        Map<String, ArrayList<Section>> result = new App().DeadlockDetection(GetStaticGraph());
        for(String key : result.keySet()) {
            String path = "";
            ArrayList<Section> sections = new ArrayList<>();
            sections = result.get(key);
            for(Section section : sections){
                path += section.sectionName;
            }
            System.out.println("starting in "+key + " has an deadlock in " + path);           
        }
    }
    

    private Map<String, ArrayList<Section>> DeadlockDetection(ArrayList<Section> sections){

        Map<String, ArrayList<Section>> result = new HashMap<String, ArrayList<Section>>();
        for (Section section : sections) {
            
            if(section.getIsEnd()){

                ArrayList<Node> endNodes = section.getAllNodes();
                for (Node endnode : endNodes) {
                    ArrayList<Section> path = new ArrayList<Section>();
                    result.put(endnode.name, recursion(endnode, path));
                    for (Section sectionFree : sections) {
                        sectionFree.setIsFree(true);
                    }
                }
            }
        }
        return result;
    }

    private ArrayList<Section> recursion(Node node, ArrayList<Section> pathYet){

        Section nodeSection = node.section;

        for (Node nodeInSection : nodeSection.getAllNodes()) {

            nodeSection.setIsFree(false);
            pathYet.add(nodeSection);
            ArrayList<Node> nextNodes = nodeInSection.getNextNodes();

            for (Node nextNode : nextNodes) {
                
                if(nextNode.section.getIsEnd()){
                    nodeSection.setIsFree(true);
                    pathYet.remove(nodeSection);
                    continue;
                }else if(!nextNode.section.getIsFree()){

                    return pathYet;
                }
                else{
                    return recursion(nextNode, pathYet);
                }
            }
        }
        return pathYet;
    }

    private static ArrayList<Section> GetStaticGraph(){

        Section yellow1 = new Section(false, "yellow1");
        Section yellow2 = new Section(false, "yellow2");
        Section purple1 = new Section(false, "purple1");
        Section purple2 = new Section(false, "purple2");
        Section end1 = new Section(true, "end1");
        Section end2 = new Section(true, "end2");
        Section end3 = new Section(true, "end3");
        Section end4 = new Section(true, "end4");
        Section end5 = new Section(true, "end5");
        Section end6 = new Section(true, "end6");
        Section end7 = new Section(true, "end7");
        Section end8 = new Section(true, "end8");

        Node R1 = new Node(yellow1, "r1");
        Node R2 = new Node(yellow1, "r2");
        Node R3 = new Node(purple1, "r3");
        Node R4 = new Node(purple1, "r4");
        Node R5 = new Node(yellow2, "r5");
        Node R6 = new Node(yellow2, "r6");
        Node R7 = new Node(purple2, "r7");
        Node R8 = new Node(purple2, "r8");

        Node R_1 = new Node(end1, "r_1");
        Node R_2 = new Node(end2, "r_2");
        Node R_3 = new Node(end3, "r_3");
        Node R_4 = new Node(end4, "r_4");
        Node R_5 = new Node(end5, "r_5");
        Node R_6 = new Node(end6, "r_6");
        Node R_7 = new Node(end7, "r_7");
        Node R_8 = new Node(end8, "r_8");

        R1.addNodeToList(R3);
        R2.addNodeToList(R_2);
        R3.addNodeToList(R_4);
        R4.addNodeToList(R6);
        R5.addNodeToList(R7);
        R6.addNodeToList(R_6);
        R7.addNodeToList(R_8);
        R8.addNodeToList(R2);

        R_1.addNodeToList(R1);
        R_2.addNodeToList(R2);
        R_3.addNodeToList(R4);
        R_4.addNodeToList(R3);
        R_5.addNodeToList(R5);
        R_6.addNodeToList(R6);
        R_7.addNodeToList(R8);
        R_8.addNodeToList(R7);

        ArrayList<Section> graph = new ArrayList<Section>();
        graph.add(yellow1);
        graph.add(yellow2);
        graph.add(purple1);
        graph.add(purple2);
        graph.add(end1);
        graph.add(end2);
        graph.add(end3);
        graph.add(end4);
        graph.add(end5);
        graph.add(end6);
        graph.add(end7);
        graph.add(end8);

        return graph;
    }
}
