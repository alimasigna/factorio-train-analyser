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

    /** This array holds all entites extracted from the json.
     * */
    private Entity[] entities;

    /** This array will represent the given json in a matrix.
     * */
    private ArrayList<Entity>[][] matrix;

    /** Creates a speicific matrix object with a given jsonString.
     * @param jsonString The JsonString that will be used to generate a Json object.
     * */
    public Matrix(String jsonString){
        setJson(jsonString);
        setEntities();
        setMatrix();
    }

    /** This will initialize the private Json field with a given Json String.
     * @param jsonString The JsonString that will be parsed into a Json object.
     * */
    private void setJson(String jsonString) {
        JsonBuilder builder = new JsonBuilder();
        json = builder.create(jsonString);
    }

    /** This setter will init the entities field and filter it.
     * */
    private void setEntities() {
        entities = json.getBlueprint().getEntities();
        filterEntities();
    }

    /** This getter will return the matrix field.
     * @return The matrix field.
     * */
    public ArrayList<Entity>[][] getMatrix() {
        return matrix;
    }

    /** This will initialize the private matrix field from the Json field.
     * */
    private void setMatrix() {
        if(json == null || entities==null) return;

        double lowX = entities[0].getPosition().getX();
        double highX = entities[0].getPosition().getX();
        double lowY = entities[0].getPosition().getY();
        double highY = entities[0].getPosition().getY();

        for(Entity entity : entities) {
            if(entity.getPosition().getX() < lowX){
                lowX = entity.getPosition().getX();
            }
            if(entity.getPosition().getX() > highX){
                highX = entity.getPosition().getX();
            }
            if(entity.getPosition().getY() < lowY) {
                lowY = entity.getPosition().getY();
            }
            if(entity.getPosition().getY() > highY) {
                highY = entity.getPosition().getY();
            }
        }

        lowX *= 2;  //double size so that we can map .5 vals
        highX *= 2;
        lowY *= 2;
        highY *= 2;
        //calculates height and width of the array
        int xDimensionLength = ((int) Math.abs(highX - lowX) + 1);
        int yDimensionLength = ((int) Math.abs(highY - lowY) + 1);
        //creating a 2D array with an array list full of enitities
        matrix = new ArrayList[xDimensionLength][yDimensionLength];
        //writing entites in matrix
        for(Entity entity : entities) {
            //normalyzing of koords
            int adjustedX = (int) (entity.getPosition().getX()*2 - lowX); //double size so that we can map .5 vals
            int adjustedY = (int) (entity.getPosition().getY()*2 - lowY);
            if(matrix[adjustedX][adjustedY] == null)
                matrix[adjustedX][adjustedY] = new ArrayList<>();
            matrix[adjustedX][adjustedY].add(entity);
        }
    }

    /** This method formats the matrix field into a String.
     * @return The String representation of the matrix field.
     * */
    @Override
    public String toString() {
        String stringifiedMatrix = "";
        if(matrix == null) return stringifiedMatrix;
        for(int i = 0; i< matrix.length; i++){
            for(int j = 0; j<matrix[i].length; j++){
                if(matrix[i][j] == null) {
                    stringifiedMatrix += " . ";
                    continue;
                }
                if (matrix[i][j].size()>1) {
                    stringifiedMatrix +=" + ";
                    continue;
                }
                stringifiedMatrix +=" # ";
            }
            stringifiedMatrix += "\n";
        }
        return stringifiedMatrix;
    }

    /** This method filters the entities field. The goal is to remove all non-train-related entities.
     * */
    private void filterEntities() {
        ArrayList<Entity> buffer = new ArrayList<>();
        for(Entity entity : entities) {
            switch (entity.getName()) {
                case "curved-rail", "straight-rail", "train-stop", "rail-signal", "rail-chain-signal" -> buffer.add(entity);
            }
        }
        entities = buffer.toArray(new Entity[0]);
    }
}
