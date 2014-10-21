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

package leshan.server.client;

import static leshan.server.client.IntegrationTestHelper.EXECUTABLE_RESOURCE_ID;
import static leshan.server.client.IntegrationTestHelper.GOOD_OBJECT_ID;
import static leshan.server.client.IntegrationTestHelper.GOOD_OBJECT_INSTANCE_ID;
import static leshan.server.client.IntegrationTestHelper.SECOND_RESOURCE_ID;
import leshan.server.lwm2m.request.ClientResponse;
import leshan.server.lwm2m.request.CoapResponseCode.ResponseCode;
import leshan.server.lwm2m.request.ContentFormat;
import leshan.server.lwm2m.request.ExecuteRequest;

import org.junit.After;
import org.junit.Test;

public class ExecuteTest {

    private IntegrationTestHelper helper = new IntegrationTestHelper();

    @After
    public void stop() {
        helper.stop();
    }

    @Test
    public void cannot_execute_write_only_resource() {
        helper.register();

        helper.sendCreate(IntegrationTestHelper.createGoodObjectInstance("hello", "goodbye"), GOOD_OBJECT_ID);

        final ClientResponse response = helper.server.send(new ExecuteRequest(helper.getClient(), GOOD_OBJECT_ID,
                GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID, "world".getBytes(), ContentFormat.TEXT));

        IntegrationTestHelper.assertEmptyResponse(response, ResponseCode.METHOD_NOT_ALLOWED);
    }

    @Test
    public void can_execute_resource() {
        helper.register();

        helper.sendCreate(IntegrationTestHelper.createGoodObjectInstance("hello", "goodbye"), GOOD_OBJECT_ID);

        final ClientResponse response = helper.server.send(new ExecuteRequest(helper.getClient(), GOOD_OBJECT_ID,
                GOOD_OBJECT_INSTANCE_ID, EXECUTABLE_RESOURCE_ID, "world".getBytes(), ContentFormat.TEXT));

        IntegrationTestHelper.assertEmptyResponse(response, ResponseCode.CHANGED);
    }

}
