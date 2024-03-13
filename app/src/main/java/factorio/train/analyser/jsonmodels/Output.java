package factorio.train.analyser.jsonmodels;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

import factorio.train.analyser.analyser.Result;
import factorio.train.analyser.graph.Graph;

public class Output {
    
    @Expose
    private ArrayList<Result> result;
    @Expose
    private Graph graph;

    public Output(ArrayList<Result> result, Graph graph) {
        this.result = result;
        this.graph = graph;
    }
}
