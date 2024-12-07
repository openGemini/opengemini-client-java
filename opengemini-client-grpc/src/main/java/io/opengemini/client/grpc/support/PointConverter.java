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

package io.opengemini.client.grpc.support;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.opengemini.client.api.Point;
import io.opengemini.client.grpc.record.ColVal;
import io.opengemini.client.grpc.record.Field;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

public final class PointConverter {
    private static final int BITS_PER_BYTE = 8;
    private static final String TIME_FIELD = "time";

    public static List<ColVal> extractColVals(List<Point> points, List<Field> schema) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("Points cannot be null or empty");
        }

        int rowCount = points.size();
        List<ColVal> colVals = new ArrayList<>(schema.size());

        // Init ColVals
        for (int i = 0; i < schema.size(); i++) {
            ColVal colVal = new ColVal();
            colVal.setLen(rowCount);
            colVal.setBitMapOffset(0);
            initializeBitmap(colVal, rowCount);
            colVals.add(colVal);
        }

        // Process each column
        Map<String, Integer> fieldIndexMap = createFieldIndexMap(schema);
        for (Field field : schema) {
            int colIndex = fieldIndexMap.get(field.getName());
            processColumn(points, colVals.get(colIndex), field);
        }
        return colVals;
    }

    /**
     * 从Points中提取Schema信息
     */
    public static List<Field> extractSchema(List<Point> points) {
        // Use a LinkedHashMap to maintain field order
        Map<String, Integer> fieldTypes = new LinkedHashMap<>();

        // Traverse all points to ensure that all possible fields and types are captured.
        for (Point point : points) {
            Map<String, Object> fields = point.getFields();
            if (fields == null) continue;

            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();
                if (value != null && !fieldTypes.containsKey(fieldName)) {
                    fieldTypes.put(fieldName, determineFieldType(value));
                }
            }

            point.getTags().forEach((tagName, tagValue) -> {
                fieldTypes.put(tagName, FieldType.TAG.getValue());
            });
        }

        // Convert to field list
        List<Field> schema = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : fieldTypes.entrySet()) {
            Field field = new Field();
            field.setName(entry.getKey());
            field.setType(entry.getValue());
            schema.add(field);
        }

        Field timeField = new Field();
        timeField.setName(TIME_FIELD);
        timeField.setType(FieldType.INT64.getValue());
        schema.add(timeField);


        return schema;
    }

    private static Map<String, Integer> createFieldIndexMap(List<Field> schema) {
        Map<String, Integer> fieldIndexMap = new HashMap<>();
        for (int i = 0; i < schema.size(); i++) {
            fieldIndexMap.put(schema.get(i).getName(), i);
        }
        return fieldIndexMap;
    }

    private static void processColumn(List<Point> points, ColVal colVal, Field field) {
        ByteBuf buffer = Unpooled.buffer();
        try {
            List<Integer> offsets = new ArrayList<>();
            int currentOffset = 0;

            for (int rowIndex = 0; rowIndex < points.size(); rowIndex++) {
                Point point = points.get(rowIndex);
                Map<String, Object> fields = new HashMap<>(point.getFields());
                fields.putAll(point.getTags());
                fields.put(TIME_FIELD, point.getTime());

                Object value = fields.get(field.getName());

                if (value == null) {
                    markAsNull(colVal, rowIndex);
                    if (field.getType() == FieldType.STRING.getValue()) {
                        offsets.add(currentOffset);
                    }
                    continue;
                }

                try {
                    currentOffset = writeValue(buffer, value, field.getType(), currentOffset, offsets);
                    markAsNonNull(colVal, rowIndex);
                } catch (Exception e) {
                    markAsNull(colVal, rowIndex);
                    if (field.getType() == FieldType.STRING.getValue()) {
                        offsets.add(currentOffset);
                    }
                }
            }

            byte[] valArray = new byte[buffer.readableBytes()];
            buffer.readBytes(valArray);
            colVal.setVal(valArray);
            colVal.setOffset(convertToLittleEndian(offsets));
        } finally {
            buffer.release();
        }
    }

    public static int[] convertToLittleEndian(List<Integer> offsets) {
        int[] intArray = offsets.stream().mapToInt(Integer::intValue).toArray();

        // 转换每个整数的字节顺序
        for (int i = 0; i < intArray.length; i++) {
            intArray[i] = Integer.reverseBytes(intArray[i]);
        }
        return intArray;
    }

    private static int writeValue(ByteBuf buffer, Object value, int type, int currentOffset, List<Integer> offsets) {
        if (type == FieldType.DOUBLE.getValue()) {
            buffer.writeDoubleLE(((Number) value).doubleValue());
        } else if (type == FieldType.FLOAT.getValue()) {
            buffer.writeFloatLE(((Number) value).floatValue());
        } else if (type == FieldType.INT64.getValue()) {
            buffer.writeLongLE(((Number) value).longValue());
        } else if (type == FieldType.INT32.getValue()) {
            buffer.writeIntLE(((Number) value).intValue());
        } else if (type == FieldType.BOOLEAN.getValue()) {
            buffer.writeBoolean((Boolean) value);
        } else if (type == FieldType.STRING.getValue() || (type == FieldType.TAG.getValue())) {
            byte[] bytes = ((String) value).getBytes();
            buffer.writeBytes(bytes);
            offsets.add(currentOffset);
            return currentOffset + bytes.length;
        }
        return currentOffset;
    }

    private static void markAsNull(ColVal colVal, int rowIndex) {
        int byteIndex = rowIndex / BITS_PER_BYTE;
        int bitOffset = rowIndex % BITS_PER_BYTE;
        colVal.getBitmap()[byteIndex] &= (byte) ~(1 << bitOffset);
        colVal.setNilCount(colVal.getNilCount() + 1);
    }

    private static void markAsNonNull(ColVal colVal, int rowIndex) {
        int byteIndex = rowIndex / BITS_PER_BYTE;
        int bitOffset = rowIndex % BITS_PER_BYTE;
        colVal.getBitmap()[byteIndex] |= (byte) (1 << bitOffset);
    }

    private static void initializeBitmap(ColVal colVal, int rowCount) {
        int bitmapSize = (rowCount + BITS_PER_BYTE - 1) / BITS_PER_BYTE;
        colVal.setBitmap(new byte[bitmapSize]);
        Arrays.fill(colVal.getBitmap(), (byte) 0);
        colVal.setNilCount(0);
    }

    private static int determineFieldType(Object value) {
        if (value instanceof Double) {
            return FieldType.DOUBLE.getValue();
        } else if (value instanceof Float) {
            return FieldType.FLOAT.getValue();
        } else if (value instanceof Long) {
            return FieldType.INT64.getValue();
        } else if (value instanceof Integer) {
            return FieldType.INT32.getValue();
        } else if (value instanceof Boolean) {
            return FieldType.BOOLEAN.getValue();
        } else if (value instanceof String) {
            return FieldType.STRING.getValue();
        } else if (value instanceof Byte) {
            return FieldType.INT32.getValue();
        }
        throw new IllegalArgumentException("Unsupported type: " + value.getClass());
    }

    @Getter
    enum FieldType {
        INT64(1),
        INT32(1),
        DOUBLE(3),
        FLOAT(3),
        STRING(4),
        BOOLEAN(5),
        TAG(6);

        private final int value;

        FieldType(int value) {
            this.value = value;
        }

    }
}
