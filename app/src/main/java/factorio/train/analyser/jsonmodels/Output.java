package factorio.train.analyser.jsonmodels;

import java.util.ArrayList;

import factorio.train.analyser.analyser.Result;
import factorio.train.analyser.graph.Graph;

public class Output {
    private ArrayList<Result> result;
    private Graph graph;

    public Output(ArrayList<Result> result, Graph graph) {
        this.result = result;
        this.graph = graph;
    }
}
