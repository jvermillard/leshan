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
package leshan.tlv;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import leshan.tlv.Tlv.TlvType;
import leshan.util.Charsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TlvDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(TlvDecoder.class);

    public static Tlv[] decode(ByteBuffer input) {

        List<Tlv> tlvs = new ArrayList<>();

        while (input.remaining() > 0) {
            input.order(ByteOrder.BIG_ENDIAN);
            int typeByte = input.get() & 0xFF;
            TlvType type;
            switch (typeByte & 0b1100_0000) {
            case 0b0000_0000:
                type = TlvType.OBJECT_INSTANCE;
                break;
            case 0b0100_0000:
                type = TlvType.RESOURCE_INSTANCE;
                break;
            case 0b1000_0000:
                type = TlvType.MULTIPLE_RESOURCE;
                break;
            case 0b1100_0000:
                type = TlvType.RESOURCE_VALUE;
                break;
            default:
                throw new IllegalArgumentException("unknown type : " + (typeByte & 0b1100_0000));
            }

            int identifier;
            // decode identifier
            if ((typeByte & 0b0010_0000) == 0) {
                identifier = input.get() & 0xFF;
            } else {
                identifier = input.getShort() & 0xFFFF;
            }

            int length;
            int lengthType = typeByte & 0b0001_1000;

            // decode length
            switch (lengthType) {
            case 0b0000_0000:
                // 2 bit length
                length = typeByte & 0b0000_0111;
                break;
            case 0b0000_1000:
                // 8 bit length
                length = input.get() & 0xFF;
                break;
            case 0b0001_0000:
                // 16 bit length
                length = input.getShort() & 0xFFFF;
                break;
            case 0b0001_1000:
                // 24 bit length
                length = ((input.get() & 0xFF) << 16) + input.getShort() & 0xFFFF;
                break;
            default:
                throw new IllegalArgumentException("unknown length type : " + (typeByte & 0b0001_1000));
            }

            if (type == TlvType.RESOURCE_VALUE || type == TlvType.RESOURCE_INSTANCE) {
                byte[] payload = new byte[length];
                input.get(payload);
                tlvs.add(new Tlv(type, null, payload, identifier));
            } else {
                // create a view of the contained TLVs
                ByteBuffer slice = input.slice();
                slice.limit(length);

                Tlv[] children = decode(slice);

                // skip the children, it will be decoded by the view
                input.position(input.position() + length);

                Tlv tlv = new Tlv(type, children, null, identifier);
                tlvs.add(tlv);
            }
        }

        return tlvs.toArray(new Tlv[] {});
    }

    /**
     * Decodes a byte array into string value.
     */
    public static String decodeString(byte[] value) {
        return new String(value, Charsets.UTF_8);
    }

    /**
     * Decodes a byte array into a boolean value.
     */
    public static boolean decodeBoolean(byte[] value) {
        if (value.length == 1) {
            if (value[0] == 0) {
                return false;
            } else if (value[0] == 1) {
                return true;
            } else {
                LOG.warn("Boolean value should be encoded as integer with value 0 or 1, not {}", value[0]);
                return false;
            }
        }
        throw new IllegalArgumentException("Invalid size");
    }

    /**
     * Decodes a byte array into a date value.
     */
    public static Date decodeDate(byte[] value) {
        BigInteger bi = new BigInteger(value);
        if (value.length <= 8) {
            return new Date(bi.longValue() * 1000L);
        } else {
            throw new IllegalArgumentException("Invalid length for a time value: " + value.length);
        }
    }

    /**
     * Decodes a byte array into an integer value.
     */
    public static Number decodeInteger(byte[] value) {
        BigInteger bi = new BigInteger(value);
        if (value.length == 1) {
            return bi.byteValue();
        } else if (value.length <= 2) {
            return bi.shortValue();
        } else if (value.length <= 4) {
            return bi.intValue();
        } else if (value.length <= 8) {
            return bi.longValue();
        } else {
            throw new IllegalArgumentException("Invalid length for an integer value: " + value.length);
        }
    }

    /**
     * Decodes a byte array into a float value.
     */
    public static Number decodeFloat(byte[] value) {
        ByteBuffer floatBb = ByteBuffer.wrap(value);
        if (value.length == 4) {
            return floatBb.getFloat();
        } else if (value.length == 8) {
            return floatBb.getDouble();
        } else {
            throw new IllegalArgumentException("Invalid length for a float value: " + value.length);
        }
    }
}
