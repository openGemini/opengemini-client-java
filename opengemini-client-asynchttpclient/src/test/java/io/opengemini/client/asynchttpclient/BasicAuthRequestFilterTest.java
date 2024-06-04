package io.opengemini.client.asynchttpclient;

import org.asynchttpclient.AsyncCompletionHandlerBase;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.asynchttpclient.filter.FilterContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BasicAuthRequestFilterTest {

    @Test
    void filter() {
        BasicAuthRequestFilter filter = new BasicAuthRequestFilter("test_name", "test_pwd");
        FilterContext<Response> context = filter.filter(
                new FilterContext.FilterContextBuilder<>(new AsyncCompletionHandlerBase(),
                        new RequestBuilder().build()).build());

        Assertions.assertEquals("Basic dGVzdF9uYW1lOnRlc3RfcHdk",
                context.getRequest().getHeaders().get("Authorization"));
    }
}
