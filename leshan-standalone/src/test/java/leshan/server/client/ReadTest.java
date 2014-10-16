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

import java.util.HashMap;
import java.util.Map;

import leshan.server.lwm2m.impl.tlv.Tlv;
import leshan.server.lwm2m.impl.tlv.Tlv.TlvType;
import leshan.server.lwm2m.impl.tlv.TlvEncoder;
import leshan.server.lwm2m.node.LwM2mNode;
import leshan.server.lwm2m.node.LwM2mObject;
import leshan.server.lwm2m.node.LwM2mObjectInstance;
import leshan.server.lwm2m.node.LwM2mResource;
import leshan.server.lwm2m.node.Value;
import leshan.server.lwm2m.request.ResponseCode;

import org.junit.Test;

public class ReadTest extends LwM2mClientServerIntegrationTest {

	private static final String HELLO = "hello";
	private static final String GOODBYE = "goodbye";

	@Test
	public void can_read_empty_object() {
		register();
		assertEmptyResponse(sendRead(GOOD_OBJECT_ID), ResponseCode.CONTENT);
	}

	@Test
	public void can_read_object_with_created_instance() {
		register();
		sendCreate(createGoodObjectInstance(HELLO, GOODBYE), GOOD_OBJECT_ID);

		final LwM2mNode objectNode = new LwM2mObject(GOOD_OBJECT_ID, new LwM2mObjectInstance[] {
				new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[] {
						new LwM2mResource(FIRST_RESOURCE_ID, Value.newBinaryValue(HELLO.getBytes())),
						new LwM2mResource(SECOND_RESOURCE_ID, Value.newBinaryValue(GOODBYE.getBytes()))
				})
		});
		assertResponse(sendRead(GOOD_OBJECT_ID), ResponseCode.CONTENT, objectNode);
	}

	@Test
	public void can_read_object_instance() {
		register();
		sendCreate(createGoodObjectInstance(HELLO, GOODBYE), GOOD_OBJECT_ID);

		final LwM2mObjectInstance objectInstanceNode = new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[] {
				new LwM2mResource(FIRST_RESOURCE_ID, Value.newBinaryValue(HELLO.getBytes())),
				new LwM2mResource(SECOND_RESOURCE_ID, Value.newBinaryValue(GOODBYE.getBytes()))
		});
		assertResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), ResponseCode.CONTENT, objectInstanceNode);
	}

	@Test
	public void can_read_resource() {
		register();
		sendCreate(createGoodObjectInstance(HELLO, GOODBYE), GOOD_OBJECT_ID);

		assertResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, FIRST_RESOURCE_ID),
				ResponseCode.CONTENT, new LwM2mResource(FIRST_RESOURCE_ID, Value.newStringValue(HELLO)));
		assertResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, SECOND_RESOURCE_ID),
				ResponseCode.CONTENT, new LwM2mResource(SECOND_RESOURCE_ID, Value.newStringValue(GOODBYE)));
	}

	@Test
	public void cannot_read_non_readable_resource() {
		register();
		sendCreate(createGoodObjectInstance(HELLO, GOODBYE), GOOD_OBJECT_ID);

		assertEmptyResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, EXECUTABLE_RESOURCE_ID), ResponseCode.METHOD_NOT_ALLOWED);
	}

	@Test
	public void cannot_read_non_existent_resource() {
		register();
		sendCreate(createGoodObjectInstance(HELLO, GOODBYE), GOOD_OBJECT_ID);

		assertEmptyResponse(sendRead(GOOD_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, INVALID_RESOURCE_ID), ResponseCode.NOT_FOUND);
	}

	@Test
	public void can_read_multiple_resource() {
		register();
		sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), MULTIPLE_OBJECT_ID);

		final Map<Integer, byte[]> map = new HashMap<Integer, byte[]>();
		map.put(0, HELLO.getBytes());
		map.put(1, GOODBYE.getBytes());
		multipleResource.setValue(map);

		// This encoding is required because the LwM2mNodeParser doesn't have a way
		// of recognizing the multiple-versus-single resource-ness for the response
		// of reading a resource.
		final byte[] tlvBytes = TlvEncoder.encode(new Tlv[] {
				new Tlv(TlvType.RESOURCE_INSTANCE, null, HELLO.getBytes(), 0),
				new Tlv(TlvType.RESOURCE_INSTANCE, null, GOODBYE.getBytes(), 1)
		}).array();
		final LwM2mNode resource = new LwM2mResource(MULTIPLE_RESOURCE_ID, Value.newStringValue(new String(tlvBytes)));

		assertResponse(sendRead(MULTIPLE_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID, MULTIPLE_RESOURCE_ID),
				ResponseCode.CONTENT, resource);
	}

	@Test
	public void can_read_object_instance_with_multiple_resource() {
		register();
		sendCreate(new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[0]), MULTIPLE_OBJECT_ID);

		final Map<Integer, byte[]> map = new HashMap<Integer, byte[]>();
		map.put(0, HELLO.getBytes());
		map.put(1, GOODBYE.getBytes());

		multipleResource.setValue(map);

		final LwM2mObjectInstance objectInstance = new LwM2mObjectInstance(GOOD_OBJECT_INSTANCE_ID, new LwM2mResource[] {
				new LwM2mResource(MULTIPLE_RESOURCE_ID, new Value<?>[] {
						Value.newBinaryValue(HELLO.getBytes()),
						Value.newBinaryValue(GOODBYE.getBytes())
				})
		});

		assertResponse(sendRead(MULTIPLE_OBJECT_ID, GOOD_OBJECT_INSTANCE_ID), ResponseCode.CONTENT, objectInstance);
	}

}
