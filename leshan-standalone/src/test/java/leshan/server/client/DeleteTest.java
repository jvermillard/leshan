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

import static leshan.server.client.IntegrationTestHelper.*;
import static leshan.server.lwm2m.request.ResponseCode.*;
import leshan.server.lwm2m.node.LwM2mObject;
import leshan.server.lwm2m.node.LwM2mObjectInstance;
import leshan.server.lwm2m.request.ClientResponse;

import org.junit.After;
import org.junit.Test;

public class DeleteTest {

    private IntegrationTestHelper helper = new IntegrationTestHelper();

    @After
    public void stop() {
        helper.stop();
    }

    @Test
    public void delete_created_object_instance() {
        helper.register();

        createAndThenAssertDeleted();
    }

    @Test
    public void delete_and_cant_read_object_instance() {
        helper.register();

        createAndThenAssertDeleted();

        assertEmptyResponse(helper.sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), NOT_FOUND);
    }

    @Test
    public void delete_and_read_object_is_empty() {
        helper.register();

        createAndThenAssertDeleted();

        assertResponse(helper.sendRead(GOOD_OBJECT_ID), CONTENT, new LwM2mObject(GOOD_OBJECT_ID,
                new LwM2mObjectInstance[0]));
    }

    @Test
    public void cannot_delete_unknown_object_instance() {
        helper.register();

        final ClientResponse responseDelete = helper.sendDelete(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID);
        assertEmptyResponse(responseDelete, NOT_FOUND);
    }

    private void createAndThenAssertDeleted() {
        helper.sendCreate(createGoodObjectInstance("hello", "goodbye"), GOOD_OBJECT_ID);

        final ClientResponse responseDelete = helper.sendDelete(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID);
        assertEmptyResponse(responseDelete, DELETED);
    }

}
