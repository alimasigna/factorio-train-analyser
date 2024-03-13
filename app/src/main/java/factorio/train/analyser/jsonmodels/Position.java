package factorio.train.analyser.jsonmodels;

import com.google.gson.annotations.Expose;

public class Position {
    @Expose
    private double x;
    @Expose
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
