package org.grizzlytech.contraband.out;

import org.json.JSONObject;

public interface Target extends AutoCloseable {

    void open(JSONObject config);

    void write(JSONObject document);
}
