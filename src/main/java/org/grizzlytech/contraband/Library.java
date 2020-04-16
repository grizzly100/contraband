package org.grizzlytech.contraband;

import org.grizzlytech.contraband.out.Target;
import org.grizzlytech.contraband.out.TargetExcelOut;
import org.grizzlytech.contraband.out.TargetStdOut;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Library {

    public static void main(String args[]) throws FileNotFoundException, IOException {

        // Get configuration parameters for this walk
        JSONObject configMap = Configuration.getConfiguration();

        File root = getDir(configMap, "rootDir");
        logInfo("Scanning root " + root.getAbsolutePath());

        // Cache the schema to be used for validation
        InputStream schema = getSchemaInputStream(configMap.get("validationSchema").toString());
        JSONObject schemaObj = JSONHelper.getObject(schema);

        // Visit the root, printing all info.json files
        try (Target target = getTarget(configMap)) {

            // Construct a predicate that will validate the JSON files
            Predicate<File> validator = f -> JSONHelper.isValid(f, schemaObj);

            // Construct a consumer that will print the outputProperties metadata
            Consumer<File> printer = f -> print(f, schemaObj, target);

            // Visit all files from the root
            visit(root, validator, printer);
        } catch (Exception ex) {
            logError(ex, false);
        }
    }


    /**
     * Visit all .json files and apply the visitor
     *
     * @param root    directory to start from
     * @param visitor consumer to apply
     */
    protected static void visit(File root, Predicate<File> validator, Consumer<File> visitor) {
        final String JSON_PATH_EXT = ".json";
        try (Stream<Path> walk = Files.walk(Paths.get(root.getAbsolutePath()))) {
            walk
                    .filter(Files::isRegularFile)
                    //.peek(System.out::println)
                    .filter(p -> p.toString().endsWith(JSON_PATH_EXT))
                    .map(x -> x.toFile())
                    .filter(validator)
                    .forEachOrdered(visitor);
        } catch (IOException ex) {
            logError(ex, false);
        }
    }


    protected static void print(File file, JSONObject schemaObj, Target output) {
        try {
            // Validate
            InputStream is = new FileInputStream(file);
            JSONObject document = JSONHelper.getObject(is);

            if (JSONHelper.isValid(document, schemaObj)) {
                output.write(document);
            } else {
                logError(" Invalid schema: " + file.getName(), false);
            }
        } catch (FileNotFoundException ex) {
            logError("Not found : " + file.getName() + ": " + ex.getMessage(), false);
        }
    }

    protected static InputStream getSchemaInputStream(String filename) {
        return Library.class.getResourceAsStream("/schema/" + filename);
    }

    protected static File getDir(JSONObject configMap, String property) {
        String path = configMap.getString(property);
        if (path == null) {
            logError("No such property " + property, true);
        }
        File dir = new File(path);
        if (!dir.exists()) {
            logError("Cannot find " + dir.getAbsolutePath(), true);
        }
        return dir;
    }

    protected static Target getTarget(JSONObject configMap) {
        String target = configMap.optString("target", "STDOUT");
        Target result = null;
        switch (target.toUpperCase()) {
            case "EXCEL":
                result = new TargetExcelOut(configMap);
                break;

            default:
                result = new TargetStdOut(configMap);
        }
        return result;
    }

    public static void logInfo(String message) {
        System.out.println("INFO: " + message);
    }

    public static void logError(String message, boolean fatal) {
        System.err.println("ERROR: " + message);
        if (fatal) System.exit(-1);
    }

    public static void logError(Exception ex, boolean fatal) {
        System.err.println("ERROR: " + ex.getMessage());
        ex.printStackTrace(System.err);
        if (fatal) System.exit(-1);
    }
}
