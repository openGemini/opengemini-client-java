package io.opengemini.client.test.common;

public class TlsTestUtil {
    private static final char[] JKS_PASSWORD = "TestOpenGemini@#123".toCharArray();
    private static final String ABSOLUTE_RESOURCE_PATH =
        TlsTestUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();

    public static String getResourcePathOfKeyStoreJks() {
        return ABSOLUTE_RESOURCE_PATH + "/jks/keystore.jks";
    }

    public static String getResourcePathOfTrustStoreJks() {
        return ABSOLUTE_RESOURCE_PATH + "/jks/truststore.jks";
    }

    public static char[] getJksPassword() {
        return JKS_PASSWORD;
    }
}
