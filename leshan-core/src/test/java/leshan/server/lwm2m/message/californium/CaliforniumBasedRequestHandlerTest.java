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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;

import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ContentFormat;
import leshan.server.lwm2m.message.CreateRequest;
import leshan.server.lwm2m.message.DeleteRequest;
import leshan.server.lwm2m.message.DiscoverRequest;
import leshan.server.lwm2m.message.DiscoverResponse;
import leshan.server.lwm2m.message.ObserveRequest;
import leshan.server.lwm2m.message.ObserveResponse;
import leshan.server.lwm2m.message.OperationType;
import leshan.server.lwm2m.message.ReadRequest;
import leshan.server.lwm2m.message.ResponseCode;
import leshan.server.lwm2m.message.WriteAttributesRequest;
import leshan.server.lwm2m.message.WriteRequest;
import leshan.server.lwm2m.observation.Observation;
import leshan.server.lwm2m.observation.ObserveSpec;
import leshan.server.lwm2m.observation.ResourceObserver;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.inf.vs.californium.coap.MediaTypeRegistry;
import ch.ethz.inf.vs.californium.coap.Option;
import ch.ethz.inf.vs.californium.coap.OptionNumberRegistry;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;

public class CaliforniumBasedRequestHandlerTest extends BasicTestSupport {

    private static final Integer OBJECT_ID_DEVICE = 3;

    private static final String TEXT_PAYLOAD = "payload";

    CoapClient coapEndpoint;
    CaliforniumBasedRequestHandler requestHandler;
    InMemoryObservationRegistry observationRegistry;

    @Before
    public void setUp() throws Exception {
        this.destination = InetAddress.getLocalHost();
        this.coapEndpoint = mock(CoapClient.class);
        this.observationRegistry = new InMemoryObservationRegistry();
        this.requestHandler = new CaliforniumBasedRequestHandler(this.coapEndpoint, this.observationRegistry);
        givenASimpleClient();
    }

    @Test
    public void testSendReadRequestReturnsContentResponse() throws Exception {

        ReadRequest request = ReadRequest.newRequest(this.client, OBJECT_ID_DEVICE);
        ifTheClientReturns(newResponse(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT, TEXT_PAYLOAD));

        ClientResponse response = this.requestHandler.send(request);
        Assert.assertTrue(response.getCode() == ResponseCode.CONTENT);
        Assert.assertEquals(TEXT_PAYLOAD, new String(response.getContent()));
    }

    @Test
    public void testSendDiscoverRequestReturnsDiscoverResponse() throws Exception {

        String coreLinkPayload = "/3/0/1;pmin=10";
        DiscoverRequest request = DiscoverRequest.newRequest(this.client, OBJECT_ID_DEVICE);
        Response coapResponse = new Response(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT);
        coapResponse.setPayload(coreLinkPayload, MediaTypeRegistry.APPLICATION_XML);
        ifTheClientReturns(coapResponse);

        ClientResponse response = this.requestHandler.send(request);
        Assert.assertTrue(response instanceof DiscoverResponse);
        Assert.assertArrayEquals(coreLinkPayload.getBytes(), response.getContent());
    }

    @Test
    public void testSendDeleteRequestSucceeds() throws Exception {

        DeleteRequest request = DeleteRequest.newRequest(this.client, 10, 1);
        ifTheClientReturns(new Response(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.DELETED));

        ClientResponse response = this.requestHandler.send(request);
        Assert.assertEquals(ResponseCode.DELETED, response.getCode());
    }

    @Test
    public void testSendWriteAttributesRequestReturnsChangedResponse() throws Exception {

        ObserveSpec spec = new ObserveSpec.Builder().maxPeriod(20).minPeriod(10).build();
        WriteAttributesRequest request = WriteAttributesRequest.newRequest(this.client, OBJECT_ID_DEVICE, spec);
        ifTheClientReturns(new Response(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CHANGED));

        ClientResponse response = this.requestHandler.send(request);
        Assert.assertNull(response.getContent());
    }

