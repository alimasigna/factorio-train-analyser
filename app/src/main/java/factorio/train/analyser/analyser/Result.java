package factorio.train.analyser.analyser;

import java.util.ArrayList;
import java.util.HashMap;

import factorio.train.analyser.graph.Node;

public class Result {
    
    private ArrayList<Node> deadlockPath;
    private ArrayList<HashMap<Node, Node>> chainSignalsVisited;
    private boolean isDeadlock;

    public Result(){
        this.deadlockPath = new ArrayList<Node>();
        this.chainSignalsVisited = new ArrayList<HashMap<Node, Node>>();
        this.isDeadlock = false;
    }

    public void addToDeadlockPath(Node newNode){
        deadlockPath.add(newNode);
    }

    public ArrayList<Node> getDeadlockPath(){
        return deadlockPath;
    }

    public ArrayList<HashMap<Node, Node>> getChainSignalsVisited(){
        return chainSignalsVisited;
    }

    public void addToChainSignalsVisited(HashMap<Node, Node> newPair){
        chainSignalsVisited.add(newPair);
    }

    public void setIsDeadlock(boolean result){
        this.isDeadlock = result;
    }

    public boolean getIsDeadlock(){
        return isDeadlock;
    }
}
