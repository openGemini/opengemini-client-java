package io.opengemini.client.api;

import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
public enum Precision {
    PRECISIONNANOSECOND("PrecisionNanoSecond", TimeUnit.NANOSECONDS),

    PRECISIONMICROSECOND("PrecisionMicrosecond", TimeUnit.MICROSECONDS),

    PRECISIONMILLISECOND("PrecisionMillisecond", TimeUnit.MILLISECONDS),

    PRECISIONSECOND("PrecisionSecond", TimeUnit.SECONDS),

    PRECISIONMINUTE("PrecisionMinute", TimeUnit.MINUTES),

    PRECISIONHOUR("PrecisionHour", TimeUnit.HOURS);

    private final String description;

    private final TimeUnit timeUnit;

    Precision(String description, TimeUnit timeUnit) {
        this.description = description;
        this.timeUnit = timeUnit;
    }
}
