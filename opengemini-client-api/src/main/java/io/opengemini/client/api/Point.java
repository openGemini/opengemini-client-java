package io.opengemini.client.api;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Point {
    String measurement;
    Precision precision = Precision.PRECISIONNANOSECOND;
    long time;
    HashMap<String, String> tags;
    HashMap<String, Object> fields;

    @SneakyThrows
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append(measurement);
        tags.forEach((key, value) -> sb.append(",").append(key).append("=").append(value));

        sb.append(" ");
        boolean firstField = true;
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            if (!firstField) {
                sb.append(",");
                firstField = false;
            }
            sb.append(entry.getKey()).append("=");
            if (entry.getValue() instanceof String) {
                sb.append("\"").append(entry.getValue()).append("\"");
            } else if ((entry.getValue() instanceof Integer) || (entry.getValue() instanceof Long)) {
                sb.append(entry.getValue()).append("i");
            } else {
                throw new Exception("ss");
            }
        }
        sb.append(" ");
        sb.append(converteTime());
        return sb.toString();
    }

    private String converteTime() {
        if (time == 0) {
            return "";
        }
        return String.valueOf(time);
    }
}
