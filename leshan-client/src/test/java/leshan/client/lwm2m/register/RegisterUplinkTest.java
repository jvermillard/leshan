package leshan.client.lwm2m.register;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import leshan.client.lwm2m.bootstrap.BootstrapMessageDeliverer.InterfaceTypes;
import leshan.client.lwm2m.bootstrap.BootstrapMessageDeliverer.OperationTypes;
import leshan.client.lwm2m.response.OperationResponse;
import leshan.client.lwm2m.response.OperationResponseCode;
import leshan.client.lwm2m.util.ResponseCallback;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import ch.ethz.inf.vs.californium.coap.CoAP.Code;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.LinkFormat;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;

/*
 * Requirements for this part of the interface can be found on Table 3.
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterUplinkTest {
	private static final int SYNC_TIMEOUT_MS = 2000;
	private static final String SERVER_HOST = "leshan.com";
	private static final int SERVER_PORT = 1234;
	private static final String ENDPOINT_NAME = UUID.randomUUID().toString();
	private byte[] actualResponsePayload;
	private String actualRequest;
	private Code actualCode;
	private ResponseCallback callback;

	@Mock
	private CoAPEndpoint endpoint;
	@Mock
	private RegisterDownlink downlink;

	private String expectedRequestRoot;
	private Map<String, String> validMap;
	private OperationResponse operationResponse;
	private final String VALID_REQUEST_PAYLOAD = "</lwm2m>;rt=\"oma.lwm2m\", </lwm2m/1/101>, </lwm2m/1/102>, </lwm2m/2/0>, </lwm2m/2/1>, </lwm2m/2/2>, </lwm2m/3/0>, </lwm2m/4/0>, </lwm2m/5>";
	private final String INVALID_REQUEST_PAYLOAD = "";
	private Object actualRequestPayload;
	private InetSocketAddress serverAddress;

	@Before
	public void setUp() {
		callback = new ResponseCallback();
		serverAddress = InetSocketAddress.createUnresolved(SERVER_HOST, SERVER_PORT);
		expectedRequestRoot = "coap://" + serverAddress.getHostString() + ":" + serverAddress.getPort() + "/rd?ep=" + ENDPOINT_NAME;

		validMap = new HashMap<String, String>();
		validMap.put("lt", "1000000");
		validMap.put("lwm2m", "1.1");
		validMap.put("b", "U");

	}

	private RegisterUplink initializeServerResponse(final InterfaceTypes interfaceType, final OperationTypes operationType, final ResponseCode responseCode) {
		doAnswer(new Answer<Void>(){

			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				final Request request = (Request) invocation.getArguments()[0];
				actualRequest = request.getURI();
				actualCode = request.getCode();
				actualRequestPayload = request.getPayloadString();

				final Response response = new Response(responseCode);
				response.setPayload(OperationResponseCode.generateReasonPhrase(OperationResponseCode.valueOf(response.getCode().value), interfaceType, operationType));

				request.setResponse(response);

				return null;
			}
		}).when(endpoint).sendRequest(any(Request.class));

		final RegisterUplink uplink = new RegisterUplink(serverAddress, endpoint, downlink);

		return uplink;
	}

	private void sendRegisterAndGetAsyncResponse(final RegisterUplink uplink, final String payload) {
		sendRegisterAndGetAsyncResponse(uplink, new HashMap<String, String>(),  payload);
	}

	private void sendRegisterAndGetAsyncResponse(final RegisterUplink uplink, final Map<String, String> parameters, final String payload) {
		uplink.register(ENDPOINT_NAME, parameters,  LinkFormat.parse(payload), callback);

		await().untilTrue(callback.isCalled());
		actualResponsePayload = callback.getResponsePayload();
	}

	private void sendRegisterAndGetSyncResponse(final RegisterUplink uplink, final String payload) {
		sendRegisterAndGetSyncResponse(uplink, new HashMap<String,String>(), payload);
	}

	private void sendRegisterAndGetSyncResponse(final RegisterUplink uplink, final Map<String, String> parameters, final String payload) {
		operationResponse = uplink.register(ENDPOINT_NAME, parameters, LinkFormat.parse(payload), SYNC_TIMEOUT_MS);
		if(operationResponse.isSuccess()){
			actualResponsePayload = operationResponse.getPayload();
		}
	}

	private void verifyResponse(final String expectedResponsePayload, final String expectedRequest) {
		assertEquals(expectedRequest, actualRequest);
		assertEquals(Code.POST, actualCode);
		assertArrayEquals(expectedResponsePayload.getBytes(), actualResponsePayload);
	}

	@Test
	public void testAsyncGoodRegistration() {
		final RegisterUplink uplink = initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED);

		sendRegisterAndGetAsyncResponse(uplink, VALID_REQUEST_PAYLOAD);

		verifyRequest(VALID_REQUEST_PAYLOAD);
		verifyResponse("\"Register\" operation is completed successfully", expectedRequestRoot);
	}


	@Test
	public void testAsyncBadRegistration() {
		final RegisterUplink uplink = initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.BAD_REQUEST);

		sendRegisterAndGetAsyncResponse(uplink, VALID_REQUEST_PAYLOAD);

		verifyRequest(VALID_REQUEST_PAYLOAD);
		verifyResponse("The mandatory parameter is not specified or unknown parameter is specified", expectedRequestRoot);
	}

	@Test
	public void testSyncGoodRegistration(){
		final RegisterUplink uplink = initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED);

		sendRegisterAndGetSyncResponse(uplink, VALID_REQUEST_PAYLOAD);

		verifyRequest(VALID_REQUEST_PAYLOAD);
		verifyResponse("\"Register\" operation is completed successfully", expectedRequestRoot);
	}

	@Test
	public void testSyncBadRegistration() {
		final RegisterUplink uplink = initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.BAD_REQUEST);

		sendRegisterAndGetSyncResponse(uplink, VALID_REQUEST_PAYLOAD);

		verifyRequest(VALID_REQUEST_PAYLOAD);
		verifyResponse("The mandatory parameter is not specified or unknown parameter is specified", expectedRequestRoot);
	}

	@Test
	public void testSyncBadNullParametersRegistration(){
		final RegisterUplink uplink = initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED);

		sendRegisterAndGetSyncResponse(uplink, null, INVALID_REQUEST_PAYLOAD);

		assertNull(actualResponsePayload);
		assertFalse(operationResponse.isSuccess());
		assertEquals(operationResponse.getResponseCode(), ResponseCode.BAD_REQUEST);
	}

	@Test
	public void testSyncBadSmsOneParametersRegistration(){
		final Map<String, String> invalidSmsMap = new HashMap<String, String>();
		invalidSmsMap.put("sms", UUID.randomUUID().toString());

		final RegisterUplink uplink = initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED);

		sendRegisterAndGetSyncResponse(uplink, invalidSmsMap, INVALID_REQUEST_PAYLOAD);

		assertNull(actualResponsePayload);
		assertFalse(operationResponse.isSuccess());
		assertEquals(operationResponse.getResponseCode(), ResponseCode.BAD_REQUEST);
	}

	@Test
	public void testSyncBadSmsTwoParametersRegistration(){
		final Map<String, String> invalidSmsMap = new HashMap<String, String>();
		invalidSmsMap.put("b", "US");

		final RegisterUplink uplink = initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED);

		sendRegisterAndGetSyncResponse(uplink, invalidSmsMap, INVALID_REQUEST_PAYLOAD);

		assertNull(actualResponsePayload);
		assertFalse(operationResponse.isSuccess());
		assertEquals(operationResponse.getResponseCode(), ResponseCode.BAD_REQUEST);
	}

	@Test
	public void testSyncBadQueueParametersRegistration(){
		final HashMap<String, String> invalidQueueMap = new HashMap<String, String>();
		invalidQueueMap.put("b", "UQ");

		final RegisterUplink uplink = initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED);

		sendRegisterAndGetSyncResponse(uplink, invalidQueueMap, INVALID_REQUEST_PAYLOAD);

		assertNull(actualResponsePayload);
		assertFalse(operationResponse.isSuccess());
		assertEquals(operationResponse.getResponseCode(), ResponseCode.BAD_REQUEST);
	}

	@Test
	public void testSyncBadInvalidParametersRegistration(){
		final HashMap<String, String> invalidIllegalMap = new HashMap<String, String>();
		invalidIllegalMap.put("b", "X");
		invalidIllegalMap.put("InvalidKey", "Lulz");

		final RegisterUplink uplink = initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED);

		sendRegisterAndGetSyncResponse(uplink, invalidIllegalMap, INVALID_REQUEST_PAYLOAD);

		assertNull(actualResponsePayload);
		assertFalse(operationResponse.isSuccess());
		assertEquals(operationResponse.getResponseCode(), ResponseCode.BAD_REQUEST);
	}

	@Test
	public void testSyncGoodAllParametersSyncRegistration(){
		final RegisterUplink uplink = initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED);

		final String validQuery = leshan.client.lwm2m.request.Request.toQueryStringMap(validMap);

		sendRegisterAndGetSyncResponse(uplink, validMap, VALID_REQUEST_PAYLOAD);

		verifyRequest(VALID_REQUEST_PAYLOAD);
		verifyResponse("\"Register\" operation is completed successfully", expectedRequestRoot + "&" + validQuery);
	}

	@Test
	public void testAsyncGoodParametersRegistration() {
		final RegisterUplink uplink = initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED);

		sendRegisterAndGetAsyncResponse(uplink, validMap, VALID_REQUEST_PAYLOAD);

		verifyRequest(VALID_REQUEST_PAYLOAD);
		verifyResponse("\"Register\" operation is completed successfully", expectedRequestRoot);
	}

	@Test
	public void testAsyncBadParametersRegistration() {
		final HashMap<String, String> invalidIllegalMap = new HashMap<String, String>();
		invalidIllegalMap.put("b", "X");
		invalidIllegalMap.put("InvalidKey", "Lulz");

		final RegisterUplink uplink = initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED);

		boolean noPayload = false;

		try{
			sendRegisterAndGetAsyncResponse(uplink, invalidIllegalMap, VALID_REQUEST_PAYLOAD);
		}
		catch(final UnsupportedOperationException uoe){
			noPayload = true;
		}

		assertTrue(noPayload);
		assertFalse(callback.isSuccess());
		assertEquals(callback.getResponseCode(), ResponseCode.BAD_REQUEST);
	}

	@Test
	public void testSyncGoodPayloadRegistration(){
		final RegisterUplink uplink = initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED);

		final String validQuery = leshan.client.lwm2m.request.Request.toQueryStringMap(validMap);


		sendRegisterAndGetSyncResponse(uplink, validMap, VALID_REQUEST_PAYLOAD);

		verifyRequest(VALID_REQUEST_PAYLOAD);
		verifyResponse("\"Register\" operation is completed successfully", expectedRequestRoot + "&" + validQuery);
	}

	private void verifyRequest(final String expectedPayload) {
		assertEquals(expectedPayload, actualRequestPayload);
	}

	@Test
	public void testSyncBadPayloadRegistration(){
		final RegisterUplink uplink = initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED);

		sendRegisterAndGetSyncResponse(uplink, validMap, INVALID_REQUEST_PAYLOAD);

		assertNull(actualResponsePayload);
		assertFalse(operationResponse.isSuccess());
		assertEquals(operationResponse.getResponseCode(), ResponseCode.BAD_REQUEST);
	}
	

	@Test
	public void testSyncBadNullPayloadRegistration(){
		final RegisterUplink uplink = initializeServerResponse(InterfaceTypes.REGISTRATION, OperationTypes.REGISTER, ResponseCode.CHANGED);

		sendRegisterAndGetSyncResponse(uplink, validMap, null);

		assertNull(actualResponsePayload);
		assertFalse(operationResponse.isSuccess());
		assertEquals(operationResponse.getResponseCode(), ResponseCode.BAD_REQUEST);
	}
}
