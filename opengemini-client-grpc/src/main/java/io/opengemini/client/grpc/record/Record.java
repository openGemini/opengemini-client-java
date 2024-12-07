/*
 * Copyright 2024 openGemini Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * <p>
 * Schema Array Structure (repeated for each field):
 * +----------------------------------------------------------------------------------------+
 * |  Field Size  |    Type    |   Name Length  |              Name                         |
 * |   (4 bytes)  | (4 bytes)  |   (4 bytes)   |     (variable bytes, UTF-8)                |
 * +----------------------------------------------------------------------------------------+
 * <p>
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

            dos.flush();
            return baos.toByteArray();
        }
    }

}
