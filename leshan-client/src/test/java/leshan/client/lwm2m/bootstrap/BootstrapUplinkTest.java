package leshan.client.lwm2m.bootstrap;

import static com.jayway.awaitility.Awaitility.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import leshan.client.lwm2m.BootstrapMessageDeliverer.InterfaceTypes;
import leshan.client.lwm2m.BootstrapMessageDeliverer.OperationTypes;
import leshan.client.lwm2m.MockedCallback;
import leshan.client.lwm2m.OperationResponseCode;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import ch.ethz.inf.vs.californium.coap.CoAP.Code;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;

@RunWith(MockitoJUnitRunner.class)
public class BootstrapUplinkTest {
	private static final String ENDPOINT_NAME = UUID.randomUUID().toString();
	private byte[] actualPayload;
	private String actualRequest;
	private Code actualCode;
	private MockedCallback callback;
	
	@Mock
	private CoAPEndpoint endpoint;
	private String expectedRequest;
	
	@Before
	public void setUp() {
		callback = new MockedCallback();
		expectedRequest = "coap://localhost/bs?ep=" + ENDPOINT_NAME;
	}

	private BootstrapUplink initializeServerResponse(final InterfaceTypes interfaceType, final OperationTypes operationType, final ResponseCode responseCode) {
		doAnswer(new Answer<Void>(){
			
			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				final Request request = (Request) invocation.getArguments()[0];
				actualRequest = request.getURI();
				actualCode = request.getCode();
				
				final Response response = new Response(responseCode);
				response.setPayload(OperationResponseCode.generateReasonPhrase(OperationResponseCode.valueOf(response.getCode().value), interfaceType, operationType));
				
				request.setResponse(response);
				
				return null;
			}
		}).when(endpoint).sendRequest(any(Request.class));
		
		final BootstrapUplink uplink = new BootstrapUplink(endpoint);
		return uplink;
	}
	
	private void sendBootstrapAndGetResponse(final BootstrapUplink uplink) {
		uplink.bootstrap(ENDPOINT_NAME, callback);
		
		await().untilTrue(callback.isCalled());
		actualPayload = callback.getResponsePayload();
	}
	
	private void verifyResponse(final String expectedPayload) {
		assertEquals(expectedRequest, actualRequest);
		assertEquals(Code.POST, actualCode);
		assertArrayEquals(expectedPayload.getBytes(), actualPayload);
	}
	
	@Test
	public void testGoodPayload() {
		final BootstrapUplink uplink = initializeServerResponse(InterfaceTypes.BOOTSTRAP, OperationTypes.REGISTER, ResponseCode.CHANGED);
		
		sendBootstrapAndGetResponse(uplink);
		
		verifyResponse("Request Bootstrap is completed successfully");
	}

	
	@Test
	public void testBadPayload() {
		final BootstrapUplink uplink = initializeServerResponse(InterfaceTypes.BOOTSTRAP, OperationTypes.REGISTER, ResponseCode.BAD_REQUEST);
		
		sendBootstrapAndGetResponse(uplink);
		
		verifyResponse("Unknown Endpoint Client Name");
	}
}
