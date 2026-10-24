package factorio.train.analyser.decoder;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.InflaterInputStream;

/**
 * Turns a Factorio blueprint string into the JSON it encodes.
 *
 * <p>A blueprint string is a version byte followed by base64-encoded,
 * zlib-deflated JSON. The version byte has been {@code 0} for every release so
 * far, including 2.0.
 *
 * @see <a href="https://wiki.factorio.com/Blueprint_string_format">Blueprint string format</a>
 */
public class Decoder {

    /**
     * Decodes a blueprint string.
     *
     * @param input the encoded blueprint string
     * @return the JSON it contains, or null if the string could not be decoded
     */
    public String decode(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        try {
            // Drop the leading version byte, then inflate as a stream: a 2.0
            // city-block blueprint runs to several megabytes of JSON, so there
            // is no sensible fixed buffer to decompress into.
            byte[] compressed = Base64.getDecoder().decode(input.trim().substring(1));
            try (InflaterInputStream inflater =
                         new InflaterInputStream(new ByteArrayInputStream(compressed))) {
                return new String(inflater.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception exception) {
            return null;
        }
    }
}
