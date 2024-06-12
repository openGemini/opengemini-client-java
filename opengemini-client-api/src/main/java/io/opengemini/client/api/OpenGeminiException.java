package io.opengemini.client.api;

/**
 * Custom exception class for handling OpenGemini specific errors.
 */
public class OpenGeminiException extends RuntimeException {

    /**
     * Constructor for exceptions with a message.
     *
     * @param message the exception message
     */
    public OpenGeminiException(final String message) {
        super(message);
    }

    /**
     * Constructor for exceptions with a message and a cause.
     *
     * @param message the exception message
     * @param cause   the cause of the exception
     */
    public OpenGeminiException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor for exceptions with a cause.
     *
     * @param cause the cause of the exception
     */
    public OpenGeminiException(final Throwable cause) {
        super(cause);
    }

    /**
     * Indicates whether retrying the operation might succeed.
     *
     * @return true if the operation may succeed if repeated, false otherwise.
     */
    public boolean isRetryWorth() {
        return true;
    }

    static final String FIELD_TYPE_CONFLICT_ERROR = "field type conflict";
    static final String POINTS_BEYOND_RETENTION_POLICY_ERROR = "points beyond retention policy";
    static final String UNABLE_TO_PARSE_ERROR = "unable to parse";
    static final String HINTED_HAND_OFF_QUEUE_NOT_EMPTY_ERROR = "hinted handoff queue not empty";
    static final String CACHE_MAX_MEMORY_SIZE_EXCEEDED_ERROR = "cache-max-memory-size exceeded";
    static final String DATABASE_NOT_FOUND_ERROR = "database not found";
    static final String USER_REQUIRED_ERROR = "user is required to write to database";
    static final String USER_NOT_AUTHORIZED_ERROR = "user is not authorized to write to database";
    static final String AUTHORIZATION_FAILED_ERROR = "authorization failed";
    static final String USERNAME_REQUIRED_ERROR = "username required";

    /**
     * Exception for database not found errors.
     */
    public static final class DatabaseNotFoundException extends OpenGeminiException {
        private DatabaseNotFoundException(final String message) {
            super(message);
        }

        @Override
        public boolean isRetryWorth() {
            return false;
        }
    }

    /**
     * Exception for hinted handoff queue not empty errors.
     */
    public static final class HintedHandOffQueueNotEmptyException extends OpenGeminiException {
        private HintedHandOffQueueNotEmptyException(final String message) {
            super(message);
        }

        @Override
        public boolean isRetryWorth() {
            return false;
        }
    }

    /**
     * Exception for unable to parse errors.
     */
    public static final class UnableToParseException extends OpenGeminiException {
        private UnableToParseException(final String message) {
            super(message);
        }

        @Override
        public boolean isRetryWorth() {
            return false;
        }
    }

    /**
     * Exception for field type conflict errors.
     */
    public static final class FieldTypeConflictException extends OpenGeminiException {
        private FieldTypeConflictException(final String message) {
            super(message);
        }

        @Override
        public boolean isRetryWorth() {
            return false;
        }
    }

    /**
     * Exception for points beyond retention policy errors.
     */
    public static final class PointsBeyondRetentionPolicyException extends OpenGeminiException {
        private PointsBeyondRetentionPolicyException(final String message) {
            super(message);
        }

        @Override
        public boolean isRetryWorth() {
            return false;
        }
    }

    /**
     * Exception for cache max memory size exceeded errors.
     */
    public static final class CacheMaxMemorySizeExceededException extends OpenGeminiException {
        private CacheMaxMemorySizeExceededException(final String message) {
            super(message);
        }

        @Override
        public boolean isRetryWorth() {
            return true;
        }
    }

    /**
     * Exception for retry buffer overrun errors.
     */
    public static final class RetryBufferOverrunException extends OpenGeminiException {
        public RetryBufferOverrunException(final String message) {
            super(message);
        }

        @Override
        public boolean isRetryWorth() {
            return false;
        }
    }

    /**
     * Exception for authorization failed errors.
     */
    public static final class AuthorizationFailedException extends OpenGeminiException {
        public AuthorizationFailedException(final String message) {
            super(message);
        }

        @Override
        public boolean isRetryWorth() {
            return false;
        }
    }

    /**
     * Build an exception from an error message.
     *
     * @param errorMessage the error message
     * @return the corresponding OpenGeminiException
     */
    private static OpenGeminiException buildExceptionFromErrorMessage(final String errorMessage) {
        if (errorMessage.contains(DATABASE_NOT_FOUND_ERROR)) {
            return new DatabaseNotFoundException(errorMessage);
        }
        if (errorMessage.contains(POINTS_BEYOND_RETENTION_POLICY_ERROR)) {
            return new PointsBeyondRetentionPolicyException(errorMessage);
        }
        if (errorMessage.contains(FIELD_TYPE_CONFLICT_ERROR)) {
            return new FieldTypeConflictException(errorMessage);
        }
        if (errorMessage.contains(UNABLE_TO_PARSE_ERROR)) {
            return new UnableToParseException(errorMessage);
        }
        if (errorMessage.contains(HINTED_HAND_OFF_QUEUE_NOT_EMPTY_ERROR)) {
            return new HintedHandOffQueueNotEmptyException(errorMessage);
        }
        if (errorMessage.contains(CACHE_MAX_MEMORY_SIZE_EXCEEDED_ERROR)) {
            return new CacheMaxMemorySizeExceededException(errorMessage);
        }
        if (errorMessage.contains(USER_REQUIRED_ERROR)
                || errorMessage.contains(USER_NOT_AUTHORIZED_ERROR)
                || errorMessage.contains(AUTHORIZATION_FAILED_ERROR)
                || errorMessage.contains(USERNAME_REQUIRED_ERROR)) {
            return new AuthorizationFailedException(errorMessage);
        }
        return new OpenGeminiException(errorMessage);
    }
}

