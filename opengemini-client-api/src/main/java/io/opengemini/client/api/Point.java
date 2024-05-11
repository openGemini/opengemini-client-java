package io.opengemini.client.api;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class Point {
    String measurement;
    private TimeUnit precision = TimeUnit.NANOSECONDS;
    Number time;
    HashMap<String, String> tags;
    HashMap<String, Object> fields;
    private static final int MAX_FRACTION_DIGITS = 340;
    private static final ThreadLocal<NumberFormat> NUMBER_FORMATTER =
            ThreadLocal.withInitial(() -> {
                NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
                numberFormat.setMaximumFractionDigits(MAX_FRACTION_DIGITS);
                numberFormat.setGroupingUsed(false);
                numberFormat.setMinimumFractionDigits(1);
                return numberFormat;
            });

    private static final int DEFAULT_STRING_BUILDER_SIZE = 1024;
    private static final ThreadLocal<StringBuilder> CACHED_STRINGBUILDERS =
            ThreadLocal.withInitial(() -> new StringBuilder(DEFAULT_STRING_BUILDER_SIZE));


    public String lineProtocol(final TimeUnit precision) {

        // setLength(0) is used for reusing cached StringBuilder instance per thread
        // it reduces GC activity and performs better then new StringBuilder()
        StringBuilder sb = CACHED_STRINGBUILDERS.get();
        sb.setLength(0);

        escapeKey(sb, measurement);
        concatenatedTags(sb);
        int writtenFields = concatenatedFields(sb);
        if (writtenFields == 0) {
            return "";
        }
        formatedTime(sb, precision);

        return sb.toString();
    }


    private void formatedTime(final StringBuilder sb, final TimeUnit precision) {
        if (this.time == null) {
            return;
        }
        TimeUnit converterPrecision = precision;

        if (converterPrecision == null) {
            converterPrecision = TimeUnit.NANOSECONDS;
        }
        if (this.time instanceof BigInteger) {
            BigInteger time = (BigInteger) this.time;
            long conversionFactor = converterPrecision.convert(1, this.precision);
            if (conversionFactor >= 1) {
                time = time.multiply(BigInteger.valueOf(conversionFactor));
            } else {
                conversionFactor = this.precision.convert(1, converterPrecision);
                time = time.divide(BigInteger.valueOf(conversionFactor));
            }
            sb.append(" ").append(time);
        } else if (this.time instanceof BigDecimal) {
            BigDecimal time = (BigDecimal) this.time;
            long conversionFactor = converterPrecision.convert(1, this.precision);
            if (conversionFactor >= 1) {
                time = time.multiply(BigDecimal.valueOf(conversionFactor));
            } else {
                conversionFactor = this.precision.convert(1, converterPrecision);
                time = time.divide(BigDecimal.valueOf(conversionFactor), RoundingMode.HALF_UP);
            }
            sb.append(" ").append(time.toBigInteger());
        } else {
            sb.append(" ").append(converterPrecision.convert(this.time.longValue(), this.precision));
        }
    }


    private void concatenatedTags(final StringBuilder sb) {
        for (Map.Entry<String, String> tag : this.tags.entrySet()) {
            sb.append(',');
            escapeKey(sb, tag.getKey());
            sb.append('=');
            escapeKey(sb, tag.getValue());
        }
        sb.append(' ');
    }

    static void escapeKey(final StringBuilder sb, final String key) {
        for (int i = 0; i < key.length(); i++) {
            switch (key.charAt(i)) {
                case ' ':
                case ',':
                case '=':
                    sb.append('\\');
                default:
                    sb.append(key.charAt(i));
            }
        }
    }

    static void escapeField(final StringBuilder sb, final String field) {
        for (int i = 0; i < field.length(); i++) {
            switch (field.charAt(i)) {
                case '\\':
                case '\"':
                    sb.append('\\');
                default:
                    sb.append(field.charAt(i));
            }
        }
    }


    private int concatenatedFields(final StringBuilder sb) {
        int fieldCount = 0;
        for (Map.Entry<String, Object> field : this.fields.entrySet()) {
            Object value = field.getValue();
            if (value == null || isNotFinite(value)) {
                continue;
            }
            escapeKey(sb, field.getKey());
            sb.append('=');
            if (value instanceof Number) {
                if (value instanceof Double || value instanceof Float || value instanceof BigDecimal) {
                    sb.append(NUMBER_FORMATTER.get().format(value));
                } else {
                    sb.append(value).append('i');
                }
            } else if (value instanceof String) {
                String stringValue = (String) value;
                sb.append('"');
                escapeField(sb, stringValue);
                sb.append('"');
            } else {
                sb.append(value);
            }

            sb.append(',');

            fieldCount++;
        }

        // efficiently chop off the trailing comma
        int lengthMinusOne = sb.length() - 1;
        if (sb.charAt(lengthMinusOne) == ',') {
            sb.setLength(lengthMinusOne);
        }

        return fieldCount;
    }

    private static boolean isNotFinite(final Object value) {
        return value instanceof Double && !Double.isFinite((Double) value)
                || value instanceof Float && !Float.isFinite((Float) value);
    }
}
