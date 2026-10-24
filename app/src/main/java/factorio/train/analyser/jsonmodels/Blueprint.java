package factorio.train.analyser.jsonmodels;

public class Blueprint {
    private Entity[] entities;
    private Icon[] icons;
    private String label;
    private long version;

    public Blueprint(Entity[] entities, Icon[] icons) {
        this.entities = entities;
        this.icons = icons;
    }

    public Entity[] getEntities() {
        return entities;
    }

    /** The name the player gave the blueprint, if any. */
    public String getLabel() {
        return label;
    }

    /**
     * The packed game version the blueprint was exported from, with the major
     * version in the top 16 bits. Zero when the blueprint states none.
     */
    public long getVersion() {
        return version;
    }
}
