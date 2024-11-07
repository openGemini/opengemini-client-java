package io.opengemini.client.grpc.record;

import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Data
public class RecMeta {
    private int[] intervalIndex;
    private long[][] times;
    private int[] tagIndex;
    private byte[][] tags;
    private ColMeta[] colMeta;

    public byte[] marshal() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            // Write intervalIndex
            writeIntArray(dos, intervalIndex);

            // Write times
            dos.writeInt(times != null ? times.length : 0);
            if (times != null) {
                for (long[] timeArray : times) {
                    writeLongArray(dos, timeArray);
                }
            }

            // Write tagIndex
            writeIntArray(dos, tagIndex);

            // Write tags
            dos.writeInt(tags != null ? tags.length : 0);
            if (tags != null) {
                for (byte[] tag : tags) {
                    dos.writeInt(tag != null ? tag.length : 0);
                    if (tag != null) {
                        dos.write(tag);
                    }
                }
            }

            // Write colMeta
            dos.writeInt(colMeta != null ? colMeta.length : 0);
            if (colMeta != null) {
                for (ColMeta meta : colMeta) {
                    if (meta != null) {
                        byte[] metaBytes = meta.marshal();
                        dos.writeInt(metaBytes.length);
                        dos.write(metaBytes);
                    } else {
                        dos.writeInt(0);
                    }
                }
            }

            return baos.toByteArray();
        }
    }

    private void writeIntArray(DataOutputStream dos, int[] array) throws IOException {
        dos.writeInt(array != null ? array.length : 0);
        if (array != null) {
            for (int value : array) {
                dos.writeInt(value);
            }
        }
    }

    private void writeLongArray(DataOutputStream dos, long[] array) throws IOException {
        dos.writeInt(array != null ? array.length : 0);
        if (array != null) {
            for (long value : array) {
                dos.writeLong(value);
            }
        }
    }


}
