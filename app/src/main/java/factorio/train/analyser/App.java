/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package factorio.train.analyser;

import java.util.ArrayList;
import factorio.train.analyser.analyser.DeadlockAnalyser;
import factorio.train.analyser.analyser.Result;
import factorio.train.analyser.decoder.Decoder;
import factorio.train.analyser.graph.Graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class App {

    private static final String blueprint =
    "0eNq9mttu4kAMht9lrgPKnGd4lVW14hDRSDSgANVWFe++CbSc6iy/jbQ3lQLJV+cf22N7+FSz1b7atHWzU5NPVc/XzVZNfn2qbb1spqv+s93HplITVe+qN1WoZvrWX7XTeqUOhaqbRfVHTfThpVBVs6t3dXV6/njx8bvZv82qtrvh/OR21z27fN2NjohCbdbb7ql10/+rjuR8LtSHmoyMix1/UbfV/PS1PhQ/sAbGBj2A9QTWnrHzffteLQah7hvqbqGRgLob8UZfAlOWjv3Z1rG/BVsC7AUi+MfaBlCENCSCIaARtjXFAVspbdOttvPXad0MK5z8ReEwBqTIAqvj44XTJR4VZ4PtY3P1Jdpm9XJUrbqb23o+2qxXFcVO10p3GaA5wbf9Dbr/01aL66Cuu6ts+5BftlXVUF8dOlJTdW80W+/bPiNk+0JZyghgO+APVABrK+AiyjqWo4V4cTR372hUeGg8ltOQR1DxoYOAC6QeHVmelvJ1qvi/npbwWIsDypKelgVcDexseG4InmGv0QIuYi8jkvOAvVREGDySo+ZwHc61HC4ewdFzuHgEx8jh4jtyZK0bHm/JDvgZlclMFnDt40xm8XjLmmGv1QIuYq9h7UT+quTRY6CislZQsWugtHZ8rs2P84/1aM1uvqkJUAGPuhA5KkRxHaGRlsAmvt2Qyozo0wNcsjcq+dx7nSkdnEabmDDgFVRuc/helzJHBcvnQiqgrWe+aj3LMVBnO0Hvea8EyQ0CLjAvcJFfpUD2JgEXsTfzd+d7LuVpvhRwMzCK0Ky8FtPZ32xGGnFv+J04pIcVcBE9HL8PtcCAynsBF9FXsN/d20tlTB+fjw+Sm/jdAaRvfp5L2RtKAReZ1Wl+NwPZa/jdDMS1/G7GAlVrcAIuYq9/nkvlhxCez++kvVFQpyHcxJ8jQeuWBVxA3yiYolig7omCKYoF5ouR19WFq/3T3++fgeJbQZ5A9HCCPAHMLaMX2IvoHHh1iv2HzqTfRYEeCJd30BEd1z8E0xVkHVMp4AJ6JF69mQxTj8SLxxT+wafiJllBvQVMeZMTcIEpb0KnLPE8ZSkBKnrUGMOFSnEkux4ww0tJwAVmeCmj711+UU2i3juXKCddOI8PPbV0RmUyMmHMhl8LG0++v2WdQsXvA2vDOIXS1xIdP4iDx1LkvT/OqbQudKSOqrKgiB0QxqO/OciXxev2OpIVBMtlSVIUvB9NStJxw/Etb1zU9ct5/JHL5Oo3MYV6r9rtqR5OuivCTUwu25DD4fAX/AZqTA==";

    public static boolean trainAnalyser(String blueprint) {
        Matrix matrix = new Matrix(blueprint);
        Graph graph = new Graph(blueprint);
        graph.getClass();
        Decoder decoder = new Decoder();
        String decodedString = decoder.decode(blueprint);
        DeadlockAnalyser analyser = new DeadlockAnalyser();
        analyser.setGraph(graph);
        ArrayList<Result> result = analyser.deadlockCheck();
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).getIsDeadlock()) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {

        if (args.length > 0) {
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
                String finalBlueprint = sb.toString();

                System.out.println(finalBlueprint+"-");
                System.out.println(blueprint+"-");
                /*boolean result = trainAnalyser(finalBlueprint);
               System.out.println(
                        "\n \n ------------------------------------------------------------------------------\n \n");
                System.out.println(result ? "Deadlock" : "No Deadlock");
                System.out.println(
                        "\n \n ------------------------------------------------------------------------------");*/
            }

        } else {
            System.out.println("Kein Inhalt angegeben.");
        }

    }
}
