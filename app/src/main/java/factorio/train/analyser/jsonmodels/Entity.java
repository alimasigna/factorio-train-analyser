package factorio.train.analyser.jsonmodels;

import factorio.train.analyser.graph.LookUp;

public class Entity {
    private int entity_number;
    private String name;
    private Position position;
    private int direction;
    public Entity(int entity_number, String name, Position position, int direction) {
        this.name = name;
        this.entity_number = entity_number;
        this.position = position;
        this.direction = direction;
    }

    public Position getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public int getDirection() {
        return direction;
    }

    public LookUp[][] getConnected() {
        return LookUp.lookUpConnected(this);
    }

    public LookUp[] getCrossed() {
        return LookUp.lookUpCrossed(this);
    }
}
