package leshan.client.lwm2m.resource;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.ReadResponse;
import leshan.client.lwm2m.operation.WriteResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class IntegerLwM2mResourceTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testIntegerParsingGoodValue() {
		final LwM2mExchange exchange = mock(LwM2mExchange.class);
		when(exchange.getRequestPayload()).thenReturn(Integer.toString(42).getBytes());

		final WriteableTestIntegerResource testResource = spy(new WriteableTestIntegerResource());
		testResource.write(exchange);

		verify(testResource).handleWrite(42, exchange);
		verify(exchange).respond(WriteResponse.success());
	}

	@Test
	public void testIntegerParsingBadValue() {
		final LwM2mExchange exchange = mock(LwM2mExchange.class);
		when(exchange.getRequestPayload()).thenReturn("badwolf".getBytes());

		final WriteableTestIntegerResource testResource = Mockito.spy(new WriteableTestIntegerResource());
		testResource.write(exchange);

		verify(testResource, never()).handleWrite(anyInt(), eq(exchange));
		verify(exchange).respond(WriteResponse.badRequest());
	}

	private class WriteableTestIntegerResource extends IntegerLwM2mResource {

		private int value;

		@Override
		protected void handleWrite(final int newValue, final LwM2mExchange exchange) {
			this.value = newValue;
			exchange.respond(WriteResponse.success());
		}

		@Override
		protected void handleRead(final LwM2mExchange exchange) {
			exchange.respond(ReadResponse.successWithInt(value));
		}


	}

}
