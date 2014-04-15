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
package leshan.server.lwm2m.message;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import leshan.server.lwm2m.client.Client;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.Endpoint;

public class LwM2mClientOperationsTest {

    private static final String TEXT_PAYLOAD = "payload";
    private static final String URI = "coap://localhost:5000/1/0";

    Endpoint endpoint;
    LwM2mClientOperations clientOperations;
    LwM2mRequest request;
    Request coapRequest;
    Client client;
    InetAddress destination;
    int destinationPort = 5000;

    @Before
    public void setUp() throws Exception {
        this.destination = InetAddress.getLocalHost();
        this.endpoint = mock(Endpoint.class);
        this.clientOperations = new LwM2mClientOperations(this.endpoint);
        this.request = mock(LwM2mRequest.class);
        this.coapRequest = mock(Request.class);
        when(this.coapRequest.getURI()).thenReturn(URI);
    }

    @Test(expected = AuthorizationException.class)
    public void testSendRequestThrowsAuthorizationException() throws Exception {
        testSendRequestHandlesResponseCode(new Response(ResponseCode.UNAUTHORIZED), OperationType.W);
    }

    @Test
    public void testSendRequestThrowsResourceNotFoundException() throws Exception {
        try {
            testSendRequestHandlesResponseCode(new Response(ResponseCode.NOT_FOUND), OperationType.R);
            Assert.fail("should have thrown exception");
        } catch (ResourceNotFoundException e) {
            Assert.assertEquals(URI, e.getUri());
        }
    }

    @Test
    public void testSendRequestThrowsOperationNotSupportedException() throws Exception {
        try {
            testSendRequestHandlesResponseCode(new Response(ResponseCode.METHOD_NOT_ALLOWED), OperationType.W);
            Assert.fail("should have thrown exception");
        } catch (OperationNotSupportedException e) {
            Assert.assertSame(OperationType.W, e.getOperation());
        }
    }

    @Test
    public void testSendRequestHandlesContentResponse() throws Exception {

        ClientResponse response = testSendRequestHandlesResponseCode(newResponse(ResponseCode.CONTENT, TEXT_PAYLOAD),
                OperationType.R);
        Assert.assertEquals(TEXT_PAYLOAD, new String(response.getContent()));
    }

    @Test
    public void testSendRequestHandlesChangedResponse() throws Exception {
        testSendRequestHandlesResponseCode(new Response(ResponseCode.CHANGED), OperationType.W);
    }

    @Test
    public void testSendRequestHandlesCreatedResponse() throws Exception {
        testSendRequestHandlesResponseCode(new Response(ResponseCode.CREATED), OperationType.W);
    }

    private ClientResponse testSendRequestHandlesResponseCode(Response coapResponse, OperationType opType)
            throws Exception {

        givenASimpleClient();
        ifTheClientReturns(coapResponse);

        ClientResponse response = this.clientOperations.sendRequest(this.coapRequest, opType);

        Assert.assertTrue(response instanceof ClientResponse);
        if (coapResponse.getPayload() != null) {
            Assert.assertArrayEquals(coapResponse.getPayload(), response.getContent());
            Assert.assertEquals(coapResponse.getOptions().getContentFormat(), response.getFormat().getCode());
        }
        Assert.assertEquals(leshan.server.lwm2m.message.ResponseCode.fromCoapCode(coapResponse.getCode()),
                response.getCode());
        return response;
    }

    @Test
    public void testSendRequestUsesEndpoint() throws Exception {

        Response coapResponse = new Response(ResponseCode.CREATED);
        when(this.coapRequest.waitForResponse(anyLong())).thenReturn(coapResponse);

        givenASimpleClient();
        ifTheClientReturns(coapResponse);

        this.clientOperations.sendRequest(this.coapRequest, OperationType.R);

        verify(this.endpoint).sendRequest(this.coapRequest);
        verify(this.coapRequest).waitForResponse(anyLong());
    }

    private void givenASimpleClient() throws UnknownHostException {
        this.client = new Client("ID", "urn:client", this.destination, this.destinationPort, "1.0", 10000L, null, null,
                null, new Date());
    }

    private void ifTheClientReturns(Response coapResponse) throws InterruptedException {
        when(this.coapRequest.waitForResponse(anyLong())).thenReturn(coapResponse);
    }

    private Response newResponse(ResponseCode responseCode, String payload) {
        Response response = new Response(responseCode);
        if (payload != null) {
            response.getOptions().setContentFormat(ContentFormat.TEXT.getCode());
            response.setPayload(payload);
        }
        return response;
    }

}
