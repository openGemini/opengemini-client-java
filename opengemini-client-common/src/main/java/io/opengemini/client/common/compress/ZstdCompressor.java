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

import io.opengemini.client.api.CompressMethod;
import com.github.luben.zstd.Zstd;

public class ZstdCompressor implements Compressor {

    @Override
    public byte[] compress(byte[] data) {
        return Zstd.compress(data);
    }

    @Override
    public byte[] decompress(byte[] data) {
        try {
            long decompressedSize = Zstd.decompressedSize(data);
            byte[] decompressedData = new byte[(int) decompressedSize];
            Zstd.decompress(decompressedData, data);
            return decompressedData;
        } catch (Exception e) {
            throw new RuntimeException("Failed to decompress data", e);
        }
    }

    @Override
    public String getName() {
        return CompressMethod.ZSTD.getValue();
    }
}