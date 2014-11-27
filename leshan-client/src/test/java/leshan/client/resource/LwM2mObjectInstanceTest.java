/*
 * Copyright (c) 2013, Sierra Wireless,
 * Copyright (c) 2014, Zebra Technologies,
 * Copyright (c) 2014, Bosch Software Innovations GmbH,
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
package leshan.client.resource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import leshan.client.exchange.LwM2mCallbackExchange;
import leshan.client.exchange.LwM2mExchange;
import leshan.client.resource.multiple.MultipleLwM2mExchange;
import leshan.client.resource.multiple.MultipleLwM2mResource;
import leshan.client.resource.string.StringLwM2mExchange;
import leshan.client.resource.string.StringLwM2mResource;
import leshan.client.response.ReadResponse;
import leshan.tlv.Tlv;
import leshan.tlv.Tlv.TlvType;
import leshan.tlv.TlvEncoder;

import org.junit.Test;

public class LwM2mObjectInstanceTest {

    private static final boolean REQUIRED = true;
    private static final boolean MANDATORY = true;
    private static final boolean SINGLE = true;
    private LwM2mClientObjectDefinition definition;

    @Test
    public void testSingleResource() {
        final int resourceId = 12;
        initializeObjectWithSingleResource(resourceId, "hello");

        assertCorrectRead(createInstance(definition, new byte[0]),
                new Tlv(TlvType.RESOURCE_VALUE, null, "hello".getBytes(), resourceId));
    }

    @Test
    public void testMultipleResourceWithOneInstance() {
        final int resourceId = 65;
        initializeObjectWithMultipleResource(resourceId, Collections.singletonMap(94, "ninety-four".getBytes()));

        assertCorrectRead(createInstance(definition, new byte[0]), new Tlv(TlvType.MULTIPLE_RESOURCE,
                new Tlv[] { new Tlv(TlvType.RESOURCE_INSTANCE, null, "ninety-four".getBytes(), 94) }, null, resourceId));
    }

    @Test
    public void testMultipleResourceWithThreeInstances() {
        final int resourceId = 65;
        final Map<Integer, byte[]> values = new HashMap<>();
        values.put(1100, "eleven-hundred".getBytes());
        values.put(10, "ten".getBytes());
        values.put(3, "three".getBytes());
        initializeObjectWithMultipleResource(resourceId, values);

        assertCorrectRead(createInstance(definition, new byte[0]), new Tlv(TlvType.MULTIPLE_RESOURCE, new Tlv[] {
                                new Tlv(TlvType.RESOURCE_INSTANCE, null, "three".getBytes(), 3),
                                new Tlv(TlvType.RESOURCE_INSTANCE, null, "ten".getBytes(), 10),
                                new Tlv(TlvType.RESOURCE_INSTANCE, null, "eleven-hundred".getBytes(), 1100) }, null,
                resourceId));
    }

    private void initializeObjectWithSingleResource(final int resourceId, final String value) {
        definition = new LwM2mClientObjectDefinition(100, MANDATORY, SINGLE, new SingleResourceDefinition(resourceId,
                new SampleSingleResource(value), !REQUIRED));
    }

    private void initializeObjectWithMultipleResource(final int resourceId, final Map<Integer, byte[]> values) {
        definition = new LwM2mClientObjectDefinition(101, MANDATORY, SINGLE, new MultipleResourceDefinition(resourceId,
                new SampleMultipleResource(values), !REQUIRED));
    }

    private void assertCorrectRead(final LwM2mClientObjectInstance instance, final Tlv... tlvs) {
        final LwM2mExchange exchange = mock(LwM2mExchange.class);
        instance.read(exchange);
        final byte[] bytes = TlvEncoder.encode(tlvs).array();
        verify(exchange).respond(ReadResponse.success(bytes));
    }

    private LwM2mClientObjectInstance createInstance(final LwM2mClientObjectDefinition definition, final byte[] payload) {
        final LwM2mClientObject obj = mock(LwM2mClientObject.class);
        final LwM2mClientObjectInstance instance = new LwM2mClientObjectInstance(0, obj, definition);
        @SuppressWarnings("unchecked")
        final LwM2mCallbackExchange<LwM2mClientObjectInstance> createExchange = mock(LwM2mCallbackExchange.class);
        when(createExchange.getRequestPayload()).thenReturn(payload);
        instance.createInstance(createExchange);
        return instance;
    }

    private class SampleSingleResource extends StringLwM2mResource {

        private final String value;

        public SampleSingleResource(final String value) {
            this.value = value;
        }

        @Override
        public void handleRead(final StringLwM2mExchange exchange) {
            exchange.respondContent(value);
        }

    }

    private class SampleMultipleResource extends MultipleLwM2mResource {

        private final Map<Integer, byte[]> values;

        public SampleMultipleResource(final Map<Integer, byte[]> values) {
            this.values = values;
        }

        @Override
        public void handleRead(final MultipleLwM2mExchange exchange) {
            exchange.respondContent(values);
        }

    }

}
