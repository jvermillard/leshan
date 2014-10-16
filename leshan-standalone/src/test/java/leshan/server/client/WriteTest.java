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

import java.util.HashMap;
import java.util.Map;

import leshan.server.lwm2m.node.LwM2mObjectInstance;
import leshan.server.lwm2m.node.LwM2mResource;
import leshan.server.lwm2m.node.Value;
import leshan.server.lwm2m.request.ResponseCode;

import org.junit.Ignore;
import org.junit.Test;

public class WriteTest extends LwM2mClientServerIntegrationTest {

    private static final String HELLO = "hello";
    private static final String GOODBYE = "goodbye";

    @Test
    public void canWriteReplaceToResource() {
        register();

        sendCreate(createGoodObjectInstance(HELLO, GOODBYE), GOOD_OBJECT_ID);

        assertEmptyResponse(sendReplace("world", GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID),
                ResponseCode.CHANGED);
        assertResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID),
                ResponseCode.CONTENT, new LwM2mResource(SECOND_RESOURCE_ID, Value.newStringValue("world")));
        assertEquals("world", secondResource.getValue());
    }

    @Test
    public void badWriteReplaceToResource() {
        register();

        sendCreate(createUnwritableResource("i'm broken!"), BROKEN_OBJECT_ID);

        assertEmptyResponse(sendReplace("fix me!", BROKEN_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, BROKEN_RESOURCE_ID),
                ResponseCode.BAD_REQUEST);
        assertResponse(sendRead(BROKEN_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, BROKEN_RESOURCE_ID),
                ResponseCode.CONTENT,  new LwM2mResource(BROKEN_RESOURCE_ID, Value.newStringValue("i'm broken!")));
    }

    @Test
    public void cannotWriteToNonWritableResource() {
        register();

        sendCreate(createGoodObjectInstance(HELLO, GOODBYE), GOOD_OBJECT_ID);

        assertEmptyResponse(sendReplace("world", GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, EXECUTABLE_RESOURCE_ID),
                ResponseCode.METHOD_NOT_ALLOWED);
    }

    @Ignore
    @Test
    public void canWriteToWritableMultipleResource() {
        register();
        sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), MULTIPLE_OBJECT_ID);

        final LwM2mResource newValues = new LwM2mResource(MULTIPLE_OBJECT_ID, new Value<?>[] {
                Value.newStringValue(HELLO),
                Value.newStringValue(GOODBYE)
        });

        final Map<Integer, byte[]> map = new HashMap<Integer, byte[]>();
        map.put(0, HELLO.getBytes());
        map.put(1, GOODBYE.getBytes());

        multipleResource.setValue(map);

        assertEmptyResponse(sendReplace(newValues, MULTIPLE_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, MULTIPLE_RESOURCE_ID),
                ResponseCode.CHANGED);
    }

    // TODO: This test tests something that is untestable by the LWM2M spec and should
    // probably be deleted. Ignored until this is confirmed
    @Ignore
    @Test
    public void canWritePartialUpdateToResource() {
        register();

        sendCreate(createGoodObjectInstance(HELLO, GOODBYE), GOOD_OBJECT_ID);

        assertEmptyResponse(sendUpdate("world", GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID),
                ResponseCode.CHANGED);
        assertResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID),
                ResponseCode.CONTENT, new LwM2mResource(SECOND_RESOURCE_ID, Value.newStringValue("world")));
        assertEquals("world", secondResource.getValue());
    }

    protected LwM2mObjectInstance createUnwritableResource(final String value) {
        return new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[] {
                new LwM2mResource(BROKEN_RESOURCE_ID, Value.newStringValue(value))
        });
    }

}
