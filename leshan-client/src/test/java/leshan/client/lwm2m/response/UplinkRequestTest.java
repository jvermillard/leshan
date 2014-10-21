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
package leshan.client.lwm2m.response;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import leshan.client.lwm2m.LwM2mClient;
import leshan.client.lwm2m.register.RegisterUplink;
import leshan.server.lwm2m.client.LinkObject;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Request.class)
public class UplinkRequestTest {
    private static final String LOCATION = "/LOCATION";
    private static final int SYNC_TIMEOUT_MS = 2000;
    private static final String SERVER_HOST = "leshan.com";
    private static final int SERVER_PORT = 1234;
    private static final String ENDPOINT_NAME = UUID.randomUUID().toString();
    private final String VALID_REQUEST_PAYLOAD = "</lwm2m>;rt=\"oma.lwm2m\", </lwm2m/1/101>, </lwm2m/1/102>, </lwm2m/2/0>, </lwm2m/2/1>, </lwm2m/2/2>, </lwm2m/3/0>, </lwm2m/4/0>, </lwm2m/5>";

    @Mock
    private CoAPEndpoint endpoint;
    @Mock
    private Request request;
    @Mock
    private Response response;
    @Mock
    private LwM2mClient client;
    private RegisterUplink uplink;
    private InetSocketAddress serverAddress;

    @Before
    public void setUp() {
        serverAddress = InetSocketAddress.createUnresolved(SERVER_HOST, SERVER_PORT);

        PowerMockito.mockStatic(Request.class);
        when(Request.newGet()).thenReturn(request);
        when(Request.newPost()).thenReturn(request);
        when(Request.newPut()).thenReturn(request);
        when(Request.newDelete()).thenReturn(request);

        when(client.getObjectModel()).thenReturn(LinkObject.parse(VALID_REQUEST_PAYLOAD.getBytes()));

        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                final Request request = (Request) invocation.getArguments()[0];

                final Response response = new Response(ResponseCode.VALID);
                response.getOptions().setLocationPath(LOCATION.substring(1));

                request.setResponse(response);

                return null;
            }
        }).when(endpoint).sendRequest(any(Request.class));

        uplink = new RegisterUplink(serverAddress, endpoint, client);
    }

    @Test
    public void testGoodResponse() throws InterruptedException {
        final Map<String, String> parameters = new HashMap<>();

        when(request.waitForResponse(any(Long.class))).thenReturn(response);
        when(response.getCode()).thenReturn(ResponseCode.VALID);

        final OperationResponse operationResponse = uplink.register(ENDPOINT_NAME, parameters, SYNC_TIMEOUT_MS);

        assertTrue(operationResponse.isSuccess());
    }

    @Test
    public void testNullTimeoutResponse() throws InterruptedException {
        final Map<String, String> parameters = new HashMap<>();

        when(request.waitForResponse(any(Long.class))).thenReturn(null);

        final OperationResponse operationResponse = uplink.register(ENDPOINT_NAME, parameters, SYNC_TIMEOUT_MS);

        assertFalse(operationResponse.isSuccess());
        assertEquals(operationResponse.getResponseCode(), ResponseCode.GATEWAY_TIMEOUT);
    }

}
