package org.grizzlytech.contraband;

import org.json.JSONObject;

import java.io.*;

/**
 * Load configuration file for the program
 */
public class Configuration {

    public static JSONObject getConfiguration() {

        JSONObject document = JSONHelper.getObject(getConfigInputStream());
        JSONObject schema = JSONHelper.getObject(getSchemaInputStream());

        boolean isValid = JSONHelper.isValid(document, schema);
        if (!isValid) {
            Library.logError("Config schema invalid", true);
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
