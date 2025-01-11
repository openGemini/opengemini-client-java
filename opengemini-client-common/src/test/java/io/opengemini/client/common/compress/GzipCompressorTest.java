package io.opengemini.client.common.compress;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GzipCompressorTest {

    private final GzipCompressor gzipCompressor = new GzipCompressor();

    @Test
    void testCompressAndDecompress() {
        String originalString = "This is a test string for GZIP compression";
        byte[] originalData = originalString.getBytes();

        // Compress the data
        byte[] compressedData = gzipCompressor.compress(originalData);
        assertNotNull(compressedData);
        assertNotEquals(0, compressedData.length);

        // Decompress the data
        byte[] decompressedData = gzipCompressor.decompress(compressedData);
        assertNotNull(decompressedData);
        assertArrayEquals(originalData, decompressedData);

        // Verify the decompressed string is the same as the original
        String decompressedString = new String(decompressedData);
        assertEquals(originalString, decompressedString);
    }
}