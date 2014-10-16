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
package leshan.client.lwm2m.resource;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.resource.bool.BooleanLwM2mExchange;
import leshan.client.lwm2m.resource.bool.BooleanLwM2mResource;
import leshan.client.lwm2m.response.ReadResponse;
import leshan.client.lwm2m.response.WriteResponse;

import org.junit.Test;

public class BooleanLwM2mResourceTest {

	@Test
	public void testReadTrue() {
		final LwM2mExchange exchange = mock(LwM2mExchange.class);

		final ReadableWriteableTestResource testResource = new ReadableWriteableTestResource(true);
		testResource.read(exchange);

		assertEquals(true, testResource.value);
		verify(exchange).respond(ReadResponse.success("1".getBytes()));
	}

	@Test
	public void testReadFalse() {
		final LwM2mExchange exchange = mock(LwM2mExchange.class);

		final ReadableWriteableTestResource testResource = new ReadableWriteableTestResource(false);
		testResource.read(exchange);

		assertEquals(false, testResource.value);
		verify(exchange).respond(ReadResponse.success("0".getBytes()));
	}

	@Test
	public void testWriteTrue() {
		final LwM2mExchange exchange = mock(LwM2mExchange.class);
		when(exchange.getRequestPayload()).thenReturn("1".getBytes());

		final ReadableWriteableTestResource testResource = new ReadableWriteableTestResource(false);
		testResource.write(exchange);

		assertEquals(true, testResource.value);
		verify(exchange).respond(WriteResponse.success());
	}

	@Test
	public void testWriteFalse() {
		final LwM2mExchange exchange = mock(LwM2mExchange.class);
		when(exchange.getRequestPayload()).thenReturn("0".getBytes());

		final ReadableWriteableTestResource testResource = new ReadableWriteableTestResource(true);
		testResource.write(exchange);

		assertEquals(false, testResource.value);
		verify(exchange).respond(WriteResponse.success());
	}

	@Test
	public void testWriteInvalid() {
		final LwM2mExchange exchange = mock(LwM2mExchange.class);
		when(exchange.getRequestPayload()).thenReturn("lolzors".getBytes());

		final ReadableWriteableTestResource testResource = new ReadableWriteableTestResource(true);
		testResource.write(exchange);

		assertEquals(true, testResource.value);
		verify(exchange).respond(WriteResponse.badRequest());
	}

	@Test
	public void testWriteInvalid2() {
		final LwM2mExchange exchange = mock(LwM2mExchange.class);
		when(exchange.getRequestPayload()).thenReturn("lolzors".getBytes());

		final ReadableWriteableTestResource testResource = new ReadableWriteableTestResource(false);
		testResource.write(exchange);

		assertEquals(false, testResource.value);
		verify(exchange).respond(WriteResponse.badRequest());
	}

	private class ReadableWriteableTestResource extends BooleanLwM2mResource {

		private boolean value;

		public ReadableWriteableTestResource(final boolean newValue) {
			value = newValue;
		}

		@Override
		protected void handleWrite(final BooleanLwM2mExchange exchange) {
			this.value = exchange.getRequestPayload();
			exchange.respondSuccess();
		}

		@Override
		protected void handleRead(final BooleanLwM2mExchange exchange) {
			exchange.respondContent(value);
		}

	}

}
