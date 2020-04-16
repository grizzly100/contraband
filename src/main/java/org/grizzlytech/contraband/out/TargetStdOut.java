package org.grizzlytech.contraband.out;

import org.grizzlytech.contraband.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

public class TargetStdOut implements Target {

    protected JSONObject config = null;

    protected List<String> outputProperties = null;

    protected String delimiter = ";";

    public TargetStdOut() {
    }

    public TargetStdOut(JSONObject config) {
        open(config);
    }

    @Override
    public void open(JSONObject config) {
        System.out.println("base open");
        this.config = config;
        this.outputProperties = JSONHelper.toListOfString((JSONArray) config.get("outputProperties"));
    }

    @Override
    public void write(JSONObject document) {
        System.out.println(formatOutputProperties(document));
    }

    @Override
    public void close() throws Exception {
    }

    protected String formatOutputProperties(JSONObject document) {
        return outputProperties.stream()
                .map(document::optString) // look-up property name in document
                .collect(Collectors.joining(delimiter));
    }
}