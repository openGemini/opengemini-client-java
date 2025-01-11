package io.opengemini.client.common.compress;

public interface Compressor {

        byte[] compress(byte[] data);

        byte[] decompress(byte[] data);

        String getName();
}
