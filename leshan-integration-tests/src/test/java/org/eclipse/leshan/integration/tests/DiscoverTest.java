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

package org.eclipse.leshan.integration.tests;

import static org.eclipse.leshan.ResponseCode.*;
import static org.eclipse.leshan.integration.tests.IntegrationTestHelper.*;
import static org.junit.Assert.assertEquals;

import org.eclipse.leshan.LinkObject;
import org.eclipse.leshan.ResponseCode;
import org.eclipse.leshan.core.response.DiscoverResponse;
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
        assertLinkFormatResponse(response, CONTENT, helper.client.getObjectModel(GOOD_OBJECT_ID));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegal_object_links_request_two() {
        helper.register();

        final DiscoverResponse response = helper.sendDiscover(GOOD_OBJECT_ID);
        assertLinkFormatResponse(response, CONTENT, helper.client.getObjectModel(GOOD_OBJECT_ID,
                GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID, SECOND_RESOURCE_ID));
    }

    @Test
    public void can_discover_object_instance() {
        helper.register();

        helper.sendCreate(createGoodObjectInstance("hello", "goodbye"), GOOD_OBJECT_ID);

        assertLinkFormatResponse(helper.sendDiscover(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), CONTENT,
                helper.client.getObjectModel(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID));
    }

    @Test
    public void can_discover_resource() {
        helper.register();

        helper.sendCreate(createGoodObjectInstance("hello", "goodbye"), GOOD_OBJECT_ID);

        assertLinkFormatResponse(helper.sendDiscover(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID),
                CONTENT, helper.client.getObjectModel(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID));
    }

    @Test
    public void cant_discover_non_existent_resource() {
        helper.register();

        helper.sendCreate(createGoodObjectInstance("hello", "goodbye"), GOOD_OBJECT_ID);

        assertEmptyResponse(helper.sendDiscover(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, 1234231), NOT_FOUND);
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
