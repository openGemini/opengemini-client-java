/*
 * Copyright 2024 openGemini Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opengemini.client.common.compress;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GzipCompressorTest {

    private final GzipCompressor gzipCompressor = new GzipCompressor();

    @Test
    void testCompressAndDecompress() {
        String originalString = "This is a test string for GZIP compression";
        byte[] originalData = originalString.getBytes();

        // Compress the data
        byte[] compressedData = gzipCompressor.compress(originalData);
        Assertions.assertNotNull(compressedData);
        Assertions.assertNotEquals(0, compressedData.length);

        // Decompress the data
        byte[] decompressedData = gzipCompressor.decompress(compressedData);
        Assertions.assertNotNull(decompressedData);
        Assertions.assertArrayEquals(originalData, decompressedData);

        // Verify the decompressed string is the same as the original
        String decompressedString = new String(decompressedData);
        Assertions.assertEquals(originalString, decompressedString);
    }
}
