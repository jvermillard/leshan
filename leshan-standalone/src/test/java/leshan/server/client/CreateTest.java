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

import static org.junit.Assert.assertEquals;
import leshan.server.lwm2m.node.LwM2mObjectInstance;
import leshan.server.lwm2m.node.LwM2mResource;
import leshan.server.lwm2m.node.Value;
import leshan.server.lwm2m.request.CreateResponse;
import leshan.server.lwm2m.request.CoapResponseCode.ResponseCode;

import org.junit.After;
import org.junit.Test;

public class CreateTest {

    IntegrationTestHelper helper = new IntegrationTestHelper();

    @After
    public void stop() {
        helper.stop();
    }

    @Test
    public void can_create_instance_of_object() {
        helper.register();

        final CreateResponse response = helper.sendCreate(
                IntegrationTestHelper.createGoodObjectInstance("hello", "goodbye"),
                IntegrationTestHelper.GOOD_OBJECT_ID);
        IntegrationTestHelper.assertEmptyResponse(response, ResponseCode.CREATED);
        assertEquals(IntegrationTestHelper.GOOD_OBJECT_ID + "/0", response.getLocation());
    }

    @Test
    public void can_create_specific_instance_of_object() {
        helper.register();

        final CreateResponse response = helper.sendCreate(IntegrationTestHelper.createGoodObjectInstance("one", "two"),
                IntegrationTestHelper.GOOD_OBJECT_ID, 14);
        IntegrationTestHelper.assertEmptyResponse(response, ResponseCode.CREATED);
        assertEquals(IntegrationTestHelper.GOOD_OBJECT_ID + "/14", response.getLocation());
    }

    @Test
    public void can_create_multiple_instance_of_object() {
        helper.register();

        final CreateResponse response = helper.sendCreate(
                IntegrationTestHelper.createGoodObjectInstance("hello", "goodbye"),
                IntegrationTestHelper.GOOD_OBJECT_ID);
        IntegrationTestHelper.assertEmptyResponse(response, ResponseCode.CREATED);
        assertEquals(IntegrationTestHelper.GOOD_OBJECT_ID + "/0", response.getLocation());

        final CreateResponse responseTwo = helper.sendCreate(
                IntegrationTestHelper.createGoodObjectInstance("hello", "goodbye"),
                IntegrationTestHelper.GOOD_OBJECT_ID);
        IntegrationTestHelper.assertEmptyResponse(responseTwo, ResponseCode.CREATED);
        assertEquals(IntegrationTestHelper.GOOD_OBJECT_ID + "/1", responseTwo.getLocation());
    }

    @Test
    public void cannot_create_instance_of_object() {
        helper.register();

        final CreateResponse response = helper
                .sendCreate(IntegrationTestHelper.createGoodObjectInstance("hello", "goodbye"),
                        IntegrationTestHelper.BAD_OBJECT_ID);
        IntegrationTestHelper.assertEmptyResponse(response, ResponseCode.NOT_FOUND);
    }

    @Test
    public void cannot_create_instance_without_all_required_resources() {
        helper.register();

        final LwM2mObjectInstance instance = new LwM2mObjectInstance(IntegrationTestHelper.GOOD_OBJECT_INSTANCE_ID,
                new LwM2mResource[] { new LwM2mResource(IntegrationTestHelper.FIRST_RESOURCE_ID,
                        Value.newStringValue("hello")) });

        final CreateResponse response = helper.sendCreate(instance, IntegrationTestHelper.GOOD_OBJECT_ID);
        IntegrationTestHelper.assertEmptyResponse(response, ResponseCode.BAD_REQUEST);

        IntegrationTestHelper.assertEmptyResponse(
                helper.sendRead(IntegrationTestHelper.GOOD_OBJECT_ID, IntegrationTestHelper.GOOD_OBJECT_INSTANCE_ID),
                ResponseCode.NOT_FOUND);
    }

