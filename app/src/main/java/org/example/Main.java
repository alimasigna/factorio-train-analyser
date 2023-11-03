package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jgrapht.Graph;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }
    public void start() {
        String inputJson = readJsonFile();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Json parsedInput;
        try{
            parsedInput = gson.fromJson(inputJson, Json.class);
            Entity[] entities = parsedInput.getBlueprint().getEntities(); //TODO only parse tracks, signals, etc
            entities = filterEntities(entities);
            //debug output
            for(Entity entity : entities) {
                System.out.println("######");
                System.out.println(entity.name + " " + "dir: "+ entity.direction+" x: " + entity.position.x + " y: " + entity.position.y);
            }
            createBetterMatrix(entities);

        } catch (Exception e) {
            System.out.printf(e.getMessage());
        }
    }

    public Entity[] normalizeEntities(Entity[] entities) {
        entities = filterEntities(entities);

        return null;
    }

    public Entity[] filterEntities(Entity[] entities) {
        ArrayList<Entity> buffer = new ArrayList<Entity>();
        for(Entity entity : entities) {
            switch (entity.name) {
                case "curved-rail":
                case "straight-rail":
                case "train-stop":
                case "rail-signal":
                case "rail-chain-signal":
                    buffer.add(entity);
                    break;
            }
        }
        return buffer.toArray(new Entity[buffer.size()]);
    }

    public ArrayList<Entity>[][] createBetterMatrix(Entity[] entities) {
        //setting lowX and highX coords
        double lowX = entities[0].position.x;
        double highX = entities[0].position.x;
        double lowY = entities[0].position.y;
        double highY = entities[0].position.y;
        for(Entity entity : entities) {
            if(entity.position.x < lowX){
                lowX = entity.position.x;
            }
            if(entity.position.x > highX){
                highX = entity.position.x;
            }
            if(entity.position.y < lowY) {
                lowY = entity.position.y;
            }
            if(entity.position.y > highY) {
                highY = entity.position.y;
            }
        }
        //calculates height and width of the array
        int xDimensionLength = (int) Math.abs(highX - lowX) + 1;
        int yDimensionLength = (int) Math.abs(highY - lowY) + 1;
        //creating a 2D array with an array list full of enitities
        ArrayList<Entity>[][] entityMatrix = new ArrayList[xDimensionLength][yDimensionLength];
        //writing entites in matrix
        for(Entity entity : entities) {
            //normalyzing of koords
            int adjustedX = (int) (entity.position.x - lowX);
            int adjustedY = (int) (entity.position.y - lowY);
            if(entityMatrix[adjustedX][adjustedY] == null)
                entityMatrix[adjustedX][adjustedY] = new ArrayList<Entity>();
            entityMatrix[adjustedX][adjustedY].add(entity);
        }

        //TODO this is a debug print
        for(int i = 0; i< entityMatrix.length; i++){
            for(int j = 0; j<entityMatrix[i].length; j++){
                if(entityMatrix[i][j] == null) {
                    System.out.print(" . ");
                    continue;
                }
                if (entityMatrix[i][j].size()>1) {
                    System.out.print(" + ");
                    continue;
                }
                System.out.print(" # ");
            }
            System.out.print("\n");
        }


        return entityMatrix;
    }
    public String readJsonFile() {
        System.out.println("Please enter Factorio JSON: ");
        Scanner myScanner = new Scanner(System.in);

        StringBuilder jsonString = new StringBuilder();
        String line;
        while (myScanner.hasNextLine()) {
            line = myScanner.nextLine();
            if (line.isEmpty()) {
                break;
            }
            jsonString.append(line);
        }
        myScanner.close();
        return jsonString.toString();
    }
}