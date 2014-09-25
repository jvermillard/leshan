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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import leshan.server.lwm2m.client.Client;
import leshan.server.lwm2m.message.ClientResponse;
import leshan.server.lwm2m.message.ContentFormat;
import leshan.server.lwm2m.message.CreateRequest;
import leshan.server.lwm2m.message.DeleteRequest;
import leshan.server.lwm2m.message.DiscoverRequest;
import leshan.server.lwm2m.message.DiscoverResponse;
import leshan.server.lwm2m.message.ReadRequest;
import leshan.server.lwm2m.message.RequestTimeoutException;
import leshan.server.lwm2m.message.ResourceAccessException;
import leshan.server.lwm2m.message.ResponseCode;
import leshan.server.lwm2m.message.WriteAttributesRequest;
import leshan.server.lwm2m.message.WriteRequest;
import leshan.server.lwm2m.observation.ObservationRegistryImpl;
import leshan.server.lwm2m.observation.ObserveSpec;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import ch.ethz.inf.vs.californium.coap.MediaTypeRegistry;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.Endpoint;

public class CaliforniumBasedRequestHandlerTest extends BasicTestSupport {

    private static final Integer OBJECT_ID_DEVICE = 3;

    private static final String TEXT_PAYLOAD = "payload";

    Endpoint coapEndpoint;
    CaliforniumBasedRequestHandler requestHandler;
    ObservationRegistryImpl observationRegistry;

    @Before
    public void setUp() throws Exception {
        givenASimpleClient();
        destination = InetAddress.getLocalHost();
        coapEndpoint = mock(Endpoint.class);
        when(coapEndpoint.getAddress()).thenReturn(registrationAddress);
        Set<Endpoint> endpointSet = new HashSet<>();
        endpointSet.add(coapEndpoint);
        observationRegistry = new ObservationRegistryImpl();
        requestHandler = new CaliforniumBasedRequestHandler(endpointSet, observationRegistry);
    }

    @Test
    public void testSendReadRequestReturnsContentResponse() throws Exception {

        ReadRequest request = ReadRequest.newRequest(client, OBJECT_ID_DEVICE);
        ifTheClientReturns(newResponse(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT, TEXT_PAYLOAD));

        ClientResponse response = requestHandler.send(request);
        Assert.assertTrue(response.getCode() == ResponseCode.CONTENT);
        Assert.assertEquals(TEXT_PAYLOAD, new String(response.getContent()));
    }

    @Test
    public void testSendDiscoverRequestReturnsDiscoverResponse() throws Exception {

        String coreLinkPayload = "/3/0/1;pmin=10";
        DiscoverRequest request = DiscoverRequest.newRequest(client, OBJECT_ID_DEVICE);
        Response coapResponse = new Response(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT);
        coapResponse.setPayload(coreLinkPayload, MediaTypeRegistry.APPLICATION_XML);
        ifTheClientReturns(coapResponse);

        ClientResponse response = requestHandler.send(request);
        Assert.assertTrue(response instanceof DiscoverResponse);
        Assert.assertArrayEquals(coreLinkPayload.getBytes(), response.getContent());
    }

    @Test
    public void testSendDeleteRequestSucceeds() throws Exception {

        DeleteRequest request = DeleteRequest.newRequest(client, 10, 1);
        ifTheClientReturns(new Response(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.DELETED));

        ClientResponse response = requestHandler.send(request);
        Assert.assertEquals(ResponseCode.DELETED, response.getCode());
    }

    @Test
    public void testSendWriteAttributesRequestReturnsChangedResponse() throws Exception {

        ObserveSpec spec = new ObserveSpec.Builder().maxPeriod(20).minPeriod(10).build();
        WriteAttributesRequest request = WriteAttributesRequest.newRequest(client, OBJECT_ID_DEVICE, spec);
        ifTheClientReturns(new Response(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CHANGED));

        ClientResponse response = requestHandler.send(request);
        Assert.assertNull(response.getContent());
    }

    @Test
    public void testSendWriteRequestReturnsChangedResponse() throws Exception {
        WriteRequest request = WriteRequest.newUpdateRequest(client, 15, 3, 1, "TEST", ContentFormat.TEXT);
        ifTheClientReturns(newResponse(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CHANGED, null));

        ClientResponse response = requestHandler.send(request);
        Assert.assertEquals(ResponseCode.CHANGED, response.getCode());
    }

    @Test
    public void testSendCreateRequestReturnsCreatedResponse() throws Exception {
        CreateRequest request = CreateRequest.newRequest(client, 15, "TEST");
        ifTheClientReturns(newResponse(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CREATED, null));

        ClientResponse response = requestHandler.send(request);
        Assert.assertEquals(ResponseCode.CREATED, response.getCode());
    }

    @Test
    public void testSendRequestReturnsNotAuthorized() throws Exception {
        ReadRequest request = ReadRequest.newRequest(client, OBJECT_ID_DEVICE);
        ifTheClientReturns(new Response(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.UNAUTHORIZED));

        ClientResponse response = requestHandler.send(request);
        Assert.assertEquals(ResponseCode.UNAUTHORIZED, response.getCode());
    }

    @Test
    public void testSendRequestReturnsNotFound() throws Exception {
        ReadRequest request = ReadRequest.newRequest(client, OBJECT_ID_DEVICE);
        ifTheClientReturns(new Response(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.NOT_FOUND));

        ClientResponse response = requestHandler.send(request);
        Assert.assertEquals(ResponseCode.NOT_FOUND, response.getCode());
    }

    @Test
    public void testCreateRequestReturnsMethodNotAllowed() throws Exception {
        CreateRequest request = CreateRequest.newRequest(client, OBJECT_ID_DEVICE, "TEST");
        ifTheClientReturns(new Response(ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.METHOD_NOT_ALLOWED));

        ClientResponse response = requestHandler.send(request);
        Assert.assertEquals(ResponseCode.METHOD_NOT_ALLOWED, response.getCode());
    }

    @Test(expected = RequestTimeoutException.class)
    public void testSendRequestThrowsRequestTimeoutException() throws Exception {
        ReadRequest request = ReadRequest.newRequest(client, OBJECT_ID_DEVICE);
        requestHandler.send(request);
        Assert.fail("Request should have timed out with exception");
    }

    @Test(expected = ResourceAccessException.class)
    public void testGetEndpointForClientThrowsException() throws IOException {
        Client clientWithoutEndpoint = new Client("ID", "urn:client", InetAddress.getLocalHost(), 10000, "1.0", 10000L,
                null, null, null, new Date(), InetSocketAddress.createUnresolved("192.168.34.17", 10000));
        requestHandler.getEndpointForClient(clientWithoutEndpoint);
    }

    @Test
    public void testGetEndpointForClientFindsEndpoint() {
        Endpoint ep = requestHandler.getEndpointForClient(client);
        Assert.assertNotNull(ep);
    }

    private void ifTheClientReturns(final Response coapResponse) {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                if (args.length > 0 && args[0] instanceof Request) {
                    ((Request) args[0]).setResponse(coapResponse);
                }
                return null;
            }
        }).when(coapEndpoint).sendRequest(any(Request.class));
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
