package factorio.train.analyser;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Loads the blueprint strings the tests run against from
 * {@code src/test/resources/blueprints}.
 *
 * <p>The {@code t-*} blueprints are small shapes written by hand for a known
 * answer. The others are real designs taken from a published 2.0 blueprint
 * book, so the tests also cover blueprints that mix the new rails with the
 * legacy ones and with elevated track.
 */
public final class Blueprints {

    private Blueprints() {
    }

    public static String load(String name) {
        String path = "/blueprints/" + name + ".txt";
        try (InputStream stream = Blueprints.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalArgumentException("missing test blueprint: " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
