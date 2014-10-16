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
package leshan.client.lwm2m.register;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.net.InetSocketAddress;
import java.util.UUID;

import leshan.client.lwm2m.LwM2mClient;
import leshan.client.lwm2m.bootstrap.BootstrapMessageDeliverer.InterfaceTypes;
import leshan.client.lwm2m.bootstrap.BootstrapMessageDeliverer.OperationTypes;
import leshan.client.lwm2m.response.OperationResponse;
import leshan.client.lwm2m.response.OperationResponseCode;
import leshan.client.lwm2m.util.ResponseCallback;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class DeregisterUplinkTest {
	private static final int SYNC_TIMEOUT_MS = 2000;
	private static final String SERVER_HOST = "leshan.com";
	private static final int SERVER_PORT = 1234;

	private static final String ENDPOINT_LOCATION = UUID.randomUUID().toString();

	@Mock
	private CoAPEndpoint endpoint;

	@Mock
	private LwM2mClient client;

	private String actualRequestLocation;

	private RegisterUplink uplink;

	private ResponseCallback callback;

	private InetSocketAddress serverAddress;
	private int tearDownEndpointStops;
	
	@Before
	public void setUp(){
		callback = new ResponseCallback();
		serverAddress = InetSocketAddress.createUnresolved(SERVER_HOST, SERVER_PORT);
		uplink = new RegisterUplink(serverAddress, endpoint, client);

		doAnswer(new Answer<Void>() {

			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				final Request request = (Request) invocation.getArguments()[0];
				actualRequestLocation = request.getOptions().getLocationPathString();
				
				final Response response = new Response(ResponseCode.DELETED);
				response.setPayload(OperationResponseCode.generateReasonPhrase(OperationResponseCode.valueOf(response.getCode().value), 
						InterfaceTypes.REGISTRATION, OperationTypes.DEREGISTER));

				request.setResponse(response);
				
				return null;
			}
		}).when(endpoint).sendRequest(any(Request.class));
	}
	
	@After
	public void tearDown(){
		uplink.stop();
		
		verify(endpoint, times(tearDownEndpointStops)).stop();
	}

	@Test
	public void testGoodSyncDeregister() {
		tearDownEndpointStops = 2;
		
		final OperationResponse response = uplink.deregister(ENDPOINT_LOCATION, SYNC_TIMEOUT_MS);
		
		
		verify(endpoint).stop();
		verify(endpoint).sendRequest(any(Request.class));
		
		assertTrue(response.isSuccess());
		assertEquals(ResponseCode.DELETED, response.getResponseCode());
		assertEquals(ENDPOINT_LOCATION, actualRequestLocation);
	}
	
	@Test
	public void testGoodAsyncDeregister() {
		tearDownEndpointStops = 2;
		
		uplink.deregister(ENDPOINT_LOCATION, callback);
		
		await().untilTrue(callback.isCalled());
		callback.getResponsePayload();

		verify(endpoint).stop();
		verify(endpoint).sendRequest(any(Request.class));
		
		assertTrue(callback.isSuccess());
		assertEquals(ResponseCode.DELETED, callback.getResponseCode());
		assertEquals(ENDPOINT_LOCATION, actualRequestLocation);
	}
	
	@Test
	public void testNullSyncDeregister() {
		tearDownEndpointStops = 1;
		
		final OperationResponse response = uplink.deregister(null, SYNC_TIMEOUT_MS);
		
		verify(endpoint, never()).stop();
		verify(endpoint, never()).sendRequest(any(Request.class));
		
		assertFalse(response.isSuccess());
		assertEquals(ResponseCode.NOT_FOUND, response.getResponseCode());
	}

}
