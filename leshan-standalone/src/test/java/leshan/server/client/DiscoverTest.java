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

import static leshan.server.client.IntegrationTestHelper.FIRST_RESOURCE_ID;
import static leshan.server.client.IntegrationTestHelper.GOOD_OBJECT_ID;
import static leshan.server.client.IntegrationTestHelper.GOOD_OBJECT_INSTANCE_ID;
import static leshan.server.client.IntegrationTestHelper.SECOND_RESOURCE_ID;
import static leshan.server.client.IntegrationTestHelper.assertEmptyResponse;
import static leshan.server.client.IntegrationTestHelper.createGoodObjectInstance;
import static org.junit.Assert.assertEquals;
import leshan.server.lwm2m.client.LinkObject;
import leshan.server.lwm2m.request.CoapResponseCode.ResponseCode;
import leshan.server.lwm2m.request.DiscoverResponse;

import org.junit.After;
import org.junit.Test;

public class DiscoverTest {

    private IntegrationTestHelper helper = new IntegrationTestHelper();

    @After
    public void stop() {
        helper.stop();
    }

    @Test
    public void can_discover_object() {
        helper.register();

        final DiscoverResponse response = helper.sendDiscover(GOOD_OBJECT_ID);
        assertLinkFormatResponse(response, ResponseCode.CONTENT, helper.client.getObjectModel(GOOD_OBJECT_ID));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegal_object_links_request_two() {
        helper.register();

        final DiscoverResponse response = helper.sendDiscover(GOOD_OBJECT_ID);
        assertLinkFormatResponse(response, ResponseCode.CONTENT, helper.client.getObjectModel(GOOD_OBJECT_ID,
                GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID, SECOND_RESOURCE_ID));
    }

    @Test
    public void can_discover_object_instance() {
        helper.register();

        helper.sendCreate(createGoodObjectInstance("hello", "goodbye"), GOOD_OBJECT_ID);

        assertLinkFormatResponse(helper.sendDiscover(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), ResponseCode.CONTENT,
                helper.client.getObjectModel(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID));
    }

    @Test
    public void can_discover_resource() {
        helper.register();

        helper.sendCreate(createGoodObjectInstance("hello", "goodbye"), GOOD_OBJECT_ID);

        assertLinkFormatResponse(helper.sendDiscover(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID),
        		ResponseCode.CONTENT, helper.client.getObjectModel(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID));
    }

    @Test
    public void cant_discover_non_existent_resource() {
        helper.register();

        helper.sendCreate(createGoodObjectInstance("hello", "goodbye"), GOOD_OBJECT_ID);

        assertEmptyResponse(helper.sendDiscover(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, 1234231), ResponseCode.NOT_FOUND);
    }

    private void assertLinkFormatResponse(final DiscoverResponse response, final ResponseCode responseCode,
            final LinkObject[] expectedObjects) {
        assertEquals(responseCode, response.getCode());

        final LinkObject[] actualObjects = response.getObjectLinks();

        assertEquals(expectedObjects.length, actualObjects.length);
        for (int i = 0; i < expectedObjects.length; i++) {
            assertEquals(expectedObjects[i].toString(), actualObjects[i].toString());
        }
    }

}
