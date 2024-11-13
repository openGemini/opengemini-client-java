package io.opengemini.client.grpc.record;

import io.opengemini.client.grpc.support.Encoder;
import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Data
public class Field {
    private int type;
    private String name;

    public byte[] marshal() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
            dos.writeShort(name.length());
            dos.write(nameBytes);

            dos.writeLong(Encoder.encodeZigZag64(type));
            dos.flush();
            return baos.toByteArray();
        }
    }
}
