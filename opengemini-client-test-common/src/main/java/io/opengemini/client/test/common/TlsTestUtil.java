package io.opengemini.client.test.common;

public class TlsTestUtil {
    public static final String JKS_PASSWORD = "TestOpenGemini@#123";
    private static final String ABSOLUTE_RESOURCE_PATH =
        TlsTestUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();

    public static String getResourcePathOfKeyStoreJks() {
        return ABSOLUTE_RESOURCE_PATH + "/jks/keystore.jks";
    }

    public static String getResourcePathOfTrustStoreJks() {
        return ABSOLUTE_RESOURCE_PATH + "/jks/truststore.jks";
    }

    public static String getJksPassword() {
        return JKS_PASSWORD;
    }
}
