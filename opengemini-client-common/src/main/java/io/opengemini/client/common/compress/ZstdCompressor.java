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