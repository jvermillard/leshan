/*
 * Copyright (c) 2013, Sierra Wireless,
 * Copyright (c) 2014, Zebra Technologies,
 * 
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
package leshan.client.lwm2m.resource;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.resource.multiple.MultipleLwM2mExchange;
import leshan.client.lwm2m.resource.multiple.MultipleLwM2mResource;
import leshan.client.lwm2m.response.ReadResponse;
import leshan.client.lwm2m.response.WriteResponse;
import leshan.server.lwm2m.impl.tlv.Tlv;
import leshan.server.lwm2m.impl.tlv.Tlv.TlvType;
import leshan.server.lwm2m.impl.tlv.TlvEncoder;

import org.junit.Test;

public class MultipleLwM2mResourceTest {

    @Test
    public void testWriteGoodValue() {
        final LwM2mExchange exchange = mock(LwM2mExchange.class);
        final Tlv[] tlvs = new Tlv[] { new Tlv(TlvType.RESOURCE_INSTANCE, null, "zero".getBytes(), 0),
                                new Tlv(TlvType.RESOURCE_INSTANCE, null, "one".getBytes(), 1),
                                new Tlv(TlvType.RESOURCE_INSTANCE, null, "two".getBytes(), 2) };
        when(exchange.getRequestPayload()).thenReturn(TlvEncoder.encode(tlvs).array());

        final ReadableWriteableTestResource testResource = new ReadableWriteableTestResource();
        testResource.write(exchange);

        final Map<Integer, byte[]> values = new HashMap<>();
        values.put(0, "zero".getBytes());
        values.put(2, "two".getBytes());
        values.put(1, "one".getBytes());
        assertDeepEquals(values, testResource.value);
        verify(exchange).respond(WriteResponse.success());
    }

    @Test
    public void testWriteNonTlvValue() {
        final LwM2mExchange exchange = mock(LwM2mExchange.class);
        when(exchange.getRequestPayload()).thenReturn("lol not a tlv".getBytes());

        final ReadableWriteableTestResource testResource = new ReadableWriteableTestResource();
        testResource.write(exchange);

        assertNull(testResource.value);
        verify(exchange).respond(WriteResponse.badRequest());
    }

    @Test
    public void testWriteIncorrectTlvValue() {
        final LwM2mExchange exchange = mock(LwM2mExchange.class);
        final Tlv[] tlvs = new Tlv[] { new Tlv(TlvType.RESOURCE_VALUE, null, "zero".getBytes(), 0),
                                new Tlv(TlvType.RESOURCE_INSTANCE, null, "one".getBytes(), 1),
                                new Tlv(TlvType.RESOURCE_INSTANCE, null, "two".getBytes(), 2) };
        when(exchange.getRequestPayload()).thenReturn(TlvEncoder.encode(tlvs).array());

        final ReadableWriteableTestResource testResource = new ReadableWriteableTestResource();
        testResource.write(exchange);

        assertNull(testResource.value);
        verify(exchange).respond(WriteResponse.badRequest());
    }

    @Test
    public void testRead() {
        final LwM2mExchange exchange = mock(LwM2mExchange.class);

        final Map<Integer, byte[]> initialValue = new HashMap<>();
        initialValue.put(0, "zero".getBytes());
        initialValue.put(2, "two".getBytes());
        initialValue.put(1, "one".getBytes());
        final ReadableWriteableTestResource testResource = new ReadableWriteableTestResource(initialValue);

        testResource.read(exchange);

        final Tlv[] tlvs = new Tlv[] { new Tlv(TlvType.RESOURCE_INSTANCE, null, "zero".getBytes(), 0),
                                new Tlv(TlvType.RESOURCE_INSTANCE, null, "one".getBytes(), 1),
                                new Tlv(TlvType.RESOURCE_INSTANCE, null, "two".getBytes(), 2) };

        assertEquals(initialValue, testResource.value);
        verify(exchange).respond(ReadResponse.success(TlvEncoder.encode(tlvs).array()));
    }

    @Test
    public void testDefaultPermissionsRead() {
        final LwM2mExchange exchange = mock(LwM2mExchange.class);

        final DefaultTestResource testResource = new DefaultTestResource();
        testResource.read(exchange);

        verify(exchange).respond(ReadResponse.notAllowed());
    }

    @Test
    public void testDefaultPermissionsWrite() {
        final LwM2mExchange exchange = mock(LwM2mExchange.class);
        when(exchange.getRequestPayload()).thenReturn("badwolf".getBytes());

        final DefaultTestResource testResource = new DefaultTestResource();
        testResource.write(exchange);

        verify(exchange).respond(WriteResponse.notAllowed());
    }

    private void assertDeepEquals(final Map<Integer, byte[]> left, final Map<Integer, byte[]> right) {
        assertEquals(left.size(), right.size());
        final Set<Entry<Integer, byte[]>> entrySet = left.entrySet();
        for (final Entry<Integer, byte[]> entry : entrySet) {
            assertArrayEquals(entry.getValue(), right.get(entry.getKey()));
        }
    }

    private class ReadableWriteableTestResource extends MultipleLwM2mResource {

        private Map<Integer, byte[]> value;

        public ReadableWriteableTestResource(final Map<Integer, byte[]> newValue) {
            value = newValue;
        }

        public ReadableWriteableTestResource() {
        }

        @Override
        protected void handleWrite(final MultipleLwM2mExchange exchange) {
            this.value = exchange.getRequestPayload();
            exchange.respondSuccess();
        }

        @Override
        protected void handleRead(final MultipleLwM2mExchange exchange) {
            exchange.respondContent(value);
        }

    }

    private class DefaultTestResource extends MultipleLwM2mResource {

    }

}
