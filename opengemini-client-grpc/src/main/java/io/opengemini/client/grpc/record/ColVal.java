// Copyright 2022 Huawei Cloud Computing Technologies Co., Ltd.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.opengemini.client.grpc.record;

import io.opengemini.client.grpc.support.Encoder;
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
