import jakarta.json.Json;
import jakarta.json.stream.JsonParser;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONParse {

    public Map<String, Object> parse(InputStream is) {
        final JsonParser parser = Json.createParser(is);
        Map<String, Object> result = null;
        if (parser.hasNext() && parser.next() == JsonParser.Event.START_OBJECT) {
            result = onStartObject(parser);
        } else {
            System.out.print("Bogus");
        }
        parser.close();
        return result;
    }

    public Map<String, Object> onStartObject(JsonParser parser) {
        Map<String, Object> map = new HashMap<>();
        String result = "";
        String key = null;
        String value = null;
        List<String> list = null;
        while (parser.hasNext()) {
            final JsonParser.Event event = parser.next();
            switch (event) {
                case KEY_NAME:
                    key = parser.getString();
                    break;
                case VALUE_STRING:
                case VALUE_NUMBER:
                case VALUE_TRUE:
                case VALUE_FALSE:
                    value = parser.getString();
                    if (list == null) {
                        map.put(key, value);
                        //System.out.println(key + "=" + value);
                    } else {
                        list.add(value);
                    }
                    break;
                case START_ARRAY:
                    list = new ArrayList<>();
                    break;
                case END_ARRAY:
                    map.put(key, list);
                    //System.out.println(key + "=" + flatten(list));
                    list = null;
                    break;

                case START_OBJECT:
                    map.put(key, onStartObject(parser));
                    break;

                case END_OBJECT:
                    return map; // return to outer loop

                default:
                    System.out.println("Hmm: " + event.name());
            }
        }
        return null;
    }

    public String flatten(List<String> list)
    {
        return String.join("/", list);
    }
}
