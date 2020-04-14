import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Library {

    public static void main(String args[]) throws FileNotFoundException, IOException {

        // root path
        String root = "M:\\Music Lossless\\Gothic\\The Sisters Of Mercy (Bootlegs)\\1983\\The Sisters Of Mercy - 1983-06-29 - Brixton Ace, London";

        // Get configuration parameters for this walk
        JSONObject configMap = Configuration.getConfiguration();

        // Cache the schema to be used for validation
        InputStream schema = getSchemaInputStream(configMap.get("validationSchema").toString());
        JSONObject schemaObj = JSONHelper.getObject(schema);

        // Construct a visitor that will print outputProperties metadata
        JSONArray props = (JSONArray) configMap.get("outputProperties");
        Predicate<File> validator = f -> JSONHelper.isValid(f,schemaObj);
        Function<JSONObject, String> formatter = x -> format(x, props, ";");
        Consumer<File> printer = p -> print(p, schemaObj,formatter);


        // Visit the root, printing all info.json files
        visit(root, validator, printer);
    }

    /**
     * Format the document to a single string
     *
     * @param document   document to format
     * @param properties properties to output
     * @param delimiter  delimiter between properties
     * @return string representation
     */
    protected static String format(JSONObject document, JSONArray properties, String delimiter) {
        return properties.toList().stream()
                .map(String::valueOf)
                .map(document::get) // look-up property name in document
                .map(String::valueOf) // handle nulls
                .collect(Collectors.joining(delimiter));
    }

    /**
     * Visit all .json files and apply the visitor
     *
     * @param root    directory to start from
     * @param visitor consumer to apply
     */
    protected static void visit(String root, Predicate<File> validator, Consumer<File> visitor) {
        final String JSON_PATH_EXT = ".json";
        try (Stream<Path> walk = Files.walk(Paths.get(root))) {
            walk
                    .filter(Files::isRegularFile)
                    //.peek(System.out::println)
                    .filter(p -> p.toString().endsWith(JSON_PATH_EXT))
                    .map(x -> x.toFile())
                    .filter(validator)
                    .forEachOrdered(visitor);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    protected static void print(File file, JSONObject schemaObj, Function<JSONObject, String> formatter) {
        try {
            // Validate
            InputStream is = new FileInputStream(file);
            JSONObject document = JSONHelper.getObject(is);

            if (JSONHelper.isValid(document, schemaObj)) {
                String values = formatter.apply(document);
                System.out.println(values);
            } else {
                System.err.println("FAIL: " + file.getName());
            }
        } catch (FileNotFoundException ex) {
            System.err.println("FAIL: " + file.getName() + ": " + ex.getMessage());
        }
    }

    protected static InputStream getSchemaInputStream(String filename) {
        return Library.class.getResourceAsStream("/schema/" + filename);
    }

}
