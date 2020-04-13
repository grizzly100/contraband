import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
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
        Map<String, Object> configMap = Configuration.getConfiguration();

        // Cache the schema to be used for validation
        InputStream schema = getSchemaInputStream(configMap.get("validationSchema").toString());
        JSONValidate v = new JSONValidate();
        JSONObject schemaObj = v.getObject(schema);

        // Construct a visitor that will print outputProperties metadata
        List<String> props = (List<String>) configMap.get("outputProperties");
        Predicate<File> validator = f -> JSONValidate.isValid(f,schemaObj);
        Function<Map<String, Object>, String> formatter = x -> format(x, props, ";");
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
    protected static String format(Map<String, Object> document, List<String> properties, String delimiter) {
        return properties.stream()
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


    protected static void print(File file, JSONObject schemaObj, Function<Map<String, Object>, String> formatter) {
        try {
            // Validate
            InputStream is = new FileInputStream(file);
            JSONValidate v = new JSONValidate();
            if (JSONValidate.isValid(v.getObject(is), schemaObj)) {
                InputStream is2 = new FileInputStream(file);
                Map<String, Object> m = new JSONParse().parse(is2);

                String values = formatter.apply(m);
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
