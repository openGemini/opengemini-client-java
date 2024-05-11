package io.opengemini.client.okhttp.impl;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import io.opengemini.client.api.Address;
import io.opengemini.client.api.AuthConfig;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.okhttp.Configuration;
import io.opengemini.client.okhttp.OpenGeminiClient;
import okhttp3.OkHttpClient;
import retrofit2.Converter.Factory;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * OpenGeminiClient的实现类，使用OkHttpClient作为HTTP客户端。
 *
 * @author Janle
 * @date 2024/5/10 10:05
 */
public class OpenGeminiOkhttpProxyClient extends AbstractOpenGeminiOkhttpClient implements OpenGeminiClient {
    private static String LOG_LEVEL_PROPERTY = "io.opengemini.client.jdk.impl.OpenGeminiOkhttpProxyClient.logLevel";
    private static final LogLevel LOG_LEVEL = LogLevel.parseLogLevel(System.getProperty(LOG_LEVEL_PROPERTY));
    private List<OpenGeminiService> openGeminiServiceList = new ArrayList<>();
    private final AtomicInteger prevIndex = new AtomicInteger(-1);
    private final Set<String> urlSet = new HashSet<>();

    public OpenGeminiOkhttpProxyClient(Configuration configuration) {
        AuthConfig authConfig = configuration.getAuthConfig();
        List<Address> addresses = configuration.getAddresses();

        for (Address address : addresses) {
            String url = address.getHost() + ":" + address.getPort();
            if (urlSet.contains(url)) {
                continue;
            }
            urlSet.add(url);
            setLogLevel(LOG_LEVEL);
            this.gzipRequestInterceptor = new GzipRequestInterceptor();
            OkHttpClient.Builder clonedOkHttpBuilder = new OkHttpClient.Builder()
                    .build().newBuilder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(gzipRequestInterceptor);
            if (authConfig != authConfig && authConfig.getUsername() != null && authConfig.getPassword() != null) {
                clonedOkHttpBuilder.addInterceptor(new BasicAuthInterceptor(authConfig.getUsername(), authConfig.getPassword()));
            }
            Factory converterFactory = MoshiConverterFactory.create();

            Moshi moshi = new Moshi.Builder().build();
            JsonAdapter<QueryResult> adapter = moshi.adapter(QueryResult.class);
            chunkProccesor = new JSONChunkProccesor(adapter);

            this.client = clonedOkHttpBuilder.build();
            Retrofit.Builder clonedRetrofitBuilder = new Retrofit.Builder().baseUrl("http://" + url).build().newBuilder();
            this.retrofit = clonedRetrofitBuilder.client(this.client)
                    .addConverterFactory(converterFactory).build();
            openGeminiServiceList.add(this.retrofit.create(OpenGeminiService.class));
        }
    }

    /**
     * 单独的本地负载均衡实现
     *
     * @return
     */
    @Override
    protected OpenGeminiService getOpenGeminiService() {
        int idx = prevIndex.addAndGet(1);
        idx = idx % this.urlSet.size();
        return openGeminiServiceList.get(idx);
    }


    @Override
    public void disableBatch() {
        this.batchEnabled.set(false);
        if (this.batchProcessor != null) {
            this.batchProcessor.flushAndShutdown();
        }
    }
}
