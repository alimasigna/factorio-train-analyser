package factorio.train.analyser.jsonmodels;

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

}
