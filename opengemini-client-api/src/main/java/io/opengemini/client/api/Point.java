package io.opengemini.client.api;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
public class Point {

    private static final char[] MEASUREMENT_ESCAPE_CHARACTERS = new char[]{',', ' '};
    private static final char[] TAG_KEY_ESCAPE_CHARACTERS = new char[]{',', '=', ' '};
    private static final char[] TAG_VALUE_ESCAPE_CHARACTERS = new char[]{',', '=', ' '};
    private static final char[] FIELD_KEY_ESCAPE_CHARACTERS = new char[]{',', '=', ' '};
    private static final char[] FIELD_VALUE_ESCAPE_CHARACTERS = new char[]{'"', '\\'};

    private static final ThreadLocal<StringBuilder> SB_CACHE = ThreadLocal.withInitial(() -> new StringBuilder(1024));

    String measurement;
    Precision precision = Precision.PRECISIONNANOSECOND;
    long time;
    Map<String, String> tags;
    Map<String, Object> fields;

    public String lineProtocol() {
        StringBuilder sb = SB_CACHE.get();
        sb.setLength(0);
        appendMeasurement(sb);
        appendTags(sb);
        int validFields = appendFields(sb);
        if (validFields <= 0) {
            return "";
        }
        appendTimestamp(sb);
        return sb.toString();
    }

    private void appendMeasurement(StringBuilder sb) {
        appendWithEscape(sb, measurement, MEASUREMENT_ESCAPE_CHARACTERS);
    }

    private void appendTags(StringBuilder sb) {
        if (tags != null && !tags.isEmpty()) {
            for (Map.Entry<String, String> tag : tags.entrySet()) {
                sb.append(',');
                appendWithEscape(sb, tag.getKey(), TAG_KEY_ESCAPE_CHARACTERS);
                sb.append('=');
                appendWithEscape(sb, tag.getValue(), TAG_VALUE_ESCAPE_CHARACTERS);
            }
        }
        sb.append(' ');
    }

    private int appendFields(StringBuilder sb) {
        int validFields = 0;
        if (fields == null || fields.isEmpty()) {
            return validFields;
        }

        boolean firstField = true;
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            Object fieldValue = entry.getValue();
            if (fieldValue == null || isNotFinite(fieldValue)) {
                continue;
            }

            if (firstField) {
                firstField = false;
            } else {
                sb.append(',');
            }

            String fieldKey = entry.getKey();
            appendWithEscape(sb, fieldKey, FIELD_KEY_ESCAPE_CHARACTERS);
            sb.append('=');
            if (fieldValue instanceof Number) {
                if (fieldValue instanceof Double || fieldValue instanceof Float || fieldValue instanceof BigDecimal) {
                    sb.append(fieldValue);
                } else {
                    sb.append(fieldValue).append('i');
                }
            } else if (fieldValue instanceof String) {
                String stringValue = (String) fieldValue;
                sb.append('"');
                appendWithEscape(sb, stringValue, FIELD_VALUE_ESCAPE_CHARACTERS);
                sb.append('"');
            } else {
                sb.append(fieldValue);
            }
            validFields++;
        }

        return validFields;
    }

    private void appendTimestamp(StringBuilder sb) {
        sb.append(' ');
        if (time != 0 && precision != null) {
            sb.append(precision.getTimeUnit().toNanos(time));
        }
    }

    @Override
    public String toString() {
        return lineProtocol();
    }

    private static void appendWithEscape(StringBuilder sb, String origin, char[] escapeChars) {
        for (char c : origin.toCharArray()) {
            if (shouldEscape(c, escapeChars)) {
                sb.append("\\");
            }
            sb.append(c);
        }
    }

    private static boolean shouldEscape(char c, char[] escapeChars) {
        for (char escapeChar : escapeChars) {
            if (escapeChar == c) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNotFinite(final Object value) {
        return (value instanceof Double && !Double.isFinite((Double) value))
                || (value instanceof Float && !Float.isFinite((Float) value));
    }
}
