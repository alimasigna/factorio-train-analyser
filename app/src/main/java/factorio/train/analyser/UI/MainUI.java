package factorio.train.analyser.UI;

import java.util.ArrayList;

import factorio.train.analyser.Matrix;
import factorio.train.analyser.analyser.DeadlockAnalyser;
import factorio.train.analyser.analyser.Result;
import factorio.train.analyser.decoder.Decoder;
import factorio.train.analyser.graph.Graph;
import factorio.train.analyser.graph.Node;
import factorio.train.analyser.jsonmodels.Entity;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainUI extends Application {

    private static final String blueprint = "0eNqV0eEKgjAQB/B3uc8LUjeXe5WIMD3qQKdsMxLZuzcnRJJBfbzt/r/duAkuzYC9Ie1ATUBVpy2o4wSWrrps5jM39ggKyGELDHTZzpUpqQHPgHSND1CJPzFA7cgRLvlYjGc9tBc0oeGVrAZzx3oXAQZ9Z0Om0/NDweFiz2AElafBrslgtVwKzz7I9EVaF7TrzX1DuVxQuUblBpr9jYo1mm2g/MfPcx5JuV+TyQYpfp8zW1Dh5x3FLaq3pTO4o7GxPz0kXBapPPAiy4vc+ydmr7FI";

    private double z;
    private double scalePos;
    private final double focusLength = 0.3;
    private final double focusLengthTranslate = 2.;

    private ArrayList<Entity>[][] matrixBlueprint;
    private ArrayList<Result> deadlocks;

    @Override
    public void start(Stage stage) {

        Matrix matrixClass = new Matrix(blueprint);
        matrixBlueprint = matrixClass.getMatrix();
        Graph graph = new Graph(blueprint);
        graph.getClass();
        DeadlockAnalyser analyser = new DeadlockAnalyser();
        analyser.setGraph(graph);
        deadlocks = analyser.deadlockCheck();

        // UI stuff
        scalePos = 1.f;
        Group root = new Group();
        Canvas canvas = new Canvas(600, 500);
        root.getChildren().add(canvas);
        Scene scene = new Scene(root);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        scene.widthProperty().addListener(evt -> draw(gc));
        scene.heightProperty().addListener(evt -> draw(gc));

        canvas.widthProperty().bind(scene.widthProperty());
        canvas.heightProperty().bind(scene.heightProperty());

        draw(gc);

        stage.setTitle("Factorio-Trainanalyser");
        stage.setScene(scene);
        stage.show();
    }

    private void draw(GraphicsContext gc) {

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        renderMatrix(gc);
    }

    private void renderMatrix(GraphicsContext gc) {
        final double canvasWidth = gc.getCanvas().getWidth();
        final double canvasHeight = gc.getCanvas().getHeight();

        double scaleSize = java.lang.Math.max(canvasWidth, canvasHeight);
        // use z to calculate perspective object position and size
        scalePos = scaleSize * 0.9 * (1 + focusLengthTranslate) / (z + focusLengthTranslate);
        scaleSize = scaleSize * (1 + focusLength) / (z + focusLength);

        gc.setFont(new Font("Arial", scaleSize / 80));
        gc.setFill(Color.BLUE);
        gc.setStroke(Color.RED);
        if (matrixBlueprint == null)
            return;

        ArrayList<MatrixEntity>[][] visualMatrix = new ArrayList[matrixBlueprint.length][matrixBlueprint[0].length];
        for (int i = 0; i < matrixBlueprint.length; i++) {
            for (int j = 0; j < matrixBlueprint[i].length; j++) {
                if (matrixBlueprint[i][j] != null) {
                    for(int k = 0; k < matrixBlueprint[i][j].size(); k++){
                        if (!matrixBlueprint[i][j].get(k).getName().equals("rail-signal")) {
                            double xPos = 2 + i * scalePos / 60;
                            double yPos = 80 + j * scalePos / 80;
                            MatrixEntity entity = new MatrixEntity(matrixBlueprint[i][j].get(k).getName(), xPos, yPos,
                                    matrixBlueprint[i][j].get(k).getDirection(),
                                    matrixBlueprint[i][j].get(k).getPosition());
                            ArrayList<MatrixEntity> newEntity = new ArrayList<MatrixEntity>();
                            newEntity.add(entity);
                            visualMatrix[i][j] = newEntity;
                            if(matrixBlueprint[i][j].size() > 1){
                                gc.fillText("X", xPos, yPos);
                            }else{
                                gc.fillText("O", xPos, yPos);
                            }
                        }
                    }
                }
            }
        }
        
        for (Result deadlock : deadlocks) {
            if (!deadlock.getIsDeadlock()) {

            } else {
                for (Node node : deadlock.getDeadlockPath()) {
                    for (int k = 1; k < node.getTracks().size(); k++) {
                        MatrixEntity start = null;
                        MatrixEntity end = null;
                        for (int i = 0; i < visualMatrix.length; i++) {
                            for (int j = 0; j < visualMatrix[i].length; j++) {
                                if (visualMatrix[i][j] != null) {
                                    for(int u = 0; u < visualMatrix[i][j].size(); u++) {
                                        if(visualMatrix[i][j].get(u).position.getX() == node.getTracks().get(k-1).getPosition().getX() && visualMatrix[i][j].get(u).position.getY() == node.getTracks().get(k-1).getPosition().getY()){
                                            start = visualMatrix[i][j].get(u);
                                        }else if(visualMatrix[i][j].get(u).position.getX() == node.getTracks().get(k).getPosition().getX() && visualMatrix[i][j].get(u).position.getY() == node.getTracks().get(k).getPosition().getY()){
                                            end = visualMatrix[i][j].get(u);
                                        }
                                    }
                                }
                            }
                        }
                        if(start != null && end != null){
                            gc.strokeLine(start.x+30, start.y-30, end.x+30, end.y-30);
                        }
                    }
                }
            }
        }
    }
}
