package org.grizzlytech.contraband.out;

import org.json.JSONObject;

public class TargetFactory {

    public static Target getTarget(JSONObject jsonConfig) {
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
