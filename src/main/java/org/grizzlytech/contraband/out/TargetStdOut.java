package org.grizzlytech.contraband.out;

import com.google.common.flogger.FluentLogger;
import org.grizzlytech.contraband.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

public class TargetStdOut implements Target {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    protected JSONObject jsonConfig = null;

    protected List<String> outputProperties = null;

    protected String delimiter = ";";

    public TargetStdOut() {
    }

    public TargetStdOut(JSONObject jsonConfig) {
        open(jsonConfig);
    }

    @Override
    public void open(JSONObject config) {
        logger.atInfo().log("Reading outputProperties");
        this.jsonConfig = config;
        this.outputProperties = JSONHelper.toListOfString((JSONArray) config.get("outputProperties"));
    }

    @Override
    public void write(JSONObject document) {
        System.out.println(formatOutputProperties(document));
    }

    @Override
    public void close() {
    }

    protected String formatOutputProperties(JSONObject document) {
        return outputProperties.stream()
                .map(document::optString) // look-up property name in document
                .collect(Collectors.joining(delimiter));
    }
}