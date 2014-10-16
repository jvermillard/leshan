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
package leshan.client.lwm2m.operation;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import leshan.client.lwm2m.response.ReadResponse;
import leshan.server.lwm2m.impl.tlv.Tlv;
import leshan.server.lwm2m.impl.tlv.Tlv.TlvType;
import leshan.server.lwm2m.impl.tlv.TlvEncoder;

import org.junit.Test;

public class ReadResponseTest {

	@Test
	public void testEqualityRobustnessForSuccesses() {
		assertEquals(ReadResponse.success("hello".getBytes()), ReadResponse.success("hello".getBytes()));
		assertNotEquals(ReadResponse.success("hello".getBytes()), ReadResponse.success("goodbye".getBytes()));
		assertNotEquals(ReadResponse.success("hello".getBytes()), ReadResponse.failure());
		assertNotEquals(ReadResponse.success("hello".getBytes()), null);
	}

	@Test
	public void testHashCodeRobustnessForSuccesses() {
		assertEquals(ReadResponse.success("hello".getBytes()).hashCode(), ReadResponse.success("hello".getBytes()).hashCode());
		assertNotEquals(ReadResponse.success("hello".getBytes()).hashCode(), ReadResponse.success("goodbye".getBytes()).hashCode());
		assertNotEquals(ReadResponse.success("hello".getBytes()).hashCode(), ReadResponse.failure().hashCode());
	}

	@Test
	public void testEqualityRobustnessForFailures() {
		assertEquals(ReadResponse.failure(), ReadResponse.failure());
		assertNotEquals(ReadResponse.failure(), ReadResponse.success("goodbye".getBytes()));
		assertNotEquals(ReadResponse.failure(), null);
	}

	@Test
	public void testHashCodeRobustnessForFailures() {
		assertEquals(ReadResponse.failure().hashCode(), ReadResponse.failure().hashCode());
		assertNotEquals(ReadResponse.failure(), ReadResponse.success("goodbye".getBytes()).hashCode());
	}

	@Test
	public void testSuccessSinglePayload() {
		final ReadResponse response = ReadResponse.success("value".getBytes());
		assertArrayEquals("value".getBytes(), response.getResponsePayload());
	}

	@Test
	public void testSuccessSingleTlv() {
		final ReadResponse response = ReadResponse.success("value".getBytes());
		assertEquals(new Tlv(TlvType.RESOURCE_VALUE, null, "value".getBytes(), 0),
				response.getResponsePayloadAsTlv());
	}

	@Test
	public void testSuccessMultiplePayload() {
		final Map<Integer, byte[]> readValues = new HashMap<>();
		readValues.put(55, "value".getBytes());
		final ReadResponse response = ReadResponse.successMultiple(readValues);
		final Tlv[] instances = new Tlv[] {
				new Tlv(TlvType.RESOURCE_INSTANCE, null, "value".getBytes(), 55)
		};
		assertArrayEquals(TlvEncoder.encode(instances).array(), response.getResponsePayload());
	}

	@Test
	public void testSuccessMultipleTlv() {
		final Map<Integer, byte[]> readValues = new HashMap<>();
		readValues.put(55, "value".getBytes());
		final ReadResponse response = ReadResponse.successMultiple(readValues);
		final Tlv[] instances = new Tlv[] {
				new Tlv(TlvType.RESOURCE_INSTANCE, null, "value".getBytes(), 55)
		};
		assertEquals(new Tlv(TlvType.MULTIPLE_RESOURCE, instances, null, 0),
				response.getResponsePayloadAsTlv());
	}

}
