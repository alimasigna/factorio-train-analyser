package factorio.train.analyser.graph;

import factorio.train.analyser.jsonmodels.Entity;

public class LookUp {
    private int x;
    private int y;
    private double direction;
    private String name;

    public LookUp(int x, int y, int direction, String name) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.name = name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getDirection() {
        return direction;
    }

    public String getName() {
        return name;
    }

    // This method maps all the possible rails that can be CONNECTED to the given
    // entity
    // it also filters which connection cant be connected to each other
    public static LookUp[][] lookUpConnected(Track track) {
        return lookUpConnected(new Entity(-1, track.getName(), track.getPosition(), track.getDirection()));
    }

    public static LookUp[][] lookUpConnected(Entity entry) {
        LookUp[] entrance = {};
        LookUp[] exit = {};
        int xPos = (int) entry.getPosition().getX();
        int yPos = (int) entry.getPosition().getY();
        switch (entry.getName()) {
            case "straight-rail":
                switch (entry.getDirection()) {
                    case 0: // |
                        entrance = new LookUp[] {
                                new LookUp(xPos, yPos + 4, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 10, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 10, 4, "curved-rail")
                        };
                        exit = new LookUp[] {
                                new LookUp(xPos, yPos - 4, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 10, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 10, 1, "curved-rail")
                        };
                        break;
                    case 2: // -
                        entrance = new LookUp[] {
                                new LookUp(xPos - 4, yPos, 2, "straight-rail"),
                                new LookUp(xPos - 10, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos - 10, yPos + 2, 6, "curved-rail")
                        };
                        exit = new LookUp[] {
                                new LookUp(xPos + 4, yPos, 2, "straight-rail"),
                                new LookUp(xPos + 10, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos + 10, yPos + 2, 3, "curved-rail")
                        };
                        break;
                    case 5: // \
                        entrance = new LookUp[] {
                                new LookUp(xPos, yPos + 4, 1, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 6, 7, "curved-rail")
                        };
                        exit = new LookUp[] {
                                new LookUp(xPos - 4, yPos, 1, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 6, 4, "curved-rail")
                        };
                        break;
                    case 1: // \
                        entrance = new LookUp[] {
                                new LookUp(xPos + 4, yPos, 5, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 6, 0, "curved-rail"),
                        };
                        exit = new LookUp[] {
                                new LookUp(xPos, yPos - 4, 5, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 6, 3, "curved-rail"),
                        };
                        break;
                    case 3: // /
                        entrance = new LookUp[] {
                                new LookUp(xPos, yPos + 4, 7, "straight-rail"),
                                new LookUp(xPos - 6, yPos + 6, 2, "curved-rail")
                        };
                        exit = new LookUp[] {
                                new LookUp(xPos + 4, yPos, 7, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 6, 5, "curved-rail"),
                        };
                        break;
                    case 7: // /
                        entrance = new LookUp[] {
                                new LookUp(xPos - 4, yPos, 3, "straight-rail"),
                                new LookUp(xPos - 6, yPos + 6, 1, "curved-rail"),
                        };
                        exit = new LookUp[] {
                                new LookUp(xPos, yPos - 4, 3, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 6, 6, "curved-rail"),
                        };
                        break;
                }
                break;
            case "curved-rail":
                switch (entry.getDirection()) {
                    case 0: // '|
                        entrance = new LookUp[] {
                                new LookUp(xPos + 2, yPos + 10, 0, "straight-rail"),
                                new LookUp(xPos + 4, yPos + 16, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 16, 5, "curved-rail")
                        };
                        exit = new LookUp[] {
                                new LookUp(xPos - 6, yPos - 6, 1, "straight-rail"),
                                new LookUp(xPos - 8, yPos - 12, 4, "curved-rail")
                        };
                        break;
                    case 1: // |'
                        entrance = new LookUp[] {
                                new LookUp(xPos - 2, yPos + 10, 0, "straight-rail"),
                                new LookUp(xPos - 4, yPos + 16, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 16, 4, "curved-rail")
                        };
                        exit = new LookUp[] {
                                new LookUp(xPos + 6, yPos - 6, 7, "straight-rail"),
                                new LookUp(xPos + 8, yPos - 12, 5, "curved-rail")
                        };
                        break;
                    case 2: // --'
                        entrance = new LookUp[] {
                                new LookUp(xPos - 10, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos - 16, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 16, yPos, 7, "curved-rail"),
                        };
                        exit = new LookUp[] {
                                new LookUp(xPos + 6, yPos - 6, 3, "straight-rail"),
                                new LookUp(xPos + 12, yPos - 8, 6, "curved-rail"),
                        };
                        break;
                    case 3: // --,
                        entrance = new LookUp[] {
                                new LookUp(xPos - 10, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos - 16, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 16, yPos - 4, 7, "curved-rail"),

                        };
                        exit = new LookUp[] {
                                new LookUp(xPos + 6, yPos + 6, 1, "straight-rail"),
                                new LookUp(xPos + 12, yPos + 8, 7, "curved-rail")

                        };
                        break;
                    case 4: // |,
                        entrance = new LookUp[] {
                                new LookUp(xPos + 6, yPos + 6, 5, "straight-rail"),
                                new LookUp(xPos + 8, yPos + 12, 0, "curved-rail")
                        };
                        exit = new LookUp[] {
                                new LookUp(xPos - 2, yPos - 10, 0, "straight-rail"),
                                new LookUp(xPos, yPos - 16, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 16, 0, "curved-rail")
                        };
                        break;
                    case 5: // ,|
                        entrance = new LookUp[] {
                                new LookUp(xPos - 6, yPos + 6, 3, "straight-rail"),
                                new LookUp(xPos - 8, yPos + 12, 1, "curved-rail")
                        };
                        exit = new LookUp[] {
                                new LookUp(xPos + 2, yPos - 10, 0, "straight-rail"),
                                new LookUp(xPos + 4, yPos - 16, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 16, 0, "curved-rail")
                        };
                        break;
                    case 6: // ,--
                        entrance = new LookUp[] {
                                new LookUp(xPos - 6, yPos + 6, 7, "straight-rail"),
                                new LookUp(xPos - 12, yPos + 8, 2, "curved-rail")
                        };
                        exit = new LookUp[] {
                                new LookUp(xPos + 10, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos + 16, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 16, yPos, 3, "curved-rail")
                        };
                        break;
                    case 7: // '--
                        entrance = new LookUp[] {
                                new LookUp(xPos - 6, yPos - 6, 5, "straight-rail"),
                                new LookUp(xPos - 12, yPos - 8, 3, "curved-rail")

                        };
                        exit = new LookUp[] {
                                new LookUp(xPos + 10, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos + 16, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 16, yPos, 2, "curved-rail")

                        };
                        break;
                }
                break;
        }
        return new LookUp[][] { entrance, exit };
    }

