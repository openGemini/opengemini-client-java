package io.opengemini.client.okhttp.msgpack;

import io.opengemini.client.api.QueryResult;
import okhttp3.ResponseBody;
import retrofit2.Converter;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Janle
 * @date 2024/5/10 15:23
 */
public class MessagePackResponseBodyConverter implements Converter<ResponseBody, QueryResult> {

    @Override
    public QueryResult convert(final ResponseBody value) throws IOException {
        try (InputStream is = value.byteStream()) {
            MessagePackTraverser traverser = new MessagePackTraverser();
            return traverser.parse(is);
        }
    }
}
