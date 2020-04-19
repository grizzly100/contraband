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

/**
 * Scan a library of recordings seeking .json files that contain metadata conforming to
 * the required Schema. Output metadata to a configurable Target (STDOUT, EXCEL)
 */
public class Library {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    public static void main(String[] args) {

        // Get configuration parameters for this library walk
        JSONObject jsonConfig = Configuration.getConfiguration();

        File root = getDir(jsonConfig, "rootDir");
        logger.atInfo().log("Scanning root: %s", root.getAbsolutePath());

        // Cache the isSchema to be used for validation
        InputStream isSchema = getSchemaInputStream(jsonConfig.get("validationSchema").toString());
        JSONObject jsonSchema = JSONHelper.parseJSONObject(isSchema);

        // Visit the root, printing all info.json files
        try (Target target = getTarget(jsonConfig)) {
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
        final String JSON_PATH_EXT = ".json";
        final Predicate<Path> isJSON = p -> p.toString().endsWith(JSON_PATH_EXT);

        try (Stream<Path> walk = Files.walk(Paths.get(root.getAbsolutePath()))) {
            walk
                    // Filter out .JSON files
                    .filter(Files::isRegularFile)
                    .filter(isJSON)
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

    protected static File getDir(JSONObject jsonConfig, String property) {
        String path = jsonConfig.getString(property);
        if (path == null) {
            logger.atSevere().log("No such property: %s", property);
        }
        assert path != null;
        File dir = new File(path);
        if (!dir.exists()) {
            logger.atSevere().log("Cannot find directory: %s", dir.getAbsolutePath());
        }
        return dir;
    }

    protected static Target getTarget(JSONObject jsonConfig) {
        String target = jsonConfig.optString("target", "STDOUT");
        Target result;
        if ("EXCEL".equals(target.toUpperCase())) {
            result = new TargetExcelOut(jsonConfig);
        } else {
            result = new TargetStdOut(jsonConfig);
        }
        return result;
    }
}
