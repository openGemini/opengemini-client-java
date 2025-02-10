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
        Assertions.assertArrayEquals(inputData, decompressedData);
    }
}
