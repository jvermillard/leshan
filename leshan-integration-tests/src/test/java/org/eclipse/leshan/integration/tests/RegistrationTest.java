/*******************************************************************************
 * Copyright (c) 2013-2015 Sierra Wireless and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *     Zebra Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.leshan.integration.tests;

import static com.jayway.awaitility.Awaitility.await;
import static org.eclipse.leshan.integration.tests.IntegrationTestHelper.ENDPOINT_IDENTIFIER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.leshan.ResponseCode;
import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.resource.LwM2mClientObjectDefinition;
import org.eclipse.leshan.client.util.ResponseCallback;
import org.eclipse.leshan.core.request.RegisterRequest;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.core.response.RegisterResponse;
import org.eclipse.leshan.server.client.Client;
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
        final RegisterResponse register = helper.register();

        assertTrue(register.getCode() == ResponseCode.CREATED);
        assertNotNull(helper.getClient());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fail_to_create_client_with_null() {
        helper.client = new LeshanClient(helper.clientAddress, helper.serverAddress,
                (LwM2mClientObjectDefinition[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fail_to_create_client_with_same_object_twice() {
        final LwM2mClientObjectDefinition objectOne = new LwM2mClientObjectDefinition(1, false, false);
        helper.client = new LeshanClient(helper.clientAddress, helper.serverAddress, objectOne, objectOne);
    }

    @Test
    public void registered_device_exists_async() {
        final ResponseCallback<RegisterResponse> callback = registerDeviceAsynch();

        assertTrue(callback.getResponseCode() == ResponseCode.CREATED);
        assertNotNull(helper.getClient());
    }

    @Test(expected = RuntimeException.class)
    public void wont_send_synchronous_if_not_started() {
        final RegisterRequest registerRequest = new RegisterRequest(ENDPOINT_IDENTIFIER);

        final RegisterResponse response = helper.client.send(registerRequest);

        assertFalse(response.getCode() != ResponseCode.CREATED);
    }

    @Test(expected = RuntimeException.class)
    public void wont_send_asynchronous_if_not_started() {
        final RegisterRequest registerRequest = new RegisterRequest(ENDPOINT_IDENTIFIER);

        final ResponseCallback<RegisterResponse> callback = new ResponseCallback<RegisterResponse>();
        helper.client.send(registerRequest, callback, callback);

        assertTrue(callback.isCalled().get());
        assertFalse(callback.getResponseCode() == ResponseCode.CREATED);
    }

    @Test
    public void registered_device_updated() {
        final RegisterResponse register = helper.register();

        final Long updatedLifetime = 1337l;
        final LwM2mResponse update = helper.update(register.getRegistrationID(), updatedLifetime);
        final Client client = helper.getClient();

        assertTrue(update.getCode() == ResponseCode.CHANGED);
        assertEquals(updatedLifetime, client.getLifeTimeInSec());
        assertNotNull(client);
    }

    @Test
    public void deregister_registered_device_then_reregister_async() {
        ResponseCallback<RegisterResponse> registerCallback = registerDeviceAsynch();

        final String registrationId = registerCallback.getResponse().getRegistrationID();

        final ResponseCallback<LwM2mResponse> deregisterCallback = new ResponseCallback<LwM2mResponse>();

        helper.deregister(registrationId, deregisterCallback);

        await().untilTrue(deregisterCallback.isCalled());

        assertTrue(deregisterCallback.getResponseCode() == ResponseCode.DELETED);
        assertNull(helper.getClient());

        registerCallback = registerDeviceAsynch();

        assertTrue(registerCallback.getResponseCode() == ResponseCode.CREATED);
        assertNotNull(helper.getClient());
    }

    private ResponseCallback<RegisterResponse> registerDeviceAsynch() {
        final ResponseCallback<RegisterResponse> registerCallback = new ResponseCallback<RegisterResponse>();

        helper.register(registerCallback);

        await().untilTrue(registerCallback.isCalled());

        return registerCallback;
    }

}