    // This method maps all the possible rails that can cross to the given entity.
    // please note they are NOT connected
    public static LookUp[] lookUpCrossed(Track entry) {
        return lookUpCrossed(new Entity(-1, entry.getName(), entry.getPosition(), entry.getDirection()));
    }

    public static LookUp[] lookUpCrossed(Entity entry) {
        LookUp[] neighbours = {};
        int xPos = (int) entry.getPosition().getX();
        int yPos = (int) entry.getPosition().getY();
        switch (entry.getName()) {
            case "straight-rail":
                switch (entry.getDirection()) {
                    case 0: // |
                        neighbours = new LookUp[] {
                                new LookUp(xPos, yPos, 2, "straight-rail"),
                                new LookUp(xPos, yPos, 5, "straight-rail"),
                                new LookUp(xPos, yPos, 1, "straight-rail"),
                                new LookUp(xPos, yPos, 3, "straight-rail"),
                                new LookUp(xPos, yPos, 7, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 2, 2, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 2, "curved-rail"),

                                new LookUp(xPos - 6, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 6, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2, 6, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 6, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 6, "curved-rail"),

                                new LookUp(xPos - 6, yPos - 2, 3, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 3, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 3, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2, 3, "curved-rail"),

                                new LookUp(xPos + 6, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2, 7, "curved-rail"),

                                new LookUp(xPos - 2, yPos + 2, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6, 4, "curved-rail"),

                                new LookUp(xPos + 2, yPos - 2, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 0, "curved-rail"),

                                new LookUp(xPos - 2, yPos + 6, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6, 1, "curved-rail"),

                                new LookUp(xPos + 2, yPos - 6, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6, 5, "curved-rail")
                        };
                        break;
                    case 2: // -
                        neighbours = new LookUp[] {
                                new LookUp(xPos, yPos, 0, "straight-rail"),
                                new LookUp(xPos, yPos, 5, "straight-rail"),
                                new LookUp(xPos, yPos, 1, "straight-rail"),
                                new LookUp(xPos, yPos, 3, "straight-rail"),
                                new LookUp(xPos, yPos, 7, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 6, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6, 1, "curved-rail"),

                                new LookUp(xPos - 2, yPos + 6, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6, 5, "curved-rail"),

                                new LookUp(xPos + 6, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 3, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 3, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2, 3, "curved-rail"),

                                new LookUp(xPos + 6, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2, 7, "curved-rail"),

                                new LookUp(xPos + 2, yPos + 6, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 4, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 4, "curved-rail"),

                                new LookUp(xPos + 2, yPos + 6, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 0, "curved-rail"),

                                new LookUp(xPos - 6, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 6, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2, 6, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 6, "curved-rail"),

                                new LookUp(xPos - 6, yPos + 2, 2, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 2, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "curved-rail")

                        };
                        break;
                    case 5: // \
                        neighbours = new LookUp[] {
                                new LookUp(xPos, yPos, 2, "straight-rail"),
                                new LookUp(xPos - 4, yPos, 2, "straight-rail"),
                                new LookUp(xPos, yPos, 7, "straight-rail"),
                                new LookUp(xPos - 4, yPos, 3, "straight-rail"),
                                new LookUp(xPos, yPos, 0, "straight-rail"),
                                new LookUp(xPos, yPos + 4, 0, "straight-rail"),
                                new LookUp(xPos, yPos, 3, "straight-rail"),
                                new LookUp(xPos, yPos + 4, 7, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 6, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6, 5, "curved-rail"),

                                new LookUp(xPos - 2, yPos + 6, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6, 4, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 4, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2, 4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 4, "curved-rail"),

                                new LookUp(xPos - 6, yPos + 6, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6, 1, "curved-rail"),

                                new LookUp(xPos + 2, yPos + 2, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 6, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6, 0, "curved-rail"),

                                new LookUp(xPos - 2, yPos - 2, 3, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2, 3, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2, 3, "curved-rail"),

                                new LookUp(xPos - 10, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6, 7, "curved-rail"),

                                new LookUp(xPos - 10, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 6, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2, 6, "curved-rail"),

                                new LookUp(xPos - 6, yPos + 6, 2, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 2, 2, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "curved-rail")
                        };
                        break;
                    case 1: // \
                        neighbours = new LookUp[] {
                                new LookUp(xPos, yPos, 2, "straight-rail"),
                                new LookUp(xPos + 4, yPos, 2, "straight-rail"),
                                new LookUp(xPos, yPos, 3, "straight-rail"),
                                new LookUp(xPos + 4, yPos, 7, "straight-rail"),
                                new LookUp(xPos, yPos, 0, "straight-rail"),
                                new LookUp(xPos, yPos + 4, 0, "straight-rail"),
                                new LookUp(xPos, yPos, 7, "straight-rail"),
                                new LookUp(xPos, yPos - 4, 3, "straight-rail"),

                                new LookUp(xPos + 6, yPos - 6, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6, 1, "curved-rail"),

                                new LookUp(xPos + 2, yPos - 6, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "curved-rail"),

                                new LookUp(xPos + 6, yPos - 6, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6, 5, "curved-rail"),

                                new LookUp(xPos - 2, yPos - 2, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 4, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 6, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6, 4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 4, "curved-rail"),

                                new LookUp(xPos + 2, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2, 7, "curved-rail"),

                                new LookUp(xPos + 10, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 3, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 3, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 3, "curved-rail"),

                                new LookUp(xPos + 10, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 2, 2, "curved-rail"),

                                new LookUp(xPos - 6, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 6, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2, 6, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 6, 6, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 6, "curved-rail")
                        };
                        break;
                    case 3: // /
                        neighbours = new LookUp[] {
                                new LookUp(xPos, yPos, 0, "straight-rail"),
                                new LookUp(xPos, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos, yPos, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos, 5, "straight-rail"),
                                new LookUp(xPos, yPos, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos, 2, "straight-rail"),
                                new LookUp(xPos, yPos, 5, "straight-rail"),
                                new LookUp(xPos, yPos + 2, 1, "straight-rail"),

                                new LookUp(xPos + 2, yPos + 2, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6, 4, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 6, 4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 4, "curved-rail"),

                                new LookUp(xPos + 2, yPos + 6, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 5, "curved-rail"),

                                new LookUp(xPos + 6, yPos + 6, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "curved-rail"),

                                new LookUp(xPos - 2, yPos + 2, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6, 1, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 6, 1, "curved-rail"),

                                new LookUp(xPos + 2, yPos - 2, 6, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2, 6, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 2, 6, "curved-rail"),

                                new LookUp(xPos - 2, yPos + 6, 2, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 2, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos + 10, yPos - 2, 2, "curved-rail"),

                                new LookUp(xPos + 10, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 3, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2, 3, "curved-rail"),

                                new LookUp(xPos - 6, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 6, 7, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 7, "curved-rail")
                        };
                        break;
                    case 7: // /
                        neighbours = new LookUp[] {
                                new LookUp(xPos, yPos, 0, "straight-rail"),
                                new LookUp(xPos, yPos - 4, 0, "straight-rail"),
                                new LookUp(xPos, yPos, 5, "straight-rail"),
                                new LookUp(xPos - 4, yPos, 1, "straight-rail"),
                                new LookUp(xPos, yPos, 2, "straight-rail"),
                                new LookUp(xPos - 4, yPos, 2, "straight-rail"),
                                new LookUp(xPos, yPos, 1, "straight-rail"),
                                new LookUp(xPos, yPos - 4, 5, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 2, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 0, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 6, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6, 0, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 0, "curved-rail"),

                                new LookUp(xPos - 2, yPos - 2, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 1, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 1, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 2, 1, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 1, "curved-rail"),

                                new LookUp(xPos - 2, yPos - 2, 4, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 6, 4, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 6, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 6, 4, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6, 4, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 4, "curved-rail"),

                                new LookUp(xPos - 2, yPos + 2, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 6, 5, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 6, 5, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 6, 5, "curved-rail"),

                                new LookUp(xPos - 2, yPos + 2, 2, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 2, 2, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "curved-rail"),
                                new LookUp(xPos + 6, yPos - 2, 2, "curved-rail"),

                                new LookUp(xPos + 2, yPos - 6, 6, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 6, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 6, "curved-rail"),
                                new LookUp(xPos - 2, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos - 6, yPos + 2, 6, "curved-rail"),
                                new LookUp(xPos - 10, yPos + 2, 6, "curved-rail"),

                                new LookUp(xPos - 10, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos + 2, yPos - 2, 7, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "curved-rail"),
                                new LookUp(xPos + 6, yPos + 2, 7, "curved-rail"),

                                new LookUp(xPos + 6, yPos + 2, 3, "curved-rail"),
                                new LookUp(xPos - 2, yPos - 2, 3, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 2, 3, "curved-rail"),
                                new LookUp(xPos - 6, yPos - 6, 3, "curved-rail"),
                                new LookUp(xPos + 2, yPos + 2, 3, "curved-rail")
                        };
                        break;
                }
                break;
            case "curved-rail":
                switch (entry.getDirection()) {
                    case 0: // '|
                        neighbours = new LookUp[] {
                                new LookUp(xPos + 2, yPos + 6, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 6, 2, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 6, 2, "straight-rail"),

                                new LookUp(xPos + 2, yPos + 6, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 6, 0, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 2, 7, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 6, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6, 7, "straight-rail"),

                                new LookUp(xPos - 6, yPos - 6, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 6, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6, 3, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 6, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6, 5, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 2, 1, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 10, 1, "straight-rail"),

                                new LookUp(xPos - 4, yPos - 12, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 0, "curved-rail"),

                                new LookUp(xPos - 4, yPos + 8, 2, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 2, "curved-rail"),

                                new LookUp(xPos - 4, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 12, 4, "curved-rail"),

                                new LookUp(xPos - 12, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 6, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 6, "curved-rail"),

                                new LookUp(xPos - 4, yPos - 8, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos, yPos - 12, 5, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 5, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 12, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 5, "curved-rail"),

                                new LookUp(xPos - 8, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 12, yPos - 8, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 8, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 7, "curved-rail"),

                                new LookUp(xPos - 8, yPos, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos - 12, 7, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 7, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos, 7, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 7, "curved-rail"),
                                new LookUp(xPos, yPos + 12, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 7, "curved-rail"),

                                new LookUp(xPos - 8, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 8, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 3, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 8, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 3, "curved-rail")
                        };
                        break;
                    case 1: // |'
                        neighbours = new LookUp[] {
                                new LookUp(xPos - 2, yPos + 6, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6, 2, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 6, 2, "straight-rail"),

                                new LookUp(xPos + 2, yPos - 6, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 6, 0, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 6, 7, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 10, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 2, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 6, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6, 3, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 2, 5, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 6, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6, 5, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 6, 5, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 6, 1, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 1, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6, 1, "straight-rail"),

                                new LookUp(xPos - 4, yPos + 12, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos, 5, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 5, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 8, 5, "curved-rail"),

                                new LookUp(xPos - 8, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 7, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 7, "curved-rail"),

                                new LookUp(xPos - 8, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 3, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 3, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos + 12, yPos - 4, 3, "curved-rail"),

                                new LookUp(xPos + 4, yPos - 12, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 1, "curved-rail"),

                                new LookUp(xPos + 4, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos, yPos - 12, 4, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 12, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 4, "curved-rail"),

                                new LookUp(xPos - 8, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 2, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 8, 2, "curved-rail"),
                                new LookUp(xPos + 12, yPos - 8, 2, "curved-rail"),

                                new LookUp(xPos + 8, yPos, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 12, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos, 0, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 0, "curved-rail"),
                                new LookUp(xPos, yPos + 12, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 0, "curved-rail"),

                                new LookUp(xPos, yPos + 8, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 8, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 6, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 8, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 6, "curved-rail")
                        };
                        break;
                    case 2: // --'
                        neighbours = new LookUp[] {
                                new LookUp(xPos - 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos - 6, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 2, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 6, 0, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 1, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2, 1, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 2, 5, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 6, 5, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 7, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2, 7, "straight-rail"),

                                new LookUp(xPos - 10, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos - 6, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 3, "straight-rail"),

                                new LookUp(xPos - 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 12, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 8, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos, 6, "curved-rail"),

                                new LookUp(xPos + 4, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 4, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 4, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 4, "curved-rail"),

                                new LookUp(xPos + 4, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 12, yPos - 4, 2, "curved-rail"),

                                new LookUp(xPos - 8, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 12, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 12, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 7, "curved-rail"),

                                new LookUp(xPos - 4, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos, yPos, 1, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 1, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 12, 1, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 1, "curved-rail"),

                                new LookUp(xPos - 4, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 0, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 0, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 12, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos, 0, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 0, "curved-rail"),

                                new LookUp(xPos, yPos - 8, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos - 12, yPos, 3, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 12, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 3, "curved-rail"),

                                new LookUp(xPos - 8, yPos, 5, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 8, 5, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 5, "curved-rail")
                        };
                        break;
                    case 3: // --,
                        neighbours = new LookUp[] {
                                new LookUp(xPos + 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "straight-rail"),

                                new LookUp(xPos + 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 6, 0, "straight-rail"),

                                new LookUp(xPos - 6, yPos - 2, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2, 3, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 2, 7, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 6, 7, "straight-rail"),

                                new LookUp(xPos - 6, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 5, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2, 5, "straight-rail"),

                                new LookUp(xPos - 10, yPos - 2, 1, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 2, 1, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 1, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 1, "straight-rail"),

                                new LookUp(xPos - 8, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 12, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 12, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 6, "curved-rail"),

                                new LookUp(xPos - 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 12, 4, "curved-rail"),

                                new LookUp(xPos, yPos + 8, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos - 12, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 12, yPos, 2, "curved-rail"),

                                new LookUp(xPos - 8, yPos, 0, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 0, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 8, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 0, "curved-rail"),

                                new LookUp(xPos, yPos - 8, 5, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 5, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 12, 5, "curved-rail"),

                                new LookUp(xPos + 12, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 3, "curved-rail"),

                                new LookUp(xPos - 8, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 1, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 1, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos, yPos, 1, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 1, "curved-rail"),

                                new LookUp(xPos + 8, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 8, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 12, yPos - 4, 7, "curved-rail")
                        };
                        break;
                    case 4: // |,
                        neighbours = new LookUp[] {
                                new LookUp(xPos - 2, yPos - 6, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6, 2, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 6, 2, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 6, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6, 0, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 6, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6, 3, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 6, 7, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6, 7, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 6, 7, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 6, 1, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 1, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 6, 1, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 10, 5, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 6, 5, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 5, "straight-rail"),

                                new LookUp(xPos + 4, yPos + 8, 7, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 8, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 8, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 7, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 7, "curved-rail"),

                                new LookUp(xPos - 4, yPos - 8, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos, yPos - 12, 5, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 5, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 12, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 5, "curved-rail"),

                                new LookUp(xPos + 4, yPos + 8, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 8, 3, "curved-rail"),
                                new LookUp(xPos + 12, yPos + 8, 3, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 3, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 3, "curved-rail"),

                                new LookUp(xPos - 4, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 12, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos, yPos, 1, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 1, "curved-rail"),
                                new LookUp(xPos, yPos + 12, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 1, "curved-rail"),

                                new LookUp(xPos - 4, yPos - 8, 2, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 2, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos + 12, yPos + 4, 2, "curved-rail"),

                                new LookUp(xPos + 4, yPos + 12, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 4, "curved-rail"),

                                new LookUp(xPos, yPos + 8, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 6, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 6, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 6, "curved-rail"),

                                new LookUp(xPos - 4, yPos - 12, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 0, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 8, 0, "curved-rail")
                        };
                        break;
                    case 5: // ,|
                        neighbours = new LookUp[] {
                                new LookUp(xPos - 6, yPos + 6, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 6, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6, 2, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 6, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6, 0, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 2, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 10, 3, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 6, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6, 7, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 6, 1, "straight-rail"),
                                new LookUp(xPos - 6, yPos + 6, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6, 1, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 2, 5, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 6, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 6, 5, "straight-rail"),

                                new LookUp(xPos - 4, yPos + 8, 2, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 8, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 8, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 2, "curved-rail"),

                                new LookUp(xPos + 4, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos, yPos - 12, 4, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 12, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 4, "curved-rail"),

                                new LookUp(xPos - 4, yPos + 8, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 8, 6, "curved-rail"),
                                new LookUp(xPos - 12, yPos + 8, 6, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 6, "curved-rail"),

                                new LookUp(xPos, yPos - 12, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos, 0, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 0, "curved-rail"),
                                new LookUp(xPos, yPos + 12, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 0, "curved-rail"),

                                new LookUp(xPos - 4, yPos + 12, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 5, "curved-rail"),

                                new LookUp(xPos + 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos - 12, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 7, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 7, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 7, "curved-rail"),

                                new LookUp(xPos + 8, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos, yPos, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 3, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 3, "curved-rail"),

                                new LookUp(xPos, yPos, 1, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 1, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 8, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 12, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 1, "curved-rail")
                        };
                        break;
                    case 6: // ,--
                        neighbours = new LookUp[] {
                                new LookUp(xPos - 6, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2, 2, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos - 6, yPos + 6, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2, 0, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 2, 5, "straight-rail"),
                                new LookUp(xPos - 6, yPos + 2, 5, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2, 5, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 6, 1, "straight-rail"),
                                new LookUp(xPos - 6, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 1, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2, 1, "straight-rail"),

                                new LookUp(xPos - 6, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 3, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 3, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2, 3, "straight-rail"),

                                new LookUp(xPos - 2, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 7, "straight-rail"),
                                new LookUp(xPos + 6, yPos - 2, 7, "straight-rail"),
                                new LookUp(xPos + 10, yPos - 2, 7, "straight-rail"),

                                new LookUp(xPos - 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos - 12, yPos, 3, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 12, yPos, 3, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 3, "curved-rail"),

                                new LookUp(xPos - 8, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 12, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 5, "curved-rail"),

                                new LookUp(xPos - 12, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 6, "curved-rail"),

                                new LookUp(xPos, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos, 0, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 0, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 0, "curved-rail"),

                                new LookUp(xPos, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 8, 2, "curved-rail"),
                                new LookUp(xPos + 12, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 2, "curved-rail"),

                                new LookUp(xPos, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos, yPos, 1, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 8, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 1, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 1, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 8, 1, "curved-rail"),

                                new LookUp(xPos, yPos + 8, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 7, "curved-rail"),
                                new LookUp(xPos - 12, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 7, "curved-rail"),
                                new LookUp(xPos + 12, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 7, "curved-rail"),

                                new LookUp(xPos + 4, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 12, 4, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 4, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 4, "curved-rail")
                        };
                        break;
                    case 7: // '--
                        neighbours = new LookUp[] {
                                new LookUp(xPos - 6, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 2, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 2, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2, 2, "straight-rail"),

                                new LookUp(xPos - 6, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 6, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 0, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 0, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2, 0, "straight-rail"),

                                new LookUp(xPos - 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos - 2, 5, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 5, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2, 5, "straight-rail"),
                                new LookUp(xPos + 10, yPos + 2, 5, "straight-rail"),

                                new LookUp(xPos - 6, yPos - 2, 1, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 1, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 1, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2, 1, "straight-rail"),

                                new LookUp(xPos + 2, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2, 3, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 6, 3, "straight-rail"),
                                new LookUp(xPos - 6, yPos - 2, 3, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 3, "straight-rail"),

                                new LookUp(xPos - 6, yPos - 2, 7, "straight-rail"),
                                new LookUp(xPos - 2, yPos - 2, 7, "straight-rail"),
                                new LookUp(xPos - 2, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos + 2, yPos + 2, 7, "straight-rail"),
                                new LookUp(xPos + 6, yPos + 2, 7, "straight-rail"),

                                new LookUp(xPos + 8, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4, 6, "curved-rail"),
                                new LookUp(xPos - 12, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 6, "curved-rail"),
                                new LookUp(xPos + 12, yPos, 6, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 6, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 6, "curved-rail"),

                                new LookUp(xPos, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 4, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 4, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 4, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 4, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 8, 4, "curved-rail"),

                                new LookUp(xPos, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos, yPos, 1, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 1, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 1, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 1, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 12, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 1, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 1, "curved-rail"),

                                new LookUp(xPos - 8, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 12, 0, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 0, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 0, "curved-rail"),

                                new LookUp(xPos - 8, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 2, "curved-rail"),
                                new LookUp(xPos + 12, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 12, yPos, 2, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 2, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 2, "curved-rail"),

                                new LookUp(xPos - 12, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 7, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 7, "curved-rail"),

                                new LookUp(xPos, yPos - 8, 5, "curved-rail"),
                                new LookUp(xPos, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos - 8, yPos, 5, "curved-rail"),
                                new LookUp(xPos - 8, yPos + 4 - 8, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 8, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 4, 5, "curved-rail"),
                                new LookUp(xPos - 4, yPos + 8, 5, "curved-rail"),
                                new LookUp(xPos + 8, yPos - 4, 5, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 5, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 5, "curved-rail"),

                                new LookUp(xPos, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 12, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 8, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos - 4, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos, 3, "curved-rail"),
                                new LookUp(xPos + 4, yPos + 4, 3, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 4, 3, "curved-rail"),
                                new LookUp(xPos - 8, yPos - 8, 3, "curved-rail")
                        };
                        break;
                }
                break;
        }
        return neighbours;
    }

