package factorio.train.analyser.graph;

import factorio.train.analyser.Decoder.Decoder;
import factorio.train.analyser.Matrix;

public class Graph {
    private Section[] sections;
    private Matrix matrix;

    public Graph(String encodedString){
        setMatrix(encodedString);
        setSections();
    }

    private void setMatrix(String encodedString) {
        Decoder decoder = new Decoder();
        String jsonString = decoder.decode(encodedString);
        if(jsonString!=null) matrix = new Matrix(jsonString);
    }

    private void setSections() {
        if(matrix == null) return;

    }
}
