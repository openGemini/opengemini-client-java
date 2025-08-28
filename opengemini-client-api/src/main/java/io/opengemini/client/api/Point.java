/*
 * Copyright 2024 openGemini Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opengemini.client.api;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
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

    private String measurement;
    private Precision precision = Precision.PRECISIONNANOSECOND;
    private long time;
    private Map<String, String> tags;
    private Map<String, Object> fields;

    public Point() {
        this.fields = new HashMap<>();
        this.tags = new HashMap<>();
    }

    public Point measurement(String measurement) {
        this.measurement = measurement;
        return this;
    }

    public Point addTag(String key, String value) {
        if (key != null && value != null) {
            this.tags.put(key, value);
        }
        return this;
    }

    public Point addField(String key, Object value) {
        if (key != null && value != null) {
            this.fields.put(key, value);
        }
        return this;
    }

    public Point time(long time, Precision precision) {
        this.time = time;
        this.precision = precision;
        return this;
    }

    /**
     * Calculate the line protocol string for this point
     *
     * @return the line protocol string without new line, empty when there are no fields to write
     * @see <a href="https://docs.opengemini.org/zh/guide/write_data/insert_line_protocol.html">
     * * OpenGemini write data line protocol doc</a>
     */
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
                sb.append('\\');
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
