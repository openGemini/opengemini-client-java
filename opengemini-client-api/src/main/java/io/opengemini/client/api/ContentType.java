package io.opengemini.client.api;

public enum ContentType {
    JSON("application/json"),
    MSGPACK("application/msgpack");

//    HTML("text/html"),
//    TEXT("text/plain"),
//    BINARY("application/octet-stream");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
