/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package factorio.train.analyser;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import factorio.train.analyser.analyser.DeadlockAnalyser;
import factorio.train.analyser.analyser.Result;
import factorio.train.analyser.graph.Graph;
import factorio.train.analyser.jsonmodels.Output;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class App {
    public String[] args;

    // use these fields for debugging purposes only!
    private final String debugBlueprint = "0eNq9mttu4kAMht9lrgPKnGd4lVW14hDRSDSgANVWFe++CbSc6iy/jbQ3lQLJV+cf22N7+FSz1b7atHWzU5NPVc/XzVZNfn2qbb1spqv+s93HplITVe+qN1WoZvrWX7XTeqUOhaqbRfVHTfThpVBVs6t3dXV6/njx8bvZv82qtrvh/OR21z27fN2NjohCbdbb7ql10/+rjuR8LtSHmoyMix1/UbfV/PS1PhQ/sAbGBj2A9QTWnrHzffteLQah7hvqbqGRgLob8UZfAlOWjv3Z1rG/BVsC7AUi+MfaBlCENCSCIaARtjXFAVspbdOttvPXad0MK5z8ReEwBqTIAqvj44XTJR4VZ4PtY3P1Jdpm9XJUrbqb23o+2qxXFcVO10p3GaA5wbf9Dbr/01aL66Cuu6ts+5BftlXVUF8dOlJTdW80W+/bPiNk+0JZyghgO+APVABrK+AiyjqWo4V4cTR372hUeGg8ltOQR1DxoYOAC6QeHVmelvJ1qvi/npbwWIsDypKelgVcDexseG4InmGv0QIuYi8jkvOAvVREGDySo+ZwHc61HC4ewdFzuHgEx8jh4jtyZK0bHm/JDvgZlclMFnDt40xm8XjLmmGv1QIuYq9h7UT+quTRY6CislZQsWugtHZ8rs2P84/1aM1uvqkJUAGPuhA5KkRxHaGRlsAmvt2Qyozo0wNcsjcq+dx7nSkdnEabmDDgFVRuc/helzJHBcvnQiqgrWe+aj3LMVBnO0Hvea8EyQ0CLjAvcJFfpUD2JgEXsTfzd+d7LuVpvhRwMzCK0Ky8FtPZ32xGGnFv+J04pIcVcBE9HL8PtcCAynsBF9FXsN/d20tlTB+fjw+Sm/jdAaRvfp5L2RtKAReZ1Wl+NwPZa/jdDMS1/G7GAlVrcAIuYq9/nkvlhxCez++kvVFQpyHcxJ8jQeuWBVxA3yiYolig7omCKYoF5ouR19WFq/3T3++fgeJbQZ5A9HCCPAHMLaMX2IvoHHh1iv2HzqTfRYEeCJd30BEd1z8E0xVkHVMp4AJ6JF69mQxTj8SLxxT+wafiJllBvQVMeZMTcIEpb0KnLPE8ZSkBKnrUGMOFSnEkux4ww0tJwAVmeCmj711+UU2i3juXKCddOI8PPbV0RmUyMmHMhl8LG0++v2WdQsXvA2vDOIXS1xIdP4iDx1LkvT/OqbQudKSOqrKgiB0QxqO/OciXxev2OpIVBMtlSVIUvB9NStJxw/Etb1zU9ct5/JHL5Oo3MYV6r9rtqR5OuivCTUwu25DD4fAX/AZqTA==";
    private boolean isInDebug = false;

    public App(String[] args) {
        this.args = args;
    }

    public static void main(String[] args) {
        App app = new App(args);
        app.run();
    }

    public void run() {
        try {
            if (args.length > 0) {
                printResult(analyse(readFromFile()));
            } else if (isInDebug) {
                System.out.println("Debug mode is active. Using the given String!");
                printResult(analyse(this.debugBlueprint));
            } else {
                System.out.println("No argmuents were given.");
            }
        } catch (JsonIOException e) {
            System.out.println("Error while exporting to json.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error while reading the file.");
            e.printStackTrace();  
        }
    }

    public boolean analyse(String blueprint) throws JsonIOException, IOException{
        Graph graph = new Graph(blueprint);
        DeadlockAnalyser analyser = new DeadlockAnalyser(graph);
        ArrayList<Result> result = analyser.deadlockCheck();
        exportToJson(result, graph);

        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).getIsDeadlock()) {
                return true;
            }
        }
        return false;
    }

    public void exportToJson(ArrayList<Result> result, Graph graph) throws JsonIOException, IOException {
            Gson gson = new GsonBuilder().create();
            Output output = new Output(result, graph);
            FileWriter writer = new FileWriter("./output.json");
            gson.toJson(output, writer);
            writer.close();
    }

    private String readFromFile() throws FileNotFoundException, IOException{
        String url = args[0];
        File blueprintFile = new File(url);
        try (BufferedReader br = new BufferedReader(new FileReader(blueprintFile))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            return sb.toString().trim();
        }
    }

    private void printResult(boolean deadlockFound) {
        if (deadlockFound) {
            System.out.println("Deadlock found! More information in output.json");
        } else {
            System.out.println("No deadlock found! More information in output.json");
        }
    }
}
