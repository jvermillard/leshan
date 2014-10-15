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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import leshan.server.lwm2m.client.LinkObject;
import leshan.server.lwm2m.impl.BasicTestSupport;
import leshan.server.lwm2m.impl.ObservationRegistryImpl;
import leshan.server.lwm2m.node.LwM2mObjectInstance;
import leshan.server.lwm2m.node.LwM2mResource;
import leshan.server.lwm2m.node.Value;
import leshan.server.lwm2m.observation.ObserveSpec;
import leshan.server.lwm2m.request.ClientResponse;
import leshan.server.lwm2m.request.CoapResponseCode.ResponseCode;
import leshan.server.lwm2m.request.ContentFormat;
import leshan.server.lwm2m.request.CreateRequest;
import leshan.server.lwm2m.request.DeleteRequest;
import leshan.server.lwm2m.request.DiscoverRequest;
import leshan.server.lwm2m.request.DiscoverResponse;
import leshan.server.lwm2m.request.LwM2mRequestVisitor;
import leshan.server.lwm2m.request.ReadRequest;
import leshan.server.lwm2m.request.RequestTimeoutException;
import leshan.server.lwm2m.request.ValueResponse;
import leshan.server.lwm2m.request.WriteAttributesRequest;
import leshan.server.lwm2m.request.WriteRequest;