    public static LookUp[] lookUpSignal(Track track) {
        return lookUpSignal(new Entity(-1, track.getName(), track.getPosition(), track.getDirection()));
    }

    public static LookUp[] lookUpSignal(Entity entry) {
        LookUp[] signals = {};
        int xPos = (int) entry.getPosition().getX();
        int yPos = (int) entry.getPosition().getY();
        switch (entry.getName()) {
            case "straight-rail":
                switch (entry.getDirection()) {
                    case 0: // |
                        signals = new LookUp[] {
                                new LookUp(xPos - 3, yPos - 1, 0, "rail-signal"),
                                new LookUp(xPos - 3, yPos + 1, 0, "rail-signal"),
                                new LookUp(xPos + 3, yPos - 1, 4, "rail-signal"),
                                new LookUp(xPos + 3, yPos + 1, 4, "rail-signal"),

                                new LookUp(xPos - 3, yPos - 1, 0, "rail-chain-signal"),
                                new LookUp(xPos - 3, yPos + 1, 0, "rail-chain-signal"),
                                new LookUp(xPos + 3, yPos - 1, 4, "rail-chain-signal"),
                                new LookUp(xPos + 3, yPos + 1, 4, "rail-chain-signal")
                        };
                        break;
                    case 2: // -
                        signals = new LookUp[] {
                                new LookUp(xPos - 1, yPos - 3, 2, "rail-signal"),
                                new LookUp(xPos + 1, yPos - 3, 2, "rail-signal"),
                                new LookUp(xPos - 1, yPos + 3, 6, "rail-signal"),
                                new LookUp(xPos + 1, yPos + 3, 6, "rail-signal"),

                                new LookUp(xPos - 1, yPos - 3, 2, "rail-chain-signal"),
                                new LookUp(xPos + 1, yPos - 3, 2, "rail-chain-signal"),
                                new LookUp(xPos - 1, yPos + 3, 6, "rail-chain-signal"),
                                new LookUp(xPos + 1, yPos + 3, 6, "rail-chain-signal")
                        };
                        break;
                    case 5: // \
                        signals = new LookUp[] {
                                new LookUp(xPos + 1, yPos - 1, 3, "rail-signal"),
                                new LookUp(xPos - 3, yPos + 3, 7, "rail-signal"),

                                new LookUp(xPos + 1, yPos - 1, 3, "rail-chain-signal"),
                                new LookUp(xPos - 3, yPos + 3, 7, "rail-chain-signal")
                        };
                        break;
                    case 1: // \
                        signals = new LookUp[] {
                                new LookUp(xPos - 1, yPos + 1, 3, "rail-signal"),
                                new LookUp(xPos + 3, yPos - 3, 7, "rail-signal"),

                                new LookUp(xPos - 1, yPos + 1, 3, "rail-chain-signal"),
                                new LookUp(xPos + 3, yPos - 3, 7, "rail-chain-signal")
                        };
                        break;
                    case 3: // /
                        signals = new LookUp[] {
                                new LookUp(xPos - 1, yPos - 1, 1, "rail-signal"),
                                new LookUp(xPos + 3, yPos + 3, 5, "rail-signal"),

                                new LookUp(xPos - 1, yPos - 1, 1, "rail-chain-signal"),
                                new LookUp(xPos + 3, yPos + 3, 5, "rail-chain-signal")
                        };
                        break;
                    case 7: // /
                        signals = new LookUp[] {
                                new LookUp(xPos - 3, yPos - 3, 1, "rail-signal"),
                                new LookUp(xPos + 1, yPos + 1, 5, "rail-signal"),

                                new LookUp(xPos - 3, yPos - 3, 1, "rail-chain-signal"),
                                new LookUp(xPos + 1, yPos + 1, 5, "rail-chain-signal")
                        };
                        break;
                }
                break;
            case "curved-rail":
                switch (entry.getDirection()) {
                    case 0: // '|
                        signals = new LookUp[] {
                                new LookUp(xPos - 1, yPos - 7, 3, "rail-signal"),
                                new LookUp(xPos - 5, yPos - 3, 7, "rail-signal"),
                                new LookUp(xPos - 1, yPos + 7, 0, "rail-signal"),
                                new LookUp(xPos + 5, yPos + 7, 4, "rail-signal"),

                                new LookUp(xPos - 1, yPos - 7, 3, "rail-chain-signal"),
                                new LookUp(xPos - 5, yPos - 3, 7, "rail-chain-signal"),
                                new LookUp(xPos - 1, yPos + 7, 0, "rail-chain-signal"),
                                new LookUp(xPos + 5, yPos + 7, 4, "rail-chain-signal")
                        };
                        break;
                    case 1: // |'
                        signals = new LookUp[] {
                                new LookUp(xPos + 1, yPos - 7, 1, "rail-signal"),
                                new LookUp(xPos + 5, yPos - 3, 5, "rail-signal"),
                                new LookUp(xPos - 5, yPos + 7, 0, "rail-signal"),
                                new LookUp(xPos + 1, yPos + 7, 4, "rail-signal"),

                                new LookUp(xPos + 1, yPos - 7, 1, "rail-chain-signal"),
                                new LookUp(xPos + 5, yPos - 3, 5, "rail-chain-signal"),
                                new LookUp(xPos - 5, yPos + 7, 0, "rail-chain-signal"),
                                new LookUp(xPos + 1, yPos + 7, 4, "rail-chain-signal")
                        };
                        break;
                    case 2: // --'
                        signals = new LookUp[] {
                                new LookUp(xPos - 7, yPos - 1, 2, "rail-signal"),
                                new LookUp(xPos - 7, yPos + 5, 6, "rail-signal"),
                                new LookUp(xPos + 3, yPos - 5, 1, "rail-signal"),
                                new LookUp(xPos + 7, yPos - 1, 5, "rail-signal"),

                                new LookUp(xPos - 7, yPos - 1, 2, "rail-chain-signal"),
                                new LookUp(xPos - 7, yPos + 5, 6, "rail-chain-signal"),
                                new LookUp(xPos + 3, yPos - 5, 1, "rail-chain-signal"),
                                new LookUp(xPos + 7, yPos - 1, 5, "rail-chain-signal")
                        };
                        break;
                    case 3: // --,
                        signals = new LookUp[] {
                                new LookUp(xPos - 7, yPos - 5, 2, "rail-signal"),
                                new LookUp(xPos - 7, yPos + 1, 6, "rail-signal"),
                                new LookUp(xPos + 7, yPos + 1, 3, "rail-signal"),
                                new LookUp(xPos + 3, yPos + 5, 7, "rail-signal"),

                                new LookUp(xPos - 7, yPos - 5, 2, "rail-chain-signal"),
                                new LookUp(xPos - 7, yPos + 1, 6, "rail-chain-signal"),
                                new LookUp(xPos + 7, yPos + 1, 3, "rail-chain-signal"),
                                new LookUp(xPos + 3, yPos + 5, 7, "rail-chain-signal")
                        };
                        break;
                    case 4: // |,
                        signals = new LookUp[] {
                                new LookUp(xPos - 5, yPos - 7, 0, "rail-signal"),
                                new LookUp(xPos + 1, yPos - 7, 4, "rail-signal"),
                                new LookUp(xPos + 5, yPos + 3, 3, "rail-signal"),
                                new LookUp(xPos + 1, yPos + 7, 7, "rail-signal"),

                                new LookUp(xPos - 5, yPos - 7, 0, "rail-chain-signal"),
                                new LookUp(xPos + 1, yPos - 7, 4, "rail-chain-signal"),
                                new LookUp(xPos + 5, yPos + 3, 3, "rail-chain-signal"),
                                new LookUp(xPos + 1, yPos + 7, 7, "rail-chain-signal")
                        };
                        break;
                    case 5: // ,|
                        signals = new LookUp[] {
                                new LookUp(xPos - 1, yPos - 7, 0, "rail-signal"),
                                new LookUp(xPos + 5, yPos - 7, 4, "rail-signal"),
                                new LookUp(xPos - 5, yPos + 3, 1, "rail-signal"),
                                new LookUp(xPos - 1, yPos + 7, 5, "rail-signal"),

                                new LookUp(xPos - 1, yPos - 7, 0, "rail-chain-signal"),
                                new LookUp(xPos + 5, yPos - 7, 4, "rail-chain-signal"),
                                new LookUp(xPos - 5, yPos + 3, 1, "rail-chain-signal"),
                                new LookUp(xPos - 1, yPos + 7, 5, "rail-chain-signal")
                        };
                        break;
                    case 6: // ,--
                        signals = new LookUp[] {
                                new LookUp(xPos + 7, yPos - 5, 2, "rail-signal"),
                                new LookUp(xPos + 7, yPos + 1, 6, "rail-signal"),
                                new LookUp(xPos - 7, yPos + 1, 1, "rail-signal"),
                                new LookUp(xPos - 3, yPos + 5, 5, "rail-signal"),

                                new LookUp(xPos + 7, yPos - 5, 2, "rail-chain-signal"),
                                new LookUp(xPos + 7, yPos + 1, 6, "rail-chain-signal"),
                                new LookUp(xPos - 7, yPos + 1, 1, "rail-chain-signal"),
                                new LookUp(xPos - 3, yPos + 5, 5, "rail-chain-signal")
                        };
                        break;
                    case 7: // '--
                        signals = new LookUp[] {
                                new LookUp(xPos + 7, yPos - 1, 2, "rail-signal"),
                                new LookUp(xPos + 7, yPos + 5, 6, "rail-signal"),
                                new LookUp(xPos - 3, yPos - 5, 3, "rail-signal"),
                                new LookUp(xPos - 7, yPos - 1, 7, "rail-signal"),

                                new LookUp(xPos + 7, yPos - 1, 2, "rail-chain-signal"),
                                new LookUp(xPos + 7, yPos + 5, 6, "rail-chain-signal"),
                                new LookUp(xPos - 3, yPos - 5, 3, "rail-chain-signal"),
                                new LookUp(xPos - 7, yPos - 1, 7, "rail-chain-signal")
                        };
                        break;
                }
                break;
        }
        return signals;
    }

