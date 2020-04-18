package org.grizzlytech.contraband;

import org.everit.json.schema.Schema;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Validate a JSON document against a schema
 */
public class JSONHelper {

    public static JSONObject parseJSONObject(InputStream is) {
        return new JSONObject(new JSONTokener(is));
    }

    public static JSONObject parseJSONObject(File fileSubject) {
        JSONObject result = null;
        try {
            InputStream subject = new FileInputStream(fileSubject);
            result = parseJSONObject(subject);
        } catch (JSONException | FileNotFoundException ex) {
            Library.logError(fileSubject.getAbsolutePath() + " with " + ex.getMessage(), false);
        }
        return result;
    }

    /**
     * Validate the document against the schema
     *
     * @param jsonDocument document to validate
     * @param jsonSchema   schema to validate against
     * @return true if valid, false if not
     */
    public static boolean isValid(JSONObject jsonDocument, JSONObject jsonSchema) {

        //Library.logInfo("OK" + jsonDocument.toString());
        try {
            SchemaLoader loader = SchemaLoader.builder()
                    .schemaJson(jsonSchema)
                    .draftV7Support()
                    .build();
            Schema schema = loader.load().build();

            //schema = SchemaLoader.load(jsonSchema);
            //System.out.println(schema.getDescription());
            schema.validate(jsonDocument);
        } catch (SchemaException | ValidationException ex) {
            Library.logError(ex.getMessage(), false);
        }
        return true;
    }


    public static List<String> toListOfString(JSONArray jsonArray) {
        ArrayList<String> result = new ArrayList<>(jsonArray.length());
        Consumer<Object> appendAction = x -> result.add(String.valueOf(x));
        jsonArray.forEach(appendAction);
        return result;
    }
}
