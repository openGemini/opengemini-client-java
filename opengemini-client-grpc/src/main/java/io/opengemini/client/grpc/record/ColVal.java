package io.opengemini.client.grpc.record;

import io.opengemini.client.grpc.support.Encoder;
import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Data
public class ColVal {
    private byte[] val;
    private int[] offset;
    private byte[] bitmap;
    private int bitMapOffset;
    private int len;
    private int nilCount;

    public byte[] marshal() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {
            
            dos.writeLong(Encoder.encodeZigZag64(len));
            dos.writeLong(Encoder.encodeZigZag64(nilCount));
            dos.writeLong(Encoder.encodeZigZag64(bitMapOffset));

            dos.writeInt(val != null ? val.length : 0);
            if (val != null) {
                dos.write(val);
            }

            dos.writeInt(bitmap != null ? bitmap.length : 0);
            if (bitmap != null) {
                dos.write(bitmap);
            }

            dos.writeInt(offset != null ? offset.length : 0);
            if (offset != null) {
                for (int off : offset) {
                    dos.writeInt(off);
                }
            }

            dos.flush();
            return baos.toByteArray();
        }
    }

}
