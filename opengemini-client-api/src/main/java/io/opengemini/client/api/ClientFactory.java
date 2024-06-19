package io.opengemini.client.api;


import java.util.HashMap;
import java.util.Map;

/**
 * Define factory creation constraints to generate a client factory for creating different invocation factories.
 */
public class ClientFactory {
    private static final Map<String, Class<? extends OpenGeminiClientFactory>> registeredClientFactorys = new HashMap<>();
    public static final String okhttp = "okhttp";


    private static final Map<String, String> registeredFactorys = new HashMap() {
        {
            put(okhttp, "io.opengemini.client.okhttp.OkHttpClientFactory");
        }
    };


    // 注册生产的sdk工厂

    public static void registerProduct(String apiType) {
        if (registeredFactorys.containsKey(apiType)) {
            throw new OpenGeminiException("检查是否有对应请求工厂的适配!");
        }
        String factory = registeredFactorys.get(apiType);
        try {
            Class<? extends OpenGeminiClientFactory> clientFactory = (Class<? extends OpenGeminiClientFactory>) Class.forName(factory);
            registeredClientFactorys.put(apiType, clientFactory);
        } catch (ClassNotFoundException e) {
            throw new OpenGeminiException("配置可以注册的工厂");
        }
    }

    /**
     * 创建客户端创建的工厂
     *
     * @param apiType
     * @return
     * @throws Exception
     */
    public static OpenGeminiClientFactory createFactory(String apiType) throws Exception {
        Class<? extends OpenGeminiClientFactory> productClass = registeredClientFactorys.get(apiType);
        if (productClass != null) {
            return productClass.getDeclaredConstructor().newInstance();
        }
        throw new IllegalArgumentException("Unknown apiType ID: " + apiType);
    }
}
