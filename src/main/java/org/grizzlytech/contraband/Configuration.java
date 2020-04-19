package org.grizzlytech.contraband;

import com.google.common.flogger.FluentLogger;
import org.json.JSONObject;

import java.io.*;

/**
 * Load configuration file for the program
 */
public class Configuration {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    public static JSONObject getConfiguration() {

        JSONObject document = JSONHelper.parseJSONObject(getConfigInputStream());
        JSONObject schema = JSONHelper.parseJSONObject(getSchemaInputStream());

        boolean isValid = JSONHelper.isValid(document, schema);
        if (!isValid) {
            logger.atSevere().log("Config schema invalid");
            System.exit(-1);
        }
        return document;
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
