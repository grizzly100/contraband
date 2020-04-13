import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.Map;

/**
 * Load configuration file for the program
 */
public class Configuration {

    public static Map<String, Object> getConfiguration() {
        boolean isValid = JSONValidate.isValid(getConfigInputStream(), getSchemaInputStream());
        if (!isValid) {
            System.exit(-1);
        }
        return new JSONParse().parse(getConfigInputStream());
    }

    protected static InputStream getConfigInputStream() {
        final String CONFIG_FILE = "/config/config.json";
        return Configuration.class.getResourceAsStream(CONFIG_FILE);
    }

    protected static InputStream getSchemaInputStream() {
        final String SCHEMA_FILE = "/schema/configSchema.json";
        return Configuration.class.getResourceAsStream(SCHEMA_FILE);
    }
}
