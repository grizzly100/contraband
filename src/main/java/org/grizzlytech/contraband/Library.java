package org.grizzlytech.contraband;

import com.google.common.flogger.FluentLogger;
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
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Library {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    public static void main(String args[]) throws FileNotFoundException, IOException {

        // Get configuration parameters for this walk
        JSONObject configMap = Configuration.getConfiguration();

        File root = getDir(configMap, "rootDir");
        logger.atInfo().log("Scanning root: %s", root.getAbsolutePath());

        // Cache the isSchema to be used for validation
        InputStream isSchema = getSchemaInputStream(configMap.get("validationSchema").toString());
        JSONObject jsonSchema = JSONHelper.parseJSONObject(isSchema);

        // Visit the root, printing all info.json files
        try (Target target = getTarget(configMap)) {
            visit(root, JSONHelper.getValidator(jsonSchema), target::write);
        } catch (Exception ex) {
            logger.atSevere().withCause(ex).log("Terminating during library walk");
        }
    }


    /**
     * Visit all .json files and apply the visitor
     *
     * @param root    directory to start from
     * @param visitor consumer to apply
     */
    protected static void visit(File root, Predicate<JSONObject> validator, Consumer<JSONObject> visitor) {

        try (Stream<Path> walk = Files.walk(Paths.get(root.getAbsolutePath()))) {
            walk
                    // Filter out .JSON files
                    .filter(Files::isRegularFile)
                    .filter(JSONHelper.JSON_PATH)
                    .map(Path::toFile)
                    // Parse the JSONObject, discard nulls, then validate
                    .map(JSONHelper::parseJSONObject)
                    .filter(Objects::nonNull)
                    .filter(validator)
                    // Apply the visitor
                    .forEachOrdered(visitor);
        } catch (IOException ex) {
            logger.atSevere().withCause(ex).log("Terminating during library walk");
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
