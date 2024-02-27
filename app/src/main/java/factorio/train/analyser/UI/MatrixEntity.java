package factorio.train.analyser.UI;

import factorio.train.analyser.jsonmodels.Position;

public class MatrixEntity {
    public String name;
    public int direction;
    public Position position;
    public double x;
    public double y;

    public MatrixEntity(String nameNew, double xNew, double yNew, int directionNew, Position positionNew){
        name = nameNew;
        x = xNew;
        y = yNew;
        direction = directionNew;
        position = positionNew;
    }
}
