package io.opengemini.client.okhttp.impl;

import java.text.SimpleDateFormat;
import java.util.EnumSet;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * @author Janle
 * @date 2024/5/10 11:52
 */
public enum TimeUtil {
    INSTANCE;

    private static final ThreadLocal<SimpleDateFormat> FORMATTER_MILLIS = new ThreadLocal<SimpleDateFormat>() {
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat dateDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            dateDF.setTimeZone(TimeZone.getTimeZone("UTC"));
            return dateDF;
        }
    };
    private static final ThreadLocal<SimpleDateFormat> FORMATTER_SECONDS = new ThreadLocal<SimpleDateFormat>() {
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat dateDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            dateDF.setTimeZone(TimeZone.getTimeZone("UTC"));
            return dateDF;
        }
    };
    private static final EnumSet<TimeUnit> ALLOWED_TIMEUNITS = EnumSet.of(TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS, TimeUnit.MILLISECONDS, TimeUnit.MICROSECONDS, TimeUnit.NANOSECONDS);
    public static final int TIME_IN_SECOND_LENGTH = 20;

    private TimeUtil() {
    }

    public static String toTimePrecision(TimeUnit t) {
        switch (t) {
            case HOURS:
                return "h";
            case MINUTES:
                return "m";
            case SECONDS:
                return "s";
            case MILLISECONDS:
                return "ms";
            case MICROSECONDS:
                return "u";
            case NANOSECONDS:
                return "n";
            default:
                throw new IllegalArgumentException("time precision must be one of:" + ALLOWED_TIMEUNITS);
        }
    }

    public static String toInfluxDBTimeFormat(long time) {
        return ((SimpleDateFormat)FORMATTER_MILLIS.get()).format(time);
    }

    public static long fromInfluxDBTimeFormat(String time) {
        try {
            return time.length() == 20 ? ((SimpleDateFormat)FORMATTER_SECONDS.get()).parse(time).getTime() : ((SimpleDateFormat)FORMATTER_MILLIS.get()).parse(time).getTime();
        } catch (Exception var2) {
            throw new RuntimeException("unexpected date format", var2);
        }
    }
}
