package io.opengemini.client.api;

public interface OpenGeminiClient {
    void ping(int idx) throws Exception;

    QueryResult query(Query query) throws Exception;
}
