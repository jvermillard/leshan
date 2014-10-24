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
package leshan.server.californium.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import leshan.server.node.LwM2mNode;
import leshan.server.node.LwM2mPath;
import leshan.server.node.LwM2mResource;
import leshan.server.observation.Observation;
import leshan.server.observation.ObservationListener;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CaliforniumObservationTest {

    final String reportedValue = "15";
    Request coapRequest;
    LwM2mPath target;

    private CaliforniumTestSupport support = new CaliforniumTestSupport();

    @Before
    public void setUp() throws Exception {
        support.givenASimpleClient();
        target = new LwM2mPath(3, 0, 15);
    }

    @Test
    public void coapNotification_is_forwarded_to_observationListener() {

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
        givenAnObserveRequest(target);
        CaliforniumObservation observation = new CaliforniumObservation(coapRequest, support.client, target);
        observation.addListener(listener);
        Response coapResponse = new Response(ResponseCode.CONTENT);
        coapResponse.setPayload(reportedValue, MediaTypeRegistry.TEXT_PLAIN);
        observation.onResponse(coapResponse);
    }

    @Test
    public void cancel_Observation_cancel_coapRequest() {

        givenAnObserveRequest(target);
        assertFalse(coapRequest.isCanceled());
        CaliforniumObservation observation = new CaliforniumObservation(coapRequest, support.client, target);
        observation.cancel();
        Assert.assertTrue(coapRequest.isCanceled());
    }

    private void givenAnObserveRequest(LwM2mPath target) {
        coapRequest = Request.newGet();
        coapRequest.getOptions().addURIPath(String.valueOf(target.getObjectId()));
        coapRequest.getOptions().addURIPath(String.valueOf(target.getObjectInstanceId()));
        coapRequest.getOptions().addURIPath(String.valueOf(target.getResourceId()));
        coapRequest.setDestination(support.client.getAddress());
        coapRequest.setDestinationPort(support.client.getPort());
    }
}
