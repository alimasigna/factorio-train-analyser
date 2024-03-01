package factorio.train.analyser.analyser;

import java.util.ArrayList;

import factorio.train.analyser.graph.Node;

public class Result {
    
    public ArrayList<Node> deadlockPath;
    public ArrayList<Node> chainSignalProtected;
    public ArrayList<Node> chainSignalsVisited;
    public String result;

    public Result(){
        this.deadlockPath = new ArrayList<Node>();
        this.chainSignalProtected = new ArrayList<Node>();
        this.chainSignalsVisited = new ArrayList<Node>();
        this.result = "no deadlock found";
    }
}
