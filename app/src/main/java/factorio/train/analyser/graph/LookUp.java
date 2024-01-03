package factorio.train.analyser.graph;

public class LookUp {
    private double x;
    private double y;
    private double direction;
    private String name;

    public LookUp(double x, double y, int direction, String name) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.name = name;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getDirection() {
        return direction;
    }

    public String getName() {
        return name;
    }
}
