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
package leshan.client.operation;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import leshan.client.exchange.LwM2mExchange;
import leshan.client.exchange.aggregate.AggregatedLwM2mExchange;
import leshan.client.exchange.aggregate.LwM2mObjectInstanceReadResponseAggregator;
import leshan.client.exchange.aggregate.LwM2mResponseAggregator;
import leshan.client.response.LwM2mResponse;
import leshan.client.response.ReadResponse;
import leshan.tlv.Tlv;
import leshan.tlv.Tlv.TlvType;
import leshan.tlv.TlvEncoder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReadResourceLwM2mResponseAggregatorTest {

    @Mock
    private LwM2mExchange coapExchange;

    @Test
    public void testSingleSuccessfulRead() {
        final int numExpectedResults = 1;
        final int resourceId = 45;
        final byte[] resourceValue = "value".getBytes();

        final LwM2mResponseAggregator aggr = new LwM2mObjectInstanceReadResponseAggregator(coapExchange,
                numExpectedResults);
        final LwM2mExchange exchange = new AggregatedLwM2mExchange(aggr, resourceId);

        exchange.respond(ReadResponse.success(resourceValue));

        final Tlv[] tlvs = new Tlv[numExpectedResults];
        tlvs[0] = new Tlv(TlvType.RESOURCE_VALUE, null, resourceValue, resourceId);
        verify(coapExchange).respond(ReadResponse.success(TlvEncoder.encode(tlvs).array()));
    }

    @Test
    public void testMultipleSuccessfulReads() {
        final int numExpectedResults = 2;
        final int resourceId1 = 45;
        final int resourceId2 = 78;
        final byte[] resourceValue1 = "hello".getBytes();
        final byte[] resourceValue2 = "world".getBytes();

        final LwM2mResponseAggregator aggr = new LwM2mObjectInstanceReadResponseAggregator(coapExchange,
                numExpectedResults);
        final LwM2mExchange exchange1 = new AggregatedLwM2mExchange(aggr, resourceId1);
        final LwM2mExchange exchange2 = new AggregatedLwM2mExchange(aggr, resourceId2);

        exchange1.respond(ReadResponse.success(resourceValue1));
        exchange2.respond(ReadResponse.success(resourceValue2));

        final Tlv[] tlvs = new Tlv[numExpectedResults];
        tlvs[0] = new Tlv(TlvType.RESOURCE_VALUE, null, resourceValue1, resourceId1);
        tlvs[1] = new Tlv(TlvType.RESOURCE_VALUE, null, resourceValue2, resourceId2);
        verify(coapExchange).respond(ReadResponse.success(TlvEncoder.encode(tlvs).array()));
    }

    @Test
    public void testIncompleteReadDoesNotSend() {
        final int numExpectedResults = 2;
        final int resourceId = 45;
        final byte[] resourceValue = "hello".getBytes();

        final LwM2mResponseAggregator aggr = new LwM2mObjectInstanceReadResponseAggregator(coapExchange,
                numExpectedResults);
        final LwM2mExchange exchange = new AggregatedLwM2mExchange(aggr, resourceId);

        exchange.respond(ReadResponse.success(resourceValue));

        verify(coapExchange, never()).respond(any(LwM2mResponse.class));
    }

}
