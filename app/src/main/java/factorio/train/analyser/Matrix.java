package factorio.train.analyser;

import factorio.train.analyser.jsonmodels.Entity;
import factorio.train.analyser.jsonmodels.Json;
import factorio.train.analyser.jsonmodels.JsonBuilder;

import java.util.ArrayList;

/** This class is used to generate a 2D matrix from a given JsonString.
 * */
public class Matrix {

    /** This is the Json from which the Matrix will be generated from.
     * */
    private Json json;

    /** This array will represent the given json in a matrix.
     * */
    private ArrayList<Entity>[][] matrix;

    /** Creates a speicific matrix object with a given jsonString.
     * @param jsonAsString The JsonString that will be used to generate a Json object.
     * */
    public Matrix(String jsonAsString){ //TODO change this to the class Laurin will create!
        setJson(jsonAsString);
    }

    /** This will initialize the private Json field with a given Json String.
     * @param jsonAsString The JsonString that will be parsed into a Json object.
     * */
    private void setJson(String jsonAsString) { //TODO change this to the class Laurn will create!
        JsonBuilder builder = new JsonBuilder();
        json = builder.create(jsonAsString);
    }

    /**This will initialize the private Matrix field from the Json field.
     * */

    private void setMatrix() {

    }
}
