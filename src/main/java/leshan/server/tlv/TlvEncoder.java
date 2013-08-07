/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package leshan.server.tlv;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.mina.codec.ProtocolEncoderException;
import org.apache.mina.codec.StatelessProtocolEncoder;

public class TlvEncoder implements StatelessProtocolEncoder<Tlv[], ByteBuffer> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Void createEncoderState() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ByteBuffer encode(Tlv[] tlvs, Void state) {
        int size = 0;
        System.err.println("start");
        for (Tlv tlv : tlvs) {

            int length = tlvEncodedLength(tlv);
            size += tlvEncodedSize(tlv, length);
            System.err.println("tlv size : " + size);
        }
        System.err.println("done");
        System.err.println("size : " + size);
        ByteBuffer b = ByteBuffer.allocate(size);
        b.order(ByteOrder.BIG_ENDIAN);
        for (Tlv tlv : tlvs) {
            encode(tlv, b);
        }
        b.flip();
        return b;
    }

    private int tlvEncodedSize(Tlv tlv, int length) {
        int size = 1 /* HEADER */;
        size += (tlv.getIdentifier() < 65_536) ? 1 : 2; /* 8 bits or 16 bits identifiers*/

        if (length < 8) {
            size += 0;
        } else if (length < 256) {
            size += 1;
        } else if (length < 65_536) {
            size += 2;
        } else if (length < 16_777_216) {
            size += 3;
        } else {
            throw new ProtocolEncoderException("length should fit in max 24bits");
        }

        size += length;
        return size;
    }

    private int tlvEncodedLength(Tlv tlv) {
        int length;
        switch (tlv.getType()) {
        case RESOURCE_VALUE:
        case RESOURCE_INSTANCE:
            length = tlv.getValue().length;
            break;
        default:
            length = 0;
            for (Tlv child : tlv.getChildren()) {
                int subLength = tlvEncodedLength(child);
                length += tlvEncodedSize(child, subLength);
            }
        }

        return length;
    }

    private void encode(Tlv tlv, ByteBuffer b) {
        int length;
        length = tlvEncodedLength(tlv);
        int typeByte;

        switch (tlv.getType()) {
        case OBJECT_INSTANCE:
            typeByte = 0b00_000000;
            break;
        case RESOURCE_INSTANCE:
            typeByte = 0b01_000000;
            break;
        case MULTIPLE_RESOURCE:
            typeByte = 0b10_000000;
            break;
        case RESOURCE_VALUE:
            // encode the value
            typeByte = 0b11_000000;
            break;
        default:
            throw new IllegalStateException("unknown TLV type : '" + tlv.getType() + "'");
        }

        // encode identifier length
        typeByte |= (tlv.getIdentifier() < 65_536) ? 0b00_0000 : 0b10_0000;

        // type of length
        if (length < 8) {
            typeByte |= length;
        } else if (length < 256) {
            typeByte |= 0b0000_1000;
        } else if (length < 65_536) {
            typeByte |= 0b0001_0000;
        } else {
            typeByte |= 0b0001_1000;
        }

        // fill the buffer
        b.put((byte) typeByte);
        if (tlv.getIdentifier() < 65_536) {
            b.put((byte) tlv.getIdentifier());
        } else {
            b.putShort((short) tlv.getIdentifier());
        }

        // write length

        if (length >= 8) {
            if (length < 256) {
                b.put((byte) length);
            } else if (length < 65_536) {
                b.putShort((short) length);
            } else {
                int msb = (length & 0xFF_00_00) >> 16;
                b.put((byte) msb);
                b.putShort((short) (length & 0xFF_FF));
                typeByte |= 0b0001_1000;
            }
        }

        switch (tlv.getType()) {
        case RESOURCE_VALUE:
        case RESOURCE_INSTANCE:
            b.put(tlv.getValue());
            break;
        default:
            for (Tlv child : tlv.getChildren()) {
                encode(child, b);
            }
            break;
        }
    }
}
