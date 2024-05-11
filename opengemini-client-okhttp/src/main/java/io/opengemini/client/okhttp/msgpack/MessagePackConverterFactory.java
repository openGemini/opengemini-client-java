package io.opengemini.client.okhttp.msgpack;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author Janle
 * @date 2024/5/10 15:22
 */
public class MessagePackConverterFactory extends Converter.Factory {
    public static MessagePackConverterFactory create() {
        return new MessagePackConverterFactory();
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(final Type type, final Annotation[] annotations,
                                                            final Retrofit retrofit) {
        return new MessagePackResponseBodyConverter();
    }
}