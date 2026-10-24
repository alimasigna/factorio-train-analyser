package factorio.train.analyser;

import factorio.train.analyser.decoder.Decoder;
import factorio.train.analyser.jsonmodels.Blueprint;
import factorio.train.analyser.jsonmodels.Entity;
import factorio.train.analyser.jsonmodels.Json;
import factorio.train.analyser.jsonmodels.JsonBuilder;
import factorio.train.analyser.jsonmodels.LegacyBlueprint;
import factorio.train.analyser.rail.RailGeometry;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads a blueprint string and hands out the entities the analyser cares about.
 *
 * <p>The 1.1 version of this class also normalised every position into a
 * doubled, non-negative 2D array so that rails could be found by their
 * coordinates. The rail model no longer searches for rails by position - it
 * matches rail ends against each other - so positions are now kept exactly as
 * the blueprint states them, negative values and halves included, and the
 * indices live in
 * {@link factorio.train.analyser.graph.RailNetwork} instead. The grid here is
 * only built for {@link #toString()}, which is a debugging aid.
 */
public class Matrix {

    private static final String TRAIN_STOP = "train-stop";
    private static final String RAIL_SIGNAL = "rail-signal";
    private static final String RAIL_CHAIN_SIGNAL = "rail-chain-signal";

    /** The decoded blueprint string. */
    private Json json;

    /** The rails, signals and train stops of the blueprint. */
    private List<Entity> entities = new ArrayList<>();

    /** Anything about the blueprint the caller should be told. */
    private List<String> warnings = new ArrayList<>();

    /**
     * Reads a blueprint.
     *
     * @param encodedJson the encoded blueprint string
     */
    public Matrix(String encodedJson) {
        setJson(encodedJson);
        setEntities();
    }

    public Matrix() {
    }

    /** The rails, signals and train stops of the blueprint, in blueprint order. */
    public List<Entity> getEntities() {
        return entities;
    }

    /**
     * Notes about how the blueprint was read: which blueprint of a book was
     * picked, and whether it had to be converted from a pre-2.0 export.
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /** The blueprint that is being analysed, or null if the string could not be read. */
    public Blueprint getBlueprint() {
        return json == null ? null : json.getBlueprint();
    }

    /** Every blueprint the string contains. More than one means it was a book. */
    public List<Blueprint> getAllBlueprints() {
        return json == null ? List.of() : json.flatten();
    }

    private void setJson(String encodedJson) {
        Decoder decoder = new Decoder();
        String decodedJson = decoder.decode(encodedJson);
        if (decodedJson == null) {
            return;
        }
        json = new JsonBuilder().create(decodedJson);
    }

    private void setEntities() {
        Blueprint blueprint = getBlueprint();
        if (blueprint == null || blueprint.getEntities() == null) {
            return;
        }
        List<Blueprint> all = getAllBlueprints();
        if (all.size() > 1) {
            warnings.add("blueprint string holds " + all.size()
                    + " blueprints; analysing the first one"
                    + (blueprint.getLabel() == null ? "" : " (\"" + blueprint.getLabel() + "\")"));
        }
        boolean legacy = LegacyBlueprint.needsMigration(blueprint);
        if (legacy) {
            warnings.add("blueprint predates the 2.0 rail rework; "
                    + "its rails were converted the way the game converts them on import");
        }
        for (Entity stated : blueprint.getEntities()) {
            Entity entity = legacy ? LegacyBlueprint.migrate(stated) : stated;
            if (isRelevant(entity.getName())) {
                entities.add(entity);
            }
        }
    }

    /**
     * Whether an entity takes part in the train route model. Everything the
     * player placed ends up in a blueprint, but only rails, signals and train
     * stops matter here.
     */
    private static boolean isRelevant(String name) {
        return RailGeometry.isRail(name)
                || RailGeometry.isUnmappedRail(name)
                || RAIL_SIGNAL.equals(name)
                || RAIL_CHAIN_SIGNAL.equals(name)
                || TRAIN_STOP.equals(name);
    }

    /**
     * Renders the blueprint as a coarse map, one character per tile: {@code #}
     * for a single entity, {@code +} where several overlap and {@code .} for
     * empty ground.
     */
    @Override
    public String toString() {
        if (entities.isEmpty()) {
            return "";
        }
        int lowX = Integer.MAX_VALUE;
        int lowY = Integer.MAX_VALUE;
        int highX = Integer.MIN_VALUE;
        int highY = Integer.MIN_VALUE;
        for (Entity entity : entities) {
            lowX = Math.min(lowX, tile(entity.getPosition().getX()));
            highX = Math.max(highX, tile(entity.getPosition().getX()));
            lowY = Math.min(lowY, tile(entity.getPosition().getY()));
            highY = Math.max(highY, tile(entity.getPosition().getY()));
        }
        int[][] counts = new int[highY - lowY + 1][highX - lowX + 1];
        for (Entity entity : entities) {
            counts[tile(entity.getPosition().getY()) - lowY][tile(entity.getPosition().getX()) - lowX]++;
        }
        StringBuilder rendered = new StringBuilder();
        for (int[] row : counts) {
            for (int count : row) {
                rendered.append(count == 0 ? " . " : count == 1 ? " # " : " + ");
            }
            rendered.append("\n");
        }
        return rendered.toString();
    }

    private static int tile(double coordinate) {
        return (int) Math.floor(coordinate);
    }
}
