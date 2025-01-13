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
import lombok.AllArgsConstructor;
import org.xerial.snappy.Snappy;

import java.io.IOException;

@AllArgsConstructor
public class SnappyCompressor implements Compressor {

    @Override
    public byte[] compress(byte[] data) {
        try {
            return Snappy.compress(data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to compress data", e);
        }
    }

    @Override
    public byte[] decompress(byte[] data) {
        try {
            return Snappy.uncompress(data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to decompress data", e);
        }
    }

    @Override
    public String getName() {
        return CompressMethod.SNAPPY.getValue();
    }
}