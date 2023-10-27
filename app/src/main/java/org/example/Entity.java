package org.example;

public class Entity {
    int entity_number;
    String name;
    Position position;
    int direction;
    public Entity(int entity_number, String name, Position position, int direction) {
        this.name = name;
        this.entity_number = entity_number;
        this.position = position;
        this.direction = direction;
    }
}
