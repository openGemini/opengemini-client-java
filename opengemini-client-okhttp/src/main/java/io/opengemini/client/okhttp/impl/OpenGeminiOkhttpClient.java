package io.opengemini.client.okhttp.impl;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.okhttp.OpenGeminiClient;
import io.opengemini.client.okhttp.msgpack.MessagePackConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter.Factory;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * OpenGeminiClient的实现类，使用OkHttpClient作为HTTP客户端。
 *
 * @author Janle
 * @date 2024/5/10 10:05
 */
public class OpenGeminiOkhttpClient extends AbstractOpenGeminiOkhttpClient implements OpenGeminiClient {
    private OpenGeminiService openGeminiService;
    private static String LOG_LEVEL_PROPERTY = "io.opengemini.client.jdk.impl.OpenGeminiOkhttpClient.logLevel";
    private static final LogLevel LOG_LEVEL = LogLevel.parseLogLevel(System.getProperty(LOG_LEVEL_PROPERTY));
    private final String hostName;

    public OpenGeminiOkhttpClient(final String url, final String username, final String password,
                                  final OkHttpClient.Builder okHttpBuilder, final ResponseFormat responseFormat) {
        this(url, username, password, okHttpBuilder, new Retrofit.Builder(), responseFormat);
    }

    public OpenGeminiOkhttpClient(final String url, final String username, final String password,
                                  final OkHttpClient.Builder okHttpBuilder, final Retrofit.Builder retrofitBuilder,
                                  final ResponseFormat responseFormat) {
        this.messagePack = ResponseFormat.MSGPACK.equals(responseFormat);
        this.hostName = parseHost(url);

        this.loggingInterceptor = new HttpLoggingInterceptor();
        setLogLevel(LOG_LEVEL);

        this.gzipRequestInterceptor = new GzipRequestInterceptor();
        OkHttpClient.Builder clonedOkHttpBuilder = okHttpBuilder.build().newBuilder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(gzipRequestInterceptor);
        if (username != null && password != null) {
            clonedOkHttpBuilder.addInterceptor(new BasicAuthInterceptor(username, password));
        }
        Factory converterFactory = null;
        switch (responseFormat) {
            case MSGPACK:
                clonedOkHttpBuilder.addInterceptor(chain -> {
                    Request request = chain.request().newBuilder().addHeader("Accept", APPLICATION_MSGPACK).build();
                    return chain.proceed(request);
                });

                converterFactory = MessagePackConverterFactory.create();
                chunkProccesor = new MessagePackChunkProccesor();
                break;
            case JSON:
            default:
                converterFactory = MoshiConverterFactory.create();
                Moshi moshi = new Moshi.Builder().build();
                JsonAdapter<QueryResult> adapter = moshi.adapter(QueryResult.class);
                chunkProccesor = new JSONChunkProccesor(adapter);
                break;
        }

        this.client = clonedOkHttpBuilder.build();
        Retrofit.Builder clonedRetrofitBuilder = retrofitBuilder.baseUrl(url).build().newBuilder();
        this.retrofit = clonedRetrofitBuilder.client(this.client)
                .addConverterFactory(converterFactory).build();
        this.openGeminiService = this.retrofit.create(OpenGeminiService.class);

    }

    public OpenGeminiOkhttpClient(final String url, final String username, final String password,
                                  final OkHttpClient.Builder client) {
        this(url, username, password, client, ResponseFormat.JSON);
    }

    @Override
    protected OpenGeminiService getOpenGeminiService() {
        return this.openGeminiService;
    }


}
