package io.opengemini.client.grpc.support;

public class Encoder {
    public static long encodeZigZag64(long value) {
        return (value << 1) ^ (value >> 63);
    }
}
