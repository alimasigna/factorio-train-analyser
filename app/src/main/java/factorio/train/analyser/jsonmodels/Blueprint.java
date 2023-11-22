package factorio.train.analyser.jsonmodels;

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
}
