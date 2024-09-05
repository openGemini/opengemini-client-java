package io.opengemini.client.spring.data.core;

import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Point;
import io.opengemini.client.api.Precision;
import io.opengemini.client.spring.data.annotation.Measurement;
import io.opengemini.client.spring.data.annotation.Tag;
import io.opengemini.client.spring.data.annotation.Time;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DefaultOpenGeminiSerializer<T> implements OpenGeminiSerializer<T> {

    private final ClassMetaData classMetaData;

    private DefaultOpenGeminiSerializer(ClassMetaData classMetaData) {
        this.classMetaData = classMetaData;
    }

    public static <T> DefaultOpenGeminiSerializer<T> of(Class<T> clazz) {
        ClassMetaData classMetaData = parseClassMetaData(clazz);
        return new DefaultOpenGeminiSerializer<>(classMetaData);
    }

    private static <T> ClassMetaData parseClassMetaData(Class<T> clazz) {
        Measurement msAnnotation = clazz.getAnnotation(Measurement.class);
        if (msAnnotation == null) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " has no @Measurement annotation");
        }

        ClassMetaData classMetaData = new ClassMetaData(msAnnotation.name());
        for (Field field : clazz.getDeclaredFields()) {
            Tag tagAnnotation = field.getAnnotation(Tag.class);
            if (tagAnnotation != null) {
                if (!String.class.equals(field.getType())) {
                    throw new IllegalArgumentException(
                            "The " + field.getName() + " field type annotated with @Tag in Class " + clazz.getName()
                                    + " should have a data type of String");
                }
                classMetaData.putTag(tagAnnotation, field);
                continue;
            }

            io.opengemini.client.spring.data.annotation.Field fieldAnnotation = field.getAnnotation(
                    io.opengemini.client.spring.data.annotation.Field.class);
            if (fieldAnnotation != null) {
                classMetaData.putField(fieldAnnotation, field);
                continue;
            }

            Time timeAnnotation = field.getAnnotation(Time.class);
            if (timeAnnotation != null) {
                classMetaData.putTime(timeAnnotation, field);
            }
        }
        return classMetaData;
    }

    @Override
    public Point serialize(T t) throws OpenGeminiException {
        Point point = new Point();
        point.setMeasurement(classMetaData.measurementName);
        point.setFields(new HashMap<>());
        point.setTags(new HashMap<>());

        try {
            for (Map.Entry<String, FieldMetaData> entry : classMetaData.fieldMap.entrySet()) {
                String columnName = entry.getKey();
                FieldMetaData fieldMetaData = entry.getValue();
                switch (fieldMetaData.fieldType) {
                    case TAG:
                        point.getTags().put(columnName, fieldMetaData.parseTagValue(t));
                        break;
                    case FIELD:
                        point.getFields().put(columnName, fieldMetaData.parseFieldValue(t));
                        break;
                    case TIME:
                        point.setTime(fieldMetaData.parseTimeValue(t));
                        point.setPrecision(fieldMetaData.timePrecision);
                        break;
                    default:
                        break;
                }
            }
            return point;
        } catch (IllegalAccessException e) {
            throw new OpenGeminiException(e);
        }
    }

    private enum FieldType {
        TAG, FIELD, TIME
    }

    private static class ClassMetaData {
        private final String measurementName;
        private final Map<String, FieldMetaData> fieldMap;

        public ClassMetaData(String measurementName) {
            this.measurementName = measurementName;
            this.fieldMap = new HashMap<>();
        }

        public void putTag(Tag tagAnnotation, Field field) {
            field.setAccessible(true);
            fieldMap.put(getTagName(tagAnnotation, field), FieldMetaData.tag(field));
        }

        public void putField(io.opengemini.client.spring.data.annotation.Field fieldAnnotation, Field field) {
            field.setAccessible(true);
            fieldMap.put(getFieldName(fieldAnnotation, field), FieldMetaData.field(field));
        }

        public void putTime(Time timeAnnotation, Field field) {
            field.setAccessible(true);
            fieldMap.put("time", FieldMetaData.time(field, timeAnnotation.precision()));
        }

        private String getTagName(Tag tagAnnotation, Field field) {
            return tagAnnotation.name().isEmpty() ? field.getName() : tagAnnotation.name();
        }

        private String getFieldName(io.opengemini.client.spring.data.annotation.Field fieldAnnotation, Field field) {
            return fieldAnnotation.name().isEmpty() ? field.getName() : fieldAnnotation.name();
        }
    }

    private static class FieldMetaData {
        private Field field;
        private FieldType fieldType;
        private Precision timePrecision;

        public static FieldMetaData tag(Field field) {
            FieldMetaData fieldMetaData = new FieldMetaData();
            fieldMetaData.field = field;
            fieldMetaData.fieldType = FieldType.TAG;
            return fieldMetaData;
        }

        public static FieldMetaData field(Field field) {
            FieldMetaData fieldMetaData = new FieldMetaData();
            fieldMetaData.field = field;
            fieldMetaData.fieldType = FieldType.FIELD;
            return fieldMetaData;
        }

        public static FieldMetaData time(Field field, Precision timePrecision) {
            FieldMetaData fieldMetaData = new FieldMetaData();
            fieldMetaData.field = field;
            fieldMetaData.fieldType = FieldType.TIME;
            fieldMetaData.timePrecision = timePrecision;
            return fieldMetaData;
        }

        public String parseTagValue(Object obj) throws IllegalAccessException {
            return Objects.toString(field.get(obj), null);
        }

        public Object parseFieldValue(Object obj) throws IllegalAccessException {
            return field.get(obj);
        }

        public long parseTimeValue(Object obj) throws IllegalAccessException {
            Object fieldValue = field.get(obj);
            if (fieldValue instanceof Long) {
                return (Long) fieldValue;
            }
            return timePrecision.getTimeUnit().convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }
    }
}
