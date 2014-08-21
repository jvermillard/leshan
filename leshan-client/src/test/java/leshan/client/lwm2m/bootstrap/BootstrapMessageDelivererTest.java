package leshan.client.lwm2m.bootstrap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import leshan.client.lwm2m.BootstrapMessageDeliverer;
import leshan.client.lwm2m.response.OperationResponse;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

import ch.ethz.inf.vs.californium.coap.CoAP.Code;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.Exchange;

import com.google.common.base.Joiner;

@RunWith(MockitoJUnitRunner.class)
public class BootstrapMessageDelivererTest {

	private final class ResponseMatcher extends BaseMatcher<Response> {

		private final ResponseCode code;
		private final byte[] payload;

		public ResponseMatcher(final ResponseCode code, final byte[] payload) {
			this.code = code;
			this.payload = payload;
		}

		@Override
		public boolean matches(final Object arg0) {
			return ((Response)arg0).getCode() == code &&
					Arrays.equals(payload, ((Response)arg0).getPayload());
		}

		@Override
		public void describeTo(final Description arg0) {
		}
	}

	private static final int OBJECT_ID = 3;
	private static final int OBJECT_INSTANCE_ID = 1;
	private static final int RESOURCE_ID = 2;

	@Test
	public void testWriteNoInstanceGoodPayload() {
	}

	@Test
	public void testWriteRootGoodPayload() {
	}

	@Test
	public void testWriteResourceGoodPayload() {
		final BootstrapDownlink downlink = mock(BootstrapDownlink.class);

		final OperationResponse response = OperationResponse.of(ResponseCode.CHANGED);
		when(downlink.write(OBJECT_ID, OBJECT_INSTANCE_ID, RESOURCE_ID)).thenReturn(response);

		final Exchange exchange = createExchange(Code.PUT);

		final BootstrapMessageDeliverer deliverer = new BootstrapMessageDeliverer(downlink);
		deliverer.deliverRequest(exchange);

		verify(downlink).write(OBJECT_ID, OBJECT_INSTANCE_ID, RESOURCE_ID);
		verify(exchange).sendResponse(Matchers.argThat(new ResponseMatcher(ResponseCode.CHANGED, null)));
	}

	@Test
	public void testWriteResourceWriteThrowsNpe() {
		final BootstrapDownlink downlink = mock(BootstrapDownlink.class);

		when(downlink.write(OBJECT_ID, OBJECT_INSTANCE_ID, RESOURCE_ID)).thenThrow(new NullPointerException("lol NPEs"));

		final Exchange exchange = createExchange(Code.PUT);

		final BootstrapMessageDeliverer deliverer = new BootstrapMessageDeliverer(downlink);
		deliverer.deliverRequest(exchange);

		verify(downlink).write(OBJECT_ID, OBJECT_INSTANCE_ID, RESOURCE_ID);
		verify(exchange).sendResponse(Matchers.argThat(new ResponseMatcher(ResponseCode.INTERNAL_SERVER_ERROR, null)));
	}

	@Test
	public void testWriteResourceWriteThrowsNfe() {
		final BootstrapDownlink downlink = mock(BootstrapDownlink.class);

		when(downlink.write(OBJECT_ID, OBJECT_INSTANCE_ID, RESOURCE_ID)).thenThrow(new NumberFormatException("lol NFEs"));

		final Exchange exchange = createExchange(Code.PUT);

		final BootstrapMessageDeliverer deliverer = new BootstrapMessageDeliverer(downlink);
		deliverer.deliverRequest(exchange);

		verify(downlink).write(OBJECT_ID, OBJECT_INSTANCE_ID, RESOURCE_ID);
		verify(exchange).sendResponse(Matchers.argThat(new ResponseMatcher(ResponseCode.INTERNAL_SERVER_ERROR, null)));
	}

	@Test
	public void testWriteToBadUri() {
		final BootstrapDownlink downlink = mock(BootstrapDownlink.class);
		final Request request = mock(Request.class);
		when(request.getCode()).thenReturn(Code.PUT);
		when(request.getURI()).thenReturn("/Nan");

		final Exchange exchange = mock(Exchange.class);
		when(exchange.getRequest()).thenReturn(request);

		final BootstrapMessageDeliverer deliverer = new BootstrapMessageDeliverer(downlink);
		deliverer.deliverRequest(exchange);

		verify(exchange).sendResponse(Matchers.argThat(new ResponseMatcher(ResponseCode.BAD_REQUEST, null)));
	}

	@Test
	public void testDeleteGoodPayload() {
	}

	@Test(expected = UnsupportedOperationException.class)
	public void cannotDeliverResponse() {
		final BootstrapDownlink downlink = mock(BootstrapDownlink.class);
		final BootstrapMessageDeliverer deliverer = new BootstrapMessageDeliverer(downlink);
		final Exchange exchange = createExchange(Code.PUT);
		deliverer.deliverResponse(exchange, new Response(ResponseCode.CHANGED));
	}

	private Exchange createExchange(final Code method) {
		final Request request = mock(Request.class);
		when(request.getCode()).thenReturn(method);
		when(request.getURI()).thenReturn(constructUri(OBJECT_ID, OBJECT_INSTANCE_ID, RESOURCE_ID));

		final Exchange exchange = mock(Exchange.class);
		when(exchange.getRequest()).thenReturn(request);
		return exchange;
	}

	private static String constructUri(final int objectId, final int objectInstanceId, final int resourceId) {
		return "/" + Joiner.on("/").skipNulls().join(objectId, objectInstanceId, resourceId);
	}
}
