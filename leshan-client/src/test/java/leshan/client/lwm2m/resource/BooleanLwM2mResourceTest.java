package leshan.client.lwm2m.resource;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.ReadResponse;
import leshan.client.lwm2m.operation.WriteResponse;

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
