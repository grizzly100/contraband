package org.grizzlytech.contraband;

import com.google.common.flogger.FluentLogger;
import org.everit.json.schema.Schema;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Validate a JSON document against a schema.
 * <p>
 * Use "Everit" JSON schema parser
 * https://github.com/everit-org/json-schema
 */
public class JSONHelper {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    public static JSONObject parseJSONObject(InputStream is) {
        return new JSONObject(new JSONTokener(is));
    }

    public static JSONObject parseJSONObject(File fileSubject) {
        JSONObject result = null;
        try {
            InputStream subject = new FileInputStream(fileSubject);
            result = parseJSONObject(subject);
        } catch (JSONException | FileNotFoundException ex) {
            logger.atSevere().log("Problem parsing file: %s\n%s",
                    fileSubject.getAbsolutePath(), ex.getMessage());
        }
        return result;
    }

    public static Predicate<JSONObject> getValidator(JSONObject jsonSchema) {
        return d -> isValid(d, jsonSchema);
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
            schema.validate(jsonDocument);
        } catch (SchemaException | ValidationException ex) {
            logger.atWarning().log("Problem validating JSONObject vs schema: %s\n%s\n%s",
                    jsonSchema.optString("title"), ex.getMessage(), jsonDocument.toString());

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
