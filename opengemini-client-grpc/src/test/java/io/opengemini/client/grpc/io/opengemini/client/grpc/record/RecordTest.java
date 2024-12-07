package io.opengemini.client.grpc.io.opengemini.client.grpc.record;

import io.opengemini.client.grpc.record.ColVal;
import io.opengemini.client.grpc.record.Field;
import io.opengemini.client.grpc.record.Record;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class RecordTest {
    private Record record;
    private byte[] testBytes = "test data".getBytes(StandardCharsets.UTF_8);

    @Before
    public void setUp() {
        record = new Record();
    }

    @Test
    public void testEmptyRecord() throws IOException {
        // 测试一个空Record的marshal
        record.setSchema(new Field[0]);
        record.setColVals(new ColVal[0]);

        byte[] bytes = record.marshal();
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    public void testBasicFieldMarshal() throws IOException {
        Field field = new Field();
        field.setType(1);
        field.setName("test_field");

        byte[] bytes = field.marshal();
        assertNotNull(bytes);

        // Validate length：4(type) + 4(name length) + name bytes
        assertEquals(4 + 4 + "test_field".length(), bytes.length);
    }

    @Test
    public void testBasicColValMarshal() throws IOException {
        ColVal colVal = new ColVal();
        colVal.setVal(testBytes);
        colVal.setOffset(new int[]{0, testBytes.length});
        colVal.setBitmap(new byte[]{(byte)0xFF});
        colVal.setBitMapOffset(0);
        colVal.setLen(testBytes.length);
        colVal.setNilCount(0);

        byte[] bytes = colVal.marshal();
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    public void testCompleteRecordMarshal() throws IOException {
        Field[] schema = new Field[2];
        schema[0] = new Field();
        schema[0].setType(1);
        schema[0].setName("id");

        schema[1] = new Field();
        schema[1].setType(2);
        schema[1].setName("value");

        record.setSchema(schema);

        ColVal[] colVals = new ColVal[2];
        colVals[0] = new ColVal();
        colVals[0].setVal(new byte[]{1,2,3,4});
        colVals[0].setOffset(new int[]{0,4});
        colVals[0].setBitmap(new byte[]{(byte)0xFF});
        colVals[0].setBitMapOffset(0);
        colVals[0].setLen(4);
        colVals[0].setNilCount(0);

        colVals[1] = new ColVal();
        colVals[1].setVal("Hello".getBytes(StandardCharsets.UTF_8));
        colVals[1].setOffset(new int[]{0,5});
        colVals[1].setBitmap(new byte[]{(byte)0xFF});
        colVals[1].setBitMapOffset(0);
        colVals[1].setLen(5);
        colVals[1].setNilCount(0);

        record.setColVals(colVals);

        byte[] bytes = record.marshal();
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        printByteStructure(bytes);

        printByteGraphStructure(bytes);
    }

    @Test
    public void testNullHandling() throws IOException {
        Record record = new Record();
        record.setSchema(new Field[0]);
        record.setColVals(new ColVal[1]);

        ColVal colVal = new ColVal();
        colVal.setVal(null);
        colVal.setOffset(null);
        colVal.setBitmap(null);
        record.getColVals()[0] = colVal;

        byte[] bytes = record.marshal();
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    public void testLargeData() throws IOException {
        // 测试大数据量
        int size = 1000;
        Field[] schema = new Field[size];
        for (int i = 0; i < size; i++) {
            schema[i] = new Field();
            schema[i].setType(1);
            schema[i].setName("field_" + i);
        }
        record.setSchema(schema);
        record.setColVals(new ColVal[0]);

        byte[] bytes = record.marshal();
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    private void printByteStructure(byte[] bytes) throws IOException {
        System.out.println("\nByte Structure Analysis:");
        System.out.println("Total length: " + bytes.length + " bytes");

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);

        int schemaLen = dis.readInt();
        System.out.println("\nSchema Section:");
        System.out.println("Schema Length: " + schemaLen);

        for (int i = 0; i < schemaLen; i++) {
            int fieldSize = dis.readInt();
            System.out.println("  Field " + i + " size: " + fieldSize + " bytes");
            int type = dis.readInt();
            int nameLen = dis.readInt();
            byte[] nameBytes = new byte[nameLen];
            dis.readFully(nameBytes);
            System.out.println("    Type: " + type);
            System.out.println("    Name: " + new String(nameBytes, StandardCharsets.UTF_8));
        }

        int colValsLen = dis.readInt();
        System.out.println("\nColVals Section:");
        System.out.println("ColVals Length: " + colValsLen);

        for (int i = 0; i < colValsLen; i++) {
            int colValSize = dis.readInt();
            System.out.println("  ColVal " + i + " size: " + colValSize + " bytes");
            dis.skipBytes(colValSize);
        }

        if (dis.available() > 0) {
            System.out.println("\nRecMeta Section:");
            System.out.println("Remaining bytes: " + dis.available());
        }
    }

    @org.junit.Test
    public void testEmptyStrings() throws IOException {
        Field field = new Field();
        field.setType(1);
        field.setName("");

        byte[] bytes = field.marshal();
        assertNotNull(bytes);
        assertEquals(8, bytes.length);
    }

    @Test
    public void testZeroLengthArrays() throws IOException {
        ColVal colVal = new ColVal();
        colVal.setVal(new byte[0]);
        colVal.setOffset(new int[0]);
        colVal.setBitmap(new byte[0]);

        byte[] bytes = colVal.marshal();
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    private void printByteGraphStructure(byte[] bytes) {
        System.out.println("Binary Structure Visualization:");
        System.out.println("Total length: " + bytes.length + " bytes\n");

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);

        try {
            // Read Schema section
            int schemaLength = dis.readInt();
            System.out.println("Schema Section:");
            System.out.println("╔══════════════════════════════════════════");
            System.out.println("║ Schema Length: " + schemaLength + " fields");

            for (int i = 0; i < schemaLength; i++) {
                int fieldSize = dis.readInt();
                System.out.println("║ Field " + i + " Size: " + fieldSize + " bytes");
                System.out.println("║ ├── Type: " + dis.readInt());
                int nameLen = dis.readInt();
                byte[] nameBytes = new byte[nameLen];
                dis.readFully(nameBytes);
                System.out.println("║ └── Name: " + new String(nameBytes, StandardCharsets.UTF_8));
            }

            // Read ColVals section
            int colValsLength = dis.readInt();
            System.out.println("╠══════════════════════════════════════════");
            System.out.println("║ ColVals Section:");
            System.out.println("║ ColVals Length: " + colValsLength + " columns");

            for (int i = 0; i < colValsLength; i++) {
                int colValSize = dis.readInt();
                System.out.println("║ Column " + i + " Size: " + colValSize + " bytes");

                // Val
                int valLen = dis.readInt();
                byte[] val = new byte[valLen];
                dis.readFully(val);
                System.out.println("║ ├── Val: " + Arrays.toString(val));

                // Offset
                int offsetLen = dis.readInt();
                int[] offsets = new int[offsetLen];
                for (int j = 0; j < offsetLen; j++) {
                    offsets[j] = dis.readInt();
                }
                System.out.println("║ ├── Offset: " + Arrays.toString(offsets));

                // Bitmap
                int bitmapLen = dis.readInt();
                byte[] bitmap = new byte[bitmapLen];
                dis.readFully(bitmap);
                System.out.println("║ ├── Bitmap: " + Arrays.toString(bitmap));

                System.out.println("║ ├── BitMapOffset: " + dis.readInt());
                System.out.println("║ ├── Len: " + dis.readInt());
                System.out.println("║ └── NilCount: " + dis.readInt());
            }
            System.out.println("╚══════════════════════════════════════════");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
