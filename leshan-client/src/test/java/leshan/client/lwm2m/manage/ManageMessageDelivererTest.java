package leshan.client.lwm2m.manage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import leshan.client.lwm2m.response.OperationResponse;
import leshan.client.lwm2m.response.OperationResponseCode;
import leshan.client.lwm2m.response.ResponseMatcher;

import org.junit.Before;
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
import ch.ethz.inf.vs.californium.server.MessageDeliverer;

@RunWith(MockitoJUnitRunner.class)
public class ManageMessageDelivererTest {

	@Mock
	Exchange exchange;

	@Mock
	private ManageDownlink downlink;

	private MessageDeliverer deliverer;

	@Before
	public void setup() {
		deliverer = new ManageMessageDeliverer(downlink);
	}

	@Test
	public void getOnResourceCallsRead() {
		deliverRequestNoPayload(Code.GET, "/4/8/15");
		verify(downlink).read(4, 8, 15);
	}

	@Test
	public void getOnResourceRespondsWithContent(){
		when(downlink.read(16, 23, 42)).thenReturn(createResponse(ResponseCode.CONTENT, "resource of life"));
		deliverRequestNoPayload(Code.GET, "/16/23/42");
		verifyResponse(OperationResponseCode.CONTENT, "resource of life");
	}

	@Test
	public void getWithExceptionRespondsWithInternalServerError() {
		when(downlink.read(4, 8, 15)).thenThrow(new NullPointerException("Lol NPEs"));
		deliverRequestNoPayload(Code.GET, "/4/8/15");
		verifyResponse(OperationResponseCode.INTERNAL_SERVER_ERROR, "Lol NPEs");
	}

	@Test
	public void getThatReturnsNullRespondsWithInternalServerError() {
		when(downlink.read(4, 8, 15)).thenReturn(null);
		deliverRequestNoPayload(Code.GET, "/4/8/15");
		verifyResponse(OperationResponseCode.INTERNAL_SERVER_ERROR, "/4/8/15 was null");
	}

	@Test
	public void getOnObjectInstanceCallsRead() {
		deliverRequestNoPayload(Code.GET, "/4/8");
		verify(downlink).read(4, 8);
	}

	@Test
	public void getOnObjectCallsRead() {
		deliverRequestNoPayload(Code.GET, "/4");
		verify(downlink).read(4);
	}

	@Test
	public void getOnObjectRespondsWithContent(){
		when(downlink.read(16)).thenReturn(createResponse(ResponseCode.CONTENT, "resource of life"));
		deliverRequestNoPayload(Code.GET, "/16");
		verifyResponse(OperationResponseCode.CONTENT, "resource of life");
	}

	@Test
	public void getOnBadUriRespondsWithBadRequest() {
		deliverRequestNoPayload(Code.GET, "/lolz");
		verifyResponse(OperationResponseCode.BAD_REQUEST, "Invalid URI");
	}

	@Test
	public void putOnResourceCallsReplace() {
		deliverRequestWithPayload(Code.PUT, "/86/75/309", "new-value");
		verify(downlink).replace(86, 75, 309, "new-value");
	}

	@Test
	public void putOnResourceRespondsWithChanged() {
		when(downlink.replace(86, 75, 309, "new-value")).thenReturn(createResponse(ResponseCode.CHANGED, "Resource has changed"));
		deliverRequestWithPayload(Code.PUT, "/86/75/309", "new-value");
		verifyResponse(OperationResponseCode.CHANGED, "Resource has changed");
	}

	@Test
	public void putOnObjectInstanceCallsReplace() {
		deliverRequestWithPayload(Code.PUT, "/86/75", "new-value");
		verify(downlink).replace(86, 75, "new-value");
	}

	@Test
	public void putOnObjectRespondsWithMethodNotAllowed() {
		deliverRequestWithPayload(Code.PUT, "/86", "new-value");
		verifyResponse(OperationResponseCode.METHOD_NOT_ALLOWED, "Target is not allowed for \"Write\" operation");
	}

	private OperationResponse createResponse(final ResponseCode responseCode, final String payload) {
		final Response response = new Response(responseCode);
		response.setPayload(payload);
		return OperationResponse.of(response);
	}

	private void deliverRequestNoPayload(final Code code, final String uri) {
		deliverRequest(code, uri, null);
	}

	private void deliverRequestWithPayload(final Code code, final String uri, final String payload) {
		deliverRequest(code, uri, payload.getBytes());
	}

	private void deliverRequest(final Code code, final String uri, final byte[] payload) {
		final Request request = mock(Request.class);
		when(request.getCode()).thenReturn(code);
		when(request.getURI()).thenReturn(uri);
		when(request.getPayload()).thenReturn(payload);

		when(exchange.getRequest()).thenReturn(request);
		deliverer.deliverRequest(exchange);
	}

	private void verifyResponse(final OperationResponseCode responseCode, final String payload) {
		final byte[] payloadBytes = payload != null ? payload.getBytes() : null;
		verify(exchange).sendResponse(Matchers.argThat(new ResponseMatcher(responseCode, payloadBytes)));
	}

}
