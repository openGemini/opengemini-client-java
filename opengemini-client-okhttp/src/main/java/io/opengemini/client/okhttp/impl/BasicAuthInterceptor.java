package io.opengemini.client.okhttp.impl;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * @author Janle
 * @date 2024/5/10 10:35
 */
public class BasicAuthInterceptor implements Interceptor {

    private String credentials;

    public BasicAuthInterceptor(final String user, final String password) {
        credentials = Credentials.basic(user, password);
    }

    @Override
    public Response intercept(final Chain chain) throws IOException {
        Request request = chain.request();
        Request authenticatedRequest = request.newBuilder().header("Authorization", credentials).build();
        return chain.proceed(authenticatedRequest);
    }
}
