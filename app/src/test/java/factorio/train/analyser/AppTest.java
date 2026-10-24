package factorio.train.analyser;

import factorio.train.analyser.decoder.Decoder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AppTest {

    @Test
    public void decodesA20BlueprintString() {
        String json = new Decoder().decode(Blueprints.load("reducteur"));
        assertTrue(json, json.contains("\"curved-rail-a\""));
        assertTrue(json, json.contains("\"half-diagonal-rail\""));
    }

    @Test
    public void decodesABlueprintTooBigForAFixedBuffer() {
        // The 1.1 decoder inflated into a 100 kB array, which silently cut off
        // anything larger. Real 2.0 blueprints run well past that.
        String json = new Decoder().decode(Blueprints.load("voie-2x2"));
        assertTrue("decoded " + json.length() + " bytes", json.length() > 100_000);
        assertTrue(json.trim().endsWith("}"));
    }

    @Test
    public void rejectsSomethingThatIsNotABlueprint() {
        assertNull(new Decoder().decode("not a blueprint"));
        assertNull(new Decoder().decode(""));
        assertNull(new Decoder().decode(null));
    }

    @Test
    public void readsTheRailsAndSignalsOutOfABlueprint() {
        Matrix matrix = new Matrix(Blueprints.load("t-signal"));
        assertEquals(5, matrix.getEntities().size());
        assertEquals(1, matrix.getAllBlueprints().size());
    }
}
