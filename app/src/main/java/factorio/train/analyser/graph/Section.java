package factorio.train.analyser.graph;

import java.util.ArrayList;

public class Section {
    private boolean isFree;
    private boolean isEnd;
    private ArrayList<Node> allNodes;
    public String sectionName;

    public Section(boolean isEndNew, String name){
        isEnd = isEndNew;
        sectionName = name;
        isFree = true;
    }

    public boolean getIsEnd(){
        return isEnd;
    }

    public boolean getIsFree(){
        return isFree;
    }

    public void setIsFree(boolean isFreeNew){
        isFree = isFreeNew;
    }

    public void addNode(Node node){
        allNodes.add(node);
    }

    public ArrayList<Node> getAllNodes(){
        return allNodes;
    }
}
