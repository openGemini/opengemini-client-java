package io.opengemini.client.api;

public enum CompressMethod {
    GZIP("gzip"),
    SNAPPY("snappy"),
    ZSTD("zstd");

    private final String value;

    CompressMethod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
