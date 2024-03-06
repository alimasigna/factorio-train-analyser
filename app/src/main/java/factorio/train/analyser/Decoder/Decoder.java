package factorio.train.analyser.decoder;

import java.util.Base64;
import java.util.zip.Inflater;

/**
 * This class is used to generate a JSON String from a Factorio Blueprint.
 */
public class Decoder {

    /**
     * This will initialize the private Json field with a given Json String.
     * 
     * @param input The encoded factorio blueprint String that will be decoded into
     *              a readable JSON String.
     * @return The generated Json String if the decoding was successful. If not, it will return null.
     * */
    public String decode(String input) {

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(input.substring(1));

            Inflater decompresser = new Inflater();
            decompresser.setInput(decodedBytes, 0, decodedBytes.length);
            byte[] result = new byte[100000]; //TODO this number might be to small for big blueprints
            int resultLength = decompresser.inflate(result);
            decompresser.end();

            String outputString = new String(result, 0, resultLength, "UTF-8");

            return outputString;
        } catch (Exception e) {
            return null;
        }
    }
}