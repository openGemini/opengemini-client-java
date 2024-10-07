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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;


class PointTest {

    @Test
    void lineProtocol_without_escaped_chars() {
        Assertions.assertEquals("test,T0=0 a=1i 1", testPoint("test", "T0", "0", "a", 1).lineProtocol());
    }

    @Test
    void lineProtocol_measurement_with_escaped_chars() {
        Assertions.assertEquals("test\\,,T0=0 a=1i 1", testPoint("test,", "T0", "0", "a", 1).lineProtocol());
        Assertions.assertEquals("test\\ ,T0=0 a=1i 1", testPoint("test ", "T0", "0", "a", 1).lineProtocol());
    }

    @Test
    void lineProtocol_tag_key_with_escaped_chars() {
        Assertions.assertEquals("test,T0\\,=0 a=1i 1", testPoint("test", "T0,", "0", "a", 1).lineProtocol());
        Assertions.assertEquals("test,T0\\==0 a=1i 1", testPoint("test", "T0=", "0", "a", 1).lineProtocol());
        Assertions.assertEquals("test,T0\\ =0 a=1i 1", testPoint("test", "T0 ", "0", "a", 1).lineProtocol());
    }

    @Test
    void lineProtocol_tag_value_with_escaped_chars() {
        Assertions.assertEquals("test,T0=0\\, a=1i 1", testPoint("test", "T0", "0,", "a", 1).lineProtocol());
        Assertions.assertEquals("test,T0=0\\= a=1i 1", testPoint("test", "T0", "0=", "a", 1).lineProtocol());
        Assertions.assertEquals("test,T0=0\\  a=1i 1", testPoint("test", "T0", "0 ", "a", 1).lineProtocol());
    }

    @Test
    void lineProtocol_field_key_with_escaped_chars() {
        Assertions.assertEquals("test,T0=0 a\\,=1i 1", testPoint("test", "T0", "0", "a,", 1).lineProtocol());
        Assertions.assertEquals("test,T0=0 a\\==1i 1", testPoint("test", "T0", "0", "a=", 1).lineProtocol());
        Assertions.assertEquals("test,T0=0 a\\ =1i 1", testPoint("test", "T0", "0", "a ", 1).lineProtocol());
    }

    @Test
    void lineProtocol_field_value_with_escaped_chars() {
        Assertions.assertEquals("test,T0=0 a=\"1\\\"\" 1", testPoint("test", "T0", "0", "a", "1\"").lineProtocol());
        Assertions.assertEquals("test,T0=0 a=\"1\\\\\" 1", testPoint("test", "T0", "0", "a", "1\\").lineProtocol());
        Assertions.assertEquals("test,T0=0 a=\"1\\\\\\\\\" 1",
                testPoint("test", "T0", "0", "a", "1\\\\").lineProtocol());
        Assertions.assertEquals("test,T0=0 a=\"1\\\\\\\\\\\\\" 1",
                testPoint("test", "T0", "0", "a", "1\\\\\\").lineProtocol());
    }

    @Test
    void lineProtocol_field_value_with_different_types() {
        Assertions.assertEquals("test,T0=0 a=1.0 1", testPoint("test", "T0", "0", "a", 1D).lineProtocol());
        Assertions.assertEquals("test,T0=0 a=1.1 1", testPoint("test", "T0", "0", "a", 1.1D).lineProtocol());
        Assertions.assertEquals("test,T0=0 a=1.0 1", testPoint("test", "T0", "0", "a", 1F).lineProtocol());
        Assertions.assertEquals("test,T0=0 a=1.1 1", testPoint("test", "T0", "0", "a", 1.1F).lineProtocol());
        Assertions.assertEquals("test,T0=0 a=1.0 1",
                testPoint("test", "T0", "0", "a", BigDecimal.valueOf(1D)).lineProtocol());
        Assertions.assertEquals("test,T0=0 a=1.1 1",
                testPoint("test", "T0", "0", "a", BigDecimal.valueOf(1.1D)).lineProtocol());
        Assertions.assertEquals("test,T0=0 a=1i 1", testPoint("test", "T0", "0", "a", 1).lineProtocol());
        Assertions.assertEquals("test,T0=0 a=1i 1", testPoint("test", "T0", "0", "a", 1L).lineProtocol());
        Assertions.assertEquals("test,T0=0 a=1i 1", testPoint("test", "T0", "0", "a", (short) 1).lineProtocol());
        Assertions.assertEquals("test,T0=0 a=1i 1", testPoint("test", "T0", "0", "a", (byte) 1).lineProtocol());
        Assertions.assertEquals("test,T0=0 a=true 1", testPoint("test", "T0", "0", "a", true).lineProtocol());
    }

    @Test
    void lineProtocol_timestamp_convert_precision() {
        Assertions.assertEquals("test,T0=0 a=1i 1",
                testPoint("test", Precision.PRECISIONNANOSECOND, "T0", "0", "a", 1).lineProtocol());
        Assertions.assertEquals("test,T0=0 a=1i 1000",
                testPoint("test", Precision.PRECISIONMICROSECOND, "T0", "0", "a", 1).lineProtocol());
        Assertions.assertEquals("test,T0=0 a=1i 1000000",
                testPoint("test", Precision.PRECISIONMILLISECOND, "T0", "0", "a", 1).lineProtocol());
        Assertions.assertEquals("test,T0=0 a=1i 1000000000",
                testPoint("test", Precision.PRECISIONSECOND, "T0", "0", "a", 1).lineProtocol());
        Assertions.assertEquals("test,T0=0 a=1i 60000000000",
                testPoint("test", Precision.PRECISIONMINUTE, "T0", "0", "a", 1).lineProtocol());
        Assertions.assertEquals("test,T0=0 a=1i 3600000000000",
                testPoint("test", Precision.PRECISIONHOUR, "T0", "0", "a", 1).lineProtocol());
    }

    @Test
    void lineProtocol_with_multi_tags_and_fields() {
        Point point = new Point();
        point.setMeasurement("test");
        point.setPrecision(Precision.PRECISIONNANOSECOND);
        point.setTime(1);

        Map<String, String> tags = new LinkedHashMap<>();
        tags.put("T0", "0");
        tags.put("T1", "1");
        point.setTags(tags);

        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("a", 1);
        fields.put("b", 2);
        point.setFields(fields);

        Assertions.assertEquals("test,T0=0,T1=1 a=1i,b=2i 1", point.lineProtocol());
    }

    private static Point testPoint(String measurement, String tagKey, String tagValue, String fieldKey,
                                   Object fieldValue) {
        return testPoint(measurement, Precision.PRECISIONNANOSECOND, tagKey, tagValue, fieldKey, fieldValue);
    }

    private static Point testPoint(String measurement, Precision precision, String tagKey, String tagValue,
                                   String fieldKey, Object fieldValue) {
        Point point = new Point();
        point.setMeasurement(measurement);
        point.setPrecision(precision);
        point.setTime(1);
        point.setTags(Collections.singletonMap(tagKey, tagValue));
        point.setFields(Collections.singletonMap(fieldKey, fieldValue));
        return point;
    }
}
