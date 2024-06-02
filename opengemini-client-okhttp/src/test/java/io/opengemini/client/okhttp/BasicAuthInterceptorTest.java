package io.opengemini.client.okhttp;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class BasicAuthInterceptorTest {

    @Test
    void intercept() throws Exception {
        Request request = new Request.Builder().url("http://127.0.0.1/test").build();
        Response response = new Response.Builder().code(200).body(
                ResponseBody.create("test", MediaType.parse("text/plain; charset=utf-8"))).message("test").request(
                request).protocol(Protocol.HTTP_1_1).build();

        Interceptor.Chain chain = Mockito.mock(Interceptor.Chain.class);
        Mockito.when(chain.request()).thenReturn(request);
        Mockito.when(chain.proceed(Mockito.any(Request.class))).thenReturn(response);

        BasicAuthInterceptor interceptor = new BasicAuthInterceptor("test_name", "test_pwd");
        try (Response ignore = interceptor.intercept(chain)) {
            ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);
            Mockito.verify(chain).proceed(captor.capture());
            Request value = captor.getValue();
            Assertions.assertEquals("Basic dGVzdF9uYW1lOnRlc3RfcHdk", value.header("Authorization"));
        }
    }
}
