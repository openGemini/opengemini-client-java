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