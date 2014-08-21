package leshan.client.lwm2m.bootstrap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import leshan.client.lwm2m.BootstrapMessageDeliverer;
import leshan.client.lwm2m.OperationResponseCode;
import leshan.client.lwm2m.ResponseMatcher;
import leshan.client.lwm2m.response.OperationResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ch.ethz.inf.vs.californium.coap.CoAP.Code;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.Exchange;

import com.google.common.base.Joiner;

@RunWith(MockitoJUnitRunner.class)
public class BootstrapMessageDelivererTest {

	private static final int OBJECT_ID = 3;
	private static final int OBJECT_INSTANCE_ID = 1;
	private static final int RESOURCE_ID = 2;

	@Mock
	private BootstrapDownlink downlink;

	@Mock
	private Exchange exchange;

	@Test
	public void testWriteNoInstanceGoodPayload() {
	}

	@Test
	public void testWriteRootGoodPayload() {
	}

	@Test
	public void testWriteResourceGoodPayload() {
		initializeWriteWithResponse(ResponseCode.CHANGED);
		initializeResourceExchange(Code.PUT);

		deliverRequest();

		verifyResourceWrite();
		verifyResponse(OperationResponseCode.CHANGED, "\"Write\" operation is completed successfully".getBytes());
	}

	@Test
	public void testWriteToBadUri() {
		initializeExchange(Code.PUT, "/NaN");

		deliverRequest();

		verifyResponse(OperationResponseCode.BAD_REQUEST, "￼The format of data to be written is different".getBytes());
	}

	@Test
	public void testWriteResourceWriteThrowsNpe() {
		initializeWriteWithException(new NullPointerException("lol NPEs"));

		initializeResourceExchange(Code.PUT);

		deliverRequest();

		verifyResourceWrite();
		verifyResponse(OperationResponseCode.INTERNAL_SERVER_ERROR, null);
	}

	@Test
	public void testWriteResourceWriteThrowsNfe() {
		initializeWriteWithException(new NumberFormatException("lol NFEs"));

		initializeResourceExchange(Code.PUT);

		deliverRequest();

		verifyResourceWrite();
		verifyResponse(OperationResponseCode.INTERNAL_SERVER_ERROR, null);
	}

	@Test
	public void testDeleteGoodPayload() {
	}

	@Test(expected = UnsupportedOperationException.class)
	public void cannotDeliverResponse() {
		initializeResourceExchange(Code.PUT);

		deliverResponse(OperationResponseCode.CHANGED);
	}

	private void initializeWriteWithResponse(final ResponseCode responseCode) {
		final OperationResponse response = OperationResponse.of(responseCode);
		when(downlink.write(OBJECT_ID, OBJECT_INSTANCE_ID, RESOURCE_ID)).thenReturn(response);
	}

	private void initializeWriteWithException(final Exception exception) {
		when(downlink.write(OBJECT_ID, OBJECT_INSTANCE_ID, RESOURCE_ID)).thenThrow(exception);
	}

	private void initializeResourceExchange(final Code method) {
		initializeExchange(method, constructUri(OBJECT_ID, OBJECT_INSTANCE_ID, RESOURCE_ID));
	}

	private void initializeExchange(final Code method, final String uri) {
		final Request request = mock(Request.class);
		when(request.getCode()).thenReturn(method);
		when(request.getURI()).thenReturn(uri);

		when(exchange.getRequest()).thenReturn(request);
	}

	private static String constructUri(final int objectId, final int objectInstanceId, final int resourceId) {
		return "/" + Joiner.on("/").skipNulls().join(objectId, objectInstanceId, resourceId);
	}

	private void deliverRequest() {
		final BootstrapMessageDeliverer deliverer = new BootstrapMessageDeliverer(downlink);
		deliverer.deliverRequest(exchange);
	}

	private void deliverResponse(final OperationResponseCode leshanResponseCode) {
		final BootstrapMessageDeliverer deliverer = new BootstrapMessageDeliverer(downlink);
		deliverer.deliverResponse(exchange, new Response(ResponseCode.valueOf(leshanResponseCode.getValue())));
	}

	private void verifyResponse(final OperationResponseCode responseCode, byte[] payload) {
		verify(exchange).sendResponse(Matchers.argThat(new ResponseMatcher(responseCode, payload)));
	}

	private void verifyResourceWrite() {
		verify(downlink).write(OBJECT_ID, OBJECT_INSTANCE_ID, RESOURCE_ID);
	}

}
