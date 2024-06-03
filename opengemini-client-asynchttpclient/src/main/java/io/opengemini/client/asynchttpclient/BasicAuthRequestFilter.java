package io.opengemini.client.asynchttpclient;

import org.asynchttpclient.Request;
import org.asynchttpclient.filter.FilterContext;
import org.asynchttpclient.filter.RequestFilter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class BasicAuthRequestFilter implements RequestFilter {

    private final String basicHeaderValue;

    public BasicAuthRequestFilter(String username, String password) {
        String usernameAndPassword = username + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(usernameAndPassword.getBytes(StandardCharsets.ISO_8859_1));
        this.basicHeaderValue = "Basic " + encoded;
    }

    @Override
    public <T> FilterContext<T> filter(FilterContext<T> ctx) {
        Request authenticatedRequest = ctx.getRequest()
                .toBuilder()
                .addHeader("Authorization", basicHeaderValue)
                .build();
        return new FilterContext.FilterContextBuilder<>(ctx).request(authenticatedRequest).build();
    }
}
