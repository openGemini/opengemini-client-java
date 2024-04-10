package io.opengemini.client.api;

public enum Precision {
    PRECISIONNANOSECOND("PrecisionNanoSecond"),

    PRECISIONMICROSECOND("PrecisionMicrosecond"),

    PRECISIONMILLISECOND("PrecisionMillisecond"),

    PRECISIONSECOND("PrecisionSecond"),

    PRECISIONMINUTE("PrecisionMinute"),

    PRECISIONHOUR("PrecisionHour");

    private final String description;

    Precision(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
