package io.opengemini.client.api;

import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
public enum Precision {
    PRECISIONNANOSECOND("PrecisionNanoSecond", TimeUnit.NANOSECONDS, "ns"),

    PRECISIONMICROSECOND("PrecisionMicrosecond", TimeUnit.MICROSECONDS, "u"),

    PRECISIONMILLISECOND("PrecisionMillisecond", TimeUnit.MILLISECONDS, "ms"),

    PRECISIONSECOND("PrecisionSecond", TimeUnit.SECONDS, "s"),

    PRECISIONMINUTE("PrecisionMinute", TimeUnit.MINUTES, "m"),

    PRECISIONHOUR("PrecisionHour", TimeUnit.HOURS, "h");

    private final String description;

    private final TimeUnit timeUnit;

    private final String epoch;

    Precision(String description, TimeUnit timeUnit, String epoch) {
        this.description = description;
        this.timeUnit = timeUnit;
        this.epoch = epoch;
    }
}