    @Test
    public void cannot_create_instance_with_extraneous_resources() {
        helper.register();

        final LwM2mObjectInstance instance = new LwM2mObjectInstance(IntegrationTestHelper.GOOD_OBJECT_INSTANCE_ID,
                new LwM2mResource[] {
                                        new LwM2mResource(IntegrationTestHelper.FIRST_RESOURCE_ID,
                                                Value.newStringValue("hello")),
                                        new LwM2mResource(IntegrationTestHelper.SECOND_RESOURCE_ID,
                                                Value.newStringValue("goodbye")),
                                        new LwM2mResource(IntegrationTestHelper.INVALID_RESOURCE_ID,
                                                Value.newStringValue("lolz")) });

        final CreateResponse response = helper.sendCreate(instance, IntegrationTestHelper.GOOD_OBJECT_ID);
        IntegrationTestHelper.assertEmptyResponse(response, ResponseCode.METHOD_NOT_ALLOWED);

        IntegrationTestHelper.assertEmptyResponse(
                helper.sendRead(IntegrationTestHelper.GOOD_OBJECT_ID, IntegrationTestHelper.GOOD_OBJECT_INSTANCE_ID),
                ResponseCode.NOT_FOUND);
    }

    @Test
    public void cannot_create_instance_with_non_writable_resource() {
        helper.register();

        final LwM2mObjectInstance instance = new LwM2mObjectInstance(IntegrationTestHelper.GOOD_OBJECT_INSTANCE_ID,
                new LwM2mResource[] {
                                        new LwM2mResource(IntegrationTestHelper.FIRST_RESOURCE_ID,
                                                Value.newStringValue("hello")),
                                        new LwM2mResource(IntegrationTestHelper.SECOND_RESOURCE_ID,
                                                Value.newStringValue("goodbye")),
                                        new LwM2mResource(IntegrationTestHelper.EXECUTABLE_RESOURCE_ID,
                                                Value.newStringValue("lolz")) });

        final CreateResponse response = helper.sendCreate(instance, IntegrationTestHelper.GOOD_OBJECT_ID);
        IntegrationTestHelper.assertEmptyResponse(response, ResponseCode.METHOD_NOT_ALLOWED);

        IntegrationTestHelper.assertEmptyResponse(
                helper.sendRead(IntegrationTestHelper.GOOD_OBJECT_ID, IntegrationTestHelper.GOOD_OBJECT_INSTANCE_ID),
                ResponseCode.NOT_FOUND);
    }

    @Test
    public void can_create_object_instance_with_empty_payload() {
        helper.register();
        IntegrationTestHelper.assertEmptyResponse(helper.sendCreate(new LwM2mObjectInstance(
                IntegrationTestHelper.GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]),
                IntegrationTestHelper.MULTIPLE_OBJECT_ID), ResponseCode.CREATED);
    }

    @Test
    public void cannot_create_mandatory_single_object() {
        helper.register();
        IntegrationTestHelper.assertEmptyResponse(helper.sendCreate(new LwM2mObjectInstance(
                IntegrationTestHelper.GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]),
                IntegrationTestHelper.MANDATORY_SINGLE_OBJECT_ID), ResponseCode.BAD_REQUEST);
    }

    @Test
    public void can_create_mandatory_multiple_object() {
        helper.register();
        IntegrationTestHelper.assertEmptyResponse(helper.sendCreate(new LwM2mObjectInstance(
                IntegrationTestHelper.GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]),
                IntegrationTestHelper.MANDATORY_MULTIPLE_OBJECT_ID), ResponseCode.CREATED);
        IntegrationTestHelper.assertEmptyResponse(helper.sendCreate(new LwM2mObjectInstance(
                IntegrationTestHelper.GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]),
                IntegrationTestHelper.MANDATORY_MULTIPLE_OBJECT_ID), ResponseCode.CREATED);
    }

    @Test
    public void cannot_create_more_than_one_single_object() {
        helper.register();
        IntegrationTestHelper.assertEmptyResponse(helper.sendCreate(new LwM2mObjectInstance(
                IntegrationTestHelper.GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]),
                IntegrationTestHelper.OPTIONAL_SINGLE_OBJECT_ID), ResponseCode.CREATED);
        IntegrationTestHelper.assertEmptyResponse(helper.sendCreate(new LwM2mObjectInstance(
                IntegrationTestHelper.GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]),
                IntegrationTestHelper.OPTIONAL_SINGLE_OBJECT_ID), ResponseCode.BAD_REQUEST);
    }

    @Test
    public void can_access_mandatory_object_without_create() {
        helper.register();
        IntegrationTestHelper.assertResponse(
                helper.sendRead(IntegrationTestHelper.MANDATORY_SINGLE_OBJECT_ID, 0,
                        IntegrationTestHelper.MANDATORY_SINGLE_RESOURCE_ID),
                ResponseCode.CONTENT,
                new LwM2mResource(IntegrationTestHelper.MANDATORY_SINGLE_RESOURCE_ID, Value.newStringValue(Integer
                        .toString(helper.intResource.getValue()))));
    }

}