import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.Endpoint;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class CaliforniumLwM2mRequestServerTest extends BasicTestSupport {

    private static final Integer OBJECT_ID_DEVICE = 3;

    private static final String TEXT_PAYLOAD = "payload";

    Endpoint coapEndpoint;
    CaliforniumLwM2mRequestSender requestSender;
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
        requestSender = new CaliforniumLwM2mRequestSender(endpointSet, observationRegistry, 100);
    }

    @Test
    public void testSendReadRequestReturnsContentResponse() throws Exception {

        ReadRequest request = new ReadRequest(client, OBJECT_ID_DEVICE, 0, 1);
        ifTheClientReturns(newTextContentResponse(org.eclipse.californium.core.coap.CoAP.ResponseCode.CONTENT,
                TEXT_PAYLOAD));

        ValueResponse response = requestSender.send(request);
        assertTrue(response.getCode() == ResponseCode.CONTENT);
        LwM2mResource content = (LwM2mResource) response.getContent();
        assertEquals(TEXT_PAYLOAD, content.getValue().value);
    }

    @Test
    public void testSendDiscoverRequestReturnsDiscoverResponse() throws Exception {

        String coreLinkPayload = "/3/0/1;pmin=10";
        DiscoverRequest request = new DiscoverRequest(client, OBJECT_ID_DEVICE);
        Response coapResponse = new Response(org.eclipse.californium.core.coap.CoAP.ResponseCode.CONTENT);
        coapResponse.setPayload(coreLinkPayload, MediaTypeRegistry.APPLICATION_LINK_FORMAT);
        ifTheClientReturns(coapResponse);

        DiscoverResponse response = requestSender.send(request);
        assertTrue(response.getCode() == ResponseCode.CONTENT);
        assertEquals(1, response.getObjectLinks().length);
        LinkObject link = response.getObjectLinks()[0];
        assertEquals("/3/0/1", link.getUrl());
    }
    
    @Test
    public void testSendDeleteRequestSucceeds() throws Exception {

        DeleteRequest request = new DeleteRequest(client, 10, 1);
        ifTheClientReturns(new Response(org.eclipse.californium.core.coap.CoAP.ResponseCode.DELETED));

        ClientResponse response = requestSender.send(request);
        assertEquals(ResponseCode.DELETED, response.getCode());
    }

    @Test
    public void testSendWriteAttributesRequestReturnsChangedResponse() throws Exception {

        ObserveSpec spec = new ObserveSpec.Builder().maxPeriod(20).minPeriod(10).build();
        WriteAttributesRequest request = new WriteAttributesRequest(client, OBJECT_ID_DEVICE, spec);
        ifTheClientReturns(new Response(org.eclipse.californium.core.coap.CoAP.ResponseCode.CHANGED));

        ClientResponse response = requestSender.send(request);
        assertEquals(ResponseCode.CHANGED, response.getCode());
    }

    @Test
    public void testSendWriteRequestReturnsChangedResponse() throws Exception {
        WriteRequest request = new WriteRequest(client, "/15/3/1", new LwM2mResource(1, Value.newStringValue("TEST")),
                ContentFormat.TEXT, true);
        ifTheClientReturns(newTextContentResponse(org.eclipse.californium.core.coap.CoAP.ResponseCode.CHANGED, null));

        ClientResponse response = requestSender.send(request);
        assertEquals(ResponseCode.CHANGED, response.getCode());
    }

    @Test
    public void testSendCreateRequestReturnsCreatedResponse() throws Exception {
        CreateRequest request = new CreateRequest(client, 15, mock(LwM2mObjectInstance.class), ContentFormat.TLV);
        ifTheClientReturns(newTextContentResponse(org.eclipse.californium.core.coap.CoAP.ResponseCode.CREATED, null));

        ClientResponse response = requestSender.send(request);
        assertEquals(ResponseCode.CREATED, response.getCode());
    }

    @Test
    public void testSendRequestReturnsNotAuthorized() throws Exception {
        ReadRequest request = new ReadRequest(client, OBJECT_ID_DEVICE);
        ifTheClientReturns(new Response(org.eclipse.californium.core.coap.CoAP.ResponseCode.UNAUTHORIZED));

        ClientResponse response = requestSender.send(request);
        assertEquals(ResponseCode.UNAUTHORIZED, response.getCode());
    }

    @Test
    public void testSendRequestReturnsNotFound() throws Exception {
        ReadRequest request = new ReadRequest(client, OBJECT_ID_DEVICE);
        ifTheClientReturns(new Response(org.eclipse.californium.core.coap.CoAP.ResponseCode.NOT_FOUND));

        ClientResponse response = requestSender.send(request);
        assertEquals(ResponseCode.NOT_FOUND, response.getCode());
    }

    @Test
    public void testCreateRequestReturnsMethodNotAllowed() throws Exception {
        CreateRequest request = new CreateRequest(client, OBJECT_ID_DEVICE, mock(LwM2mObjectInstance.class),
                ContentFormat.TLV);
        ifTheClientReturns(new Response(org.eclipse.californium.core.coap.CoAP.ResponseCode.METHOD_NOT_ALLOWED));

        ClientResponse response = requestSender.send(request);
        assertEquals(ResponseCode.METHOD_NOT_ALLOWED, response.getCode());
    }

    @Test(expected = RequestTimeoutException.class)
    public void testSendRequestThrowsRequestTimeoutException() throws Exception {
        ReadRequest request = new ReadRequest(client, OBJECT_ID_DEVICE);
        requestSender.send(request);
        fail("Request should have timed out with exception");
    }

    @Test
    public void try_send_but_generate_unexpected_exception_in_the_response_visitor() {
        DeleteRequest rq = new DeleteRequest(client, "/1/0/4") {
            @Override
            public void accept(LwM2mRequestVisitor visitor) {
               if (visitor instanceof CaliforniumLwM2mResponseBuilder) {
                     throw new RuntimeException("meh");
               } else {
                    super.accept(visitor);
               }
            }
        };
        ifTheClientReturns(new Response(org.eclipse.californium.core.coap.CoAP.ResponseCode.DELETED));
        
        try {
            requestSender.send(rq);
            fail("no exception reported");
        } catch (RuntimeException e) {
            assertEquals("meh", e.getMessage());
        }
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

    private Response newTextContentResponse(org.eclipse.californium.core.coap.CoAP.ResponseCode responseCode,
            String payload) {
        Response response = new Response(responseCode);
        if (payload != null) {
            response.getOptions().setContentFormat(ContentFormat.TEXT.getCode());
            response.setPayload(payload);
        }
        return response;
    }
}