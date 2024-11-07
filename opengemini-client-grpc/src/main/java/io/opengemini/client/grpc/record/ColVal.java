package io.opengemini.client.grpc.record;

import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

            // Write Val
            dos.writeInt(val != null ? val.length : 0);
            if (val != null) {
                dos.write(val);
            }

            // Write Offset
            dos.writeInt(offset != null ? offset.length : 0);
            if (offset != null) {
                for (int off : offset) {
                    dos.writeInt(off);
                }
            }

            // Write Bitmap
            dos.writeInt(bitmap != null ? bitmap.length : 0);
            if (bitmap != null) {
                dos.write(bitmap);
            }

            // Write other fields
            dos.writeInt(bitMapOffset);
            dos.writeInt(len);
            dos.writeInt(nilCount);

            dos.flush();
            return baos.toByteArray();
        }
    }
}
