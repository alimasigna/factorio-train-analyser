package factorio.train.analyser.jsonmodels;
public class Position {
    private double x;
    private double y;
    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX (double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY (double y) {
        this.y = y;
    }
}
