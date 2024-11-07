package io.opengemini.client.grpc.record;

import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Data
public class Record {
    private RecMeta recMeta;
    private ColVal[] colVals;
    private Field[] schema;

    public byte[] marshal() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            // Write Schema
            dos.writeInt(schema.length);
            for (Field field : schema) {
                byte[] fieldBytes = field.marshal();
                dos.writeInt(fieldBytes.length);
                dos.write(fieldBytes);
            }

            // Write ColVals
            dos.writeInt(colVals.length);
            for (ColVal colVal : colVals) {
                byte[] colValBytes = colVal.marshal();
                dos.writeInt(colValBytes.length);
                dos.write(colValBytes);
            }

            // Write RecMeta
            if (recMeta != null) {
                byte[] recMetaBytes = recMeta.marshal();
                dos.write(recMetaBytes);
            }

            dos.flush();
            return baos.toByteArray();
        }
    }

}
