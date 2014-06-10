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

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Date;

import org.junit.Test;

/**
 * Unit tests for {@link Tlv} instantiation methods.
 */
public class TlvTest {

    @Test
    public void new_integer_tlv() {
        Tlv tlv = Tlv.newIntegerValue(TlvType.RESOURCE_VALUE, 123, 456);
        assertEquals(TlvType.RESOURCE_VALUE, tlv.getType());
        assertNull(tlv.getChildren());
        assertEquals(456, tlv.getIdentifier());

        // check value
        ByteBuffer bb = ByteBuffer.wrap(tlv.getValue());
        assertEquals(123, bb.getInt());
        assertEquals(0, bb.remaining());
    }

    @Test
    public void new_long_tlv() {
        long value = System.currentTimeMillis();
        Tlv tlv = Tlv.newLongValue(TlvType.RESOURCE_INSTANCE, value, 456);
        assertEquals(TlvType.RESOURCE_INSTANCE, tlv.getType());
        assertNull(tlv.getChildren());
        assertEquals(456, tlv.getIdentifier());

        // check value
        ByteBuffer bb = ByteBuffer.wrap(tlv.getValue());
        assertEquals(value, bb.getLong());
        assertEquals(0, bb.remaining());
    }

    @Test
    public void new_string_tlv() throws UnsupportedEncodingException {
        Tlv tlv = Tlv.newStringValue(TlvType.RESOURCE_VALUE, "value", 456);
        assertEquals(TlvType.RESOURCE_VALUE, tlv.getType());
        assertNull(tlv.getChildren());
        assertEquals(456, tlv.getIdentifier());

        // check value
        assertEquals("value", new String(tlv.getValue(), "UTF-8"));
    }

    @Test
    public void new_date_tlv() {
        long timestamp = System.currentTimeMillis();
        Tlv tlv = Tlv.newDateValue(TlvType.RESOURCE_INSTANCE, new Date(timestamp), 456);
        assertEquals(TlvType.RESOURCE_INSTANCE, tlv.getType());
        assertNull(tlv.getChildren());
        assertEquals(456, tlv.getIdentifier());

        // check value
        ByteBuffer bb = ByteBuffer.wrap(tlv.getValue());
        assertEquals(timestamp / 1000L, bb.getLong());
        assertEquals(0, bb.remaining());
    }

    @Test
    public void new_boolean_tlv() {
        Tlv tlv = Tlv.newBooleanValue(TlvType.RESOURCE_INSTANCE, true, 456);
        assertEquals(TlvType.RESOURCE_INSTANCE, tlv.getType());
        assertNull(tlv.getChildren());
        assertEquals(456, tlv.getIdentifier());

        // check value
        assertEquals(1, tlv.getValue().length);
        assertEquals(1, tlv.getValue()[0]);
    }
}
