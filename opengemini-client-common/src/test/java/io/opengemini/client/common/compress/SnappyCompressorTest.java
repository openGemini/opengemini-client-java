package io.opengemini.client.common.compress;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class SnappyCompressorTest {
    @Test
    public void testCompression() {
        SnappyCompressor snappyCompressor = new SnappyCompressor();
        // Example input data
        String input = "This is a test string to compress";
        byte[] inputData = input.getBytes();

        // Compress the data
        byte[] compressedData = snappyCompressor.compress(inputData);

        // Decompress the data
        byte[] decompressedData = snappyCompressor.decompress(compressedData);

        // Verify the decompressed data matches the original input
        assertArrayEquals(inputData, decompressedData);
    }
}
