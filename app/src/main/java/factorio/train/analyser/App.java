/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package factorio.train.analyser;

import java.util.ArrayList;

import factorio.train.analyser.analyser.Deadlock_Analyser;
import factorio.train.analyser.analyser.Result;
import factorio.train.analyser.decoder.Decoder;
import factorio.train.analyser.graph.Graph;
import factorio.train.analyser.graph.Node;
import factorio.train.analyser.graph.Section;
import factorio.train.analyser.graph.Track;
import factorio.train.analyser.jsonmodels.Entity;
import factorio.train.analyser.jsonmodels.Position;

public class App {

    private static final String blueprint = "0eNqdl1luwjAQhu8yzy6Kl3jiXKWqKhaLWgKDklAVody9IUBF6VBmeCMo/vLP4lkOMFvt4rZJuYP6AGm+yS3Urwdo0zJPV8f/uv02Qg2pi2tQkKfr41MzTSvoFaS8iF9Q6/5NQcxd6lI8nR8f9u95t57FZnjh18mXM13BdtMOZzb5+KGB4ws7KRXsh1/lpOx79QdkfkBtN6CWH93LqIVAXUAUxrIxWv+DcUyztLk2S8EiNXF+esER2JLrLX3B4i3WEFgv9h1SRiNXHYrUVeKQkOoCNyROpE4XfOed5QUGVfOplk+VX5JAuVLbJziP1Tk+FfnUkk8NfKoXJyXtSXyC81id4MoIsifwqfyom0KaS6jJ+q+lnrzDMdKOhOZORxLfErQkx4ktozmltCmNlj1sSsZLKyAy6qpBaQVkUStxTDjU8ASVGj4KaQXkqLNaWgFZVCPOSg7VPkGlOE5aAVnqSmkFZFG9dKpDf3svPcVFcVaSg5OtpHMdU18Qx5rU5wrpZMfT58S7CVZ0J3DiuQvJacHJbwfNEa8no2U3nWDY7Mbdr75aFRV8xqY9pXqlHQaDlQvWB9/33xr+qH4=";

    public String decode() {
        Matrix matrix = new Matrix(blueprint);
        Graph graph = new Graph(blueprint);
        graph.getClass();
        Decoder decoder = new Decoder();
        String decodedString = decoder.decode(blueprint);
        Deadlock_Analyser analyser = new Deadlock_Analyser(graph);
        ArrayList<Result> result = analyser.deadlockCheck();
        return (decodedString + "\n" + matrix);
        //Matrix matrix_copy = matrix;
        //return deadlockmatrix(matrix_copy, result);
    }
/*
    private String deadlockmatrix(Matrix matrix_blueprint, ArrayList<ArrayList<Section>> deadlocks) {
        String result = "";
        for (ArrayList<Section> deadlock : deadlocks) {
            if (deadlock.size() > 1) {
                ArrayList<Position> positions = new ArrayList<Position>();
                for (Section section : deadlock) {
                    for (Node node : section.getNodes()) {
                        for (Track track : node.getTracks()) {
                            positions.add(track.getPosition());
                        }
                    }

                }
                ArrayList<Entity>[][] matrix_entities = matrix_blueprint.getMatrix();
                for (ArrayList<Entity>[] arrayLists : matrix_entities) {
                    for (ArrayList<Entity> arrayList : arrayLists) {
                        if (arrayList != null) {
                            for (int i = 0; i < arrayList.size(); i++) {
                                for (int j = 0; j < positions.size(); j++) {
                                    if (arrayList.get(i).getPosition().getX() == positions.get(j).getX()
                                            && arrayList.get(i).getPosition().getY() == positions.get(j).getY()) {
                                        continue;
                                    } else if (j == positions.size() - 1
                                            && !(arrayList.get(i).getPosition().getX() == positions.get(j).getX())) {
                                        arrayList.remove(i);
                                    }
                                }
                            }
                        }
                    }
                }
                Matrix deadlockMatrix = new Matrix();
                deadlockMatrix.overrideMatrix(matrix_entities);
                System.out.println("\n" + "------" + "\n" + deadlockMatrix.toString());
            } else {
                System.out.println("\n" + "------" + "\n" + "no deadlock");
            }
        }
        return result;
    }
*/
    public static void main(String[] args) {
        System.out.println(new App().decode());
    }
}
