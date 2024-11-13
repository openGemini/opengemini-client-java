package io.opengemini.client.grpc.support;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.opengemini.client.api.Point;
import io.opengemini.client.grpc.record.ColVal;
import io.opengemini.client.grpc.record.Field;
import lombok.Getter;

import java.util.*;

public final class PointConverter {
    private static final int BITS_PER_BYTE = 8;


    public static List<ColVal> extractColVals(List<Point> points, List<Field> schema) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("Points cannot be null or empty");
        }

        int rowCount = points.size();
        List<ColVal> colVals = new ArrayList<>();

        // 初始化每个ColVal
        for (int i = 0; i < schema.size(); i++) {
            ColVal colVal = new ColVal();
            colVal.setLen(rowCount);
            colVal.setBitMapOffset(0);
            initializeBitmap(colVal, rowCount);
            colVals.add(colVal);
        }

        // 处理每一列
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
        // 使用LinkedHashMap保持字段顺序
        Map<String, Integer> fieldTypes = new LinkedHashMap<>();

        // 遍历所有Point以确保捕获所有可能的字段和类型
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

        // 转换为Field列表
        List<Field> schema = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : fieldTypes.entrySet()) {
            Field field = new Field();
            field.setName(entry.getKey());
            field.setType(entry.getValue());
            schema.add(field);
        }


        return schema;
    }

    /**
     * 创建字段名到索引的映射
     */
    private static Map<String, Integer> createFieldIndexMap(List<Field> schema) {
        Map<String, Integer> fieldIndexMap = new HashMap<>();
        for (int i = 0; i < schema.size(); i++) {
            fieldIndexMap.put(schema.get(i).getName(), i);
        }
        return fieldIndexMap;
    }

    /**
     * 处理单个列的数据
     */
    private static void processColumn(List<Point> points, ColVal colVal, Field field) {
        ByteBuf buffer = Unpooled.buffer();
        List<Integer> offsets = new ArrayList<>();
        int currentOffset = 0;

        for (int rowIndex = 0; rowIndex < points.size(); rowIndex++) {
            Point point = points.get(rowIndex);
            Map<String, Object> fields = point.getFields();
            Object value = fields != null ? fields.get(field.getName()) : null;

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

        if (field.getType() == FieldType.STRING.getValue()) {
            colVal.setOffset(offsets.stream().mapToInt(Integer::intValue).toArray());
        }

        buffer.release();
    }


    /**
     * 写入单个值
     */
    private static int writeValue(ByteBuf buffer, Object value, int type, int currentOffset, List<Integer> offsets) {
        if (type == FieldType.DOUBLE.getValue()) {
            buffer.writeDouble(((Number) value).doubleValue());
        } else if (type == FieldType.FLOAT.getValue()) {
            buffer.writeFloat(((Number) value).floatValue());
        } else if (type == FieldType.INT64.getValue()) {
            buffer.writeLong(((Number) value).longValue());
        } else if (type == FieldType.INT32.getValue()) {
            buffer.writeInt(((Number) value).intValue());
        } else if (type == FieldType.BOOLEAN.getValue()) {
            buffer.writeBoolean((Boolean) value);
        } else if (type == FieldType.STRING.getValue() || type == FieldType.TAG.getValue()) {
            byte[] bytes = ((String) value).getBytes();
            buffer.writeBytes(bytes);
            offsets.add(currentOffset);
            return currentOffset + bytes.length;
        }
        return currentOffset;
    }

    /**
     * 标记为空值
     */
    private static void markAsNull(ColVal colVal, int rowIndex) {
        int byteIndex = rowIndex / BITS_PER_BYTE;
        int bitOffset = rowIndex % BITS_PER_BYTE;
        colVal.getBitmap()[byteIndex] &= (byte) ~(1 << bitOffset);
        colVal.setNilCount(colVal.getNilCount() + 1);
    }

    /**
     * 标记为非空值
     */
    private static void markAsNonNull(ColVal colVal, int rowIndex) {
        int byteIndex = rowIndex / BITS_PER_BYTE;
        int bitOffset = rowIndex % BITS_PER_BYTE;
        colVal.getBitmap()[byteIndex] |= (byte) (1 << bitOffset);
    }

    /**
     * 初始化bitmap
     */
    private static void initializeBitmap(ColVal colVal, int rowCount) {
        int bitmapSize = (rowCount + BITS_PER_BYTE - 1) / BITS_PER_BYTE;
        colVal.setBitmap(new byte[bitmapSize]);
        Arrays.fill(colVal.getBitmap(), (byte) 0);
        colVal.setNilCount(0);
    }

    /**
     * 确定字段类型
     */
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
            return FieldType.INT32.getValue(); // 单个byte作为int32处理
        }
        throw new IllegalArgumentException("Unsupported type: " + value.getClass());
    }

    /**
     * 字段类型枚举
     */
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
