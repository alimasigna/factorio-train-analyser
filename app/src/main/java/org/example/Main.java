package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jgrapht.Graph;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;
import java.util.zip.Inflater;

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
            ArrayList<Entity>[][] entityMatrix = createMatrix(entities);
            createDirectionsGraph(entityMatrix);
        } catch (Exception e) {
            System.out.printf(e.getMessage());
        }
    }
    public void createDirectionsGraph(ArrayList<Entity>[][] entityMatrix) {

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
    public ArrayList<Entity>[][] createMatrix(Entity[] entities) {
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
        lowX *= 2;  //double size so that we can map .5 vals
        highX *= 2;
        lowY *= 2;
        highY *= 2;
        //calculates height and width of the array
        int xDimensionLength = ((int) Math.abs(highX - lowX) + 1);
        int yDimensionLength = ((int) Math.abs(highY - lowY) + 1);
        //creating a 2D array with an array list full of enitities
        ArrayList<Entity>[][] entityMatrix = new ArrayList[xDimensionLength][yDimensionLength];
        //writing entites in matrix
        for(Entity entity : entities) {
            //normalyzing of koords
            int adjustedX = (int) (entity.position.x*2 - lowX); //double size so that we can map .5 vals
            int adjustedY = (int) (entity.position.y*2 - lowY);
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
        String outputString = null;
        byte[] base64decoded = Base64.getDecoder().decode(jsonString.toString().substring(1));  //decode base64
        int compressedDataLength = base64decoded.length;
        try {
            // Decompress the bytes
            Inflater decompresser = new Inflater();
            decompresser.setInput(base64decoded, 0, compressedDataLength);
            byte[] result = new byte[100];
            int resultLength = decompresser.inflate(result);
            decompresser.end();
            // Decode the bytes into a String
            outputString = new String(result, 0, resultLength, "UTF-8");
        } catch(java.io.UnsupportedEncodingException ex) {

        } catch (java.util.zip.DataFormatException ex) {
        }
        return outputString;
    }
}