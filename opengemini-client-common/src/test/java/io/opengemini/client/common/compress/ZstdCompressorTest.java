package io.opengemini.client.common.compress;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ZstdCompressorTest {

    @Test
    public void testCompression() {
        ZstdCompressor zstdCompressor = new ZstdCompressor();
        // Example input data
        String input = "This is a test string to compress";
        byte[] inputData = input.getBytes();

        // Compress the data
        byte[] compressedData = zstdCompressor.compress(inputData);

        // Decompress the data
        byte[] decompressedData = zstdCompressor.decompress(compressedData);

        // Verify the decompressed data matches the original input
        assertArrayEquals(inputData, decompressedData);
    }
}