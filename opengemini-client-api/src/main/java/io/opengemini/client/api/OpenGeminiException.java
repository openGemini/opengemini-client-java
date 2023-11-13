package io.opengemini.client.api;

public class OpenGeminiException extends Exception {
    private static final int DEFAULT_STATUS_CODE = 500;

    private final int statusCode;

    public OpenGeminiException(Throwable t) {
        super(t);
        statusCode = DEFAULT_STATUS_CODE;
    }

    public OpenGeminiException(String message) {
        this(message, DEFAULT_STATUS_CODE);
    }

    public OpenGeminiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