    @Test
    public void testSendObserveRequestReturnsObserveResponseWithObservationId() throws Exception {

        ResourceObserver observer = mock(ResourceObserver.class);
        ObserveRequest request = ObserveRequest.newRequest(this.client, observer, OBJECT_ID_DEVICE);
        Response successfulResponse = newResponse(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT,
                TEXT_PAYLOAD);
        successfulResponse.getOptions().addOption(new Option(OptionNumberRegistry.OBSERVE));

        ifTheClientReturns(successfulResponse);

        ClientResponse response = this.requestHandler.send(request);
        Assert.assertTrue(response instanceof ObserveResponse);
        ObserveResponse observeResponse = (ObserveResponse) response;
        Assert.assertNotNull(observeResponse.getObservationId());
        Assert.assertArrayEquals(TEXT_PAYLOAD.getBytes(), response.getContent());

        Observation observation = this.observationRegistry.getObservation(observeResponse.getObservationId());
        Assert.assertEquals(observer, observation.getResourceObserver());

    }

    @Test
    public void testCancelObservation() {
        ResourceObserver observer = mock(ResourceObserver.class);
        ObserveRequest request = ObserveRequest.newRequest(this.client, observer, OBJECT_ID_DEVICE);
        Response successfulResponse = newResponse(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT,
                TEXT_PAYLOAD);
        successfulResponse.getOptions().addOption(new Option(OptionNumberRegistry.OBSERVE));

        ifTheClientReturns(successfulResponse);

        ClientResponse response = this.requestHandler.send(request);
        Assert.assertTrue(response instanceof ObserveResponse);
        ObserveResponse observeResponse = (ObserveResponse) response;
        Assert.assertNotNull(observeResponse.getObservationId());
        this.observationRegistry.cancelObservation(observeResponse.getObservationId());
        Assert.assertNull(this.observationRegistry.getObservation(observeResponse.getObservationId()));
    }

    @Test
    public void testSendWriteRequestReturnsChangedResponse() throws Exception {
        WriteRequest request = WriteRequest.newUpdateRequest(this.client, 15, 3, 1, "TEST", ContentFormat.TEXT);
        ifTheClientReturns(newResponse(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CHANGED, null));

        ClientResponse response = this.requestHandler.send(request);
        Assert.assertEquals(ResponseCode.CHANGED, response.getCode());
    }

    @Test
    public void testSendCreateRequestReturnsCreatedResponse() throws Exception {
        CreateRequest request = CreateRequest.newRequest(this.client, 15, "TEST");
        ifTheClientReturns(newResponse(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CREATED, null));

        ClientResponse response = this.requestHandler.send(request);
        Assert.assertEquals(ResponseCode.CREATED, response.getCode());
    }

    @Test
    public void testSendRequestReturnsNotAuthorized() throws Exception {
        ReadRequest request = ReadRequest.newRequest(this.client, OBJECT_ID_DEVICE);
        ifTheClientReturns(new Response(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.UNAUTHORIZED));

        ClientResponse response = this.requestHandler.send(request);
        Assert.assertEquals(ResponseCode.UNAUTHORIZED, response.getCode());
    }

    @Test
    public void testSendRequestReturnsNotFound() throws Exception {
        ReadRequest request = ReadRequest.newRequest(this.client, OBJECT_ID_DEVICE);
        ifTheClientReturns(new Response(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.NOT_FOUND));

        ClientResponse response = this.requestHandler.send(request);
        Assert.assertEquals(ResponseCode.NOT_FOUND, response.getCode());
    }

    @Test
    public void testCreateRequestReturnsMethodNotAllowed() throws Exception {
        CreateRequest request = CreateRequest.newRequest(this.client, OBJECT_ID_DEVICE, "TEST");
        ifTheClientReturns(new Response(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.METHOD_NOT_ALLOWED));

        ClientResponse response = this.requestHandler.send(request);
        Assert.assertEquals(ResponseCode.METHOD_NOT_ALLOWED, response.getCode());
    }

    private void ifTheClientReturns(Response coapResponse) {
        when(this.coapEndpoint.send(any(Request.class), any(OperationType.class))).thenReturn(coapResponse);
    }

    private Response newResponse(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode responseCode, String payload) {
        Response response = new Response(responseCode);
        if (payload != null) {
            response.getOptions().setContentFormat(ContentFormat.TEXT.getCode());
            response.setPayload(payload);
        }
        return response;
    }
}
