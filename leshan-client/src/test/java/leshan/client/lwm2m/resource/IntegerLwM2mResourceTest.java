package leshan.client.lwm2m.resource;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.ReadResponse;
import leshan.client.lwm2m.operation.WriteResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IntegerLwM2mResourceTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

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

		final ReadableWriteableTestResource testResource = new ReadableWriteableTestResource();
		testResource.write(exchange);

		assertEquals(0, testResource.value);
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

		private int value;

	}

}
