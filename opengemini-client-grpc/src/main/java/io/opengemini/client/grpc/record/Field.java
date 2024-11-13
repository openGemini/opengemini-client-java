package io.opengemini.client.grpc.record;

import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Data
public class Field {
    private int type;
    private String name;

    public byte[] marshal() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

//            dos.writeInt(type);
//            byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
//            dos.writeInt(nameBytes.length);
//            dos.write(nameBytes);

            dos.writeChars(name);
            dos.writeLong(type);

            dos.flush();
            return baos.toByteArray();
        }
    }
}
