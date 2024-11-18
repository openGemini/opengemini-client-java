package io.opengemini.client.grpc.record;

import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Record Binary Structure:
 * +----------------------------------------------------------------------------------------+
 * |                                   Record Header                                        |
 * +----------------------------------------------------------------------------------------+
 * |  Schema Length  |    Schema Array     |   ColVals Length   |      ColVals Array        |
 * |    (4 bytes)   |   (variable size)   |     (4 bytes)      |    (variable size)         |
 * +----------------------------------------------------------------------------------------+
 *
 * Schema Array Structure (repeated for each field):
 * +----------------------------------------------------------------------------------------+
 * |  Field Size  |    Type    |   Name Length  |              Name                         |
 * |   (4 bytes)  | (4 bytes)  |   (4 bytes)   |     (variable bytes, UTF-8)                |
 * +----------------------------------------------------------------------------------------+
 *
 * ColVals Array Structure (repeated for each column):
 * +----------------------------------------------------------------------------------------+
 * |  ColVal Size |                          ColVal Content                                 |
 * |   (4 bytes)  |                                                                         |
 * |              |  +----------------------------------------------------------+           |
 * |              |  | Val Length  |  Val Content   |                           |           |
 * |              |  | (4 bytes)   | (variable)     |                           |           |
 * |              |  +----------------------------------------------------------+           |
 * |              |  | Offset Length  |  Offset Array                           |           |
 * |              |  | (4 bytes)      | (4 bytes * length)                      |           |
 * |              |  +----------------------------------------------------------+           |
 * |              |  | Bitmap Length  |  Bitmap                                 |           |
 * |              |  | (4 bytes)      | (variable bytes)                        |           |
 * |              |  +----------------------------------------------------------+           |
 * |              |  | BitMapOffset   |  Length        |  NilCount              |           |
 * |              |  | (4 bytes)      |  (4 bytes)     |  (4 bytes)            |            |
 * |              |  +----------------------------------------------------------+           |
 * +----------------------------------------------------------------------------------------+
 */
@Data
public class Record {
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

            // TODO: Write RecMeta

            dos.flush();
            return baos.toByteArray();
        }
    }

}
