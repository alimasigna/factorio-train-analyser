package factorio.train.analyser.Decoder;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class Decoder {

    public String decode(String input) throws UnsupportedEncodingException, DataFormatException {

        if (input != null) {
            byte[] decodedBytes = Base64.getDecoder().decode(input.substring(1));

            Inflater decompresser = new Inflater();
            decompresser.setInput(decodedBytes, 0, decodedBytes.length);
            byte[] result = new byte[10000];
            int resultLength = decompresser.inflate(result);
            decompresser.end();

            String outputString = new String(result, 0, resultLength, "UTF-8");

            return outputString;

        } else {
            return null;
        }
    }
}