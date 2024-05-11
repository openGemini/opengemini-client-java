package io.opengemini.client.okhttp;

import io.opengemini.client.okhttp.impl.OpenGeminiOkhttpClient;
import io.opengemini.client.okhttp.impl.OpenGeminiOkhttpProxyClient;
import io.opengemini.client.okhttp.impl.Preconditions;
import okhttp3.OkHttpClient;

import java.util.Objects;

/**
 * 创建OpenGeminiClient工场。
 *
 * @author Janle
 * @date 2024/5/10 9:57
 */
public enum OpenGeminiOkhttpClientFactory {
    INSTANCE;


    public static OpenGeminiClient connect(final Configuration configuration) {
        Preconditions.checkNonEmptyString(configuration.getAddresses().get(0).getHost(), "url");
        return new OpenGeminiOkhttpProxyClient(configuration);
    }

    /**
     * Create a connection to a OpenGeminiClient.
     *
     * @param url the url to connect to.
     * @return a InfluxDB adapter suitable to access a OpenGeminiClient.
     */
    public static OpenGeminiClient connect(final String url) {
        Preconditions.checkNonEmptyString(url, "url");
        return new OpenGeminiOkhttpClient(url, null, null, new OkHttpClient.Builder());
    }

    /**
     * Create a connection to a InfluxDB.
     *
     * @param url      the url to connect to.
     * @param username the username which is used to authorize against the influxDB instance.
     * @param password the password for the username which is used to authorize against the influxDB
     *                 instance.
     * @return a InfluxDB adapter suitable to access a InfluxDB.
     */
    public static OpenGeminiClient connect(final String url, final String username, final String password) {
        Preconditions.checkNonEmptyString(url, "url");
        Preconditions.checkNonEmptyString(username, "username");
        return new OpenGeminiOkhttpClient(url, username, password, new OkHttpClient.Builder());
    }

    /**
     * Create a connection to a InfluxDB.
     *
     * @param url    the url to connect to.
     * @param client the HTTP client to use
     * @return a InfluxDB adapter suitable to access a InfluxDB.
     */
    public static OpenGeminiClient connect(final String url, final OkHttpClient.Builder client) {
        Preconditions.checkNonEmptyString(url, "url");
        Objects.requireNonNull(client, "client");
        return new OpenGeminiOkhttpClient(url, null, null, client);
    }

    /**
     * Create a connection to a InfluxDB.
     *
     * @param url      the url to connect to.
     * @param username the username which is used to authorize against the influxDB instance.
     * @param password the password for the username which is used to authorize against the influxDB
     *                 instance.
     * @param client   the HTTP client to use
     * @return a InfluxDB adapter suitable to access a InfluxDB.
     */
    public static OpenGeminiClient connect(final String url, final String username, final String password,
                                           final OkHttpClient.Builder client) {
        return connect(url, username, password, client, OpenGeminiClient.ResponseFormat.JSON);
    }

    /**
     * Create a connection to a InfluxDB.
     *
     * @param url            the url to connect to.
     * @param username       the username which is used to authorize against the influxDB instance.
     * @param password       the password for the username which is used to authorize against the influxDB
     *                       instance.
     * @param client         the HTTP client to use
     * @param responseFormat The {@code ResponseFormat} to use for response from InfluxDB server
     * @return a InfluxDB adapter suitable to access a InfluxDB.
     */
    public static OpenGeminiClient connect(final String url, final String username, final String password,
                                           final OkHttpClient.Builder client, final OpenGeminiClient.ResponseFormat responseFormat) {
        Preconditions.checkNonEmptyString(url, "url");
        Preconditions.checkNonEmptyString(username, "username");
        Objects.requireNonNull(client, "client");
        return new OpenGeminiOkhttpClient(url, username, password, client, responseFormat);
    }
}