package leshan.client.lwm2m.resource;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.ReadResponse;
import leshan.client.lwm2m.operation.WriteResponse;

import org.junit.Test;

public class StringLwM2mResourceTest {

	@Test
	public void testWriteGoodValue() {
		final LwM2mExchange exchange = mock(LwM2mExchange.class);
		final String valueToWrite = "zeus";
		when(exchange.getRequestPayload()).thenReturn(valueToWrite.getBytes());

		final ReadableWriteableTestResource testResource = new ReadableWriteableTestResource();
		testResource.write(exchange);

		assertEquals(valueToWrite, testResource.value);
		verify(exchange).respond(WriteResponse.success());
	}

	@Test
	public void testRead() {
		final LwM2mExchange exchange = mock(LwM2mExchange.class);

		final String initialValue = "redballoon";
		final ReadableWriteableTestResource testResource = new ReadableWriteableTestResource(initialValue);
		testResource.read(exchange);

		assertEquals(initialValue, testResource.value);
		verify(exchange).respond(ReadResponse.success(initialValue.getBytes()));
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

	private class ReadableWriteableTestResource extends StringLwM2mResource {

		private String value;

		public ReadableWriteableTestResource(final String newValue) {
			value = newValue;
		}

		public ReadableWriteableTestResource() {
		}

		@Override
		protected void handleWrite(final StringLwM2mExchange exchange) {
			this.value = exchange.getRequestPayload();
			exchange.respondSuccess();
		}

		@Override
		protected void handleRead(final StringLwM2mExchange exchange) {
			exchange.respondContent(value);
		}

	}

	private class DefaultTestResource extends IntegerLwM2mResource {

	}

}
