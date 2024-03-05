package factorio.train.analyser.analyser;

import java.util.ArrayList;
import java.util.HashMap;

import factorio.train.analyser.graph.Node;

public class Result {
    
    public ArrayList<Node> deadlockPath;
    public ArrayList<HashMap<Node, Node>> chainSignalProtected;
    public ArrayList<HashMap<Node, Node>> chainSignalsVisited;
    public boolean result;

    public Result(){
        this.deadlockPath = new ArrayList<Node>();
        this.chainSignalProtected = new ArrayList<HashMap<Node, Node>>();
        this.chainSignalsVisited = new ArrayList<HashMap<Node, Node>>();
        this.result = false;
    }
}
