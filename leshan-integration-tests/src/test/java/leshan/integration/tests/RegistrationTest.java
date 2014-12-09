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

package leshan.integration.tests;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.*;

import java.util.Collections;

import leshan.client.californium.LeshanClient;
import leshan.client.request.RegisterRequest;
import leshan.client.resource.LwM2mClientObjectDefinition;
import leshan.client.response.OperationResponse;
import leshan.client.server.Server;
import leshan.client.util.ResponseCallback;

import org.junit.After;
import org.junit.Test;

public class RegistrationTest {

	private final IntegrationTestHelper helper = new IntegrationTestHelper();

	@After
	public void stop() {
		helper.stop();
	}

	@Test
	public void registered_device_exists() {
		final OperationResponse register = helper.register();

		assertTrue(register.isSuccess());
		assertNotNull(helper.getClient());
	}

	@Test(expected = IllegalArgumentException.class)
	public void fail_to_create_client_with_null() {
		helper.client = new LeshanClient(Collections.singleton(helper.clientAddress), Collections.singleton(helper.serverAddress),
				(LwM2mClientObjectDefinition[]) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void fail_to_create_client_with_same_object_twice() {
		final LwM2mClientObjectDefinition objectOne = new LwM2mClientObjectDefinition(1, false, false);
		helper.client = new LeshanClient(Collections.singleton(helper.clientAddress), Collections.singleton(helper.serverAddress),
				objectOne, objectOne);
	}

	@Test
	public void registered_device_exists_async() {
		final ResponseCallback callback = new ResponseCallback();

		helper.register(callback);

		await().untilTrue(callback.isCalled());

		assertTrue(callback.isSuccess());
		assertNotNull(helper.getClient());
	}

	@Test
	public void wont_send_synchronous_if_not_started(){
		final RegisterRequest registerRequest = 
				new RegisterRequest(new Server(helper.ENDPOINT, helper.CLIENT_PORT, 
						helper.clientParameters, helper.serverAddress, helper.TIMEOUT_MS, helper.clientAddress));

		final OperationResponse response = helper.client.send(registerRequest);

		assertFalse(response.isSuccess());
	}

	@Test
	public void wont_send_asynchronous_if_not_started(){
		final RegisterRequest registerRequest = 
				new RegisterRequest(new Server(helper.ENDPOINT, helper.CLIENT_PORT, 
						helper.clientParameters, helper.serverAddress, helper.TIMEOUT_MS, helper.clientAddress));

		final ResponseCallback callback = new ResponseCallback();
		helper.client.send(registerRequest, callback);

		assertTrue(callback.isCalled().get());
		assertFalse(callback.isSuccess());
	}

	@Test
	public void deregister_registered_device_async(){
		final ResponseCallback registerCallback = new ResponseCallback();

		helper.register(registerCallback);

		await().untilTrue(registerCallback.isCalled());
		
		assertNotNull(helper.getClient());
		
		final String clientLocation = registerCallback.getResponse().getLocation();
		
		final ResponseCallback deregisterCallback = new ResponseCallback();

		helper.deregister(clientLocation, deregisterCallback);

		await().untilTrue(deregisterCallback.isCalled());

		assertTrue(deregisterCallback.isSuccess());
		assertNull(helper.getClient());
	}

}
