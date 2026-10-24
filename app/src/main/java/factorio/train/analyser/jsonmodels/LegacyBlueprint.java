package factorio.train.analyser.jsonmodels;

/**
 * Brings a pre-2.0 blueprint up to date, the same way the game does when such a
 * blueprint is imported.
 *
 * <p>Two things changed in 2.0. The old rail shapes were renamed with a
 * {@code legacy-} prefix to make room for the new ones, and the direction enum
 * went from eight values to sixteen, so every direction in an old blueprint is
 * half of what it would be today.
 *
 * <p>Applying both leaves the analyser with one set of names and one direction
 * convention to reason about, and means blueprints collected before the update -
 * including the ones the original study was written against - still work.
 */
public final class LegacyBlueprint {

    /** Factorio packs the major version into the top 16 bits of the version field. */
    private static final int VERSION_MAJOR_SHIFT = 48;

    private LegacyBlueprint() {
    }

    /** Whether a blueprint predates the 2.0 rail rework and needs converting. */
    public static boolean needsMigration(Blueprint blueprint) {
        long version = blueprint.getVersion();
        return version > 0 && (version >>> VERSION_MAJOR_SHIFT) < 2;
    }

    /** The same entity as a 2.0 blueprint would state it. */
    public static Entity migrate(Entity entity) {
        return new Entity(
                entity.getEntityNumber(),
                migrateName(entity.getName()),
                entity.getPosition(),
                entity.getDirection() * 2);
    }

    private static String migrateName(String name) {
        return switch (name) {
            case "straight-rail" -> "legacy-straight-rail";
            case "curved-rail" -> "legacy-curved-rail";
            default -> name;
        };
    }
}
