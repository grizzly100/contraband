package org.grizzlytech.contraband;

import org.everit.json.schema.Schema;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
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

    public static boolean isValid(File fileSubject, JSONObject jsonSchema) {
        boolean valid = false;
        try {
            InputStream subject = new FileInputStream(fileSubject);
            if (isValid(getObject(subject), jsonSchema)) {
                valid = true;
            } else {
                System.err.println("Failed validation: " + fileSubject.getName());
            }
        } catch (FileNotFoundException ex) {
            System.err.println("Failed to load: " + fileSubject.getName() + " : " + ex.getMessage());
        }
        return valid;
    }

    /**
     * Validate the document against the schema
     *
     * @param jsonDocument document to validate
     * @param jsonSchema   schema to validate against
     * @return true if valid, false if not
     */
    public static boolean isValid(JSONObject jsonDocument, JSONObject jsonSchema) {

        try {
            SchemaLoader loader = SchemaLoader.builder()
                    .schemaJson(jsonSchema)
                    .draftV7Support()
                    .build();
            Schema schema = loader.load().build();

            //schema = SchemaLoader.load(jsonSchema);
            //System.out.println(schema.getDescription());
            schema.validate(jsonDocument);
        } catch (SchemaException ex) {
            System.err.println(ex.fillInStackTrace());
        } catch (ValidationException ex) {
            System.err.println(ex.getAllMessages());
        }
        return true;
    }

    /**
     * Remember an InputStream can generally only be read once, so do not call twice with same stream
     *
     * @param is InputStream to read
     * @return the JSONObject representation
     */
    public static JSONObject getObject(InputStream is) {
        return new JSONObject(new JSONTokener(is));
    }

    public static List<String> toListOfString(JSONArray jsonArray) {
        ArrayList<String> result = new ArrayList<>(jsonArray.length());
        Consumer<Object> appendAction = x -> result.add(String.valueOf(x));
        jsonArray.forEach(appendAction);
        return result;
    }
}
