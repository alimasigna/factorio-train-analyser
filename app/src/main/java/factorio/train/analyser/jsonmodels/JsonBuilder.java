package factorio.train.analyser.jsonmodels;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/** This class is used to create a Json Object.
 * */
public class JsonBuilder {

    /** Creates a JSON Object from a given JSON string.
     * @param jsonString The string that shall be converted to an object representation.
     * @return The generated Json Object if the parsing was successful. If the parsing didn't work it will return null.
     * */
    public Json create(String jsonString) {
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            return gson.fromJson(jsonString, Json.class);
        } catch (Exception e) {
            return null;
        }
    }
}
