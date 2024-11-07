package io.opengemini.client.grpc.io.opengemini.client.grpc.record;

import io.opengemini.client.grpc.record.*;
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
        // 测试Field的基本marshal功能
        Field field = new Field();
        field.setType(1);
        field.setName("test_field");

        byte[] bytes = field.marshal();
        assertNotNull(bytes);

        // 验证长度：4(type) + 4(name length) + name bytes
        assertEquals(4 + 4 + "test_field".length(), bytes.length);
    }

    @Test
    public void testBasicColValMarshal() throws IOException {
        // 测试ColVal的基本marshal功能
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
    public void testColMetaMarshal() throws IOException {
        // 测试ColMeta的各种数据类型marshal
        ColMeta colMeta = new ColMeta();
        colMeta.setSetFlag(true);
        colMeta.setMin(1);
        colMeta.setMax(100);
        colMeta.setMinTime(1000L);
        colMeta.setMaxTime(2000L);
        colMeta.setFirst(1.5);
        colMeta.setLast("last");
        colMeta.setFirstTime(1500L);
        colMeta.setLastTime(2500L);
        colMeta.setSum(5050.5);
        colMeta.setCount(100L);

        byte[] bytes = colMeta.marshal();
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    public void testCompleteRecordMarshal() throws IOException {
        // 测试完整Record的marshal
        // 构建Schema
        Field[] schema = new Field[2];
        schema[0] = new Field();
        schema[0].setType(1);
        schema[0].setName("id");

        schema[1] = new Field();
        schema[1].setType(2);
        schema[1].setName("value");

        record.setSchema(schema);

        // 构建ColVals
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

        // 构建RecMeta
        RecMeta recMeta = new RecMeta();
        recMeta.setIntervalIndex(new int[]{0,1,2});
        recMeta.setTimes(new long[][]{{1000L, 2000L}, {3000L, 4000L}});
        recMeta.setTagIndex(new int[]{0,1});
        recMeta.setTags(new byte[][]{{1,2}, {3,4}});

        long minTime = new Date().getTime() * 1_000_000 - 10000;
        long maxTime = new Date().getTime() * 1_000_000;

        ColMeta[] colMetas = new ColMeta[2];
        colMetas[0] = new ColMeta();
        colMetas[0].setSetFlag(true);
        colMetas[0].setMin(1);
        colMetas[0].setMax(100);
        colMetas[0].setMinTime(minTime);
        colMetas[0].setMaxTime(maxTime);
        colMetas[0].setFirst(1);
        colMetas[0].setLast(100);
        colMetas[0].setFirstTime(minTime);
        colMetas[0].setLastTime(maxTime);
        colMetas[0].setSum(5050);
        colMetas[0].setCount(100);

        colMetas[1] = new ColMeta();
        colMetas[1].setSetFlag(true);
        colMetas[1].setMin(1.5);
        colMetas[1].setMax(100.5);
        colMetas[1].setMinTime(minTime);
        colMetas[1].setMaxTime(maxTime);
        colMetas[1].setFirst(1.5);
        colMetas[1].setLast(100.5);
        colMetas[1].setFirstTime(minTime);
        colMetas[1].setLastTime(maxTime);
        colMetas[1].setSum(5050.5);
        colMetas[1].setCount(100L);

        recMeta.setColMeta(colMetas);
        record.setRecMeta(recMeta);

        byte[] bytes = record.marshal();
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        // 打印字节结构
        printByteStructure(bytes);

        // 打印图形化结构
        printByteGraphStructure(bytes);
    }

    @Test
    public void testNullHandling() throws IOException {
        // 测试null值处理
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

    @Test
    public void testAllDataTypes() throws IOException {
        // 测试所有支持的数据类型
        ColMeta colMeta = new ColMeta();
        colMeta.setSetFlag(true);

        // 测试所有支持的数据类型
        colMeta.setMin(null);                    // null type
        colMeta.setMax(100);                     // int type
        colMeta.setFirst(1000L);                 // long type
        colMeta.setLast(1.5);                    // double type
        colMeta.setSum("string value");          // string type
        colMeta.setCount(new byte[]{1,2,3});     // bytes type

        byte[] bytes = colMeta.marshal();
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test(expected = IOException.class)
    public void testUnsupportedType() throws IOException {
        // 测试不支持的数据类型
        ColMeta colMeta = new ColMeta();
        colMeta.setMin(new Object());  // 不支持的类型

        colMeta.marshal();
    }

    private void printByteStructure(byte[] bytes) throws IOException {
        System.out.println("\nByte Structure Analysis:");
        System.out.println("Total length: " + bytes.length + " bytes");

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);

        // 读取Schema部分
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

        // 读取ColVals部分
        int colValsLen = dis.readInt();
        System.out.println("\nColVals Section:");
        System.out.println("ColVals Length: " + colValsLen);

        for (int i = 0; i < colValsLen; i++) {
            int colValSize = dis.readInt();
            System.out.println("  ColVal " + i + " size: " + colValSize + " bytes");
            // 跳过具体内容
            dis.skipBytes(colValSize);
        }

        // 如果还有剩余字节，说明是RecMeta部分
        if (dis.available() > 0) {
            System.out.println("\nRecMeta Section:");
            System.out.println("Remaining bytes: " + dis.available());
        }
    }

    @Test
    public void testBoundaryValues() throws IOException {
        // 测试边界值
        ColMeta colMeta = new ColMeta();
        colMeta.setSetFlag(true);
        colMeta.setMin(Integer.MAX_VALUE);
        colMeta.setMax(Integer.MIN_VALUE);
        colMeta.setMinTime(Long.MAX_VALUE);
        colMeta.setMaxTime(Long.MIN_VALUE);
        colMeta.setFirst(Double.MAX_VALUE);
        colMeta.setLast(Double.MIN_VALUE);
        colMeta.setFirstTime(Long.MAX_VALUE);
        colMeta.setLastTime(Long.MIN_VALUE);

        byte[] bytes = colMeta.marshal();
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @org.junit.Test
    public void testEmptyStrings() throws IOException {
        // 测试空字符串
        Field field = new Field();
        field.setType(1);
        field.setName("");

        byte[] bytes = field.marshal();
        assertNotNull(bytes);
        assertEquals(8, bytes.length); // 4(type) + 4(空字符串长度0)
    }

    @Test
    public void testZeroLengthArrays() throws IOException {
        // 测试0长度数组
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
