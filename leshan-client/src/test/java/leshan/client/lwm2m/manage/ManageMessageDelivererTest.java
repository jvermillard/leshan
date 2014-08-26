package leshan.client.lwm2m.manage;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import leshan.client.lwm2m.response.OperationResponse;
import leshan.client.lwm2m.response.OperationResponseCode;
import leshan.client.lwm2m.response.ResponseMatcher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ch.ethz.inf.vs.californium.coap.CoAP.Code;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.MediaTypeRegistry;
import ch.ethz.inf.vs.californium.coap.OptionSet;
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
	public void getAcceptLinkFormatOnResourceCallsDiscover() {
		deliverRequestAcceptLinkFormat(Code.GET, "/3/1/2");
		verify(downlink).discover(3, 1, 2);
	}

	@Test
	public void getAcceptLinkFormatOnObjectInstanceCallsDiscover() {
		deliverRequestAcceptLinkFormat(Code.GET, "/3/1");
		verify(downlink).discover(3, 1);
	}

	@Test
	public void getAcceptLinkFormatOnObjectCallsDiscover() {
		deliverRequestAcceptLinkFormat(Code.GET, "/3");
		verify(downlink).discover(3);
	}

	@Test
	public void postToResourceCallsPartialUpdateOrExecute() {
		deliverRequestWithPayload(Code.POST, "/1/2/3", "payload");
		verify(downlink).partialUpdateOrExecute(1, 2, 3, "payload");
	}

	@Test
	public void postToObjectInstanceCallsPartialUpdateOrCreate() {
		deliverRequestWithPayload(Code.POST, "/1/2", "payload");
		verify(downlink).partialUpdateOrCreate(1, 2, "payload");
	}

	@Test
	public void postToObjectCallsCreate() {
		deliverRequestWithPayload(Code.POST, "/1", "payload");
		verify(downlink).create(1, "payload");
	}

	@Test
	public void putOnResourceCallsReplace() {
		deliverRequestWithPayload(Code.PUT, "/86/75/309", "new-value");
		verify(downlink).replace(86, 75, 309, "new-value");
	}

	@Test
	public void putOnObjectInstanceCallsReplace() {
		deliverRequestWithPayload(Code.PUT, "/86/75", "new-value");
		verify(downlink).replace(86, 75, "new-value");
	}

	@Test
	public void putOnObjectCallsNothing() {
		deliverRequestWithPayload(Code.PUT, "/86", "new-value");
		verifyZeroInteractions(downlink);
	}

	@Test
	public void putWithQueryParamsOnResourceCallsWriteAttributes() {
		deliverRequestWithQueryParams(Code.PUT, "/90/21/0", "lt=45", "pmin=1000");
		verify(downlink).writeAttributes(90, 21, 0, Arrays.asList("lt=45", "pmin=1000"));
	}

	@Test
	public void putWithQueryParamsOnObjectInstanceCallsWriteAttributes() {
		deliverRequestWithQueryParams(Code.PUT, "/90/21", "lt=45", "pmin=1000");
		verify(downlink).writeAttributes(90, 21, Arrays.asList("lt=45", "pmin=1000"));
	}

	@Test
	public void putWithQueryParamsOnObjectCallsWriteAttributes() {
		deliverRequestWithQueryParams(Code.PUT, "/90", "lt=45", "pmin=1000");
		verify(downlink).writeAttributes(90, Arrays.asList("lt=45", "pmin=1000"));
	}

	@Test
	public void deleteOnResourceCallsNothing() {
		deliverRequestNoPayload(Code.DELETE, "/5/10/15");
		verifyZeroInteractions(downlink);
	}

	@Test
	public void deleteOnObjectInstanceCallsDelete() {
		deliverRequestNoPayload(Code.DELETE, "/5/10");
		verify(downlink).delete(5, 10);
	}

	@Test
	public void deleteOnObjectCallsNothing() {
		deliverRequestNoPayload(Code.DELETE, "/5");
		verifyZeroInteractions(downlink);
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
	public void postToResourceWithPayloadRespondsWithChanged() {
		when(downlink.partialUpdateOrExecute(1, 2, 3, "payload")).thenReturn(createResponse(ResponseCode.CHANGED, "\"Execute\" operation is completed successfully"));
		deliverRequestWithPayload(Code.POST, "/1/2/3", "payload");
		verifyResponse(OperationResponseCode.CHANGED, "\"Execute\" operation is completed successfully");
	}

	@Test
	public void putOnResourceRespondsWithChanged() {
		when(downlink.replace(86, 75, 309, "new-value")).thenReturn(createResponse(ResponseCode.CHANGED, "Resource has changed"));
		deliverRequestWithPayload(Code.PUT, "/86/75/309", "new-value");
		verifyResponse(OperationResponseCode.CHANGED, "Resource has changed");
	}

	@Test
	public void putOnObjectRespondsWithMethodNotAllowed() {
		deliverRequestWithPayload(Code.PUT, "/86", "new-value");
		verifyResponse(OperationResponseCode.METHOD_NOT_ALLOWED, "Target is not allowed for \"Write\" operation");
	}

	@Test
	public void deleteOnResourceRespondsWithMethodNotAllowed() {
		deliverRequestNoPayload(Code.DELETE, "/5/10/15");
		verifyResponse(OperationResponseCode.METHOD_NOT_ALLOWED, "Target is not allowed for \"Delete\" operation");
	}

	@Test
	public void deleteOnResourceRespondsWithDeleted() {
		when(downlink.delete(5, 10)).thenReturn(createResponse(ResponseCode.DELETED, "\"Delete\" operation is completed successfully"));
		deliverRequestNoPayload(Code.DELETE, "/5/10");
		verifyResponse(OperationResponseCode.DELETED, "\"Delete\" operation is completed successfully");
	}

	@Test
	public void deleteOnObjectRespondsWithMethodNotAllowed() {
		deliverRequestNoPayload(Code.DELETE, "/5");
		verifyResponse(OperationResponseCode.METHOD_NOT_ALLOWED, "Target is not allowed for \"Delete\" operation");
	}

	private OperationResponse createResponse(final ResponseCode responseCode, final String payload) {
		final Response response = new Response(responseCode);
		response.setPayload(payload);
		return OperationResponse.of(response);
	}

	private void deliverRequestNoPayload(final Code code, final String uri) {
		deliverRequest(code, uri, new byte[0], new OptionSet());
	}

	private void deliverRequestAcceptLinkFormat(final Code code, final String uri) {
		final OptionSet optionSet = new OptionSet();
		optionSet.setAccept(MediaTypeRegistry.APPLICATION_LINK_FORMAT);
		deliverRequest(code, uri, new byte[0], optionSet);
	}

	private void deliverRequestWithQueryParams(final Code code, final String uri, final String... queries) {
		final OptionSet optionSet = new OptionSet();
		for (final String query : queries) {
			optionSet.addURIQuery(query);
		}
		deliverRequest(code, uri, new byte[0], optionSet);
	}

	private void deliverRequestWithPayload(final Code code, final String uri, final String payload) {
		deliverRequest(code, uri, payload.getBytes(), new OptionSet());
	}

	private void deliverRequest(final Code code, final String uri, final byte[] payload, final OptionSet optionSet) {
		final Request request = mock(Request.class);
		when(request.getCode()).thenReturn(code);
		when(request.getURI()).thenReturn(uri);
		when(request.getPayload()).thenReturn(payload);
		when(request.getOptions()).thenReturn(optionSet);

		when(exchange.getRequest()).thenReturn(request);
		deliverer.deliverRequest(exchange);
	}

	private void verifyResponse(final OperationResponseCode responseCode, final String payload) {
		final byte[] payloadBytes = payload != null ? payload.getBytes() : null;
		verify(exchange).sendResponse(argThat(new ResponseMatcher(responseCode, payloadBytes)));
	}

}
