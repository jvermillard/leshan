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
package leshan.client.lwm2m.bootstrap;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

import java.net.InetSocketAddress;
import java.util.UUID;

import leshan.client.lwm2m.bootstrap.BootstrapMessageDeliverer.InterfaceTypes;
import leshan.client.lwm2m.bootstrap.BootstrapMessageDeliverer.OperationTypes;
import leshan.client.lwm2m.response.OperationResponse;
import leshan.client.lwm2m.util.ResponseCallback;

import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class BootstrapUplinkTest {
    private static final int SYNC_TIMEOUT_MS = 2000;
    private static final String ENDPOINT_NAME = UUID.randomUUID().toString();
    private byte[] actualPayload;
    private String actualRequest;
    private Code actualCode;
    private ResponseCallback callback;

    @Mock
    private CoAPEndpoint endpoint;
    @Mock
    private BootstrapDownlink downlink;
    private String expectedRequest;
    private InetSocketAddress serverAddress;

    @Before
    public void setUp() {
        callback = new ResponseCallback();
        expectedRequest = "coap://localhost/bs?ep=" + ENDPOINT_NAME;
        serverAddress = InetSocketAddress.createUnresolved("localhost", 1234);
    }

    private BootstrapUplink initializeServerResponse(final InterfaceTypes interfaceType,
            final OperationTypes operationType, final ResponseCode responseCode) {
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                final Request request = (Request) invocation.getArguments()[0];
                actualRequest = request.getURI();
                actualCode = request.getCode();

                final Response response = new Response(responseCode);

                request.setResponse(response);

                return null;
            }
        }).when(endpoint).sendRequest(any(Request.class));

        final BootstrapUplink uplink = new BootstrapUplink(serverAddress, endpoint, downlink);
        return uplink;
    }

    private void sendBootstrapAndGetAsyncResponse(final BootstrapUplink uplink) {
        uplink.bootstrap(ENDPOINT_NAME, callback);

        await().untilTrue(callback.isCalled());
        if (callback.isSuccess()) {
            actualPayload = callback.getResponsePayload();
        }
    }

    private void sendBootstrapAndGetSyncResponse(final BootstrapUplink uplink) {
        final OperationResponse operationResponse = uplink.bootstrap(ENDPOINT_NAME, SYNC_TIMEOUT_MS);
        if (operationResponse.isSuccess()) {
            actualPayload = operationResponse.getPayload();
        }
    }

    private void verifyResponse(final String expectedPayload) {
        assertEquals(expectedRequest, actualRequest);
        assertEquals(Code.POST, actualCode);
        if (expectedPayload != null) {
            assertArrayEquals(expectedPayload.getBytes(), actualPayload);
        } else {
            assertTrue(actualPayload == null);
        }
    }

    @Test
    public void testGoodAsyncRequestPayload() {
        final BootstrapUplink uplink = initializeServerResponse(InterfaceTypes.BOOTSTRAP, OperationTypes.REQUEST,
                ResponseCode.CHANGED);

        sendBootstrapAndGetAsyncResponse(uplink);
    }

    @Test
    public void testBadAsyncPayload() {
        final BootstrapUplink uplink = initializeServerResponse(InterfaceTypes.BOOTSTRAP, OperationTypes.REQUEST,
                ResponseCode.BAD_REQUEST);

        sendBootstrapAndGetAsyncResponse(uplink);

        verifyResponse(null);
    }

    @Test
    public void testGoodSyncPayload() {
        final BootstrapUplink uplink = initializeServerResponse(InterfaceTypes.BOOTSTRAP, OperationTypes.REQUEST,
                ResponseCode.CHANGED);

        sendBootstrapAndGetSyncResponse(uplink);
    }

    @Test
    public void testBadSyncPayload() {
        final BootstrapUplink uplink = initializeServerResponse(InterfaceTypes.BOOTSTRAP, OperationTypes.REQUEST,
                ResponseCode.BAD_REQUEST);

        sendBootstrapAndGetSyncResponse(uplink);

        verifyResponse(null);
    }
}
