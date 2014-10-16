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
import leshan.client.lwm2m.resource.integer.IntegerLwM2mExchange;
import leshan.client.lwm2m.resource.integer.IntegerLwM2mResource;
import leshan.client.lwm2m.response.ReadResponse;
import leshan.client.lwm2m.response.WriteResponse;

import org.junit.Test;

public class IntegerLwM2mResourceTest {

	@Test
	public void testWriteGoodValue() {
		final LwM2mExchange exchange = mock(LwM2mExchange.class);
		when(exchange.getRequestPayload()).thenReturn(Integer.toString(42).getBytes());

		final ReadableWriteableTestResource testResource = new ReadableWriteableTestResource();
		testResource.write(exchange);

		assertEquals(42, testResource.value);
		verify(exchange).respond(WriteResponse.success());
	}

	@Test
	public void testWriteBadValue() {
		final LwM2mExchange exchange = mock(LwM2mExchange.class);
		when(exchange.getRequestPayload()).thenReturn("badwolf".getBytes());

		final ReadableWriteableTestResource testResource = new ReadableWriteableTestResource(8675309);
		testResource.write(exchange);

		assertEquals(8675309, testResource.value);
		verify(exchange).respond(WriteResponse.badRequest());
	}

	@Test
	public void testRead() {
		final LwM2mExchange exchange = mock(LwM2mExchange.class);

		final ReadableWriteableTestResource testResource = new ReadableWriteableTestResource(84);
		testResource.read(exchange);

		assertEquals(84, testResource.value);
		verify(exchange).respond(ReadResponse.success(Integer.toString(84).getBytes()));
	}

	@Test
	public void testDefaultPermissionsRead() {
		final LwM2mExchange exchange = mock(LwM2mExchange.class);

		final DefaultTestResource testResource = new DefaultTestResource();
		testResource.read(exchange);

		verify(exchange).respond(ReadResponse.notAllowed());
	}

	@Test
	public void testDefaultPermissionsWrite() {
		final LwM2mExchange exchange = mock(LwM2mExchange.class);
		when(exchange.getRequestPayload()).thenReturn("badwolf".getBytes());

		final DefaultTestResource testResource = new DefaultTestResource();
		testResource.write(exchange);

		verify(exchange).respond(WriteResponse.notAllowed());
	}

	private class ReadableWriteableTestResource extends IntegerLwM2mResource {

		private int value;

		public ReadableWriteableTestResource(final int newValue) {
			value = newValue;
		}

		public ReadableWriteableTestResource() {
		}

		@Override
		protected void handleWrite(final IntegerLwM2mExchange exchange) {
			this.value = exchange.getRequestPayload();
			exchange.respondSuccess();
		}

		@Override
		protected void handleRead(final IntegerLwM2mExchange exchange) {
			exchange.respondContent(value);
		}

	}

	private class DefaultTestResource extends IntegerLwM2mResource {

	}

}
