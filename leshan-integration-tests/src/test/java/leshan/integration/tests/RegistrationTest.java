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
import static leshan.integration.tests.IntegrationTestHelper.*;
import static org.junit.Assert.*;
import leshan.client.LwM2mClient;
import leshan.client.register.RegisterUplink;
import leshan.client.resource.LwM2mClientObjectDefinition;
import leshan.client.response.OperationResponse;
import leshan.client.util.ResponseCallback;

import org.junit.After;
import org.junit.Test;

public class RegistrationTest {

    private IntegrationTestHelper helper = new IntegrationTestHelper();

    @After
    public void stop() {
        helper.stop();
    }

    @Test
    public void registered_device_exists() {
        final RegisterUplink registerUplink = helper.registerAndGetUplink();
        final OperationResponse register = registerUplink.register(ENDPOINT, helper.clientParameters, TIMEOUT_MS);

        assertTrue(register.isSuccess());
        assertNotNull(helper.getClient());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fail_to_create_client_with_null() {
        helper.client = new LwM2mClient((LwM2mClientObjectDefinition[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fail_to_create_client_with_same_object_twice() {
        final LwM2mClientObjectDefinition objectOne = new LwM2mClientObjectDefinition(1, false, false);
        helper.client = new LwM2mClient(objectOne, objectOne);
    }

    @Test
    public void registered_device_exists_async() {
        final RegisterUplink registerUplink = helper.registerAndGetUplink();
        final ResponseCallback callback = new ResponseCallback();
        registerUplink.register(ENDPOINT, helper.clientParameters, callback);

        await().untilTrue(callback.isCalled());

        assertTrue(callback.isSuccess());
        assertNotNull(helper.getClient());
    }

}
