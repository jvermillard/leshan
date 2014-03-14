/*
 * Copyright (c) 2013, Sierra Wireless
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of {{ project }} nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package leshan.server.lwm2m.tlv;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TlvEncoder {

	private static final Logger LOG = LoggerFactory.getLogger(TlvEncoder.class);
	
    public ByteBuffer encode(Tlv[] tlvs) {
        int size = 0;
        
        LOG.trace("start");
        for (Tlv tlv : tlvs) {

            int length = tlvEncodedLength(tlv);
            size += tlvEncodedSize(tlv, length);
            LOG.trace("tlv size : {}", size);
        }
        LOG.trace("done\nsize : {}", size);
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
        size += (tlv.getIdentifier() < 65_536) ? 1 : 2; /* 8 bits or 16 bits identifiers */

        if (length < 8) {
            size += 0;
        } else if (length < 256) {
            size += 1;
        } else if (length < 65_536) {
            size += 2;
        } else if (length < 16_777_216) {
            size += 3;
        } else {
            throw new IllegalArgumentException("length should fit in max 24bits");
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
            throw new IllegalArgumentException("unknown TLV type : '" + tlv.getType() + "'");
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