    public static LookUp[] lookUpOutgoingTracks(Entity signal) {
        LookUp[] outgoingTracks = {};
        int xPos = (int) signal.getPosition().getX();
        int yPos = (int) signal.getPosition().getY();
        if (signal.getName().equals("rail-signal") || signal.getName().equals("rail-chain-signal")) {
            switch (signal.getDirection()) {
                case 0:
                    outgoingTracks = new LookUp[] {
                            new LookUp(xPos + 3, yPos - 3, 0, "straight-rail"),
                            new LookUp(xPos + 1, yPos + 9, 5, "curved-rail"),
                            new LookUp(xPos + 5, yPos + 9, 4, "curved-rail"),
                    };
                    break;
                case 6:
                    outgoingTracks = new LookUp[] {
                            new LookUp(xPos + 3, yPos - 3, 2, "straight-rail"),
                            new LookUp(xPos + 9, yPos - 5, 2, "curved-rail"),
                            new LookUp(xPos + 9, yPos - 1, 3, "curved-rail"),
                    };
                    break;
                case 2:
                    outgoingTracks = new LookUp[] {
                            new LookUp(xPos - 3, yPos - 3, 2, "straight-rail"),
                            new LookUp(xPos - 9, yPos + 1, 7, "curved-rail"),
                            new LookUp(xPos - 9, yPos + 5, 6, "curved-rail"),
                    };
                    break;
                case 4:
                    outgoingTracks = new LookUp[] {
                            new LookUp(xPos - 3, yPos - 3, 0, "straight-rail"),
                            new LookUp(xPos - 5, yPos - 9, 0, "curved-rail"),
                            new LookUp(xPos - 1, yPos - 9, 1, "curved-rail"),
                    };
                    break;
            }
        }
        return outgoingTracks;
    }
}
