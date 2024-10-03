package io.opengemini.client.spring.data.core;

/**
 * Interface that specified a basic set of OpenGemini operations, implemented by {@link OpenGeminiTemplate}.
 * A useful option for extensibility and testability (as it can be easily mocked or stubbed).
 */
public interface OpenGeminiOperations {
    <T> MeasurementOperations<T> opsForMeasurement(Class<T> clazz);

    <T> MeasurementOperations<T> opsForMeasurement(String databaseName,
                                                   String retentionPolicyName,
                                                   String measurementName,
                                                   Class<T> clazz);
}
