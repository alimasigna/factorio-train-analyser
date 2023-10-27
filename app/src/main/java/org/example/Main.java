package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jgrapht.Graph;

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
            Entity[] entities = parsedInput.getBlueprint().getEntities();

            for (Entity entity : entities ) {
                System.out.println("############");
                System.out.println(entity.name);
                System.out.println(entity.position.x);
                System.out.println(entity.position.y);
            }
        } catch (Exception e) {
            System.out.printf(e.getMessage());
        }
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