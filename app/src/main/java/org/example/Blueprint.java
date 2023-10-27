package org.example;

public class Blueprint {
    private Entity[] entities;
    private Icon[] icons;
    public  Blueprint (Entity[] entities, Icon[] icons) {
        this.entities = entities;
        this.icons = icons;
    }

    public Entity[] getEntities() {
        return entities;
    }

    public void setEntities(Entity[] entities) {
        this.entities = entities;
    }

    public Icon[] getIcons() {
        return icons;
    }

    public void setIcons(Icon[] icons) {
        this.icons = icons;
    }
}
