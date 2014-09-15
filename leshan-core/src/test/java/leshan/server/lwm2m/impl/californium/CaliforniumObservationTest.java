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
package leshan.server.lwm2m.impl.californium;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import leshan.server.lwm2m.impl.BasicTestSupport;
import leshan.server.lwm2m.node.LwM2mNode;
import leshan.server.lwm2m.node.LwM2mPath;
import leshan.server.lwm2m.node.LwM2mResource;
import leshan.server.lwm2m.observation.Observation;
import leshan.server.lwm2m.observation.ObservationListener;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.MediaTypeRegistry;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;

public class CaliforniumObservationTest {

    final String reportedValue = "15";
    Request coapRequest;
    LwM2mPath target;

    private BasicTestSupport support = new BasicTestSupport();

    @Before
    public void setUp() throws Exception {
        support.givenASimpleClient();
        this.target = new LwM2mPath(3, 0, 15);
    }

    @Test
    public void testNotificationIsForwardedToResourceObserver() {

        ObservationListener listener = new ObservationListener() {
            @Override
            public void newValue(Observation observation, LwM2mNode value) {
                assertEquals(CaliforniumObservationTest.this.reportedValue, ((LwM2mResource) value).getValue().value);
                assertEquals(3, observation.getPath().getObjectId());
                assertEquals((Integer) 0, observation.getPath().getObjectInstanceId());
                assertEquals((Integer) 15, observation.getPath().getResourceId());
                assertEquals(CaliforniumObservationTest.this.support.client, observation.getClient());
            }

            @Override
            public void cancelled(Observation observation) {
            }
        };
        givenAnObserveRequest(this.target);
        CaliforniumObservation observation = new CaliforniumObservation(this.coapRequest, this.support.client,
                this.target);
        observation.addListener(listener);
        Response coapResponse = new Response(ResponseCode.CONTENT);
        coapResponse.setPayload(this.reportedValue, MediaTypeRegistry.TEXT_PLAIN);
        observation.onResponse(coapResponse);
    }

    @Test
    public void testCancelInvokesCancelOnCoapRequest() {

        givenAnObserveRequest(this.target);
        assertFalse(this.coapRequest.isCanceled());
        CaliforniumObservation observation = new CaliforniumObservation(this.coapRequest, this.support.client,
                this.target);
        observation.cancel();
        Assert.assertTrue(this.coapRequest.isCanceled());
    }

    private void givenAnObserveRequest(LwM2mPath target) {
        this.coapRequest = Request.newGet();
        this.coapRequest.getOptions().addURIPath(String.valueOf(target.getObjectId()));
        this.coapRequest.getOptions().addURIPath(String.valueOf(target.getObjectInstanceId()));
        this.coapRequest.getOptions().addURIPath(String.valueOf(target.getResourceId()));
        this.coapRequest.setDestination(this.support.client.getAddress());
        this.coapRequest.setDestinationPort(this.support.client.getPort());
    }
}
