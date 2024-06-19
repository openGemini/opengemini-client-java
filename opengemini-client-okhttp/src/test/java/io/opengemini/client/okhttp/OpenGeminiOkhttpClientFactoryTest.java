package io.opengemini.client.okhttp;

import io.opengemini.client.api.Address;
import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.TlsConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utils.TlsTestUtil;
import java.time.Duration;
import java.util.Collections;

class OpenGeminiOkhttpClientFactoryTest {
    static final String KEYSTORE_JKS_PATH = TlsTestUtil.getResourcePathOfKeyStoreJks();
    static final String TRUSTSTORE_JKS_PATH = TlsTestUtil.getResourcePathOfTrustStoreJks();
    static final String JKS_PASSWORD = TlsTestUtil.getJksPassword();

    private static TlsConfig getBasicTlsConfig() {
        TlsConfig tlsConfig = new TlsConfig();
        tlsConfig.tlsVersions = new String[]{"TLSv1.3"};
        tlsConfig.tlsCipherSuites = new String[]{"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256"};
        return tlsConfig;
    }

    @Test
    public void testGetBasicClient() {
        Configuration configuration = Configuration.builder()
                .addresses(Collections.singletonList(new Address("127.0.0.1", 8086)))
                .connectTimeout(Duration.ofSeconds(3)).timeout(Duration.ofSeconds(5)).build();
        try {
            OpenGeminiOkhttpClientFactory.create(configuration);
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void testGetClientWithKeyStoreOnly() {
        // build basic tls config
        TlsConfig tlsConfig = getBasicTlsConfig();

        // test tls config with keystore only
        tlsConfig.setKeyStorePath(KEYSTORE_JKS_PATH);
        tlsConfig.setKeyStorePassword(JKS_PASSWORD);
        tlsConfig.setTlsVerifyDisabled(true);
        Configuration configuration = Configuration.builder()
                .addresses(Collections.singletonList(new Address("127.0.0.1", 8086)))
                .connectTimeout(Duration.ofSeconds(3)).timeout(Duration.ofSeconds(5)).tlsEnabled(true)
                .tlsConfig(tlsConfig).build();
        try {
            OpenGeminiOkhttpClientFactory.create(configuration);
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void testGetClientWithTrustStoreOnly() {
        // build basic tls config
        TlsConfig tlsConfig = getBasicTlsConfig();

        // test tls config with truststore only
        tlsConfig.setTrustStorePath(TRUSTSTORE_JKS_PATH);
        tlsConfig.setTrustStorePassword(JKS_PASSWORD);
        tlsConfig.setTlsVerifyDisabled(false);
        Configuration configuration = Configuration.builder()
                .addresses(Collections.singletonList(new Address("127.0.0.1", 8086)))
                .connectTimeout(Duration.ofSeconds(3)).timeout(Duration.ofSeconds(5)).tlsEnabled(true)
                .tlsConfig(tlsConfig).build();
        try {
            OpenGeminiOkhttpClientFactory.create(configuration);
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void testGetClientWithKeyStoreAndTrustStore() {
        // build basic tls config
        TlsConfig tlsConfig = getBasicTlsConfig();

        // test tls config with key store and trust store
        tlsConfig.setKeyStorePath(KEYSTORE_JKS_PATH);
        tlsConfig.setKeyStorePassword(JKS_PASSWORD);
        tlsConfig.setTrustStorePath(TRUSTSTORE_JKS_PATH);
        tlsConfig.setTrustStorePassword(JKS_PASSWORD);
        tlsConfig.setTlsVerifyDisabled(false);
        Configuration configuration = Configuration.builder()
                .addresses(Collections.singletonList(new Address("127.0.0.1", 8086)))
                .connectTimeout(Duration.ofSeconds(3)).timeout(Duration.ofSeconds(5)).tlsEnabled(true)
                .tlsConfig(tlsConfig).build();
        try {
            OpenGeminiOkhttpClientFactory.create(configuration);
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void testGetClientWithoutTrustStoreConfigButEnableTlsVerification() {
        // build basic tls config
        TlsConfig tlsConfig = getBasicTlsConfig();

        // test enable tls verification but lack trust store config
        tlsConfig.setTlsVerifyDisabled(false);
        Configuration configuration = Configuration.builder()
                .addresses(Collections.singletonList(new Address("127.0.0.1", 8086)))
                .connectTimeout(Duration.ofSeconds(3)).timeout(Duration.ofSeconds(5)).tlsEnabled(true)
                .tlsConfig(tlsConfig).build();
        Assertions.assertThrows(OpenGeminiException.class, () -> OpenGeminiOkhttpClientFactory.create(configuration));
    }
}
