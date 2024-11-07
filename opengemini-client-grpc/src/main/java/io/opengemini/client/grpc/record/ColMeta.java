package io.opengemini.client.grpc.record;

import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Data
public class ColMeta {
    private boolean isSetFlag;
    private Object min;
    private Object max;
    private long minTime;
    private long maxTime;

    private Object first;
    private Object last;
    private long firstTime;
    private long lastTime;

    private Object sum;
    private Object count;

    public byte[] marshal() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {


            // Write isSetFlag
            dos.writeBoolean(isSetFlag);

            // Write interface{} values
            writeInterfaceValue(dos, min);
            writeInterfaceValue(dos, max);
            dos.writeLong(minTime);
            dos.writeLong(maxTime);

            writeInterfaceValue(dos, first);
            writeInterfaceValue(dos, last);
            dos.writeLong(firstTime);
            dos.writeLong(lastTime);

            writeInterfaceValue(dos, sum);
            writeInterfaceValue(dos, count);

            return baos.toByteArray();
        }
    }

    private void writeInterfaceValue(DataOutputStream dos, Object value) throws IOException {
        if (value == null) {
            dos.writeByte(0); // null type
            return;
        }

        if (value instanceof Integer) {
            dos.writeByte(1); // int type
            dos.writeInt((Integer) value);
        } else if (value instanceof Long) {
            dos.writeByte(2); // long type
            dos.writeLong((Long) value);
        } else if (value instanceof Double) {
            dos.writeByte(3); // double type
            dos.writeDouble((Double) value);
        } else if (value instanceof String) {
            dos.writeByte(4); // string type
            byte[] bytes = ((String) value).getBytes(StandardCharsets.UTF_8);
            dos.writeInt(bytes.length);
            dos.write(bytes);
        } else if (value instanceof byte[]) {
            dos.writeByte(5); // bytes type
            byte[] bytes = (byte[]) value;
            dos.writeInt(bytes.length);
            dos.write(bytes);
        } else if (value instanceof Float) {
            dos.writeByte(6); // float type
            dos.writeFloat((Float) value);
        } else if (value instanceof Boolean) {
            dos.writeByte(7); // boolean type
            dos.writeBoolean((Boolean) value);
        } else {
            throw new IOException("Unsupported type: " + value.getClass());
        }
    }
}
