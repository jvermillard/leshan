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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import leshan.client.lwm2m.LwM2mClient;
import leshan.client.lwm2m.bootstrap.BootstrapMessageDeliverer.InterfaceTypes;
import leshan.client.lwm2m.bootstrap.BootstrapMessageDeliverer.OperationTypes;
import leshan.client.lwm2m.response.OperationResponse;
import leshan.client.lwm2m.util.ResponseCallback;
import leshan.server.lwm2m.client.LinkObject;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class UpdateUplinkTest {
	private static final int SYNC_TIMEOUT_MS = 2000;
	private static final String SERVER_HOST = "leshan.com";
	private static final int SERVER_PORT = 1234;

	private static final String ENDPOINT_LOCATION = UUID.randomUUID().toString();
	private final String VALID_REQUEST_PAYLOAD = "</lwm2m>;rt=\"oma.lwm2m\", </lwm2m/1/101>, </lwm2m/1/102>, </lwm2m/2/0>, </lwm2m/2/1>, </lwm2m/2/2>, </lwm2m/3/0>, </lwm2m/4/0>, </lwm2m/5>";

	@Mock
	private CoAPEndpoint endpoint;
	@Mock
	private LwM2mClient client;

	private String actualRequestLocation;
	private String expectedRequestLocation;

	private String actualRequestPayload;
	private ResponseCallback callback;
	private InetSocketAddress serverAddress;
	private int tearDownEndpointStops;
	private RegisterUplink uplink;


	@Before
	public void setUp(){
		actualRequestLocation = null;
		expectedRequestLocation = null;
		callback = new ResponseCallback();
		serverAddress = InetSocketAddress.createUnresolved(SERVER_HOST, SERVER_PORT);
	}

	@After
	public void tearDown(){
		uplink.stop();

		verify(endpoint, times(tearDownEndpointStops)).stop();
	}


	public void initializeServerResponse(final InterfaceTypes interfaceType, final OperationTypes operationType, final ResponseCode responseCode, final String objectsAndInstances){
		tearDownEndpointStops = 1;

		if(objectsAndInstances != null){
			Mockito.when(client.getObjectModel()).thenReturn(LinkObject.parse(objectsAndInstances.getBytes()));
		}

		doAnswer(new Answer<Void>() {

			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				final Request request = (Request) invocation.getArguments()[0];
				actualRequestLocation = request.getURI();
				actualRequestPayload = request.getPayloadString();

				final Response response = new Response(responseCode);

				request.setResponse(response);

				return null;
			}
		}).when(endpoint).sendRequest(any(Request.class));

		uplink = new RegisterUplink(serverAddress, endpoint, client);
	}

	private void verifySuccessfulSyncUpdate(final String expectedRequest, final String validQuery,
			final String expectedRequestPayload, final OperationResponse response, final ResponseCode responseCode) {
		assertTrue(response.isSuccess());
		assertEquals(response.getResponseCode(), responseCode);
		assertEquals(expectedRequestLocation, actualRequestLocation);
		assertEquals(expectedRequestPayload, actualRequestPayload);
		verify(endpoint).sendRequest(any(Request.class));
	}

	private void verifySuccessfulAsyncUpdate(final String expectedRequest, final String validQuery,
			final String expectedRequestPayload, final ResponseCode responseCode) {
		assertTrue(callback.isSuccess());
		assertEquals(callback.getResponseCode(), responseCode);
		assertEquals(expectedRequestLocation, actualRequestLocation);
		assertEquals(expectedRequestPayload, actualRequestPayload);
		verify(endpoint).sendRequest(any(Request.class));
	}

	private void verifyUnsuccessfulUpdate(final String expectedRequest, final String validQuery,
			final OperationResponse response, final ResponseCode responseCode) {
		verify(endpoint, never()).sendRequest(any(Request.class));
		assertFalse(response.isSuccess());
		assertEquals(response.getResponseCode(), responseCode);
	}

	private Map<String, String> generateValidParameters() {
		final Map<String, String> validMap = new HashMap<String, String>();
		validMap.put("lt", "1000000");
		validMap.put("lwm2m", "1.1");
		validMap.put("b", "U");
		return validMap;
	}

	@Test
	public void testGoodSyncUpdate() {
		final Map<String, String> validMap = generateValidParameters();
		final String validQuery = leshan.client.lwm2m.request.Request.toQueryStringMap(validMap);

		expectedRequestLocation ="coap://localhost/?" + validQuery;

		initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.UPDATE, ResponseCode.CHANGED, VALID_REQUEST_PAYLOAD);

		final OperationResponse response = uplink.update(ENDPOINT_LOCATION, validMap, SYNC_TIMEOUT_MS);

		verifySuccessfulSyncUpdate(expectedRequestLocation, validQuery, VALID_REQUEST_PAYLOAD, response, ResponseCode.CHANGED);
	}

	@Test
	public void testGoodSyncWithPayloadUpdate() {
		final Map<String, String> validMap = generateValidParameters();
		final String validQuery = leshan.client.lwm2m.request.Request.toQueryStringMap(validMap);

		expectedRequestLocation ="coap://localhost/?" + validQuery;

		initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.UPDATE, ResponseCode.CHANGED, VALID_REQUEST_PAYLOAD);

		final OperationResponse response = uplink.update(ENDPOINT_LOCATION, validMap, SYNC_TIMEOUT_MS);

		verifySuccessfulSyncUpdate(expectedRequestLocation, validQuery, VALID_REQUEST_PAYLOAD, response, ResponseCode.CHANGED);
	}

	@Test
	public void testBadParametersSyncUpdate() {
		initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.UPDATE, ResponseCode.BAD_REQUEST, VALID_REQUEST_PAYLOAD);

		final Map<String, String> invalidSmsMap = new HashMap<String, String>();
		invalidSmsMap.put("sms", UUID.randomUUID().toString());
		final String validQuery = leshan.client.lwm2m.request.Request.toQueryStringMap(invalidSmsMap);

		final OperationResponse response = uplink.update(ENDPOINT_LOCATION, invalidSmsMap, SYNC_TIMEOUT_MS);

		verifyUnsuccessfulUpdate(expectedRequestLocation, validQuery, response, ResponseCode.BAD_REQUEST);
	}

	@Test
	public void testNoParametersSyncUpdate() {
		initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.UPDATE, ResponseCode.BAD_REQUEST, VALID_REQUEST_PAYLOAD);

		final Map<String, String> emptyMap = new HashMap<String, String>();
		final String validQuery = leshan.client.lwm2m.request.Request.toQueryStringMap(emptyMap);

		final OperationResponse response = uplink.update(ENDPOINT_LOCATION, emptyMap, SYNC_TIMEOUT_MS);

		verifyUnsuccessfulUpdate(expectedRequestLocation, validQuery, response, ResponseCode.BAD_REQUEST);
	}

	@Test
	public void testGoodAsyncWithPayloadUpdate() {
		final Map<String, String> validMap = generateValidParameters();
		final String validQuery = leshan.client.lwm2m.request.Request.toQueryStringMap(validMap);

		expectedRequestLocation ="coap://localhost/?" + validQuery;

		initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.UPDATE, ResponseCode.CHANGED, VALID_REQUEST_PAYLOAD);


		uplink.update(ENDPOINT_LOCATION, validMap, callback);

		await().untilTrue(callback.isCalled());

		verifySuccessfulAsyncUpdate(expectedRequestLocation, validQuery, VALID_REQUEST_PAYLOAD, ResponseCode.CHANGED);
	}

}
