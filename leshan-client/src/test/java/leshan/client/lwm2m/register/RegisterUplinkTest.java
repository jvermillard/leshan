package leshan.client.lwm2m.register;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import leshan.client.lwm2m.MockedCallback;
import leshan.client.lwm2m.OperationResponseCode;
import leshan.client.lwm2m.BootstrapMessageDeliverer.InterfaceTypes;
import leshan.client.lwm2m.BootstrapMessageDeliverer.OperationTypes;
import leshan.client.lwm2m.bootstrap.BootstrapUplink;
import leshan.client.lwm2m.response.OperationResponse;

import org.junit.Before;
import org.junit.Ignore;
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

/*
 * Requirements for this part of the interface can be found on Table 3.
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterUplinkTest {
	private static final int SYNC_TIMEOUT_MS = 2000;
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
		expectedRequest = "coap://localhost/rd?ep=" + ENDPOINT_NAME;
	}

	private RegisterUplink initializeServerResponse(final InterfaceTypes interfaceType, final OperationTypes operationType, final ResponseCode responseCode) {
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
		
		final RegisterUplink uplink = new RegisterUplink(endpoint);
		
		return uplink;
	}
	
	private void sendRegisterAndGetAsyncResponse(final RegisterUplink uplink) {
		uplink.register(ENDPOINT_NAME, callback);
		
		await().untilTrue(callback.isCalled());
		actualPayload = callback.getResponsePayload();
	}
	
	private void sendRegisterAndGetSyncResponse(final RegisterUplink uplink) {
		final OperationResponse operationResponse = uplink.register(ENDPOINT_NAME, SYNC_TIMEOUT_MS);
		actualPayload = operationResponse.getPayload();
	}
	
	private void verifyResponse(final String expectedPayload) {
		assertEquals(expectedRequest, actualRequest);
		assertEquals(Code.POST, actualCode);
		assertArrayEquals(expectedPayload.getBytes(), actualPayload);
	}
	
	@Test
	public void testGoodAsyncPayload() {
		final RegisterUplink uplink = initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED);
		
		sendRegisterAndGetAsyncResponse(uplink);
		
		verifyResponse("\"Register\" operation is completed successfully");
	}
	
	
	@Test
	public void testBadAsyncPayload() {
		final RegisterUplink uplink = initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.BAD_REQUEST);
		
		sendRegisterAndGetAsyncResponse(uplink);
		
		verifyResponse("The mandatory parameter is not specified or unknown parameter is specified");
	}
	
	@Test
	public void testGoodSyncPayload(){
		final RegisterUplink uplink = initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED);
		
		sendRegisterAndGetSyncResponse(uplink);
		
		verifyResponse("\"Register\" operation is completed successfully");
	}
	
	@Test
	public void testBadSyncPayload() {
		final RegisterUplink uplink = initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.BAD_REQUEST);
		
		sendRegisterAndGetSyncResponse(uplink);
		
		verifyResponse("The mandatory parameter is not specified or unknown parameter is specified");
	}
}
