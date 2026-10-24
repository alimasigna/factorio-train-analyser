package factorio.train.analyser.jsonmodels;

import java.util.ArrayList;
import java.util.List;

/**
 * The top level of a decoded blueprint string, which holds either a single
 * blueprint or a blueprint book.
 */
public class Json {
    private Blueprint blueprint;

    /** Field name matches the key Factorio writes, so Gson can bind it directly. */
    private BlueprintBook blueprint_book;

    public Json(Blueprint blueprint) {
        this.blueprint = blueprint;
    }

    /**
     * The blueprint to analyse. For a book this is the first entry holding any
     * entities, since the analyser looks at one junction at a time.
     */
    public Blueprint getBlueprint() {
        List<Blueprint> all = flatten();
        return all.isEmpty() ? null : all.get(0);
    }

    /** Every blueprint this string contains, books flattened, in book order. */
    public List<Blueprint> flatten() {
        List<Blueprint> flattened = new ArrayList<>();
        if (blueprint != null && blueprint.getEntities() != null
                && blueprint.getEntities().length > 0) {
            flattened.add(blueprint);
        }
        if (blueprint_book != null) {
            flattened.addAll(blueprint_book.flatten());
        }
        return flattened;
    }
}
