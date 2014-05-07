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
package leshan.server.lwm2m.message.californium;

import static org.mockito.Mockito.mock;
import leshan.server.lwm2m.message.ContentFormat;
import leshan.server.lwm2m.message.ResourceSpec;
import leshan.server.lwm2m.observation.ResourceObserver;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.MediaTypeRegistry;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;

public class CaliforniumBasedObservationTest extends BasicTestSupport {

    final String reportedValue = "15";
    Request coapRequest;
    ResourceSpec target;

    @Before
    public void setUp() throws Exception {
        givenASimpleClient();
        this.target = new ResourceSpec(this.client, 3, 0, 15);
    }

    @Test
    public void testNotificationIsForwardedToResourceObserver() {

        ResourceObserver observer = new ResourceObserver() {

            @Override
            public void notify(byte[] content, ContentFormat contentFormat, ResourceSpec target) {
                Assert.assertArrayEquals(CaliforniumBasedObservationTest.this.reportedValue.getBytes(), content);
                Assert.assertEquals((Integer) 3, target.getObjectId());
                Assert.assertEquals((Integer) 0, target.getObjectInstanceId());
                Assert.assertEquals((Integer) 15, target.getResourceId());
            }
        };

        givenAnObserveRequest(this.target);
        CaliforniumBasedObservation observation = new CaliforniumBasedObservation(this.coapRequest, observer, this.target);

        Response coapResponse = new Response(ResponseCode.CONTENT);
        coapResponse.setPayload(this.reportedValue, MediaTypeRegistry.TEXT_PLAIN);
        observation.onResponse(coapResponse);
    }

    @Test
    public void testCancelInvokesCancelOnCoapRequest() {

        ResourceObserver observer = mock(ResourceObserver.class);
        givenAnObserveRequest(this.target);
        Assert.assertFalse(this.coapRequest.isCanceled());
        CaliforniumBasedObservation observation = new CaliforniumBasedObservation(this.coapRequest, observer, this.target);

        observation.cancel();

        Assert.assertTrue(this.coapRequest.isCanceled());
    }

    private void givenAnObserveRequest(ResourceSpec target) {
        this.coapRequest = Request.newGet();
        this.coapRequest.getOptions().addURIPath(String.valueOf(target.getObjectId()));
        this.coapRequest.getOptions().addURIPath(String.valueOf(target.getObjectInstanceId()));
        this.coapRequest.getOptions().addURIPath(String.valueOf(target.getResourceId()));
        this.coapRequest.setDestination(target.getClient().getAddress());
        this.coapRequest.setDestinationPort(target.getClient().getPort());
    }
}
