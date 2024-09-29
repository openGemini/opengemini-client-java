package io.opengemini.client.spring.data.core;

import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Point;
import io.opengemini.client.api.Precision;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.api.Series;
import io.opengemini.client.api.SeriesResult;
import io.opengemini.client.spring.data.annotation.Measurement;
import io.opengemini.client.spring.data.annotation.Tag;
import io.opengemini.client.spring.data.annotation.Time;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DefaultOpenGeminiSerializer<T> implements OpenGeminiSerializer<T> {

    private static final DateTimeFormatter RFC3339_FORMATTER = new DateTimeFormatterBuilder().appendPattern(
                    "yyyy-MM-dd'T'HH:mm:ss")
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .appendZoneOrOffsetId()
            .toFormatter();

    private final ClassMetaData<T> classMetaData;

    private DefaultOpenGeminiSerializer(ClassMetaData<T> classMetaData) {
        this.classMetaData = classMetaData;
    }

    public static <T> DefaultOpenGeminiSerializer<T> of(Class<T> clazz) {
        ClassMetaData<T> classMetaData = parseClassMetaData(clazz);
        return new DefaultOpenGeminiSerializer<>(classMetaData);
    }

    private static <T> ClassMetaData<T> parseClassMetaData(Class<T> clazz) {
        Measurement msAnnotation = clazz.getAnnotation(Measurement.class);
        if (msAnnotation == null) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " has no @Measurement annotation");
        }

        Map<String, AbstractFieldMetaData> fieldMap = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            Tag tagAnnotation = field.getAnnotation(Tag.class);
            if (tagAnnotation != null) {
                TagFieldMetaData fieldMetaData = TagFieldMetaData.of(clazz, tagAnnotation, field);
                fieldMap.put(fieldMetaData.getName(), fieldMetaData);
                continue;
            }

            io.opengemini.client.spring.data.annotation.Field fieldAnnotation = field.getAnnotation(
                    io.opengemini.client.spring.data.annotation.Field.class);
            if (fieldAnnotation != null) {
                ConcreteFieldMetaData fieldMetaData = ConcreteFieldMetaData.of(clazz, fieldAnnotation, field);
                fieldMap.put(fieldMetaData.getName(), fieldMetaData);
                continue;
            }

            Time timeAnnotation = field.getAnnotation(Time.class);
            if (timeAnnotation != null) {
                TimeFieldMetaData fieldMetaData = TimeFieldMetaData.of(clazz, timeAnnotation, field);
                fieldMap.put(fieldMetaData.getName(), fieldMetaData);
            }
        }
        return new ClassMetaData<>(clazz, fieldMap);
    }

    @Override
    public Point serialize(String measurementName, T pojo) throws OpenGeminiException {
        Point point = new Point();
        point.setMeasurement(measurementName);
        point.setFields(new HashMap<>());
        point.setTags(new HashMap<>());

        try {
            for (AbstractFieldMetaData fieldMetaData : classMetaData.getFieldMap().values()) {
                fieldMetaData.fillPoint(point, pojo);
            }
            return point;
        } catch (IllegalAccessException e) {
            throw new OpenGeminiException(e);
        }
    }

    @Override
    public List<Point> serialize(String measurementName, List<T> pojoList) throws OpenGeminiException {
        if (CollectionUtils.isEmpty(pojoList)) {
            return Collections.emptyList();
        }
        List<Point> points = new LinkedList<>();
        for (T pojo : pojoList) {
            points.add(serialize(measurementName, pojo));
        }
        return points;
    }

    @Override
    public List<T> deserialize(String measurementName, QueryResult queryResult) throws OpenGeminiException {
        validateResultNoError(queryResult);

        List<T> pojoList = new LinkedList<>();
        for (SeriesResult seriesResult : queryResult.getResults()) {
            if (seriesResult == null || CollectionUtils.isEmpty(seriesResult.getSeries())) {
                continue;
            }
            for (Series series : seriesResult.getSeries()) {
                if (!StringUtils.equals(series.getName(), measurementName)) {
                    continue;
                }
                pojoList.addAll(parseSeriesAsPojoList(series));
            }
        }

        return pojoList;
    }

    private Collection<? extends T> parseSeriesAsPojoList(Series series) throws OpenGeminiException {
        List<T> pojoList = new LinkedList<>();
        int columnSize = series.getColumns().size();
        try {
            T pojo = null;
            for (List<Object> row : series.getValues()) {
                for (int i = 0; i < columnSize; i++) {
                    AbstractFieldMetaData correspondingField = classMetaData.getFieldMap()
                            .get(series.getColumns().get(i));
                    if (correspondingField != null) {
                        if (pojo == null) {
                            pojo = classMetaData.newInstance();
                        }
                        setFieldValue(pojo, correspondingField, row.get(i));
                    }
                }
                if (series.getTags() != null && !series.getTags().isEmpty()) {
                    for (Map.Entry<String, String> entry : series.getTags().entrySet()) {
                        AbstractFieldMetaData correspondingField = classMetaData.getFieldMap().get(entry.getKey());
                        if (correspondingField != null) {
                            setFieldValue(pojo, correspondingField, entry.getValue());
                        }
                    }
                }
                if (pojo != null) {
                    pojoList.add(pojo);
                    pojo = null;
                }
            }
        } catch (Exception e) {
            throw new OpenGeminiException(e);
        }
        return pojoList;
    }

    private void setFieldValue(T pojo, AbstractFieldMetaData fieldMetaData, Object fieldValue)
            throws OpenGeminiException {
        if (fieldValue == null) {
            return;
        }
        try {
            fieldMetaData.setFieldValue(pojo, fieldValue);
        } catch (IllegalAccessException e) {
            throw new OpenGeminiException(e);
        }
    }

    private void validateResultNoError(QueryResult queryResult) throws OpenGeminiException {
        if (queryResult.getError() != null) {
            throw new OpenGeminiException("InfluxDB returned an error: " + queryResult.getError());
        }

        for (SeriesResult seriesResult : queryResult.getResults()) {
            if (seriesResult != null && seriesResult.getError() != null) {
                throw new OpenGeminiException("InfluxDB returned an error with Series: " + seriesResult.getError());
            }
        }
    }

    @Getter
    private static class ClassMetaData<T> {
        private final Class<T> clazz;
        private final Map<String, AbstractFieldMetaData> fieldMap;

        public ClassMetaData(Class<T> clazz, Map<String, AbstractFieldMetaData> fieldMap) {
            this.clazz = clazz;
            this.fieldMap = fieldMap;
        }

        public T newInstance() throws NoSuchMethodException, InvocationTargetException, InstantiationException,
                IllegalAccessException {
            return clazz.getDeclaredConstructor().newInstance();
        }
    }

    @Getter
    private abstract static class AbstractFieldMetaData {

        protected final String name;
        protected final Field field;

        public AbstractFieldMetaData(String name, Field field) {
            this.name = name;
            this.field = field;
        }

        protected static void validateFieldType(Class<?> clazz,
                                                Field field,
                                                Set<Class<?>> validClasses,
                                                Class<?> annotationClazz) {
            if (!validClasses.contains(field.getType())) {
                throw new IllegalArgumentException(
                        "The " + field.getName() + " field type annotated with @" + annotationClazz.getSimpleName()
                                + " in Class " + clazz.getName() + " should have a data type of " + validClasses);
            }
        }

        public abstract void fillPoint(Point point, Object pojo) throws IllegalAccessException;

        public void setFieldValue(Object pojo, Object fieldValue) throws IllegalAccessException {
            Object convertedValue = convertFieldValue(fieldValue);
            if (convertedValue != null) {
                field.set(pojo, convertedValue);
            }
        }

        public abstract Object convertFieldValue(Object fieldValue);

    }

    private static class TagFieldMetaData extends AbstractFieldMetaData {

        private static final Set<Class<?>> VALID_CLASSES = new HashSet<>();

        static {
            VALID_CLASSES.add(String.class);
        }

        public TagFieldMetaData(String name, Field field) {
            super(name, field);
        }

        public static TagFieldMetaData of(Class<?> clazz, Tag tagAnnotation, Field field) {
            validateFieldType(clazz, field, VALID_CLASSES, Tag.class);

            String tagName = tagAnnotation.name().isEmpty() ? field.getName() : tagAnnotation.name();
            field.setAccessible(true);
            return new TagFieldMetaData(tagName, field);
        }

        @Override
        public void fillPoint(Point point, Object pojo) throws IllegalAccessException {
            point.getTags().put(name, Objects.toString(field.get(pojo), null));
        }

        @Override
        public Object convertFieldValue(Object fieldValue) {
            return String.valueOf(fieldValue);
        }
    }

    private static class ConcreteFieldMetaData extends AbstractFieldMetaData {

        private static final Set<Class<?>> VALID_CLASSES = new HashSet<>();

        static {
            VALID_CLASSES.add(String.class);
            VALID_CLASSES.add(Double.class);
            VALID_CLASSES.add(double.class);
            VALID_CLASSES.add(Float.class);
            VALID_CLASSES.add(float.class);
            VALID_CLASSES.add(Long.class);
            VALID_CLASSES.add(long.class);
            VALID_CLASSES.add(Integer.class);
            VALID_CLASSES.add(int.class);
            VALID_CLASSES.add(BigDecimal.class);
            VALID_CLASSES.add(BigInteger.class);
            VALID_CLASSES.add(Boolean.class);
            VALID_CLASSES.add(boolean.class);
        }

        public ConcreteFieldMetaData(String name, Field field) {
            super(name, field);
        }

        public static ConcreteFieldMetaData of(Class<?> clazz,
                                               io.opengemini.client.spring.data.annotation.Field fieldAnnotation,
                                               Field field) {
            validateFieldType(clazz, field, VALID_CLASSES, io.opengemini.client.spring.data.annotation.Field.class);

            String fieldName = fieldAnnotation.name().isEmpty() ? field.getName() : fieldAnnotation.name();
            field.setAccessible(true);
            return new ConcreteFieldMetaData(fieldName, field);
        }

        @Override
        public void fillPoint(Point point, Object pojo) throws IllegalAccessException {
            point.getFields().put(name, field.get(pojo));
        }

        @Override
        public Object convertFieldValue(Object fieldValue) {
            Class<?> fieldType = field.getType();
            if (String.class.isAssignableFrom(fieldType)) {
                return String.valueOf(fieldValue);
            }
            if (Double.class.isAssignableFrom(fieldType) || double.class.isAssignableFrom(fieldType)) {
                return fieldValue;
            }
            if (Float.class.isAssignableFrom(fieldType) || float.class.isAssignableFrom(fieldType)) {
                return ((Double) fieldValue).floatValue();
            }
            if (BigDecimal.class.isAssignableFrom(fieldType)) {
                return BigDecimal.valueOf((Double) fieldValue);
            }
            if (Long.class.isAssignableFrom(fieldType) || long.class.isAssignableFrom(fieldType)) {
                return ((Double) fieldValue).longValue();
            }
            if (Integer.class.isAssignableFrom(fieldType) || int.class.isAssignableFrom(fieldType)) {
                return ((Double) fieldValue).intValue();
            }
            if (BigInteger.class.isAssignableFrom(fieldType)) {
                return BigInteger.valueOf(((Double) fieldValue).longValue());
            }
            if (Boolean.class.isAssignableFrom(fieldType) || boolean.class.isAssignableFrom(fieldType)) {
                return Boolean.valueOf(String.valueOf(fieldValue));
            }
            return null;
        }
    }

    private static class TimeFieldMetaData extends AbstractFieldMetaData {

        private static final Set<Class<?>> VALID_CLASSES = new HashSet<>();

        static {
            VALID_CLASSES.add(Long.class);
            VALID_CLASSES.add(long.class);
        }

        private final Precision timePrecision;

        public TimeFieldMetaData(String name, Field field, Precision timePrecision) {
            super(name, field);
            this.timePrecision = timePrecision;
        }

        public static TimeFieldMetaData of(Class<?> clazz, Time timeAnnotation, Field field) {
            validateFieldType(clazz, field, VALID_CLASSES, Time.class);

            field.setAccessible(true);
            return new TimeFieldMetaData("time", field, timeAnnotation.precision());
        }

        @Override
        public void fillPoint(Point point, Object pojo) throws IllegalAccessException {
            point.setTime(parseTimeValueFromPojo(pojo));
            point.setPrecision(timePrecision);
        }

        @Override
        public Object convertFieldValue(Object fieldValue) {
            Long nanos = parseNanoTimeValueFromResult(fieldValue);
            if (nanos != null) {
                return timePrecision.getTimeUnit().convert(nanos, TimeUnit.NANOSECONDS);
            }
            return null;
        }

        private long parseTimeValueFromPojo(Object obj) throws IllegalAccessException {
            Object fieldValue = field.get(obj);
            if (fieldValue == null) {
                return timePrecision.getTimeUnit().convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
            } else {
                return (Long) fieldValue;
            }
        }

        private Long parseNanoTimeValueFromResult(Object fieldValue) {
            if (fieldValue instanceof String) {
                Instant instant = Instant.from(RFC3339_FORMATTER.parse(String.valueOf(fieldValue)));
                return TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano();
            }
            if (fieldValue instanceof Long) {
                return (Long) fieldValue;
            }
            return null;
        }
    }
}
