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

	private static final String WRITE_OPERATION_COMPLETED_SUCCESSFULLY = "\"Write\" operation is completed successfully";
	private static final String BAD_WRITE_REQUEST = "￼The format of data to be written is different";
	private static final String DELETE_OPERATION_TARGET_NOT_ALLOWED = "Target is not allowed for \"Delete\" operation";
	private static final String DELETE_OPERATION_COMPLETED_SUCCESSFULLY = "\"Delete\" operation is completed successfully";
	private static final int OBJECT_ID = 3;
	private static final int OBJECT_INSTANCE_ID = 1;
	private static final int RESOURCE_ID = 2;

	@Mock
	private BootstrapDownlink downlink;

	@Mock
	private Exchange exchange;

	@Test
	public void testWriteNoInstanceGoodPayload() {
		initializeExchange(Code.PUT, "/3");

		deliverRequest();

		verifyResponse(OperationResponseCode.CHANGED, WRITE_OPERATION_COMPLETED_SUCCESSFULLY);
	}

	@Test
	public void testWriteRootGoodPayload() {
		initializeExchange(Code.PUT, "/");

		deliverRequest();

		verifyResponse(OperationResponseCode.CHANGED, WRITE_OPERATION_COMPLETED_SUCCESSFULLY);
	}

	@Test
	public void testWriteResourceGoodPayload() {
		initializeWriteWithResponse(new Response(ResponseCode.CHANGED));
		initializeResourceExchange(Code.PUT);

		deliverRequest();

		verifyResourceWrite();
		verifyResponse(OperationResponseCode.CHANGED, WRITE_OPERATION_COMPLETED_SUCCESSFULLY);
	}

	@Test
	public void testWriteToBadUri() {
		initializeExchange(Code.PUT, "/NaN");

		deliverRequest();

		verifyResponse(OperationResponseCode.BAD_REQUEST, BAD_WRITE_REQUEST);
	}

	@Test
	public void testWriteResourceWriteThrowsNpe() {
		initializeWriteWithException(new NullPointerException());

		initializeResourceExchange(Code.PUT);

		deliverRequest();

		verifyResourceWrite();
		verifyResponse(OperationResponseCode.INTERNAL_SERVER_ERROR, null);
	}

	@Test
	public void testWriteResourceWriteThrowsNfe() {
		initializeWriteWithException(new NumberFormatException());

		initializeResourceExchange(Code.PUT);

		deliverRequest();

		verifyResourceWrite();
		verifyResponse(OperationResponseCode.INTERNAL_SERVER_ERROR, null);
	}

	@Test
	public void testDeleteGoodPayload() {
		initializeDeleteWithResponse(new Response(ResponseCode.DELETED));
		initializeResourceExchange(Code.DELETE);

		deliverRequest();

		verifyResourceDelete();
		verifyResponse(OperationResponseCode.DELETED, DELETE_OPERATION_COMPLETED_SUCCESSFULLY);
	}

	@Test
	public void testDeleteRoot() {
		initializeExchange(Code.DELETE, "/");

		deliverRequest();

		verifyResponse(OperationResponseCode.DELETED, DELETE_OPERATION_COMPLETED_SUCCESSFULLY);
	}

	@Test
	public void testDeleteObjectId() {
		initializeExchange(Code.DELETE, "/3");

		deliverRequest();

		verifyResponse(OperationResponseCode.DELETED, DELETE_OPERATION_COMPLETED_SUCCESSFULLY);
	}

	@Test
	public void testDeleteObjectIdAndInstanceId() {
		initializeExchange(Code.DELETE, "/3/1");

		deliverRequest();

		verifyResponse(OperationResponseCode.DELETED, DELETE_OPERATION_COMPLETED_SUCCESSFULLY);
	}

	@Test
	public void testDeleteBadUri() {
		initializeExchange(Code.DELETE, "/NaN");

		deliverRequest();

		verifyResponse(OperationResponseCode.METHOD_NOT_ALLOWED, DELETE_OPERATION_TARGET_NOT_ALLOWED);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void cannotDeliverWriteResponse() {
		initializeResourceExchange(Code.PUT);

		deliverResponse(OperationResponseCode.CHANGED);
	}

	private void initializeWriteWithResponse(final Response response) {
		final OperationResponse operationResponse = OperationResponse.of(response);
		when(downlink.write(OBJECT_ID, OBJECT_INSTANCE_ID, RESOURCE_ID)).thenReturn(operationResponse);
	}

	private void initializeDeleteWithResponse(final Response response) {
		final OperationResponse operationResponse = OperationResponse.of(response);
		when(downlink.delete(OBJECT_ID, OBJECT_INSTANCE_ID)).thenReturn(operationResponse);
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

	private void deliverRequest() {
		final BootstrapMessageDeliverer deliverer = new BootstrapMessageDeliverer(downlink);
		deliverer.deliverRequest(exchange);
	}

	private void deliverResponse(final OperationResponseCode leshanResponseCode) {
		final BootstrapMessageDeliverer deliverer = new BootstrapMessageDeliverer(downlink);
		deliverer.deliverResponse(exchange, new Response(ResponseCode.valueOf(leshanResponseCode.getValue())));
	}

	private void verifyResourceWrite() {
		verify(downlink).write(OBJECT_ID, OBJECT_INSTANCE_ID, RESOURCE_ID);
	}

	private void verifyResourceDelete() {
		verify(downlink).delete(OBJECT_ID, OBJECT_INSTANCE_ID);
	}

	private void verifyResponse(final OperationResponseCode responseCode, final String payload) {
		final byte[] payloadBytes = payload != null ? payload.getBytes() : null;
		verify(exchange).sendResponse(Matchers.argThat(new ResponseMatcher(responseCode, payloadBytes)));
	}

	private static String constructUri(final int objectId, final int objectInstanceId, final int resourceId) {
		return "/" + Joiner.on("/").skipNulls().join(objectId, objectInstanceId, resourceId);
	}
}
