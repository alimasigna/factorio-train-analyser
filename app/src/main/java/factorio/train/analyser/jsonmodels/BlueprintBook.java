package factorio.train.analyser.jsonmodels;

import java.util.ArrayList;
import java.util.List;

/**
 * A blueprint book, which 2.0 blueprint strings use freely - most published
 * rail designs come as a book rather than a bare blueprint.
 *
 * <p>Each entry of a book has the same shape as a top-level blueprint string,
 * so an entry is a {@link Json} and a book can contain further books.
 */
public class BlueprintBook {
    private Json[] blueprints;

    public BlueprintBook(Json[] blueprints) {
        this.blueprints = blueprints;
    }

    /** Every blueprint in the book that holds at least one entity, books flattened. */
    public List<Blueprint> flatten() {
        List<Blueprint> flattened = new ArrayList<>();
        if (blueprints == null) {
            return flattened;
        }
        for (Json entry : blueprints) {
            if (entry == null) {
                continue;
            }
            flattened.addAll(entry.flatten());
        }
        return flattened;
    }
}
